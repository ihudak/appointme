package eu.dec21.appointme.users.roles.repository;

import eu.dec21.appointme.users.roles.entity.Role;
import eu.dec21.appointme.users.users.entity.User;
import eu.dec21.appointme.users.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("RoleRepository Tests")
class RoleRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine")
            .withDatabaseName("appme_users")
            .withUsername("pguser")
            .withPassword("p@ssw0rD!");

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    private Role adminRole;
    private Role userRole;
    private Role moderatorRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create test roles
        adminRole = createRole("ROLE_ADMIN");
        userRole = createRole("ROLE_USER");
        moderatorRole = createRole("ROLE_MODERATOR");
    }

    private Role createRole(String name) {
        return roleRepository.save(Role.builder()
                .name(name)
                .build());
    }

    private User createUser(String email, String firstName, String lastName, List<Role> roles) {
        return userRepository.save(User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password("password123")
                .emailVerified(true)
                .locked(false)
                .roles(roles)
                .groups(List.of())
                .build());
    }

    // ========== findByName Tests ==========

    @Test
    @DisplayName("findByName should return role when name exists")
    void findByName_shouldReturnRole_whenNameExists() {
        Optional<Role> result = roleRepository.findByName("ROLE_ADMIN");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ROLE_ADMIN");
        assertThat(result.get().getId()).isEqualTo(adminRole.getId());
    }

    @Test
    @DisplayName("findByName should return empty when name does not exist")
    void findByName_shouldReturnEmpty_whenNameDoesNotExist() {
        Optional<Role> result = roleRepository.findByName("ROLE_NONEXISTENT");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByName should be case-sensitive")
    void findByName_shouldBeCaseSensitive() {
        Optional<Role> result = roleRepository.findByName("role_admin");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByName should handle null name gracefully")
    void findByName_shouldHandleNullName() {
        Optional<Role> result = roleRepository.findByName(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByName should handle empty string name")
    void findByName_shouldHandleEmptyStringName() {
        Optional<Role> result = roleRepository.findByName("");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByName should return role with users loaded lazily")
    void findByName_shouldReturnRoleWithUsersLazy() {
        // Create users with the role
        User user1 = createUser("user1@example.com", "User", "One", new ArrayList<>(List.of(adminRole)));
        User user2 = createUser("user2@example.com", "User", "Two", new ArrayList<>(List.of(adminRole)));

        Optional<Role> result = roleRepository.findByName("ROLE_ADMIN");

        assertThat(result).isPresent();
        Role role = result.get();
        // Users are lazily loaded - they exist in DB but may not be loaded immediately
        assertThat(role.getId()).isEqualTo(adminRole.getId());
    }

    // ========== defaultRoles Static Method Tests ==========

    @Test
    @DisplayName("defaultRoles should return list of default roles")
    void defaultRoles_shouldReturnListOfDefaultRoles() {
        List<Role> defaults = RoleRepository.defaultRoles();

        assertThat(defaults).hasSize(2);
        assertThat(defaults).extracting(Role::getName)
                .containsExactly("User", "Admin");
    }

    @Test
    @DisplayName("defaultRoles should return immutable list")
    void defaultRoles_shouldReturnImmutableList() {
        List<Role> defaults = RoleRepository.defaultRoles();

        assertThat(defaults).isInstanceOf(List.class);
        // Verify it's immutable by checking the list type
        assertThat(defaults.getClass().getName()).contains("Immutable");
    }

    @Test
    @DisplayName("defaultRoles should return roles without IDs")
    void defaultRoles_shouldReturnRolesWithoutIds() {
        List<Role> defaults = RoleRepository.defaultRoles();

        assertThat(defaults).allMatch(role -> role.getId() == null);
    }

    // ========== JPA Standard CRUD Tests ==========

    @Test
    @DisplayName("save should persist new role")
    void save_shouldPersistNewRole() {
        Role newRole = Role.builder()
                .name("ROLE_GUEST")
                .build();

        Role saved = roleRepository.save(newRole);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("ROLE_GUEST");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("save should update existing role")
    void save_shouldUpdateExistingRole() {
        adminRole.setName("ROLE_SUPER_ADMIN");

        Role updated = roleRepository.save(adminRole);

        assertThat(updated.getId()).isEqualTo(adminRole.getId());
        assertThat(updated.getName()).isEqualTo("ROLE_SUPER_ADMIN");
    }

    @Test
    @DisplayName("findById should return role when ID exists")
    void findById_shouldReturnRole_whenIdExists() {
        Optional<Role> result = roleRepository.findById(adminRole.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("findById should return empty when ID does not exist")
    void findById_shouldReturnEmpty_whenIdDoesNotExist() {
        Optional<Role> result = roleRepository.findById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll should return all roles")
    void findAll_shouldReturnAllRoles() {
        List<Role> roles = roleRepository.findAll();

        assertThat(roles).hasSize(3);
        assertThat(roles).extracting(Role::getName)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER", "ROLE_MODERATOR");
    }

    @Test
    @DisplayName("findAll with pagination should return correct page")
    void findAll_withPagination_shouldReturnCorrectPage() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        Page<Role> page = roleRepository.findAll(pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("findAll with pagination should return second page")
    void findAll_withPagination_shouldReturnSecondPage() {
        Pageable pageable = PageRequest.of(1, 2, Sort.by("name"));

        Page<Role> page = roleRepository.findAll(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("findAll with sort should return roles sorted by name")
    void findAll_withSort_shouldReturnRolesSortedByName() {
        List<Role> roles = roleRepository.findAll(Sort.by("name"));

        assertThat(roles).hasSize(3);
        assertThat(roles.get(0).getName()).isEqualTo("ROLE_ADMIN");
        assertThat(roles.get(1).getName()).isEqualTo("ROLE_MODERATOR");
        assertThat(roles.get(2).getName()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("findAll with sort descending should return roles in reverse order")
    void findAll_withSortDescending_shouldReturnRolesInReverseOrder() {
        List<Role> roles = roleRepository.findAll(Sort.by(Sort.Direction.DESC, "name"));

        assertThat(roles).hasSize(3);
        assertThat(roles.get(0).getName()).isEqualTo("ROLE_USER");
        assertThat(roles.get(1).getName()).isEqualTo("ROLE_MODERATOR");
        assertThat(roles.get(2).getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("count should return total number of roles")
    void count_shouldReturnTotalNumberOfRoles() {
        long count = roleRepository.count();

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("existsById should return true when role exists")
    void existsById_shouldReturnTrue_whenRoleExists() {
        boolean exists = roleRepository.existsById(adminRole.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById should return false when role does not exist")
    void existsById_shouldReturnFalse_whenRoleDoesNotExist() {
        boolean exists = roleRepository.existsById(99999L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("deleteById should remove role")
    void deleteById_shouldRemoveRole() {
        Long roleId = adminRole.getId();

        roleRepository.deleteById(roleId);

        assertThat(roleRepository.existsById(roleId)).isFalse();
        assertThat(roleRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("delete should remove role entity")
    void delete_shouldRemoveRoleEntity() {
        roleRepository.delete(adminRole);

        assertThat(roleRepository.existsById(adminRole.getId())).isFalse();
        assertThat(roleRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteAll should remove all roles")
    void deleteAll_shouldRemoveAllRoles() {
        roleRepository.deleteAll();

        assertThat(roleRepository.count()).isEqualTo(0);
    }

    // ========== Edge Cases & Special Scenarios ==========

    @Test
    @DisplayName("should handle role name with underscores")
    void shouldHandleRoleNameWithUnderscores() {
        Role roleWithUnderscores = createRole("ROLE_SUPER_ADMIN");

        Optional<Role> result = roleRepository.findByName("ROLE_SUPER_ADMIN");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ROLE_SUPER_ADMIN");
    }

    @Test
    @DisplayName("should handle role name with numbers")
    void shouldHandleRoleNameWithNumbers() {
        Role roleWithNumbers = createRole("ROLE_LEVEL_1");

        Optional<Role> result = roleRepository.findByName("ROLE_LEVEL_1");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ROLE_LEVEL_1");
    }

    @Test
    @DisplayName("should handle very long role names")
    void shouldHandleVeryLongRoleNames() {
        // Max length is 50 characters
        String longName = "ROLE_" + "A".repeat(45); // 5 + 45 = 50
        Role roleWithLongName = createRole(longName);

        Optional<Role> result = roleRepository.findByName(longName);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(longName);
        assertThat(result.get().getName().length()).isEqualTo(50);
    }

    @Test
    @DisplayName("should preserve timestamps on save")
    void shouldPreserveTimestampsOnSave() {
        Role saved = roleRepository.save(Role.builder()
                .name("ROLE_NEW")
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("findAll on empty repository should return empty list")
    void findAll_onEmptyRepository_shouldReturnEmptyList() {
        roleRepository.deleteAll();

        List<Role> roles = roleRepository.findAll();

        assertThat(roles).isEmpty();
    }

    @Test
    @DisplayName("findAll with pagination on empty repository should return empty page")
    void findAll_withPagination_onEmptyRepository_shouldReturnEmptyPage() {
        roleRepository.deleteAll();

        Page<Role> page = roleRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("should handle role with multiple users")
    void shouldHandleRoleWithMultipleUsers() {
        User user1 = createUser("user1@example.com", "User", "One", new ArrayList<>(List.of(adminRole)));
        User user2 = createUser("user2@example.com", "User", "Two", new ArrayList<>(List.of(adminRole)));
        User user3 = createUser("user3@example.com", "User", "Three", new ArrayList<>(List.of(adminRole)));

        Optional<Role> result = roleRepository.findById(adminRole.getId());

        assertThat(result).isPresent();
        // Role loaded, users relationship exists
        assertThat(result.get().getId()).isEqualTo(adminRole.getId());
    }

    @Test
    @DisplayName("should handle user with multiple roles")
    void shouldHandleUserWithMultipleRoles() {
        User multiRoleUser = createUser("multi@example.com", "Multi", "Role",
                new ArrayList<>(List.of(adminRole, userRole, moderatorRole)));

        // Verify roles still exist
        assertThat(roleRepository.findByName("ROLE_ADMIN")).isPresent();
        assertThat(roleRepository.findByName("ROLE_USER")).isPresent();
        assertThat(roleRepository.findByName("ROLE_MODERATOR")).isPresent();
    }

    @Test
    @DisplayName("findByName should find exact matches only")
    void findByName_shouldFindExactMatchesOnly() {
        createRole("ROLE");
        createRole("ROLE_A");
        createRole("ROLE_AB");

        Optional<Role> result = roleRepository.findByName("ROLE_A");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ROLE_A");
    }

    @Test
    @DisplayName("should handle role name with spaces")
    void shouldHandleRoleNameWithSpaces() {
        Role roleWithSpaces = createRole("ROLE ADMIN");

        Optional<Role> result = roleRepository.findByName("ROLE ADMIN");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ROLE ADMIN");
    }

    @Test
    @DisplayName("should handle single character role name")
    void shouldHandleSingleCharacterRoleName() {
        Role singleChar = createRole("R");

        Optional<Role> result = roleRepository.findByName("R");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("R");
    }

    @Test
    @DisplayName("deleteById should not throw when ID does not exist")
    void deleteById_shouldNotThrow_whenIdDoesNotExist() {
        // Spring Data JPA's deleteById silently succeeds if entity doesn't exist
        roleRepository.deleteById(99999L);

        // Verify other roles still exist
        assertThat(roleRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("should handle standard Spring Security role naming convention")
    void shouldHandleStandardSpringSecurityRoleNamingConvention() {
        // adminRole already exists from setUp as "ROLE_ADMIN"
        Optional<Role> result = roleRepository.findByName("ROLE_ADMIN");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).startsWith("ROLE_");
    }
}
