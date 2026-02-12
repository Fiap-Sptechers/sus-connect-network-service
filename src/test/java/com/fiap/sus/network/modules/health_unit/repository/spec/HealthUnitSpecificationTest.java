package com.fiap.sus.network.modules.health_unit.repository.spec;

import com.fiap.sus.network.modules.health_unit.dto.HealthUnitFilter;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HealthUnitSpecificationTest {

    @Test
    void withFilter_ShouldCreatePredicate() {
        HealthUnitFilter filter = new HealthUnitFilter("Unit", "123", "SP", null, null, null);
        UUID userId = UUID.randomUUID();
        
        Specification<HealthUnit> spec = HealthUnitSpecification.withFilter(filter, userId, false);
        
        Root<HealthUnit> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        
        Path<Object> path = mock(Path.class);
        Join<Object, Object> join = mock(Join.class);
        
        when(root.get(anyString())).thenReturn(path);
        when(root.join(anyString(), any(JoinType.class))).thenReturn(join);
        when(join.get(anyString())).thenReturn(path);
        when(path.get(anyString())).thenReturn(path);
        
        spec.toPredicate(root, query, cb);
        
        verify(cb, atLeastOnce()).and(any(Predicate[].class));
    }

    @Test
    void withFilter_Admin_ShouldNotJoinUserUnits() {
        HealthUnitFilter filter = new HealthUnitFilter(null, null, null, null, null, null);
        Specification<HealthUnit> spec = HealthUnitSpecification.withFilter(filter, UUID.randomUUID(), true);
        
        Root<HealthUnit> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> path = mock(Path.class);
        
        when(root.get("deleted")).thenReturn(path);
        
        spec.toPredicate(root, query, cb);
        
        verify(root, never()).join(eq("userUnits"), any(JoinType.class));
    }
    
    @Test
    void withFilter_BoundingBox_ShouldAddBetween() {
        HealthUnitFilter filter = new HealthUnitFilter(null, null, null, null, null, null);
        Specification<HealthUnit> spec = HealthUnitSpecification.withFilter(filter, null, true, -23.0, -22.0, -46.0, -45.0);
        
        Root<HealthUnit> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> path = mock(Path.class);
        
        when(root.get(anyString())).thenReturn(path);
        when(path.get(anyString())).thenReturn(path);
        
        spec.toPredicate(root, query, cb);
        
        verify(cb, times(2)).between(any(), any(Double.class), any(Double.class));
    }
}
