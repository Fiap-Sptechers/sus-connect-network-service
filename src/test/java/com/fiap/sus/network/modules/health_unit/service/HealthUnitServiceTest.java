package com.fiap.sus.network.modules.health_unit.service;

import com.fiap.sus.network.core.exception.BusinessException;
import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.core.security.CustomUserDetails;
import com.fiap.sus.network.modules.health_unit.dto.*;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import com.fiap.sus.network.modules.health_unit.mapper.HealthUnitMapper;
import com.fiap.sus.network.modules.health_unit.repository.HealthUnitRepository;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import com.fiap.sus.network.modules.user.service.AccessControlService;
import com.fiap.sus.network.modules.user.entity.User;
import com.fiap.sus.network.modules.shift.repository.ShiftRepository;
import com.fiap.sus.network.modules.shift.mapper.ShiftMapper;
import com.fiap.sus.network.modules.health_unit.entity.Address;
import com.fiap.sus.network.modules.health_unit.enums.DistanceUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.Map;
import com.fiap.sus.network.shared.util.DistanceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthUnitServiceTest {

    @Mock
    private HealthUnitRepository repository;
    @Mock
    private GeocodingService geocodingService;
    @Mock
    private HealthUnitMapper unitMapper;
    @Mock
    private AccessControlService accessControlService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private ShiftMapper shiftMapper;

    @InjectMocks
    private HealthUnitService service;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getId()).thenReturn(userId);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void create_ShouldSave_WhenCnpjDoesNotExist() {
        HealthUnitRequest request = new HealthUnitRequest("Unit 1", "00000000000191", new AddressRequest("Street", null, null, null, "City", "SP", "00000000"), null);
        when(repository.existsByCnpj(request.cnpj())).thenReturn(false);
        
        GeocodingService.GeocodedLocation location = new GeocodingService.GeocodedLocation(0., 0., "Addr", "State");
        when(geocodingService.geocode(anyString())).thenReturn(location);

        HealthUnit entity = new HealthUnit();
        Address addr = new Address();
        entity.setAddress(addr);
        entity.setId(UUID.randomUUID());

        when(unitMapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(unitMapper.toDto(entity)).thenReturn(new HealthUnitResponse(entity.getId(), "Unit 1", "00000000000191", null, null, null));
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        HealthUnitResponse response = service.create(request);

        assertNotNull(response);
        verify(repository).save(entity);
        verify(accessControlService).grantUnitAccessSystem(any(), any(), eq("MANAGER"));
    }

    @Test
    void create_ShouldThrow_WhenCnpjExists() {
        HealthUnitRequest request = new HealthUnitRequest("Unit 1", "00000000000191", null, null);
        when(repository.existsByCnpj(request.cnpj())).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(request));
    }

    @Test
    void delete_ShouldMarkAsDeleted_WhenFound() {
        UUID unitId = UUID.randomUUID();
        HealthUnit unit = new HealthUnit();
        unit.setDeleted(false);
        
        when(repository.findById(unitId)).thenReturn(Optional.of(unit));
        
        service.delete(unitId);
        
        assertTrue(unit.isDeleted());
        verify(repository).save(unit);
    }
    
    @Test
    void getStatus_ShouldReturnStatus() {
        UUID unitId = UUID.randomUUID();
        HealthUnit unit = new HealthUnit();
        unit.setId(unitId);
        unit.setName("Test Unit");
        
        when(repository.findById(unitId)).thenReturn(Optional.of(unit));
        when(shiftRepository.findByUnitId(unitId)).thenReturn(new ArrayList<>());
        
        HealthUnitStatusResponse response = service.getStatus(unitId);
        
        assertEquals(unitId, response.id());
        assertEquals("Test Unit", response.name());
    }
    
    @Test
    void findById_ShouldReturnUnit_WhenAccessGranted() {
        UUID unitId = UUID.randomUUID();
        HealthUnit unit = new HealthUnit();
        
        doNothing().when(accessControlService).checkAccess(unitId);
        when(repository.findById(unitId)).thenReturn(Optional.of(unit));
        when(unitMapper.toDto(unit)).thenReturn(new HealthUnitResponse(unitId, "Name", "123", null, null, null));
        
        HealthUnitResponse response = service.findById(unitId);
        assertNotNull(response);
    }

    @Test
    void findAll_ShouldFilterByRadius() {
        String baseAddr = "Paulista";
        HealthUnitFilter filter = new HealthUnitFilter("Unit", "123", "SP", baseAddr, 10.0, DistanceUnit.KM);

        when(accessControlService.isAdmin()).thenReturn(true);
        when(geocodingService.geocode(baseAddr)).thenReturn(new GeocodingService.GeocodedLocation(-23.5, -46.6, "SP", "SP"));

        HealthUnit unit = new HealthUnit();
        unit.setAddress(new Address());
        unit.getAddress().setLatitude(-23.51);
        unit.getAddress().setLongitude(-46.61);
        
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(unit));
        when(geocodingService.filterByRadius(anyList(), anyDouble(), anyDouble(), anyDouble(), any())).thenReturn(Map.of(unit, 1.5));
        when(unitMapper.toDto(any(), anyString())).thenReturn(new HealthUnitResponse(UUID.randomUUID(), "Unit", "123", null, null, "1.5 km"));

        Page<HealthUnitResponse> result = service.findAll(filter, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAll_ShouldThrow_WhenRadiusTooLarge() {
        HealthUnitFilter filter = new HealthUnitFilter(null, null, null, "Paulista", 200.0, DistanceUnit.KM);
        assertThrows(BusinessException.class, () -> service.findAll(filter, Pageable.unpaged()));
    }

    @Test
    void findAll_ShouldReturnEmpty_WhenGeocodingFails() {
        HealthUnitFilter filter = new HealthUnitFilter(null, null, null, "Unknown", 10.0, DistanceUnit.KM);
        when(accessControlService.isAdmin()).thenReturn(true);
        when(geocodingService.geocode(anyString())).thenReturn(new GeocodingService.GeocodedLocation(0.0, 0.0, null, null));

        Page<HealthUnitResponse> result = service.findAll(filter, Pageable.unpaged());

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_ShouldUseRegularFilter_WhenNoRadius() {
        HealthUnitFilter filter = new HealthUnitFilter("Unit 1", null, null, null, null, null);
        when(accessControlService.isAdmin()).thenReturn(true);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        service.findAll(filter, Pageable.unpaged());

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }
}
