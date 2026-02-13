package com.fiap.sus.network.modules.user.entity;

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
@Table(name = "roles")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., ADMIN, MANAGER
    
    @Column(nullable = false)
    private Integer level; // Hierarchy level (e.g. 100 for Admin, 50 for Manager)

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean admin = false;
}
