package com.fiap.sus.network.modules.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Where;
import com.fiap.sus.network.shared.entity.BaseEntity;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import java.util.UUID;

@Entity
@Table(name = "user_units")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class UserUnit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private HealthUnit unit;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
