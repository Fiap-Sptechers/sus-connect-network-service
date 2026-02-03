package com.fiap.sus.network.modules.specialty.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Where;
import com.fiap.sus.network.shared.entity.BaseEntity;
import java.util.UUID;

@Entity
@Table(name = "specialties")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class Specialty extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    public Specialty(String name) {
        this.name = name;
    }
}
