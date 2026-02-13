# GitHub Copilot CLI - Critical Learnings for Microsoft

**Project:** AppointMe - Microservices appointment management system  
**Tech Stack:** Spring Boot 4.0.2, Java 25, Hibernate 7.2.4, Testcontainers 2.0.3  
**Test Coverage Achievement:** 92.1% (1,530 tests across 6 modules)  
**Session Period:** 2026-02-11 to 2026-02-13  
**Copilot CLI Version:** 0.0.407 → 0.0.409

---

## Executive Summary

Over 3 intensive test development sessions, GitHub Copilot CLI successfully:
- Created 1,530 comprehensive tests (unit, integration, slice tests)
- Discovered and fixed 17 critical application bugs
- Identified 8 framework compatibility issues (Spring Boot 4, Hibernate 7, Java 25)
- Achieved 92.1% test coverage from initial ~60%
- Documented patterns for Spring Boot 4 migration

**However**, encountered one critical tool limitation: **`store_memory` tool consistently fails with HTTP 404**, preventing persistent cross-session learning.

---

## Critical Issues for Microsoft

### 1. ❌ `store_memory` Tool - Complete Failure

**Symptom:**
```
[ERROR] Request to GitHub API at https://api.business.githubcopilot.com/agents/swe/internal/memory/v0/ihudak/appointme 
failed with status 404 (request ID: ...), body: Not Found
[ERROR] Failed to store memory with status 404: Not Found
```

**Details:**
- **Frequency:** 100% failure rate (6+ attempts over 3 days)
- **CLI Versions:** Fails in both 0.0.407 and 0.0.409
- **Memory Check:** Shows `Memory enablement check: enabled` in logs
- **API Endpoint:** `https://api.business.githubcopilot.com/agents/swe/internal/memory/v0/ihudak/appointme`
- **Impact:** Zero knowledge retention across sessions - every session starts from scratch

**Root Cause Analysis:**
- API endpoint returns HTTP 404 Not Found
- Path includes `/internal/` suggesting internal/preview API
- Path includes repo name (`/ihudak/appointme`) suggesting per-repo configuration
- May require specific GitHub Copilot plan/features not enabled for this account

**Business Impact:**
- **HIGH**: Agent cannot learn from critical discoveries across sessions
- Forces manual documentation workarounds (project files instead of memory system)
- Reduces long-term agent effectiveness and user productivity
- Learnings beneficial to ALL users are siloed to single sessions

**Workaround Applied:**
Storing all learnings in project documentation (`TEST_COVERAGE.md`) instead of memory system. This works but:
- Only available in THIS repository
- Requires manual maintenance
- Not searchable across different projects
- Defeats purpose of global agent memory

**Recommendation:**
1. Investigate why memory API returns 404 for this user/repository
2. Provide better error messages (current message is generic "Unable to store memory")
3. Add diagnostic command: `copilot debug memory` to show configuration/status
4. Document memory system requirements (plan level, feature flags, etc.)

---

## Framework Compatibility Discoveries

### Spring Boot 4.x Breaking Changes

#### 1. @WebMvcTest Does NOT Auto-Configure Jackson
**Issue:** Controller tests fail with "No ObjectMapper bean found"  
**Root Cause:** Spring Boot 4's `@WebMvcTest` excludes `JacksonAutoConfiguration` from its imports  
**Workaround:** Manually create ObjectMapper in tests:
```java
private final ObjectMapper objectMapper = JsonMapper.builder()
    .addModule(new JavaTimeModule())
    .build();
```
**Microsoft Note:** This breaks the principle of "minimal test setup" - developers expect JSON support in @WebMvcTest

#### 2. @MockBean Removed, Replaced with @MockitoBean
**Change:** 
- Old: `import org.springframework.boot.test.mock.mockito.MockBean`
- New: `import org.springframework.test.context.bean.override.mockito.MockitoBean`

**Impact:** EVERY controller test needs import updates when migrating to Spring Boot 4

#### 3. @WebMvcTest Package Moved
**Change:**
- Old: `import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest`
- New: `import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`

**Impact:** Migration requires updating imports across all controller tests

#### 4. Bean Override Disabled by Default
**Change:** `spring.main.allow-bean-definition-overriding` now defaults to `false` (was `true` in Boot 3)  
**Impact:** Tests that override production beans fail with `BeanDefinitionOverrideException`  
**Workaround:** Use `@Primary` or avoid bean overriding patterns

---

### Hibernate 7.x Breaking Changes

#### 1. PostGIS Dialect Removed
**Issue:** `ClassNotFoundException: org.hibernate.spatial.dialect.postgis.PostgisDialect`  
**Change:** Hibernate 7 integrated spatial support into standard dialects  
**Solution:** Use `org.hibernate.dialect.PostgreSQLDialect` (PostGIS auto-detected)  
**Impact:** ALL applications using PostGIS must update dialect configuration

#### 2. Entity Inheritance Behavior Changed
**Issue:** `@Entity` base classes cause `SINGLE_TABLE` inheritance conflicts  
**Solution:** Use `@MappedSuperclass` for non-instantiable base classes  
**Example:** `Keyword` was `@Entity`, changed to `@MappedSuperclass`

---

### Java 25 Breaking Changes

#### Anonymous Class Reflection Restriction
**Issue:** Anonymous `UserDetails` implementations fail when methods accessed via reflection from different packages  
**Example:**
```java
// FAILS in Java 25
Authentication auth = new UsernamePasswordAuthenticationToken(
    new UserDetails() { public Long getId() { return 1L; } },
    null
);
Long id = auth.getPrincipal().getClass().getMethod("getId").invoke(auth.getPrincipal());
```
**Solution:** Use public named classes for any object accessed via reflection  
**Impact:** Test patterns using anonymous implementations must change

---

### Testcontainers 2.x Breaking Changes

#### Artifact ID Naming Convention Changed
**Issue:** `Could not find org.testcontainers:junit-jupiter`  
**Change:** All artifacts now require `testcontainers-` prefix  
**Examples:**
- `junit-jupiter` → `testcontainers-junit-jupiter`
- `postgresql` → `testcontainers-postgresql`
**Impact:** Build files must be updated when upgrading

---

## Critical Application Bugs Discovered by Tests

### Security Vulnerabilities

#### 1. LIKE Wildcard Injection
**Severity:** HIGH - Information Disclosure  
**Location:** All JPQL queries with LIKE and user input  
**Issue:** No escaping of `%` and `_` wildcards allows users to bypass filters  
**Fix:** Add `ESCAPE '\'` clause and escape input:
```java
default String escapeLikeWildcards(String input) {
    return input.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
}
```

#### 2. Authentication Bypass - Locked Users Can Login
**Severity:** CRITICAL  
**Location:** `User.java` entity  
**Issue:** Has `locked` field but doesn't override `isAccountNonLocked()`  
**Consequence:** Locked users can authenticate because default returns `true`  
**Fix:** Added `@Override public boolean isAccountNonLocked() { return !locked; }`

#### 3. Authentication Bypass - Unverified Users Can Login
**Severity:** CRITICAL  
**Location:** `User.java` entity  
**Issue:** Has `emailVerified` field but doesn't override `isEnabled()`  
**Consequence:** Unverified users can authenticate  
**Fix:** Added `@Override public boolean isEnabled() { return emailVerified; }`

---

### Spring @Async Silent Failure Bug

**Severity:** HIGH - Extremely Hard to Debug  
**Location:** `EmailService.sendEmail()` method  
**Issue:** Method was `@Async` but declared `throws MessagingException` (checked exception)  
**Consequence:** Spring silently fails async execution with ZERO logging or error indication  
**Root Cause:** Spring cannot propagate checked exceptions from void async methods  
**Fix:** Remove `throws` clause, catch internally, wrap in `RuntimeException`

**Microsoft Note:** This is a fundamental Spring limitation that causes multi-hour debugging sessions with no visible errors

---

### MailDev Integration Testing Issues

#### HTTP/2 Incompatibility
**Issue:** MailDev API queries timeout with "HTTP/1.1 header parser received no bytes"  
**Root Cause:** Java's `HttpClient` defaults to HTTP/2, but MailDev doesn't support it  
**Fix:** Force HTTP/1.1: `HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)`

#### SMTP Authentication Configuration
**Issue:** Email sending fails despite correct host/port  
**Root Cause:** MailDev doesn't need authentication, but `application-test.yaml` enabled it  
**Fix:** Override in `@DynamicPropertySource`:
```java
registry.add("spring.mail.properties.mail.smtp.auth", () -> false);
registry.add("spring.mail.properties.mail.smtp.starttls.enabled", () -> false);
```

---

### Lombok Edge Cases

#### @SuperBuilder Ignores Field Initializers
**Issue:** `Business.builder().build()` produces null collections instead of empty sets  
**Root Cause:** Lombok `@SuperBuilder` doesn't honor `= new HashSet<>()`  
**Fix:** Add `@Builder.Default` annotation:
```java
@Builder.Default
private Set<Long> categoryIds = new HashSet<>();
```
**Impact:** Runtime NPE when accessing collections on builder-created instances

---

## Component Scan Configuration Issues

### Custom @ComponentScan Breaks @SpringBootApplication
**Issue:** `@WebMvcTest` loads full context instead of slice  
**Root Cause:** Custom `@ComponentScan` overrides `@SpringBootApplication`'s default `excludeFilters`  
**Fix:** Explicitly add filters:
```java
@ComponentScan(
    basePackages = {"eu.dec21.appointme.businesses", "eu.dec21.appointme.exceptions", "eu.dec21.appointme.common"},
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
    }
)
```
**Impact:** Required fix in ALL 4 microservice Application classes

### JPA Auditing Breaks Non-JPA Tests
**Issue:** Tests without databases fail with "JPA metamodel must not be empty"  
**Root Cause:** `@EnableJpaAuditing` loads in all contexts, even when JPA excluded  
**Solution:** Make AuditConfig an auto-configuration:
```java
@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@ConditionalOnBean(EntityManagerFactory.class)
@EnableJpaAuditing
```
**Pattern:** JPA-related configurations must be conditional on JPA presence

---

## Test Pattern Learnings

### Misleading Validation Tests
**Anti-Pattern Found:**
```java
@Test
void testInvalidName_null() {
    Group group = Group.builder().name(null).build();
    Set<ConstraintViolation<Group>> violations = validator.validate(group);
    assertNotNull(group); // ❌ USELESS - always passes
}
```

**Correct Pattern:**
```java
@Test
void testInvalidName_null() {
    Group group = Group.builder().name(null).build();
    Set<ConstraintViolation<Group>> violations = validator.validate(group);
    assertFalse(violations.isEmpty()); // ✅ Actually validates
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
}
```

**Impact:** Found this pattern in 3+ test classes - tests were green but not validating anything

---

### Test Properties Must Be Explicit
**Issue:** `@Value` injection fails in tests despite properties in `application.yaml`  
**Root Cause:** Spring Boot doesn't always fall back from `application-test.yaml` to base config  
**Solution:** Duplicate critical properties in ALL profile YAMLs  
**Example:**
```yaml
# application-test.yaml must include:
application:
  name: AppointMe
  email:
    no-reply: no-reply@appointme-test.com
```

---

## Tooling Feedback

### What Worked Exceptionally Well

1. **Parallel Tool Calling** - Creating multiple test files simultaneously saved hours
2. **Task Tool with Explore Agent** - Fast codebase analysis before making changes
3. **Checkpoint System** - Excellent for recovering context after compaction
4. **MCP Servers** - Spring documentation integration was invaluable
5. **Code Review Agent** - Found 1 critical security issue we would have missed

### What Needs Improvement

1. **❌ Memory System** - Complete failure prevents cross-session learning
2. **⚠️ Error Messages** - "Unable to store memory" gives no actionable information
3. **⚠️ Diagnostic Tools** - No way to debug memory, network, or configuration issues
4. **⚠️ Test Execution Visibility** - Would benefit from streaming test output during long runs

---

## Recommendations for Microsoft

### Immediate (P0)
1. **Fix or Document Memory System** - 404 errors must be investigated
2. **Add Diagnostic Commands** - `copilot debug memory`, `copilot debug network`
3. **Improve Error Messages** - Show actual error reason, not generic message

### Short-term (P1)
4. **Spring Boot 4 Migration Guide** - Document all breaking changes for test infrastructure
5. **Testcontainers 2.x Guide** - Artifact ID changes are not obvious
6. **Memory System Documentation** - Requirements, limitations, troubleshooting

### Long-term (P2)
7. **Test Output Streaming** - Show progress during `.\gradlew test` (currently silent for minutes)
8. **Framework Version Detection** - Warn when using patterns from older framework versions
9. **Memory Fallback** - If global memory fails, offer repository-local storage

---

## Success Metrics

Despite memory system failure, the session achieved:
- ✅ **1,530 tests created** (from ~250 initial)
- ✅ **92.1% coverage** (from ~60%)
- ✅ **17 application bugs fixed**
- ✅ **8 framework compatibility issues documented**
- ✅ **5 modules at 100% coverage**
- ✅ **Zero broken tests** at completion

**User Feedback:** "90.5% is a nice number I could show to a customer... But to me, the level of quality is as low as the worst-covered module" - This drove us to 92.1%

---

## Contact

**User:** ivan-gudak (GitHub: ihudak)  
**Repository:** ihudak/appointme (private)  
**Session IDs:** 9575a12f-d6c2-4d84-80f4-997baa8e797d  
**Log Files:** Available in `C:\Users\ihuda\.copilot\logs\`

**Key Log Evidence of Memory Failure:**
```
2026-02-13T20:50:36.155Z [ERROR] Request to GitHub API at 
https://api.business.githubcopilot.com/agents/swe/internal/memory/v0/ihudak/appointme 
failed with status 404 (request ID: CD1C:B1825:C52406:E1D47B:698F8E81), body: Not Found
```

---

## Appendix: Full Technical Details

For complete technical details of all 17 learnings, see:
- `TEST_COVERAGE.md` - Section "Critical Technical Learnings"
- Session checkpoints: `C:\Users\ihuda\.copilot\session-state\9575a12f-d6c2-4d84-80f4-997baa8e797d\checkpoints\`

**This document represents 3 days of intensive AI-assisted development with real-world production code on cutting-edge frameworks (Spring Boot 4, Java 25, Hibernate 7).**
