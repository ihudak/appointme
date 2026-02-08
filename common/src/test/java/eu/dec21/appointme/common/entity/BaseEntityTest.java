package eu.dec21.appointme.common.entity;

import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    // Concrete test entity to test abstract BaseEntity
    @Entity
    @SuperBuilder
    @NoArgsConstructor
    private static class TestEntity extends BaseEntity {
        // No additional fields needed for testing
    }

    // ===== Constructor Tests =====

    @Test
    void testNoArgsConstructor() {
        TestEntity entity = new TestEntity();

        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getCreatedBy());
        assertNull(entity.getUpdatedBy());
    }

    @Test
    void testAllArgsConstructor() {
        TestEntity entity = new TestEntity();
        entity.setId(123L);
        entity.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        entity.setUpdatedAt(LocalDateTime.of(2024, 1, 2, 15, 30));
        entity.setCreatedBy(456L);
        entity.setUpdatedBy(789L);

        assertEquals(123L, entity.getId());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertEquals(456L, entity.getCreatedBy());
        assertEquals(789L, entity.getUpdatedBy());
    }

    @Test
    void testSuperBuilder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusHours(1);

        TestEntity entity = TestEntity.builder()
                .id(100L)
                .createdAt(now)
                .updatedAt(later)
                .createdBy(200L)
                .updatedBy(300L)
                .build();

        assertEquals(100L, entity.getId());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(later, entity.getUpdatedAt());
        assertEquals(200L, entity.getCreatedBy());
        assertEquals(300L, entity.getUpdatedBy());
    }

    // ===== Inheritance Tests =====

    @Test
    void testInheritance_extendsBaseBasicEntity() {
        TestEntity entity = new TestEntity();

        // Verify fields from BaseBasicEntity are accessible
        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());

        // Verify fields from BaseEntity are accessible
        assertNull(entity.getCreatedBy());
        assertNull(entity.getUpdatedBy());
    }

    @Test
    void testInheritance_allFieldsAccessible() {
        TestEntity entity = TestEntity.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(10L)
                .updatedBy(20L)
                .build();

        assertNotNull(entity.getId());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertNotNull(entity.getCreatedBy());
        assertNotNull(entity.getUpdatedBy());
    }

    // ===== CreatedBy Field Tests =====

    @Test
    void testCreatedBy_defaultNull() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getCreatedBy());
    }

    @Test
    void testCreatedBy_setAndGet() {
        TestEntity entity = new TestEntity();
        entity.setCreatedBy(100L);

        assertEquals(100L, entity.getCreatedBy());
    }

    @Test
    void testCreatedBy_withDifferentValues() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();

        entity1.setCreatedBy(1L);
        entity2.setCreatedBy(2L);

        assertEquals(1L, entity1.getCreatedBy());
        assertEquals(2L, entity2.getCreatedBy());
        assertNotEquals(entity1.getCreatedBy(), entity2.getCreatedBy());
    }

    @Test
    void testCreatedBy_sameAsUpdatedBy() {
        TestEntity entity = new TestEntity();
        entity.setCreatedBy(100L);
        entity.setUpdatedBy(100L);

        assertEquals(entity.getCreatedBy(), entity.getUpdatedBy());
    }

    // ===== UpdatedBy Field Tests =====

    @Test
    void testUpdatedBy_defaultNull() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getUpdatedBy());
    }

    @Test
    void testUpdatedBy_setAndGet() {
        TestEntity entity = new TestEntity();
        entity.setUpdatedBy(200L);

        assertEquals(200L, entity.getUpdatedBy());
    }

    @Test
    void testUpdatedBy_differentFromCreatedBy() {
        TestEntity entity = new TestEntity();
        entity.setCreatedBy(100L);
        entity.setUpdatedBy(200L);

        assertEquals(100L, entity.getCreatedBy());
        assertEquals(200L, entity.getUpdatedBy());
        assertNotEquals(entity.getCreatedBy(), entity.getUpdatedBy());
    }

    @Test
    void testUpdatedBy_changesOverTime() {
        TestEntity entity = new TestEntity();
        entity.setCreatedBy(100L);
        entity.setUpdatedBy(100L);

        assertEquals(entity.getCreatedBy(), entity.getUpdatedBy());

        // Simulate update by different user
        entity.setUpdatedBy(200L);
        assertEquals(200L, entity.getUpdatedBy());
        assertNotEquals(entity.getCreatedBy(), entity.getUpdatedBy());
    }

    // ===== Full Audit Trail Tests =====

    @Test
    void testAuditTrail_creationWorkflow() {
        TestEntity entity = new TestEntity();
        Long userId = 123L;
        LocalDateTime now = LocalDateTime.now();

        // Simulate entity creation by user 123
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);

        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertEquals(userId, entity.getCreatedBy());
        assertEquals(userId, entity.getUpdatedBy());
        assertEquals(entity.getCreatedBy(), entity.getUpdatedBy());
    }

    @Test
    void testAuditTrail_updateBySameUser() {
        TestEntity entity = new TestEntity();
        Long userId = 123L;
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 15, 30);

        // Initial creation
        entity.setCreatedAt(created);
        entity.setUpdatedAt(created);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);

        // Same user updates the entity
        entity.setUpdatedAt(updated);
        // updatedBy remains the same user

        assertEquals(userId, entity.getCreatedBy());
        assertEquals(userId, entity.getUpdatedBy());
        assertTrue(entity.getUpdatedAt().isAfter(entity.getCreatedAt()));
    }

    @Test
    void testAuditTrail_updateByDifferentUser() {
        TestEntity entity = new TestEntity();
        Long creator = 100L;
        Long editor = 200L;
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 5, 14, 30);

        // User 100 creates the entity
        entity.setCreatedAt(created);
        entity.setUpdatedAt(created);
        entity.setCreatedBy(creator);
        entity.setUpdatedBy(creator);

        // User 200 updates the entity
        entity.setUpdatedAt(updated);
        entity.setUpdatedBy(editor);

        assertEquals(creator, entity.getCreatedBy());
        assertEquals(editor, entity.getUpdatedBy());
        assertNotEquals(entity.getCreatedBy(), entity.getUpdatedBy());
        assertTrue(entity.getUpdatedAt().isAfter(entity.getCreatedAt()));
    }

    @Test
    void testAuditTrail_multipleUpdates() {
        TestEntity entity = new TestEntity();
        LocalDateTime creation = LocalDateTime.of(2024, 1, 1, 10, 0);

        // Created by user 100
        entity.setCreatedAt(creation);
        entity.setUpdatedAt(creation);
        entity.setCreatedBy(100L);
        entity.setUpdatedBy(100L);

        // Updated by user 200
        entity.setUpdatedAt(creation.plusDays(1));
        entity.setUpdatedBy(200L);
        assertEquals(200L, entity.getUpdatedBy());

        // Updated by user 300
        entity.setUpdatedAt(creation.plusDays(2));
        entity.setUpdatedBy(300L);
        assertEquals(300L, entity.getUpdatedBy());

        // CreatedBy should remain unchanged
        assertEquals(100L, entity.getCreatedBy());
    }

    // ===== Builder Pattern Tests =====

    @Test
    void testBuilder_withCreatedBy() {
        TestEntity entity = TestEntity.builder()
                .createdBy(100L)
                .build();

        assertEquals(100L, entity.getCreatedBy());
        assertNull(entity.getUpdatedBy());
    }

    @Test
    void testBuilder_withUpdatedBy() {
        TestEntity entity = TestEntity.builder()
                .updatedBy(200L)
                .build();

        assertNull(entity.getCreatedBy());
        assertEquals(200L, entity.getUpdatedBy());
    }

    @Test
    void testBuilder_withBothAuditFields() {
        TestEntity entity = TestEntity.builder()
                .createdBy(100L)
                .updatedBy(200L)
                .build();

        assertEquals(100L, entity.getCreatedBy());
        assertEquals(200L, entity.getUpdatedBy());
    }

    @Test
    void testBuilder_withAllFields() {
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 15, 30);

        TestEntity entity = TestEntity.builder()
                .id(999L)
                .createdAt(created)
                .updatedAt(updated)
                .createdBy(111L)
                .updatedBy(222L)
                .build();

        assertEquals(999L, entity.getId());
        assertEquals(created, entity.getCreatedAt());
        assertEquals(updated, entity.getUpdatedAt());
        assertEquals(111L, entity.getCreatedBy());
        assertEquals(222L, entity.getUpdatedBy());
    }

    // ===== Complete Audit Scenarios =====

    @Test
    void testScenario_newEntityCreation() {
        Long currentUserId = 456L;
        LocalDateTime now = LocalDateTime.now();

        TestEntity entity = TestEntity.builder()
                .createdAt(now)
                .updatedAt(now)
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        assertEquals(currentUserId, entity.getCreatedBy());
        assertEquals(currentUserId, entity.getUpdatedBy());
        assertEquals(entity.getCreatedAt(), entity.getUpdatedAt());
    }

    @Test
    void testScenario_entityLifecycle() {
        TestEntity entity = new TestEntity();

        // Step 1: Creation by user 1
        Long user1 = 1L;
        LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        entity.setId(100L);
        entity.setCreatedAt(time1);
        entity.setUpdatedAt(time1);
        entity.setCreatedBy(user1);
        entity.setUpdatedBy(user1);

        assertEquals(user1, entity.getCreatedBy());
        assertEquals(user1, entity.getUpdatedBy());

        // Step 2: First update by user 2
        Long user2 = 2L;
        LocalDateTime time2 = time1.plusDays(1);
        entity.setUpdatedAt(time2);
        entity.setUpdatedBy(user2);

        assertEquals(user1, entity.getCreatedBy());
        assertEquals(user2, entity.getUpdatedBy());

        // Step 3: Second update by user 3
        Long user3 = 3L;
        LocalDateTime time3 = time2.plusDays(1);
        entity.setUpdatedAt(time3);
        entity.setUpdatedBy(user3);

        assertEquals(user1, entity.getCreatedBy());
        assertEquals(user3, entity.getUpdatedBy());
        assertTrue(entity.getUpdatedAt().isAfter(entity.getCreatedAt()));
    }

    @Test
    void testScenario_systemVsUserActions() {
        TestEntity entity = new TestEntity();
        Long systemUserId = 0L; // Assuming 0 represents system
        Long regularUserId = 100L;
        LocalDateTime now = LocalDateTime.now();

        // System creates entity
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(systemUserId);
        entity.setUpdatedBy(systemUserId);

        assertEquals(systemUserId, entity.getCreatedBy());
        assertEquals(systemUserId, entity.getUpdatedBy());

        // Regular user updates entity
        entity.setUpdatedAt(now.plusHours(1));
        entity.setUpdatedBy(regularUserId);

        assertEquals(systemUserId, entity.getCreatedBy());
        assertEquals(regularUserId, entity.getUpdatedBy());
    }
}
