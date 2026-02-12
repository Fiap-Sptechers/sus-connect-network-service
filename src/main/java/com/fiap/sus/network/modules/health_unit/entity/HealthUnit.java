package com.fiap.sus.network.modules.health_unit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Where;
import com.fiap.sus.network.shared.entity.BaseEntity;
import com.fiap.sus.network.modules.user.entity.UserUnit;
import java.util.UUID;

@Entity
@Table(name = "health_units")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class HealthUnit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String cnpj;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Contact> contacts = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "unit")
    private java.util.Set<UserUnit> userUnits = new java.util.HashSet<>();
}
