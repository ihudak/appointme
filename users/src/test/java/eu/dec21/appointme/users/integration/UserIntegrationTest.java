package eu.dec21.appointme.users.integration;

import eu.dec21.appointme.users.roles.entity.Role;
import eu.dec21.appointme.users.roles.repository.RoleRepository;
import eu.dec21.appointme.users.users.entity.User;
import eu.dec21.appointme.users.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Users module using Testcontainers.
 * Tests user management, authentication, and authorization features.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class UserIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("appme_users")
            .withUsername("pguser")
            .withPassword("p@ssw0rD!");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldConnectToPostgresContainer() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void shouldCreateAndRetrieveUser() {
        // Given
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getFirstName()).isEqualTo("John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");
        assertThat(savedUser.isLocked()).isFalse();

        // Verify password is encrypted
        assertThat(savedUser.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        User user = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();

        userRepository.save(user);

        // When
        var foundUser = userRepository.findByEmail("jane.smith@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(foundUser.get().getFirstName()).isEqualTo("Jane");
    }

    @Test
    void shouldCreateRoleAndAssignToUser() {
        // Given - Create role
        Role role = Role.builder()
                .name("USER")
                .build();
        Role savedRole = roleRepository.save(role);

        // Given - Create user with role
        User user = User.builder()
                .firstName("Bob")
                .lastName("Wilson")
                .email("bob.wilson@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        user.getRoles().add(savedRole);

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().get(0).getName()).isEqualTo("USER");
    }

    @Test
    void shouldFindLockedAndUnlockedUsers() {
        // Given
        User unlockedUser = User.builder()
                .firstName("Unlocked")
                .lastName("User")
                .email("unlocked@example.com")
                .password(passwordEncoder.encode("password123"))
                .locked(false)
                .build();

        User lockedUser = User.builder()
                .firstName("Locked")
                .lastName("User")
                .email("locked@example.com")
                .password(passwordEncoder.encode("password123"))
                .locked(true)
                .build();

        userRepository.save(unlockedUser);
        userRepository.save(lockedUser);

        // When
        var allUsers = userRepository.findAll();
        var unlockedUsers = allUsers.stream().filter(u -> !u.isLocked()).toList();

        // Then
        assertThat(unlockedUsers).hasSizeGreaterThanOrEqualTo(1);
        assertThat(unlockedUsers).anyMatch(u -> u.getEmail().equals("unlocked@example.com"));
    }
}
