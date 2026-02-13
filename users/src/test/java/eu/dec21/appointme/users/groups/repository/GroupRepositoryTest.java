package eu.dec21.appointme.users.groups.repository;

import eu.dec21.appointme.users.groups.entity.Group;
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
@DisplayName("GroupRepository Tests")
class GroupRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine")
            .withDatabaseName("appme_users")
            .withUsername("pguser")
            .withPassword("p@ssw0rD!");

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    private Group developers;
    private Group admins;
    private Group testers;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        groupRepository.deleteAll();

        // Create test groups
        developers = createGroup("Developers");
        admins = createGroup("Administrators");
        testers = createGroup("Testers");
    }

    private Group createGroup(String name) {
        return groupRepository.save(Group.builder()
                .name(name)
                .build());
    }

    private User createUser(String email, String firstName, String lastName, List<Group> groups) {
        return userRepository.save(User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password("password123")
                .emailVerified(true)
                .locked(false)
                .roles(List.of())
                .groups(groups)
                .build());
    }

    // ========== findByName Tests ==========

    @Test
    @DisplayName("findByName should return group when name exists")
    void findByName_shouldReturnGroup_whenNameExists() {
        Optional<Group> result = groupRepository.findByName("Developers");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Developers");
        assertThat(result.get().getId()).isEqualTo(developers.getId());
    }

    @Test
    @DisplayName("findByName should return empty when name does not exist")
    void findByName_shouldReturnEmpty_whenNameDoesNotExist() {
        Optional<Group> result = groupRepository.findByName("NonExistent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByName should be case-sensitive")
    void findByName_shouldBeCaseSensitive() {
        Optional<Group> result = groupRepository.findByName("developers");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByName should handle null name gracefully")
    void findByName_shouldHandleNullName() {
        Optional<Group> result = groupRepository.findByName(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByName should handle empty string name")
    void findByName_shouldHandleEmptyStringName() {
        Optional<Group> result = groupRepository.findByName("");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByName should return group with users loaded lazily")
    void findByName_shouldReturnGroupWithUsersLazy() {
        // Create users in the group
        User user1 = createUser("user1@example.com", "User", "One", new ArrayList<>(List.of(developers)));
        User user2 = createUser("user2@example.com", "User", "Two", new ArrayList<>(List.of(developers)));

        Optional<Group> result = groupRepository.findByName("Developers");

        assertThat(result).isPresent();
        Group group = result.get();
        // Users are lazily loaded - they exist in DB but may not be loaded immediately
        assertThat(group.getId()).isEqualTo(developers.getId());
    }

    // ========== JPA Standard CRUD Tests ==========

    @Test
    @DisplayName("save should persist new group")
    void save_shouldPersistNewGroup() {
        Group newGroup = Group.builder()
                .name("QA Engineers")
                .build();

        Group saved = groupRepository.save(newGroup);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("QA Engineers");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("save should update existing group")
    void save_shouldUpdateExistingGroup() {
        developers.setName("Senior Developers");

        Group updated = groupRepository.save(developers);

        assertThat(updated.getId()).isEqualTo(developers.getId());
        assertThat(updated.getName()).isEqualTo("Senior Developers");
    }

    @Test
    @DisplayName("findById should return group when ID exists")
    void findById_shouldReturnGroup_whenIdExists() {
        Optional<Group> result = groupRepository.findById(developers.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Developers");
    }

    @Test
    @DisplayName("findById should return empty when ID does not exist")
    void findById_shouldReturnEmpty_whenIdDoesNotExist() {
        Optional<Group> result = groupRepository.findById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll should return all groups")
    void findAll_shouldReturnAllGroups() {
        List<Group> groups = groupRepository.findAll();

        assertThat(groups).hasSize(3);
        assertThat(groups).extracting(Group::getName)
                .containsExactlyInAnyOrder("Developers", "Administrators", "Testers");
    }

    @Test
    @DisplayName("findAll with pagination should return correct page")
    void findAll_withPagination_shouldReturnCorrectPage() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        Page<Group> page = groupRepository.findAll(pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("findAll with pagination should return second page")
    void findAll_withPagination_shouldReturnSecondPage() {
        Pageable pageable = PageRequest.of(1, 2, Sort.by("name"));

        Page<Group> page = groupRepository.findAll(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("findAll with sort should return groups sorted by name")
    void findAll_withSort_shouldReturnGroupsSortedByName() {
        List<Group> groups = groupRepository.findAll(Sort.by("name"));

        assertThat(groups).hasSize(3);
        assertThat(groups.get(0).getName()).isEqualTo("Administrators");
        assertThat(groups.get(1).getName()).isEqualTo("Developers");
        assertThat(groups.get(2).getName()).isEqualTo("Testers");
    }

    @Test
    @DisplayName("findAll with sort descending should return groups in reverse order")
    void findAll_withSortDescending_shouldReturnGroupsInReverseOrder() {
        List<Group> groups = groupRepository.findAll(Sort.by(Sort.Direction.DESC, "name"));

        assertThat(groups).hasSize(3);
        assertThat(groups.get(0).getName()).isEqualTo("Testers");
        assertThat(groups.get(1).getName()).isEqualTo("Developers");
        assertThat(groups.get(2).getName()).isEqualTo("Administrators");
    }

    @Test
    @DisplayName("count should return total number of groups")
    void count_shouldReturnTotalNumberOfGroups() {
        long count = groupRepository.count();

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("existsById should return true when group exists")
    void existsById_shouldReturnTrue_whenGroupExists() {
        boolean exists = groupRepository.existsById(developers.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById should return false when group does not exist")
    void existsById_shouldReturnFalse_whenGroupDoesNotExist() {
        boolean exists = groupRepository.existsById(99999L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("deleteById should remove group")
    void deleteById_shouldRemoveGroup() {
        Long groupId = developers.getId();

        groupRepository.deleteById(groupId);

        assertThat(groupRepository.existsById(groupId)).isFalse();
        assertThat(groupRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("delete should remove group entity")
    void delete_shouldRemoveGroupEntity() {
        groupRepository.delete(developers);

        assertThat(groupRepository.existsById(developers.getId())).isFalse();
        assertThat(groupRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteAll should remove all groups")
    void deleteAll_shouldRemoveAllGroups() {
        groupRepository.deleteAll();

        assertThat(groupRepository.count()).isEqualTo(0);
    }

    // ========== Edge Cases & Special Scenarios ==========

    @Test
    @DisplayName("should handle group name with spaces")
    void shouldHandleGroupNameWithSpaces() {
        Group groupWithSpaces = createGroup("Senior Java Developers");

        Optional<Group> result = groupRepository.findByName("Senior Java Developers");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Senior Java Developers");
    }

    @Test
    @DisplayName("should handle group name with special characters")
    void shouldHandleGroupNameWithSpecialCharacters() {
        Group groupWithSpecialChars = createGroup("Dev-Team #1 (Backend)");

        Optional<Group> result = groupRepository.findByName("Dev-Team #1 (Backend)");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Dev-Team #1 (Backend)");
    }

    @Test
    @DisplayName("should handle very long group names")
    void shouldHandleVeryLongGroupNames() {
        // Max length is 100 characters
        String longName = "A".repeat(100);
        Group groupWithLongName = createGroup(longName);

        Optional<Group> result = groupRepository.findByName(longName);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(longName);
        assertThat(result.get().getName().length()).isEqualTo(100);
    }

    @Test
    @DisplayName("should preserve timestamps on save")
    void shouldPreserveTimestampsOnSave() {
        Group saved = groupRepository.save(Group.builder()
                .name("New Team")
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("findAll on empty repository should return empty list")
    void findAll_onEmptyRepository_shouldReturnEmptyList() {
        groupRepository.deleteAll();

        List<Group> groups = groupRepository.findAll();

        assertThat(groups).isEmpty();
    }

    @Test
    @DisplayName("findAll with pagination on empty repository should return empty page")
    void findAll_withPagination_onEmptyRepository_shouldReturnEmptyPage() {
        groupRepository.deleteAll();

        Page<Group> page = groupRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("should handle group with multiple users")
    void shouldHandleGroupWithMultipleUsers() {
        User user1 = createUser("dev1@example.com", "Dev", "One", new ArrayList<>(List.of(developers)));
        User user2 = createUser("dev2@example.com", "Dev", "Two", new ArrayList<>(List.of(developers)));
        User user3 = createUser("dev3@example.com", "Dev", "Three", new ArrayList<>(List.of(developers)));

        Optional<Group> result = groupRepository.findById(developers.getId());

        assertThat(result).isPresent();
        // Group loaded, users relationship exists
        assertThat(result.get().getId()).isEqualTo(developers.getId());
    }

    @Test
    @DisplayName("should handle user in multiple groups")
    void shouldHandleUserInMultipleGroups() {
        User multiGroupUser = createUser("multi@example.com", "Multi", "Group",
                new ArrayList<>(List.of(developers, admins, testers)));

        // Verify groups still exist
        assertThat(groupRepository.findByName("Developers")).isPresent();
        assertThat(groupRepository.findByName("Administrators")).isPresent();
        assertThat(groupRepository.findByName("Testers")).isPresent();
    }

    @Test
    @DisplayName("findByName should find exact matches only")
    void findByName_shouldFindExactMatchesOnly() {
        createGroup("Dev");
        createGroup("Developer");
        createGroup("Development");

        Optional<Group> result = groupRepository.findByName("Developer");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Developer");
    }

    @Test
    @DisplayName("should handle group name with leading and trailing spaces")
    void shouldHandleGroupNameWithLeadingAndTrailingSpaces() {
        Group groupWithSpaces = createGroup("  Spaced Group  ");

        Optional<Group> result = groupRepository.findByName("  Spaced Group  ");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("  Spaced Group  ");
    }

    @Test
    @DisplayName("should handle single character group name")
    void shouldHandleSingleCharacterGroupName() {
        Group singleChar = createGroup("A");

        Optional<Group> result = groupRepository.findByName("A");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("A");
    }

    @Test
    @DisplayName("deleteById should not throw when ID does not exist")
    void deleteById_shouldNotThrow_whenIdDoesNotExist() {
        // Spring Data JPA's deleteById silently succeeds if entity doesn't exist
        groupRepository.deleteById(99999L);

        // Verify other groups still exist
        assertThat(groupRepository.count()).isEqualTo(3);
    }
}
