package eu.dec21.appointme.users.roles.repository;

import eu.dec21.appointme.users.roles.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    static java.util.List<Role> defaultRoles() {
        return java.util.List.of(
                Role.builder().name("User").build(),
                Role.builder().name("Admin").build()
        );
    }
}
