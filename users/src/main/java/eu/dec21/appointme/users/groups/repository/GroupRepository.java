package eu.dec21.appointme.users.groups.repository;

import eu.dec21.appointme.users.groups.entity.Group;
import eu.dec21.appointme.users.roles.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository  extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);
}
