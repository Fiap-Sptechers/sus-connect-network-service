package com.fiap.sus.network.modules.shift.entity;

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
import com.fiap.sus.network.modules.doctor.entity.Doctor;
import java.util.UUID;

@Entity
@Table(name = "shifts")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class Shift extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @ManyToOne
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Column(name = "waiting_patients")
    private Integer waitingPatients = 0;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "shifts_doctors",
        joinColumns = @JoinColumn(name = "shift_id"),
        inverseJoinColumns = @JoinColumn(name = "doctor_id")
    )
    private Set<Doctor> doctors = new HashSet<>();
    
    @Transient
    public Integer getActiveDoctors() {
        return doctors != null ? doctors.size() : 0;
    }
}
