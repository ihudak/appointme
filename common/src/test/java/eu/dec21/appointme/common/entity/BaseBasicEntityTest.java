package eu.dec21.appointme.common.entity;

import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BaseBasicEntityTest {

    // Concrete test entity to test abstract BaseBasicEntity
    @Entity
    @SuperBuilder
    @NoArgsConstructor
    private static class TestBasicEntity extends BaseBasicEntity {
        // No additional fields needed for testing
    }

    // ===== Constructor Tests =====

    @Test
    void testNoArgsConstructor() {
        TestBasicEntity entity = new TestBasicEntity();

        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        Long id = 123L;
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 15, 30);

        TestBasicEntity entity = new TestBasicEntity();
        entity.setId(id);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        assertEquals(123L, entity.getId());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }

    @Test
    void testSuperBuilder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusHours(1);

        TestBasicEntity entity = TestBasicEntity.builder()
                .id(456L)
                .createdAt(now)
                .updatedAt(later)
                .build();

        assertEquals(456L, entity.getId());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(later, entity.getUpdatedAt());
    }

    // ===== ID Field Tests =====

    @Test
    void testId_defaultNull() {
        TestBasicEntity entity = new TestBasicEntity();
        assertNull(entity.getId());
    }

    @Test
    void testId_setAndGet() {
        TestBasicEntity entity = new TestBasicEntity();
        entity.setId(999L);

        assertEquals(999L, entity.getId());
    }

    @Test
    void testId_withDifferentValues() {
        TestBasicEntity entity1 = new TestBasicEntity();
        TestBasicEntity entity2 = new TestBasicEntity();

        entity1.setId(1L);
        entity2.setId(2L);

        assertEquals(1L, entity1.getId());
        assertEquals(2L, entity2.getId());
        assertNotEquals(entity1.getId(), entity2.getId());
    }

    // ===== CreatedAt Field Tests =====

    @Test
    void testCreatedAt_defaultNull() {
        TestBasicEntity entity = new TestBasicEntity();
        assertNull(entity.getCreatedAt());
    }

    @Test
    void testCreatedAt_setAndGet() {
        TestBasicEntity entity = new TestBasicEntity();
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);

        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    void testCreatedAt_withPastDate() {
        TestBasicEntity entity = new TestBasicEntity();
        LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        entity.setCreatedAt(pastDate);

        assertEquals(pastDate, entity.getCreatedAt());
    }

    @Test
    void testCreatedAt_withFutureDate() {
        TestBasicEntity entity = new TestBasicEntity();
        LocalDateTime futureDate = LocalDateTime.of(2030, 12, 31, 23, 59);
        entity.setCreatedAt(futureDate);

        assertEquals(futureDate, entity.getCreatedAt());
    }

    // ===== UpdatedAt Field Tests =====

    @Test
    void testUpdatedAt_defaultNull() {
        TestBasicEntity entity = new TestBasicEntity();
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void testUpdatedAt_setAndGet() {
        TestBasicEntity entity = new TestBasicEntity();
        LocalDateTime now = LocalDateTime.now();
        entity.setUpdatedAt(now);

        assertEquals(now, entity.getUpdatedAt());
    }

    @Test
    void testUpdatedAt_afterCreatedAt() {
        TestBasicEntity entity = new TestBasicEntity();
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 15, 30);

        entity.setCreatedAt(created);
        entity.setUpdatedAt(updated);

        assertTrue(entity.getUpdatedAt().isAfter(entity.getCreatedAt()));
    }

    @Test
    void testUpdatedAt_sameAsCreatedAt() {
        TestBasicEntity entity = new TestBasicEntity();
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 10, 0);

        entity.setCreatedAt(timestamp);
        entity.setUpdatedAt(timestamp);

        assertEquals(entity.getCreatedAt(), entity.getUpdatedAt());
    }

    // ===== Audit Fields Workflow Tests =====

    @Test
    void testAuditFields_creationWorkflow() {
        TestBasicEntity entity = new TestBasicEntity();
        LocalDateTime creationTime = LocalDateTime.now();

        // Simulate creation
        entity.setCreatedAt(creationTime);
        entity.setUpdatedAt(creationTime);

        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertEquals(entity.getCreatedAt(), entity.getUpdatedAt());
    }

    @Test
    void testAuditFields_updateWorkflow() {
        TestBasicEntity entity = new TestBasicEntity();
        LocalDateTime creationTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updateTime = LocalDateTime.of(2024, 1, 5, 14, 30);

        // Initial creation
        entity.setCreatedAt(creationTime);
        entity.setUpdatedAt(creationTime);

        // Simulate update
        entity.setUpdatedAt(updateTime);

        assertEquals(creationTime, entity.getCreatedAt());
        assertEquals(updateTime, entity.getUpdatedAt());
        assertTrue(entity.getUpdatedAt().isAfter(entity.getCreatedAt()));
    }

    @Test
    void testAuditFields_multipleUpdates() {
        TestBasicEntity entity = new TestBasicEntity();
        LocalDateTime creation = LocalDateTime.of(2024, 1, 1, 10, 0);

        entity.setCreatedAt(creation);
        entity.setUpdatedAt(creation);

        // First update
        LocalDateTime firstUpdate = creation.plusDays(1);
        entity.setUpdatedAt(firstUpdate);
        assertEquals(firstUpdate, entity.getUpdatedAt());

        // Second update
        LocalDateTime secondUpdate = firstUpdate.plusDays(1);
        entity.setUpdatedAt(secondUpdate);
        assertEquals(secondUpdate, entity.getUpdatedAt());

        // CreatedAt should remain unchanged
        assertEquals(creation, entity.getCreatedAt());
    }

    // ===== Builder Pattern Tests =====

    @Test
    void testBuilder_withId() {
        TestBasicEntity entity = TestBasicEntity.builder()
                .id(100L)
                .build();

        assertEquals(100L, entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void testBuilder_withTimestamps() {
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 0, 0);

        TestBasicEntity entity = TestBasicEntity.builder()
                .createdAt(created)
                .updatedAt(updated)
                .build();

        assertNull(entity.getId());
        assertEquals(created, entity.getCreatedAt());
        assertEquals(updated, entity.getUpdatedAt());
    }

    @Test
    void testBuilder_withAllFields() {
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 15, 30);

        TestBasicEntity entity = TestBasicEntity.builder()
                .id(999L)
                .createdAt(created)
                .updatedAt(updated)
                .build();

        assertEquals(999L, entity.getId());
        assertEquals(created, entity.getCreatedAt());
        assertEquals(updated, entity.getUpdatedAt());
    }

    // ===== Timestamp Comparison Tests =====

    @Test
    void testTimestamps_sequentialOrder() {
        TestBasicEntity entity = new TestBasicEntity();
        
        LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime time2 = time1.plusMinutes(30);
        LocalDateTime time3 = time2.plusHours(2);

        entity.setCreatedAt(time1);
        entity.setUpdatedAt(time2);

        assertTrue(entity.getUpdatedAt().isAfter(entity.getCreatedAt()));

        entity.setUpdatedAt(time3);
        assertTrue(entity.getUpdatedAt().isAfter(entity.getCreatedAt()));
    }

    @Test
    void testTimestamps_withPrecision() {
        TestBasicEntity entity = new TestBasicEntity();
        
        LocalDateTime precise = LocalDateTime.of(2024, 1, 1, 10, 30, 45, 123456789);
        entity.setCreatedAt(precise);

        assertEquals(2024, entity.getCreatedAt().getYear());
        assertEquals(1, entity.getCreatedAt().getMonthValue());
        assertEquals(1, entity.getCreatedAt().getDayOfMonth());
        assertEquals(10, entity.getCreatedAt().getHour());
        assertEquals(30, entity.getCreatedAt().getMinute());
        assertEquals(45, entity.getCreatedAt().getSecond());
        assertEquals(123456789, entity.getCreatedAt().getNano());
    }

    // ===== Entity Identity Tests =====

    @Test
    void testEntity_withSameId() {
        TestBasicEntity entity1 = TestBasicEntity.builder().id(1L).build();
        TestBasicEntity entity2 = TestBasicEntity.builder().id(1L).build();

        assertEquals(entity1.getId(), entity2.getId());
    }

    @Test
    void testEntity_withDifferentIds() {
        TestBasicEntity entity1 = TestBasicEntity.builder().id(1L).build();
        TestBasicEntity entity2 = TestBasicEntity.builder().id(2L).build();

        assertNotEquals(entity1.getId(), entity2.getId());
    }

    @Test
    void testEntity_nullIdComparison() {
        TestBasicEntity entity1 = new TestBasicEntity();
        TestBasicEntity entity2 = new TestBasicEntity();

        assertNull(entity1.getId());
        assertNull(entity2.getId());
    }
}
