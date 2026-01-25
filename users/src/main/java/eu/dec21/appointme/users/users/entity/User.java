package eu.dec21.appointme.users.users.entity;

import eu.dec21.appointme.common.entity.BaseBasicEntity;
import eu.dec21.appointme.users.groups.entity.Group;
import eu.dec21.appointme.users.roles.entity.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Email(message = "Invalid email address")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Pattern(
            regexp = "^(\\+[1-9]\\d{1,14})?$",
            message = "Invalid phone number (expected E.164, e.g. +4915112345678)"
    )
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    private String imageUrl;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean emailVerified;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
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

    private String fullName() {
        String ln = lastName == null ? "" : lastName.trim();
        String fn = firstName == null ? "" : firstName.trim();
        return (ln +  (ln.isBlank() ? "" : ", ") + fn).trim();
    }

    public String fullNameReverse() {
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        return (fn + (fn.isBlank() ? "" : " ") + ln).trim();
    }
}
