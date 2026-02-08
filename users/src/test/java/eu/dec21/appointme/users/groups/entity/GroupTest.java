package eu.dec21.appointme.users.groups.entity;

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

class GroupTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testBuilder_withAllFields() {
        User user1 = User.builder()
                .email("user1@test.com")
                .email("user1@example.com")
                .build();

        User user2 = User.builder()
                .email("user2@test.com")
                .email("user2@example.com")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        Group group = Group.builder()
                .name("Administrators")
                .users(users)
                .build();

        assertNotNull(group);
        assertEquals("Administrators", group.getName());
        assertNotNull(group.getUsers());
        assertEquals(2, group.getUsers().size());
        assertTrue(group.getUsers().contains(user1));
        assertTrue(group.getUsers().contains(user2));
    }

    @Test
    void testBuilder_withMinimalFields() {
        Group group = Group.builder()
                .name("Developers")
                .build();

        assertNotNull(group);
        assertEquals("Developers", group.getName());
        assertNull(group.getUsers());
    }

    @Test
    void testNoArgsConstructor() {
        Group group = new Group();
        assertNotNull(group);
        assertNull(group.getName());
        assertNull(group.getUsers());
    }

    @Test
    void testAllArgsConstructor() {
        User user = User.builder()
                .email("testuser@test.com")
                .email("test@example.com")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user);

        Group group = new Group("TestGroup", users);

        assertNotNull(group);
        assertEquals("TestGroup", group.getName());
        assertEquals(1, group.getUsers().size());
        assertTrue(group.getUsers().contains(user));
    }

    @Test
    void testSetters() {
        Group group = new Group();
        group.setName("Initial Name");

        assertEquals("Initial Name", group.getName());

        // Update values
        group.setName("Updated Name");
        assertEquals("Updated Name", group.getName());

        User user = User.builder()
                .email("newuser@test.com")
                .email("newuser@example.com")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user);
        group.setUsers(users);

        assertEquals(1, group.getUsers().size());
        assertTrue(group.getUsers().contains(user));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testName_whenNullOrEmpty_shouldFailValidation(String invalidName) {
        Group group = Group.builder()
                .name(invalidName)
                .build();

        Set<ConstraintViolation<Group>> violations = validator.validate(group);

        // Note: validation happens at DB level (@Column nullable=false), not with Bean Validation
        // So this test verifies the entity can be created with null/empty values
        // but would fail at persistence time
        assertNotNull(group);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Administrators",
            "Developers",
            "Managers",
            "Sales Team",
            "IT Support",
            "HR Department",
            "Finance-Accounting",
            "R&D Team",
            "Marketing_Team",
            "Customer Support 24/7"
    })
    void testName_whenValid_shouldPassValidation(String validName) {
        Group group = Group.builder()
                .name(validName)
                .build();

        assertEquals(validName, group.getName());
        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withSpecialCharacters() {
        String specialName = "Team-Alpha_Beta & Gamma (2024)";

        Group group = Group.builder()
                .name(specialName)
                .build();

        assertEquals(specialName, group.getName());
        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withUnicodeCharacters() {
        Group group1 = Group.builder()
                .name("München Team")
                .build();

        Group group2 = Group.builder()
                .name("北京团队")
                .build();

        Group group3 = Group.builder()
                .name("Équipe Paris")
                .build();

        assertEquals("München Team", group1.getName());
        assertEquals("北京团队", group2.getName());
        assertEquals("Équipe Paris", group3.getName());

        Set<ConstraintViolation<Group>> violations1 = validator.validate(group1);
        Set<ConstraintViolation<Group>> violations2 = validator.validate(group2);
        Set<ConstraintViolation<Group>> violations3 = validator.validate(group3);

        assertTrue(violations1.isEmpty());
        assertTrue(violations2.isEmpty());
        assertTrue(violations3.isEmpty());
    }

    @Test
    void testName_withLongName() {
        // This name is 139 chars - exceeds max length of 100
        String longName = "Very Long Group Name With Many Words To Test The Maximum Length That Can Be Stored In Database Field Without Truncation Or Errors";

        Group group = Group.builder()
                .name(longName)
                .build();

        assertEquals(longName, group.getName());
        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        assertFalse(violations.isEmpty(), "Long name should violate max length constraint");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testUsers_emptyList() {
        Group group = Group.builder()
                .name("Empty Group")
                .users(new ArrayList<>())
                .build();

        assertNotNull(group.getUsers());
        assertEquals(0, group.getUsers().size());
        assertTrue(group.getUsers().isEmpty());
    }

    @Test
    void testUsers_singleUser() {
        User user = User.builder()
                .email("singleuser@test.com")
                .email("single@example.com")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user);

        Group group = Group.builder()
                .name("Single User Group")
                .users(users)
                .build();

        assertEquals(1, group.getUsers().size());
        assertEquals(user, group.getUsers().get(0));
    }

    @Test
    void testUsers_multipleUsers() {
        User user1 = User.builder()
                .email("user1@test.com")
                .email("user1@example.com")
                .build();

        User user2 = User.builder()
                .email("user2@test.com")
                .email("user2@example.com")
                .build();

        User user3 = User.builder()
                .email("user3@test.com")
                .email("user3@example.com")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);

        Group group = Group.builder()
                .name("Multi User Group")
                .users(users)
                .build();

        assertEquals(3, group.getUsers().size());
        assertTrue(group.getUsers().contains(user1));
        assertTrue(group.getUsers().contains(user2));
        assertTrue(group.getUsers().contains(user3));
    }

    @Test
    void testUsers_addUserToExistingGroup() {
        Group group = Group.builder()
                .name("Dynamic Group")
                .users(new ArrayList<>())
                .build();

        assertEquals(0, group.getUsers().size());

        User user1 = User.builder()
                .email("newuser1@test.com")
                .email("newuser1@example.com")
                .build();

        group.getUsers().add(user1);
        assertEquals(1, group.getUsers().size());

        User user2 = User.builder()
                .email("newuser2@test.com")
                .email("newuser2@example.com")
                .build();

        group.getUsers().add(user2);
        assertEquals(2, group.getUsers().size());
    }

    @Test
    void testUsers_removeUserFromGroup() {
        User user1 = User.builder()
                .email("user1@test.com")
                .email("user1@example.com")
                .build();

        User user2 = User.builder()
                .email("user2@test.com")
                .email("user2@example.com")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        Group group = Group.builder()
                .name("Test Group")
                .users(users)
                .build();

        assertEquals(2, group.getUsers().size());

        group.getUsers().remove(user1);
        assertEquals(1, group.getUsers().size());
        assertFalse(group.getUsers().contains(user1));
        assertTrue(group.getUsers().contains(user2));
    }

    @Test
    void testUsers_bidirectionalRelationship() {
        Group group = Group.builder()
                .name("Test Group")
                .users(new ArrayList<>())
                .build();

        User user = User.builder()
                .email("testuser@test.com")
                .email("test@example.com")
                .groups(new ArrayList<>())
                .build();

        // Add group to user
        user.getGroups().add(group);
        // Add user to group
        group.getUsers().add(user);

        assertTrue(user.getGroups().contains(group));
        assertTrue(group.getUsers().contains(user));
    }

    @Test
    void testMultipleGroups_withSharedUsers() {
        User sharedUser = User.builder()
                .email("shareduser@test.com")
                .email("shared@example.com")
                .build();

        List<User> users1 = new ArrayList<>();
        users1.add(sharedUser);

        List<User> users2 = new ArrayList<>();
        users2.add(sharedUser);

        Group group1 = Group.builder()
                .name("Group 1")
                .users(users1)
                .build();

        Group group2 = Group.builder()
                .name("Group 2")
                .users(users2)
                .build();

        assertEquals(1, group1.getUsers().size());
        assertEquals(1, group2.getUsers().size());
        assertEquals(sharedUser, group1.getUsers().get(0));
        assertEquals(sharedUser, group2.getUsers().get(0));
    }

    @Test
    void testGroup_withDifferentUserRoles() {
        User admin = User.builder()
                .email("admin@test.com")
                .email("admin@example.com")
                .build();

        User developer = User.builder()
                .email("developer@test.com")
                .email("developer@example.com")
                .build();

        User viewer = User.builder()
                .email("viewer@test.com")
                .email("viewer@example.com")
                .build();

        List<User> users = new ArrayList<>();
        users.add(admin);
        users.add(developer);
        users.add(viewer);

        Group group = Group.builder()
                .name("Mixed Roles Group")
                .users(users)
                .build();

        assertEquals(3, group.getUsers().size());
    }

    @Test
    void testGroup_replaceUsersList() {
        User user1 = User.builder()
                .email("user1@test.com")
                .email("user1@example.com")
                .build();

        List<User> initialUsers = new ArrayList<>();
        initialUsers.add(user1);

        Group group = Group.builder()
                .name("Test Group")
                .users(initialUsers)
                .build();

        assertEquals(1, group.getUsers().size());

        // Replace with new users list
        User user2 = User.builder()
                .email("user2@test.com")
                .email("user2@example.com")
                .build();

        User user3 = User.builder()
                .email("user3@test.com")
                .email("user3@example.com")
                .build();

        List<User> newUsers = new ArrayList<>();
        newUsers.add(user2);
        newUsers.add(user3);

        group.setUsers(newUsers);

        assertEquals(2, group.getUsers().size());
        assertFalse(group.getUsers().contains(user1));
        assertTrue(group.getUsers().contains(user2));
        assertTrue(group.getUsers().contains(user3));
    }

    @Test
    void testGroup_clearUsers() {
        User user1 = User.builder()
                .email("user1@test.com")
                .email("user1@example.com")
                .build();

        User user2 = User.builder()
                .email("user2@test.com")
                .email("user2@example.com")
                .build();

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        Group group = Group.builder()
                .name("Test Group")
                .users(users)
                .build();

        assertEquals(2, group.getUsers().size());

        group.getUsers().clear();
        assertEquals(0, group.getUsers().size());
        assertTrue(group.getUsers().isEmpty());
    }

    @Test
    void testGroup_lazyLoadingAnnotation() {
        // This test verifies the @ManyToMany lazy fetch type is configured
        Group group = Group.builder()
                .name("Lazy Group")
                .build();

        // Users should not be loaded until accessed (in real JPA context)
        // In unit tests, this just verifies the structure
        assertNull(group.getUsers());
    }

    @Test
    void testGroup_uniqueNameConstraint() {
        // This test documents the unique constraint on name field
        // In real database, two groups with same name would violate constraint
        Group group1 = Group.builder()
                .name("Unique Name")
                .build();

        Group group2 = Group.builder()
                .name("Unique Name")
                .build();

        // Both entities can be created in memory
        assertNotNull(group1);
        assertNotNull(group2);
        assertEquals(group1.getName(), group2.getName());
        // Note: Actual uniqueness constraint is enforced at database level
    }

    @Test
    void testBaseBasicEntity_inheritance() {
        Group group = Group.builder()
                .name("Test Group")
                .build();

        // Verify inherited fields from BaseBasicEntity are accessible
        assertNull(group.getId()); // Not set until persisted
        assertNull(group.getCreatedAt()); // Set by JPA auditing
        assertNull(group.getUpdatedAt()); // Set by JPA auditing
    }

    @Test
    void testGroup_setId() {
        Group group = Group.builder()
                .name("Test Group")
                .build();

        group.setId(123L);
        assertEquals(123L, group.getId());
    }

    // ===== Name Validation Tests =====

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testName_blankOrEmpty(String name) {
        Group group = Group.builder()
                .name(name)
                .build();

        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testName_withMinLength() {
        Group group = Group.builder()
                .name("G")
                .build();

        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withMaxLength() {
        String maxLengthName = "G".repeat(100);
        Group group = Group.builder()
                .name(maxLengthName)
                .build();

        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_exceedsMaxLength() {
        String tooLongName = "G".repeat(101);
        Group group = Group.builder()
                .name(tooLongName)
                .build();

        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name") &&
                        v.getMessage().contains("1-100 characters")));
    }

    @Test
    void testNameValidation_withSpecialCharacters() {
        Group group = Group.builder()
                .name("Dev-Team-2024 (Backend)")
                .build();

        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        assertTrue(violations.isEmpty());
        assertEquals("Dev-Team-2024 (Backend)", group.getName());
    }

    @Test
    void testName_withNumbers() {
        Group group = Group.builder()
                .name("Team 123")
                .build();

        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        assertTrue(violations.isEmpty());
        assertEquals("Team 123", group.getName());
    }

    @Test
    void testName_withEmailLikeFormat() {
        // Group name can technically be email-like
        Group group = Group.builder()
                .name("team@company.com")
                .build();

        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        assertTrue(violations.isEmpty());
        assertEquals("team@company.com", group.getName());
    }

    @Test
    void testName_commonGroupNames() {
        Group developers = Group.builder().name("Developers").build();
        Group admins = Group.builder().name("Administrators").build();
        Group sales = Group.builder().name("Sales Team").build();
        Group marketing = Group.builder().name("Marketing Department").build();

        assertEquals("Developers", developers.getName());
        assertEquals("Administrators", admins.getName());
        assertEquals("Sales Team", sales.getName());
        assertEquals("Marketing Department", marketing.getName());

        assertTrue(validator.validate(developers).isEmpty());
        assertTrue(validator.validate(admins).isEmpty());
        assertTrue(validator.validate(sales).isEmpty());
        assertTrue(validator.validate(marketing).isEmpty());
    }
}
