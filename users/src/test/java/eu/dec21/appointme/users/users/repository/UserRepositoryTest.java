package eu.dec21.appointme.users.users.repository;

import eu.dec21.appointme.users.groups.entity.Group;
import eu.dec21.appointme.users.groups.repository.GroupRepository;
import eu.dec21.appointme.users.roles.entity.Role;
import eu.dec21.appointme.users.roles.repository.RoleRepository;
import eu.dec21.appointme.users.users.entity.User;
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
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine")
            .withDatabaseName("appme_users")
            .withUsername("pguser")
            .withPassword("p@ssw0rD!");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GroupRepository groupRepository;

    private User user1;
    private User user2;
    private User user3;
    private Role adminRole;
    private Role userRole;
    private Group developers;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        groupRepository.deleteAll();

        // Create roles
        adminRole = roleRepository.save(Role.builder()
                .name("ROLE_ADMIN")
                .build());

        userRole = roleRepository.save(Role.builder()
                .name("ROLE_USER")
                .build());

        // Create group
        Group devGroup = Group.builder()
                .name("Developers")
                .build();
        developers = groupRepository.save(devGroup);

        // Create test users
        user1 = createUser("john.doe@example.com", "John", "Doe", "+4915112345678", true, false, List.of(adminRole), List.of(developers));
        user2 = createUser("jane.smith@example.com", "Jane", "Smith", "+4917098765432", true, false, List.of(userRole), List.of());
        user3 = createUser("locked.user@example.com", "Locked", "User", null, false, true, List.of(userRole), List.of());
    }

    private User createUser(String email, String firstName, String lastName, String phone,
                            boolean emailVerified, boolean locked, List<Role> roles, List<Group> groups) {
        return userRepository.save(User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phone)
                .password("encodedPassword123")
                .emailVerified(emailVerified)
                .locked(locked)
                .roles(roles)
                .groups(groups)
                .build());
    }

    // ========== findByEmail Tests ==========

    @Test
    @DisplayName("findByEmail should return user when email exists")
    void findByEmail_shouldReturnUser_whenEmailExists() {
        Optional<User> result = userRepository.findByEmail("john.doe@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.get().getFirstName()).isEqualTo("John");
        assertThat(result.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("findByEmail should return empty when email does not exist")
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEmail should be case-sensitive")
    void findByEmail_shouldBeCaseSensitive() {
        Optional<User> result = userRepository.findByEmail("JOHN.DOE@EXAMPLE.COM");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEmail should return user with roles and groups eagerly loaded")
    void findByEmail_shouldLoadRolesAndGroupsEagerly() {
        Optional<User> result = userRepository.findByEmail("john.doe@example.com");

        assertThat(result).isPresent();
        User user = result.get();
        assertThat(user.getRoles()).hasSize(1);
        assertThat(user.getRoles().get(0).getName()).isEqualTo("ROLE_ADMIN");
        assertThat(user.getGroups()).hasSize(1);
        assertThat(user.getGroups().get(0).getName()).isEqualTo("Developers");
    }

    @Test
    @DisplayName("findByEmail should return user with empty roles when no roles assigned")
    void findByEmail_shouldReturnUserWithEmptyRoles_whenNoRolesAssigned() {
        User userWithoutRoles = createUser("noroles@example.com", "No", "Roles", null, true, false, List.of(), List.of());

        Optional<User> result = userRepository.findByEmail("noroles@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getRoles()).isEmpty();
        assertThat(result.get().getGroups()).isEmpty();
    }

    @Test
    @DisplayName("findByEmail should handle null email gracefully")
    void findByEmail_shouldHandleNullEmail() {
        Optional<User> result = userRepository.findByEmail(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEmail should handle empty string email")
    void findByEmail_shouldHandleEmptyStringEmail() {
        Optional<User> result = userRepository.findByEmail("");

        assertThat(result).isEmpty();
    }

    // ========== JPA Standard CRUD Tests ==========

    @Test
    @DisplayName("save should persist new user")
    void save_shouldPersistNewUser() {
        User newUser = User.builder()
                .email("new.user@example.com")
                .firstName("New")
                .lastName("User")
                .password("password123")
                .emailVerified(false)
                .locked(false)
                .roles(List.of())
                .groups(List.of())
                .build();

        User saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("new.user@example.com");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("save should update existing user")
    void save_shouldUpdateExistingUser() {
        user1.setFirstName("UpdatedName");

        User updated = userRepository.save(user1);

        assertThat(updated.getId()).isEqualTo(user1.getId());
        assertThat(updated.getFirstName()).isEqualTo("UpdatedName");
    }

    @Test
    @DisplayName("findById should return user when ID exists")
    void findById_shouldReturnUser_whenIdExists() {
        Optional<User> result = userRepository.findById(user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("findById should return empty when ID does not exist")
    void findById_shouldReturnEmpty_whenIdDoesNotExist() {
        Optional<User> result = userRepository.findById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll should return all users")
    void findAll_shouldReturnAllUsers() {
        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder(
                        "john.doe@example.com",
                        "jane.smith@example.com",
                        "locked.user@example.com"
                );
    }

    @Test
    @DisplayName("findAll with pagination should return correct page")
    void findAll_withPagination_shouldReturnCorrectPage() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("email"));

        Page<User> page = userRepository.findAll(pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("findAll with pagination should return second page")
    void findAll_withPagination_shouldReturnSecondPage() {
        Pageable pageable = PageRequest.of(1, 2, Sort.by("email"));

        Page<User> page = userRepository.findAll(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("findAll with sort should return users sorted by email")
    void findAll_withSort_shouldReturnUsersSortedByEmail() {
        List<User> users = userRepository.findAll(Sort.by("email"));

        assertThat(users).hasSize(3);
        assertThat(users.get(0).getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(users.get(1).getEmail()).isEqualTo("john.doe@example.com");
        assertThat(users.get(2).getEmail()).isEqualTo("locked.user@example.com");
    }

    @Test
    @DisplayName("findAll with sort descending should return users in reverse order")
    void findAll_withSortDescending_shouldReturnUsersInReverseOrder() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "email"));

        assertThat(users).hasSize(3);
        assertThat(users.get(0).getEmail()).isEqualTo("locked.user@example.com");
        assertThat(users.get(1).getEmail()).isEqualTo("john.doe@example.com");
        assertThat(users.get(2).getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    @DisplayName("count should return total number of users")
    void count_shouldReturnTotalNumberOfUsers() {
        long count = userRepository.count();

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("existsById should return true when user exists")
    void existsById_shouldReturnTrue_whenUserExists() {
        boolean exists = userRepository.existsById(user1.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById should return false when user does not exist")
    void existsById_shouldReturnFalse_whenUserDoesNotExist() {
        boolean exists = userRepository.existsById(99999L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("deleteById should remove user")
    void deleteById_shouldRemoveUser() {
        Long userId = user1.getId();

        userRepository.deleteById(userId);

        assertThat(userRepository.existsById(userId)).isFalse();
        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("delete should remove user entity")
    void delete_shouldRemoveUserEntity() {
        userRepository.delete(user1);

        assertThat(userRepository.existsById(user1.getId())).isFalse();
        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteAll should remove all users")
    void deleteAll_shouldRemoveAllUsers() {
        userRepository.deleteAll();

        assertThat(userRepository.count()).isEqualTo(0);
    }

    // ========== Edge Cases & Special Scenarios ==========

    @Test
    @DisplayName("should handle user with null firstName and lastName")
    void shouldHandleUserWithNullNames() {
        User userWithNullNames = createUser("nullnames@example.com", null, null, null, true, false, List.of(), List.of());

        Optional<User> result = userRepository.findByEmail("nullnames@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isNull();
        assertThat(result.get().getLastName()).isNull();
    }

    @Test
    @DisplayName("should handle user with null phoneNumber")
    void shouldHandleUserWithNullPhoneNumber() {
        Optional<User> result = userRepository.findByEmail("locked.user@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("should handle user with emailVerified false")
    void shouldHandleUserWithEmailVerifiedFalse() {
        Optional<User> result = userRepository.findByEmail("locked.user@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().isEmailVerified()).isFalse();
        assertThat(result.get().isEnabled()).isFalse(); // isEnabled() delegates to emailVerified
    }

    @Test
    @DisplayName("should handle user with locked true")
    void shouldHandleUserWithLockedTrue() {
        Optional<User> result = userRepository.findByEmail("locked.user@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().isLocked()).isTrue();
        assertThat(result.get().isAccountNonLocked()).isFalse(); // isAccountNonLocked() returns !locked
    }

    @Test
    @DisplayName("should handle user with multiple roles")
    void shouldHandleUserWithMultipleRoles() {
        User multiRoleUser = createUser("multi@example.com", "Multi", "Role", null, true, false,
                List.of(adminRole, userRole), List.of());

        Optional<User> result = userRepository.findByEmail("multi@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getRoles()).hasSize(2);
        assertThat(result.get().getRoles()).extracting(Role::getName)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    @DisplayName("should handle user with multiple groups")
    void shouldHandleUserWithMultipleGroups() {
        Group testGroup = Group.builder()
                .name("Testers")
                .build();
        Group testers = groupRepository.save(testGroup);

        User multiGroupUser = createUser("multigroup@example.com", "Multi", "Group", null, true, false,
                List.of(), List.of(developers, testers));

        Optional<User> result = userRepository.findByEmail("multigroup@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getGroups()).hasSize(2);
        assertThat(result.get().getGroups()).extracting(Group::getName)
                .containsExactlyInAnyOrder("Developers", "Testers");
    }

    @Test
    @DisplayName("should preserve timestamps on save")
    void shouldPreserveTimestampsOnSave() {
        User saved = userRepository.save(User.builder()
                .email("timestamp@example.com")
                .firstName("Time")
                .lastName("Stamp")
                .password("password")
                .emailVerified(true)
                .locked(false)
                .roles(List.of())
                .groups(List.of())
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("should handle very long email addresses")
    void shouldHandleVeryLongEmails() {
        // Email max length is 255 - use a realistic long email
        String longEmail = "very.long.email.address.for.testing.purposes.that.is.still.valid@example-domain-with-long-name.com"; // ~100 chars

        User userWithLongEmail = createUser(longEmail, "Long", "Email", null, true, false, List.of(), List.of());

        Optional<User> result = userRepository.findByEmail(longEmail);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(longEmail);
    }

    @Test
    @DisplayName("findAll on empty repository should return empty list")
    void findAll_onEmptyRepository_shouldReturnEmptyList() {
        userRepository.deleteAll();

        List<User> users = userRepository.findAll();

        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("findAll with pagination on empty repository should return empty page")
    void findAll_withPagination_onEmptyRepository_shouldReturnEmptyPage() {
        userRepository.deleteAll();

        Page<User> page = userRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("should maintain referential integrity with roles after user update")
    void shouldMaintainReferentialIntegrityWithRoles() {
        user2.setRoles(new ArrayList<>(List.of(adminRole, userRole))); // Use mutable list
        userRepository.save(user2);

        Optional<User> result = userRepository.findByEmail("jane.smith@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getRoles()).hasSize(2);
    }
}
