package eu.dec21.appointme.users.users.entity;

import eu.dec21.appointme.common.entity.BaseBasicEntity;
import eu.dec21.appointme.users.groups.entity.Group;
import eu.dec21.appointme.users.roles.entity.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.ArrayList;
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

    @Size(min = 1, max = 50, message = "First name must be 1-50 characters")
    @Column(length = 50)
    private String firstName;

    @Size(min = 1, max = 50, message = "Last name must be 1-50 characters")
    @Column(length = 50)
    private String lastName;

    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(unique = true, nullable = false, length = 255)
    @Email(message = "Invalid email address")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(
            regexp = "^(\\+[1-9]\\d{1,14})?$",
            message = "Invalid phone number (expected E.164, e.g. +4915112345678)"
    )
    @Column(length = 20)
    private String phoneNumber;

    @Size(min = 8, max = 255, message = "Password must be 8-255 characters")
    @Column(nullable = false)
    private String password;

    @Size(max = 2048, message = "Image URL must not exceed 2048 characters")
    @URL(message = "Invalid image URL")
    @Column(length = 2048)
    private String imageUrl;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean emailVerified;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean locked;

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    private List<Role> roles = new ArrayList<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    private List<Group> groups = new ArrayList<>();

    @Override
    public String getName() {
        return email;
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.roles == null) {
            return List.of();
        }
        return this.roles.stream().map(role -> (GrantedAuthority) role::getName).toList();
    }

    @Override
    @NonNull
    public String getUsername() {
        return email;
    }

    public String fullName() {
        String ln = lastName == null ? "" : lastName.trim();
        String fn = firstName == null ? "" : firstName.trim();
        return (ln +  (ln.isBlank() || fn.isBlank() ? "" : ", ") + fn).trim();
    }

    public String fullNameReverse() {
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        return (fn + (fn.isBlank() ? "" : " ") + ln).trim();
    }
}

