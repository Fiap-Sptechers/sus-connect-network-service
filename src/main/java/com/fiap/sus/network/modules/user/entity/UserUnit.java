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
import java.util.UUID;

@Entity
@Table(name = "user_units")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class UserUnit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private HealthUnit unit;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
