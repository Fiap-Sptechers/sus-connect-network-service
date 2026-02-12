package com.fiap.sus.network.modules.health_unit.entity;

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
@Table(name = "addresses")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String number;

    private String complement;

    @Column(nullable = false)
    private String neighborhood;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(nullable = false)
    private String zipCode;

    private Double latitude;
    private Double longitude;
    
    public String toFormattedString() {
        return String.format("%s, %s - %s, %s - %s, %s", street, number, neighborhood, city, state, zipCode);
    }
}
