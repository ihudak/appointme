package eu.dec21.appointme.users.users.entity;

import eu.dec21.appointme.users.groups.entity.Group;
import eu.dec21.appointme.users.roles.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails, Principal {
    @Id
    @GeneratedValue
    private Long id;

    private String firstName;
    private String lastName;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private boolean emailVerified;
    private boolean locked;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles;
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Group> groups;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @CreatedDate
    @Column(insertable = true, nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public String getName() {
        return email;
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(role -> (GrantedAuthority) role::getName).toList();
    }

    @Override
    @NonNull
    public String getUsername() {
        return email;
    }

    public String fullName() {
        return lastName + " " + firstName;
    }

    public String fullNameReverse() {
        return firstName + " " + lastName;
    }
}
