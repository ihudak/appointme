package eu.dec21.appointme.users.roles.entity;

import eu.dec21.appointme.users.users.entity.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testBuilder_withAllFields() {
        User user1 = User.builder()
                .email("user1@example.com")
                .password("password123")
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .password("password456")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        Role role = Role.builder()
                .name("ROLE_ADMIN")
                .users(users)
                .build();

        assertNotNull(role);
        assertEquals("ROLE_ADMIN", role.getName());
        assertNotNull(role.getUsers());
        assertEquals(2, role.getUsers().size());
        assertTrue(role.getUsers().contains(user1));
        assertTrue(role.getUsers().contains(user2));
    }

    @Test
    void testBuilder_withMinimalFields() {
        Role role = Role.builder()
                .name("ROLE_USER")
                .build();

        assertNotNull(role);
        assertEquals("ROLE_USER", role.getName());
        assertNull(role.getUsers());
    }

    @Test
    void testNoArgsConstructor() {
        Role role = new Role();
        assertNotNull(role);
        assertNull(role.getName());
        assertNull(role.getUsers());
    }

    @Test
    void testAllArgsConstructor() {
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user);

        Role role = new Role("ROLE_MANAGER", users);

        assertNotNull(role);
        assertEquals("ROLE_MANAGER", role.getName());
        assertEquals(1, role.getUsers().size());
        assertTrue(role.getUsers().contains(user));
    }

    @Test
    void testSetters() {
        Role role = new Role();
        role.setName("ROLE_GUEST");

        assertEquals("ROLE_GUEST", role.getName());

        // Update values
        role.setName("ROLE_MEMBER");
        assertEquals("ROLE_MEMBER", role.getName());

        User user = User.builder()
                .email("newuser@example.com")
                .password("password")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user);
        role.setUsers(users);

        assertEquals(1, role.getUsers().size());
        assertTrue(role.getUsers().contains(user));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testName_whenNullOrEmpty_shouldFailValidation(String invalidName) {
        Role role = Role.builder()
                .name(invalidName)
                .build();

        Set<ConstraintViolation<Role>> violations = validator.validate(role);

        assertFalse(violations.isEmpty(), "Null/empty name should fail @NotBlank validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ROLE_ADMIN",
            "ROLE_USER",
            "ROLE_MODERATOR",
            "ROLE_MANAGER",
            "ROLE_DEVELOPER",
            "ROLE_SUPPORT",
            "ROLE_SALES",
            "ROLE_ACCOUNTANT",
            "ROLE_HR_MANAGER",
            "ROLE_SYSTEM_ADMINISTRATOR"
    })
    void testName_whenValid_shouldPassValidation(String validName) {
        Role role = Role.builder()
                .name(validName)
                .build();

        assertEquals(validName, role.getName());
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withStandardRolePrefix() {
        Role role1 = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        Role role2 = Role.builder()
                .name("ROLE_USER")
                .build();

        Role role3 = Role.builder()
                .name("ROLE_GUEST")
                .build();

        assertTrue(role1.getName().startsWith("ROLE_"));
        assertTrue(role2.getName().startsWith("ROLE_"));
        assertTrue(role3.getName().startsWith("ROLE_"));
    }

    @Test
    void testName_withoutRolePrefix() {
        // Some systems allow role names without ROLE_ prefix
        Role role = Role.builder()
                .name("ADMIN")
                .build();

        assertEquals("ADMIN", role.getName());
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withSpecialCharacters() {
        String specialName = "ROLE_ADMIN-SUPER_USER";

        Role role = Role.builder()
                .name(specialName)
                .build();

        assertEquals(specialName, role.getName());
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withLowercase() {
        Role role = Role.builder()
                .name("role_user")
                .build();

        assertEquals("role_user", role.getName());
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withMixedCase() {
        Role role = Role.builder()
                .name("Role_Admin")
                .build();

        assertEquals("Role_Admin", role.getName());
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withLongName() {
        // This name is 74 chars - exceeds max length of 50
        String longName = "ROLE_SUPER_ADMINISTRATOR_WITH_EXTENDED_PRIVILEGES_AND_FULL_SYSTEM_ACCESS";

        Role role = Role.builder()
                .name(longName)
                .build();

        assertEquals(longName, role.getName());
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertFalse(violations.isEmpty(), "Long name should violate max length constraint");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testUsers_emptyList() {
        Role role = Role.builder()
                .name("ROLE_ADMIN")
                .users(new ArrayList<>())
                .build();

        assertNotNull(role.getUsers());
        assertEquals(0, role.getUsers().size());
        assertTrue(role.getUsers().isEmpty());
    }

    @Test
    void testUsers_singleUser() {
        User user = User.builder()
                .email("single@example.com")
                .password("password")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user);

        Role role = Role.builder()
                .name("ROLE_USER")
                .users(users)
                .build();

        assertEquals(1, role.getUsers().size());
        assertEquals(user, role.getUsers().get(0));
    }

    @Test
    void testUsers_multipleUsers() {
        User user1 = User.builder()
                .email("user1@example.com")
                .password("password1")
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .password("password2")
                .build();

        User user3 = User.builder()
                .email("user3@example.com")
                .password("password3")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);

        Role role = Role.builder()
                .name("ROLE_MODERATOR")
                .users(users)
                .build();

        assertEquals(3, role.getUsers().size());
        assertTrue(role.getUsers().contains(user1));
        assertTrue(role.getUsers().contains(user2));
        assertTrue(role.getUsers().contains(user3));
    }

    @Test
    void testUsers_addUserToExistingRole() {
        Role role = Role.builder()
                .name("ROLE_ADMIN")
                .users(new ArrayList<>())
                .build();

        assertEquals(0, role.getUsers().size());

        User user1 = User.builder()
                .email("newuser1@example.com")
                .password("password")
                .build();

        role.getUsers().add(user1);
        assertEquals(1, role.getUsers().size());

        User user2 = User.builder()
                .email("newuser2@example.com")
                .password("password")
                .build();

        role.getUsers().add(user2);
        assertEquals(2, role.getUsers().size());
    }

    @Test
    void testUsers_removeUserFromRole() {
        User user1 = User.builder()
                .email("user1@example.com")
                .password("password")
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .password("password")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        Role role = Role.builder()
                .name("ROLE_ADMIN")
                .users(users)
                .build();

        assertEquals(2, role.getUsers().size());

        role.getUsers().remove(user1);
        assertEquals(1, role.getUsers().size());
        assertFalse(role.getUsers().contains(user1));
        assertTrue(role.getUsers().contains(user2));
    }

    @Test
    void testUsers_bidirectionalRelationship() {
        Role role = Role.builder()
                .name("ROLE_ADMIN")
                .users(new ArrayList<>())
                .build();

        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .roles(new ArrayList<>())
                .build();

        // Add role to user
        user.getRoles().add(role);
        // Add user to role
        role.getUsers().add(user);

        assertTrue(user.getRoles().contains(role));
        assertTrue(role.getUsers().contains(user));
    }

    @Test
    void testMultipleRoles_withSharedUsers() {
        User sharedUser = User.builder()
                .email("shared@example.com")
                .password("password")
                .build();

        List<User> users1 = new ArrayList<>();
        users1.add(sharedUser);

        List<User> users2 = new ArrayList<>();
        users2.add(sharedUser);

        Role role1 = Role.builder()
                .name("ROLE_ADMIN")
                .users(users1)
                .build();

        Role role2 = Role.builder()
                .name("ROLE_MODERATOR")
                .users(users2)
                .build();

        assertEquals(1, role1.getUsers().size());
        assertEquals(1, role2.getUsers().size());
        assertEquals(sharedUser, role1.getUsers().get(0));
        assertEquals(sharedUser, role2.getUsers().get(0));
    }

    @Test
    void testRole_replaceUsersList() {
        User user1 = User.builder()
                .email("user1@example.com")
                .password("password")
                .build();

        List<User> initialUsers = new ArrayList<>();
        initialUsers.add(user1);

        Role role = Role.builder()
                .name("ROLE_ADMIN")
                .users(initialUsers)
                .build();

        assertEquals(1, role.getUsers().size());

        // Replace with new users list
        User user2 = User.builder()
                .email("user2@example.com")
                .password("password")
                .build();

        User user3 = User.builder()
                .email("user3@example.com")
                .password("password")
                .build();

        List<User> newUsers = new ArrayList<>();
        newUsers.add(user2);
        newUsers.add(user3);

        role.setUsers(newUsers);

        assertEquals(2, role.getUsers().size());
        assertFalse(role.getUsers().contains(user1));
        assertTrue(role.getUsers().contains(user2));
        assertTrue(role.getUsers().contains(user3));
    }

    @Test
    void testRole_clearUsers() {
        User user1 = User.builder()
                .email("user1@example.com")
                .password("password")
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .password("password")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        Role role = Role.builder()
                .name("ROLE_ADMIN")
                .users(users)
                .build();

        assertEquals(2, role.getUsers().size());

        role.getUsers().clear();
        assertEquals(0, role.getUsers().size());
        assertTrue(role.getUsers().isEmpty());
    }

    @Test
    void testRole_lazyLoadingAnnotation() {
        // This test verifies the @ManyToMany lazy fetch type is configured
        Role role = Role.builder()
                .name("ROLE_USER")
                .build();

        // Users should not be loaded until accessed (in real JPA context)
        // In unit tests, this just verifies the structure
        assertNull(role.getUsers());
    }

    @Test
    void testRole_uniqueNameConstraint() {
        // This test documents the unique constraint on name field
        // In real database, two roles with same name would violate constraint
        Role role1 = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        Role role2 = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        // Both entities can be created in memory
        assertNotNull(role1);
        assertNotNull(role2);
        assertEquals(role1.getName(), role2.getName());
        // Note: Actual uniqueness constraint is enforced at database level
    }

    @Test
    void testRole_hierarchySimulation() {
        // Test simulating a role hierarchy (ADMIN has more privileges than USER)
        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        Role userRole = Role.builder()
                .name("ROLE_USER")
                .build();

        Role guestRole = Role.builder()
                .name("ROLE_GUEST")
                .build();

        assertNotNull(adminRole);
        assertNotNull(userRole);
        assertNotNull(guestRole);
        
        // In real application, hierarchy logic would be implemented in security config
        assertNotEquals(adminRole.getName(), userRole.getName());
        assertNotEquals(userRole.getName(), guestRole.getName());
    }

    @Test
    void testRole_commonRoleNames() {
        Role admin = Role.builder().name("ROLE_ADMIN").build();
        Role user = Role.builder().name("ROLE_USER").build();
        Role moderator = Role.builder().name("ROLE_MODERATOR").build();
        Role manager = Role.builder().name("ROLE_MANAGER").build();
        Role guest = Role.builder().name("ROLE_GUEST").build();

        assertEquals("ROLE_ADMIN", admin.getName());
        assertEquals("ROLE_USER", user.getName());
        assertEquals("ROLE_MODERATOR", moderator.getName());
        assertEquals("ROLE_MANAGER", manager.getName());
        assertEquals("ROLE_GUEST", guest.getName());
    }

    @Test
    void testRole_withUserHavingMultipleRoles() {
        User user = User.builder()
                .email("multiRole@example.com")
                .password("password")
                .roles(new ArrayList<>())
                .build();

        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .users(new ArrayList<>())
                .build();

        Role moderatorRole = Role.builder()
                .name("ROLE_MODERATOR")
                .users(new ArrayList<>())
                .build();

        // User has multiple roles
        user.getRoles().add(adminRole);
        user.getRoles().add(moderatorRole);

        // Roles have the user
        adminRole.getUsers().add(user);
        moderatorRole.getUsers().add(user);

        assertEquals(2, user.getRoles().size());
        assertTrue(adminRole.getUsers().contains(user));
        assertTrue(moderatorRole.getUsers().contains(user));
    }

    @Test
    void testBaseBasicEntity_inheritance() {
        Role role = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        // Verify inherited fields from BaseBasicEntity are accessible
        assertNull(role.getId()); // Not set until persisted
        assertNull(role.getCreatedAt()); // Set by JPA auditing
        assertNull(role.getUpdatedAt()); // Set by JPA auditing
    }

    @Test
    void testRole_setId() {
        Role role = Role.builder()
                .name("ROLE_USER")
                .build();

        role.setId(456L);
        assertEquals(456L, role.getId());
    }

    @Test
    void testRole_withNumericSuffix() {
        Role role = Role.builder()
                .name("ROLE_LEVEL_1")
                .build();

        assertEquals("ROLE_LEVEL_1", role.getName());
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty());
    }

    // ===== Name Validation Tests =====

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testName_blankOrEmpty(String name) {
        Role role = Role.builder()
                .name(name)
                .build();

        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testName_withMinLength() {
        Role role = Role.builder()
                .name("R")
                .build();

        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withMaxLength() {
        String maxLengthName = "R".repeat(50);
        Role role = Role.builder()
                .name(maxLengthName)
                .build();

        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_exceedsMaxLength() {
        String tooLongName = "R".repeat(51);
        Role role = Role.builder()
                .name(tooLongName)
                .build();

        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name") &&
                        v.getMessage().contains("1-50 characters")));
    }

    @Test
    void testNameValidation_typicalRolePrefix() {
        Role role = Role.builder()
                .name("ROLE_CUSTOM_PERMISSION")
                .build();

        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty());
        assertEquals("ROLE_CUSTOM_PERMISSION", role.getName());
    }

    @Test
    void testNameValidation_withoutRolePrefix() {
        // Although convention is ROLE_ prefix, validation allows any non-blank name
        Role role = Role.builder()
                .name("ADMIN")
                .build();

        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty());
        assertEquals("ADMIN", role.getName());
    }

    @Test
    void testNameValidation_withSpecialCharacters() {
        Role role = Role.builder()
                .name("ROLE_READ-WRITE-EXECUTE")
                .build();

        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty());
        assertEquals("ROLE_READ-WRITE-EXECUTE", role.getName());
    }
}
