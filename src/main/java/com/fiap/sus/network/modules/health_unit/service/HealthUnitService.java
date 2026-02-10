package com.fiap.sus.network.modules.health_unit.service;

import com.fiap.sus.network.modules.user.service.AccessControlService;

import com.fiap.sus.network.modules.health_unit.dto.HealthUnitFilter;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitRequest;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitResponse;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import com.fiap.sus.network.modules.health_unit.mapper.HealthUnitMapper;
import com.fiap.sus.network.modules.health_unit.repository.HealthUnitRepository;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import com.fiap.sus.network.modules.health_unit.repository.spec.HealthUnitSpecification;
import com.fiap.sus.network.modules.shift.repository.ShiftRepository;
import com.fiap.sus.network.modules.shift.mapper.ShiftMapper;
import com.fiap.sus.network.core.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiap.sus.network.core.exception.BusinessException;
import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitStatusResponse;
import com.fiap.sus.network.modules.health_unit.enums.DistanceUnit;
import com.fiap.sus.network.modules.user.entity.User;
import com.fiap.sus.network.modules.shift.entity.Shift;
import com.fiap.sus.network.modules.shift.dto.ShiftResponse;
import com.fiap.sus.network.shared.util.DistanceUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fiap.sus.network.modules.health_unit.service.GeocodingService;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthUnitService {

    private final HealthUnitRepository repository;
    private final GeocodingService geocodingService;
    private final HealthUnitMapper unitMapper;
    private final AccessControlService accessControlService;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;

    @Transactional
    public HealthUnitResponse create(HealthUnitRequest request) {
        log.info("Creating unit: {} (CNPJ: {})", request.name(), request.cnpj());
        
        if (repository.existsByCnpj(request.cnpj())) {
            throw new BusinessException("CNPJ already registered: " + request.cnpj());
        }

        GeocodingService.GeocodedLocation location = geocodingService.geocode(request.address().toFormattedString());
        
        HealthUnit unit = unitMapper.toEntity(request);
        
        if (unit.getAddress() != null) {
            unit.getAddress().setLatitude(location.lat());
            unit.getAddress().setLongitude(location.lon());
        }
        
        HealthUnit saved = repository.save(unit);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userRepository.findById(userDetails.getId()).orElseThrow();
            accessControlService.grantUnitAccessSystem(user, saved, "MANAGER");
        }
        
        return unitMapper.toDto(saved);
    }

    public HealthUnitStatusResponse getStatus(UUID id) {
        HealthUnit unit = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Unit not found"));
        List<Shift> shifts = shiftRepository.findByUnitId(id);
        
        List<ShiftResponse> shiftsDto = shifts.stream()
                .map(shiftMapper::toDto)
                .toList();
                
        return new HealthUnitStatusResponse(unit.getId(), unit.getName(), shiftsDto);
    }

    @Transactional
    public void delete(UUID id) {
        HealthUnit unit = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Unit not found"));
        unit.setDeleted(true);
        repository.save(unit);
    }

    @Transactional(readOnly = true)
    public Page<HealthUnitResponse> findAll(HealthUnitFilter filter, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = null;
        boolean isAdmin = false;
        
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            userId = userDetails.getId();
            isAdmin = accessControlService.isAdmin();
            log.debug("Requisição autenticada - userId: {}, isAdmin: {}", userId, isAdmin);
        } else {
            log.debug("Requisição pública - sem autenticação");
        }
        
        if (filter.baseAddress() != null && filter.radius() != null && filter.distanceUnit() != null) {
             double radiusInKm = filter.distanceUnit().toKilometers(filter.radius());
             if (radiusInKm > 100.0) {
                 throw new BusinessException("Radius too large. Maximum allowed is 100 km.");
             }

             GeocodingService.GeocodedLocation location = geocodingService.geocode(filter.baseAddress());
             if (location.lat() == 0.0 && location.lon() == 0.0) {
                 log.warn("Could not geocode address: {}", filter.baseAddress());
                 return Page.empty(pageable);
             }

             double[] bbox = DistanceUtils.calculateBoundingBox(location.lat(), location.lon(), radiusInKm);
             log.debug("Radius search: lat={}, lon={}, radius={}km, bbox=[{}, {}, {}, {}]", 
                     location.lat(), location.lon(), radiusInKm, bbox[0], bbox[1], bbox[2], bbox[3]);
             
             // Use original filter (don't override city/state with geocoded info to avoid strict match failures)
             Specification<HealthUnit> optimizedSpec = HealthUnitSpecification.withFilter(filter, userId, isAdmin, bbox[0], bbox[1], bbox[2], bbox[3]);
             
             
             int dbBatchSize = 200;
             int maxBatches = 50;
             
             java.util.concurrent.ConcurrentLinkedQueue<java.util.Map.Entry<HealthUnit, Double>> nearbyQueue = 
                 new java.util.concurrent.ConcurrentLinkedQueue<>();
             
             int totalProcessed = 0;
             int batchNumber = 0;
             
             while (batchNumber < maxBatches) {
                 Pageable dbPageable = org.springframework.data.domain.PageRequest.of(batchNumber, dbBatchSize);
                 Page<HealthUnit> dbPage = repository.findAll(optimizedSpec, dbPageable);
                 
                 if (dbPage.isEmpty()) {
                     break;
                 }
                 
                 List<HealthUnit> batch = dbPage.getContent();
                 log.debug("Processing database batch {}: {} units (total processed: {})", 
                         batchNumber + 1, batch.size(), totalProcessed);
                 
                 List<HealthUnit> batchCopy = new java.util.ArrayList<>(batch.size());
                 for (HealthUnit unit : batch) {
                     if (unit != null) {
                         try {
                             if (unit.getAddress() != null) {
                                 Double lat = unit.getAddress().getLatitude();
                                 Double lon = unit.getAddress().getLongitude();
                                 if (lat != null && lon != null) {
                                     try {
                                         org.hibernate.Hibernate.initialize(unit.getContacts());
                                     } catch (Exception e) {
                                         if (unit.getContacts() != null) {
                                             java.util.List<com.fiap.sus.network.modules.health_unit.entity.Contact> contactsCopy = 
                                                 new java.util.ArrayList<>(unit.getContacts());
                                             for (com.fiap.sus.network.modules.health_unit.entity.Contact c : contactsCopy) {
                                                 if (c != null) {
                                                     c.getId();
                                                 }
                                             }
                                         }
                                     }
                                     batchCopy.add(unit);
                                 }
                             }
                         } catch (Exception e) {
                             log.debug("Erro ao materializar unidade {}: {}. Pulando...", unit.getId(), e.getMessage());
                         }
                     }
                 }
                 
                 Map<HealthUnit, Double> batchNearbyMap = geocodingService.filterByRadius(
                     batchCopy, location.lat(), location.lon(), radiusInKm, DistanceUnit.KM);
                 
                 for (java.util.Map.Entry<HealthUnit, Double> entry : batchNearbyMap.entrySet()) {
                     nearbyQueue.offer(entry);
                 }
                 
                 totalProcessed += batch.size();
                 
                 if (batch.size() < dbBatchSize) {
                     break;
                 }
                 
                 batchNumber++;
                 
                 if (!pageable.isUnpaged() && nearbyQueue.size() >= (pageable.getOffset() + pageable.getPageSize())) {
                     log.debug("Enough units collected for pagination. Stopping database queries.");
                     break;
                 }
             }
             
             log.debug("Filtered to {} units within radius (from {} processed)", nearbyQueue.size(), totalProcessed);
             
             List<java.util.Map.Entry<HealthUnit, Double>> allNearbyUnits = new java.util.ArrayList<>(nearbyQueue);
             allNearbyUnits.sort(java.util.Map.Entry.comparingByValue());
             
             List<HealthUnitResponse> filteredList = new java.util.ArrayList<>(allNearbyUnits.size());
             for (int i = 0; i < allNearbyUnits.size(); i++) {
                 try {
                     java.util.Map.Entry<HealthUnit, Double> entry = allNearbyUnits.get(i);
                     HealthUnit unit = entry.getKey();
                     Double distKm = entry.getValue();
                     
                     HealthUnitResponse dto = unitMapper.toDto(unit, formatDistance(distKm));
                     if (dto != null) {
                         filteredList.add(dto);
                     }
                 } catch (java.util.ConcurrentModificationException e) {
                     log.warn("ConcurrentModificationException ao processar unidade no índice {}. Pulando...", i);
                 } catch (Exception e) {
                     log.warn("Erro ao processar unidade no índice {}: {}. Pulando...", i, e.getMessage());
                 }
             }
             
             log.info("Final nearby search result: {} units within radius", filteredList.size());
                
             if (pageable.isUnpaged()) {
                 return new PageImpl<>(filteredList, pageable, filteredList.size());
             }
                
             int start = (int) pageable.getOffset();
             int end = Math.min((start + pageable.getPageSize()), filteredList.size());
             if (start > filteredList.size()) return Page.empty(pageable);
             
             List<HealthUnitResponse> pageContent = new java.util.ArrayList<>(
                 filteredList.subList(start, end));
             
             return new PageImpl<>(pageContent, pageable, filteredList.size());
        } else {
             Specification<HealthUnit> spec = HealthUnitSpecification.withFilter(filter, userId, isAdmin);
             return repository.findAll(spec, pageable).map(unitMapper::toDto);
        }
    }
    
    public HealthUnitResponse findById(UUID id) {
        accessControlService.checkAccess(id);
        HealthUnit unit = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Unity not found"));
        return unitMapper.toDto(unit);
    }

    private String formatDistance(Double distKm) {
        if (distKm == null) return null;
        
        if (distKm >= 1.0) {
            return String.format("%.1f km", distKm);
        } else {
            long meters = Math.round(distKm * 1000);
            return meters + " m";
        }
    }
}
