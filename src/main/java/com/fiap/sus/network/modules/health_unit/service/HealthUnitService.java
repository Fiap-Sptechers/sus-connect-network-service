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
import com.fiap.sus.network.core.exception.ExternalServiceException;
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

        GeocodingService.GeocodedLocation location;
        try {
            location = geocodingService.geocode(request.address().toFormattedString());
        } catch (ExternalServiceException e) {
            log.error("Geocoding failed for address: {}", request.address().toFormattedString(), e);
            throw new BusinessException("Não foi possível obter as coordenadas para o endereço informado. Verifique o endereço ou tente novamente mais tarde.");
        }
        
        HealthUnit unit = unitMapper.toEntity(request);
        
        if (unit.getAddress() != null && location != null) {
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
             
             List<HealthUnit> units = repository.findAll(optimizedSpec);
             log.debug("Found {} units in bounding box", units.size());
             
             Map<HealthUnit, Double> nearbyMap = geocodingService.filterByRadius(units, location.lat(), location.lon(), radiusInKm, DistanceUnit.KM);
             
             List<HealthUnitResponse> filteredList = units.stream()
                .filter(nearbyMap::containsKey)
                .map(unit -> {
                    Double distKm = nearbyMap.get(unit);
                    return unitMapper.toDto(unit, formatDistance(distKm));
                })
                .toList();
             
             log.info("Final nearby search result: {} units within radius", filteredList.size());
                
             if (pageable.isUnpaged()) {
                 return new PageImpl<>(filteredList, pageable, filteredList.size());
             }
                
             int start = (int) pageable.getOffset();
             int end = Math.min((start + pageable.getPageSize()), filteredList.size());
             if (start > filteredList.size()) return Page.empty(pageable);
             
             return new PageImpl<>(filteredList.subList(start, end), pageable, filteredList.size());
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
