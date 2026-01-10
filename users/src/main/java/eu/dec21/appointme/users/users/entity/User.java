package eu.dec21.appointme.users.users.entity;

import eu.dec21.appointme.common.entity.BaseBasicEntity;
import eu.dec21.appointme.users.groups.entity.Group;
import eu.dec21.appointme.users.roles.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseBasicEntity implements UserDetails, Principal {

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
