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

import java.time.LocalDate;

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
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("password123"))
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .accountLocked(false)
                .enabled(true)
                .build();

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getFirstname()).isEqualTo("John");
        assertThat(savedUser.getLastname()).isEqualTo("Doe");
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.isAccountLocked()).isFalse();

        // Verify password is encrypted
        assertThat(savedUser.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        User user = User.builder()
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@example.com")
                .password(passwordEncoder.encode("password123"))
                .dateOfBirth(LocalDate.of(1992, 5, 15))
                .accountLocked(false)
                .enabled(true)
                .build();

        userRepository.save(user);

        // When
        var foundUser = userRepository.findByEmail("jane.smith@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(foundUser.get().getFirstname()).isEqualTo("Jane");
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
                .firstname("Bob")
                .lastname("Wilson")
                .email("bob.wilson@example.com")
                .password(passwordEncoder.encode("password123"))
                .dateOfBirth(LocalDate.of(1985, 3, 20))
                .accountLocked(false)
                .enabled(true)
                .build();
        user.getRoles().add(savedRole);

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().iterator().next().getName()).isEqualTo("USER");
    }

    @Test
    void shouldFindEnabledUsersOnly() {
        // Given
        User enabledUser = User.builder()
                .firstname("Enabled")
                .lastname("User")
                .email("enabled@example.com")
                .password(passwordEncoder.encode("password123"))
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .enabled(true)
                .accountLocked(false)
                .build();

        User disabledUser = User.builder()
                .firstname("Disabled")
                .lastname("User")
                .email("disabled@example.com")
                .password(passwordEncoder.encode("password123"))
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .enabled(false)
                .accountLocked(false)
                .build();

        userRepository.save(enabledUser);
        userRepository.save(disabledUser);

        // When
        var allUsers = userRepository.findAll();
        var enabledUsers = allUsers.stream().filter(User::isEnabled).toList();

        // Then
        assertThat(enabledUsers).hasSize(1);
        assertThat(enabledUsers.get(0).getEmail()).isEqualTo("enabled@example.com");
    }
}
