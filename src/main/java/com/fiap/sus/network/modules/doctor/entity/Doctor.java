package com.fiap.sus.network.modules.doctor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;
import com.fiap.sus.network.shared.entity.BaseEntity;
import com.fiap.sus.network.modules.specialty.entity.Specialty;
import java.util.UUID;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class Doctor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String crm;

    @ManyToMany
    @JoinTable(
        name = "doctors_specialties",
        joinColumns = @JoinColumn(name = "doctor_id"),
        inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    private Set<Specialty> specialties = new HashSet<>();
}
