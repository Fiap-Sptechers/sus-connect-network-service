package com.fiap.sus.network.modules.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.fiap.sus.network.shared.entity.BaseEntity;

@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, name = "cpf_cnpj")
    private String cpfCnpj;

    @Column(nullable = false)
    private String password;

    // Global roles (e.g. System Admin)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> globalRoles = new HashSet<>();

    // Unit-scoped roles
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<UserUnit> unitRoles = new HashSet<>();
    
    public boolean isAdmin() {
        return globalRoles.stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()) || "ADMIN".equals(r.getName()));
    }
}
