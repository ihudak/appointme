# Copilot Instructions for AppointMe

## Project Overview

AppointMe is a **microservices-based appointment management system** built with Spring Boot 4.0.2 and Java 25. The project uses a **multi-module Gradle build** with separate deployable services that are designed to run independently in Kubernetes.

### Architecture

This is a **microservices architecture** where each module is a standalone Spring Boot application with its own database and API:

- **businesses** - Manages business entities, locations (PostGIS), ratings, and business-category relationships
- **categories** - Manages hierarchical category tree (parent-child relationships)
- **users** - Handles authentication (JWT), authorization (roles), user management, and groups
- **feedback** - Manages user feedback and reviews
- **common** - Shared utilities, base entities, response models, security utils
- **exceptions** - Shared exception handling across all modules

### Key Architectural Patterns

1. **Shared Modules Pattern**: `common` and `exceptions` are dependency modules (not runnable services) that are imported by all Spring Boot applications via `implementation project(':common')` and `implementation project(':exceptions')`.

2. **Module Scanning**: Each Spring Boot application uses `@ComponentScan` to include its own package, plus `eu.dec21.appointme.common` and `eu.dec21.appointme.exceptions`:
   ```java
   @ComponentScan(basePackages = {
       "eu.dec21.appointme.businesses",  // or categories, users, etc.
       "eu.dec21.appointme.exceptions",
       "eu.dec21.appointme.common"
   })
   ```

3. **Entity ID References**: Microservices do NOT use JPA relationships across service boundaries. Instead:
   - Business entity stores `categoryIds` as `Set<Long>` (not foreign key to Category entity)
   - Business entity stores `ownerId` as `Long` (not foreign key to User entity)
   - This maintains service independence and allows separate databases

4. **Base Entity Hierarchy**: All entities extend from base classes in `common`:
   - `BaseBasicEntity` - Provides `id`, `createdAt`, `updatedAt`
   - `BaseEntity` - Extends `BaseBasicEntity`, adds `createdBy`, `updatedBy` (uses Spring Data JPA Auditing)

5. **Standard Response Format**: All paginated responses use `PageResponse<T>` from `common.response`:
   ```java
   public ResponseEntity<PageResponse<BusinessResponse>> findAll(...) {
       return new PageResponse<>(content, totalElements, totalPages, pageNumber, pageSize, last, empty);
   }
   ```

## Build, Test, and Run Commands

### Building the Project

```bash
# Build all modules
.\gradlew build

# Build specific module
.\gradlew :businesses:build
.\gradlew :categories:build

# Clean and build
.\gradlew clean build
```

### Running Tests

```bash
# Run all tests (automatically starts Docker Compose services)
.\gradlew test

# Run tests for specific module
.\gradlew :businesses:test
.\gradlew :categories:test

# Run tests without Docker
.\gradlew test -PwithDocker=false
```

**Important**: Tests automatically run `composeUp` before execution and `composeDownNoFail` after completion. The `withDocker` flag is `true` by default.

### Running Applications

```bash
# Run specific service (automatically starts Docker dependencies)
.\gradlew :businesses:bootRun
.\gradlew :categories:bootRun
.\gradlew :users:bootRun

# Run without Docker Compose
.\gradlew :users:bootRun -PwithDocker=false
```

### Docker Compose Management

```bash
# Start infrastructure services (Postgres, Keycloak, MailDev, Grafana, Mongo)
.\gradlew composeUp

# Stop infrastructure services
.\gradlew composeDownNoFail
```

**Services available in compose.yaml:**
- PostgreSQL (port 5432) - Main database for all services
- Keycloak (port 8080) - OAuth2/OIDC authentication
- MailDev (ports 1080, 1025) - Email testing
- Grafana LGTM (ports 3000, 4317, 4318) - Observability stack
- MongoDB (port 27017) - For future use

## Code Conventions

### Entity Design

1. **Entities extend base classes**: Use `BaseEntity` (for entities needing audit fields) or `BaseBasicEntity` (for simple entities)

2. **Entity annotations**: Always use this pattern:
   ```java
   @Getter
   @Setter
   @SuperBuilder
   @AllArgsConstructor
   @NoArgsConstructor
   @Entity
   @Table(name = "table_name", indexes = {...})
   public class MyEntity extends BaseEntity {
   ```

3. **Cross-service references**: NEVER use `@ManyToOne` or `@OneToMany` across service boundaries. Use ID collections:
   ```java
   @ElementCollection
   @CollectionTable(name = "business_category_ids", ...)
   @Column(name = "category_id")
   private Set<Long> categoryIds = new HashSet<>();
   ```

4. **Embedded objects**: Use `@Embedded` for value objects like `Address`:
   ```java
   @Embedded
   private Address address;
   ```

5. **PostGIS support**: Business module uses PostGIS for geospatial data:
   ```java
   @Column(columnDefinition = "geography(Point,4326)")
   private Point location;
   ```

### Repository Patterns

1. **Spring Data JPA**: Use interface-based repositories extending `JpaRepository`

2. **Custom queries**: Use `@Query` with JPQL for complex queries:
   ```java
   @Query("SELECT b FROM Business b JOIN b.categoryIds c WHERE c = :categoryId")
   Page<Business> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
   ```

3. **Pagination**: Always accept `Pageable` and return `Page<T>` for list queries

### Service Layer

1. **Service injection**: Use constructor injection with `@RequiredArgsConstructor` (Lombok):
   ```java
   @Service
   @RequiredArgsConstructor
   public class BusinessService {
       private final BusinessRepository businessRepository;
       private final BusinessMapper businessMapper;
   }
   ```

2. **Security context access**: Use `SecurityUtils` from `common.util`:
   ```java
   Long userId = SecurityUtils.getUserIdFromAuthenticationOrThrow(connectedUser);
   Long currentUserId = SecurityUtils.getCurrentUserIdOrThrow();
   ```

3. **Entity not found**: Throw `EntityNotFoundException` with descriptive message:
   ```java
   .orElseThrow(() -> new EntityNotFoundException("Business not found with id " + id))
   ```

### Controller Patterns

1. **Controller structure**:
   ```java
   @RestController
   @RequestMapping("businesses")
   @RequiredArgsConstructor
   @Tag(name = "Businesses", description = "Businesses API")
   public class BusinessController {
   ```

2. **OpenAPI documentation**: Always add `@Operation` with `summary` and `description`:
   ```java
   @GetMapping("{id}")
   @Operation(summary = "Get a business by ID", description = "Retrieves a business by its ID")
   public ResponseEntity<BusinessResponse> getBusinessById(@PathVariable Long id) {
   ```

3. **Pagination parameters**: Use consistent defaults:
   ```java
   @RequestParam(name = "page", defaultValue = "0", required = false) int page,
   @RequestParam(name = "size", defaultValue = "10", required = false) int size
   ```

4. **Response wrapping**: Always wrap responses in `ResponseEntity`:
   ```java
   return ResponseEntity.ok(service.findById(id));
   ```

### Configuration

1. **Configuration properties**: Use `@ConfigurationProperties` pattern:
   ```java
   @Getter
   @Component
   @ConfigurationProperties(prefix = "application.rating")
   public class RatingConfig {
       private int confidenceThreshold = 10;
       private double globalMean = 3.5;
   }
   ```

2. **Enable features**: Application classes enable JPA Auditing and Async:
   ```java
   @SpringBootApplication
   @EnableJpaAuditing(auditorAwareRef = "auditorProvider")
   @EnableAsync
   ```

### Multilingual Support

Entities that need translations use embedded keyword entities:
- `BusinessKeyword` extends `Keyword` (from common)
- `CategoryKeyword` extends `Keyword` (from common)
- Keywords have `locale` field for i18n

Pattern:
```java
@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<CategoryKeyword> keywords = new LinkedHashSet<>();
```

### Security & Authentication

- **JWT-based authentication** implemented in `users` module
- **JwtService** handles token generation/validation
- **JwtFilter** intercepts requests to validate tokens
- **SecurityUtils** provides helper methods to extract user ID from security context
- Each service configures its own `SecurityConfig`

### Rating System (Business Module)

Business ratings use **Bayesian averaging**:
```java
// Formula: (C × m + n × r) / (C + n)
// C = confidence threshold, m = global mean, n = review count, r = average rating
```
Configured via `application.rating.confidenceThreshold` and `application.rating.globalMean`.

## Inter-Service Communication

When services need to communicate (e.g., Business calling Categories for subcategory hierarchy):

1. Use **OpenFeign** for REST client communication
2. Add dependency: `implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'`
3. Enable with `@EnableFeignClients` on main application class
4. Create Feign client interface with service URL from config
5. Services discover each other via Kubernetes service names in production

## Module-Specific Notes

### Categories Module
- Hierarchical structure: Category has `parent` (self-referential `@ManyToOne`)
- Root categories have `parent = null`
- `CategoryRepository` has methods: `findByParentId()`, `findByParentIsNull()`

### Business Module
- Dependencies: `hibernate-spatial`, `jts-core` for PostGIS support
- Stores multiple category IDs (not foreign keys)
- Uses weighted rating algorithm for business reputation

### Users Module
- Contains authentication logic (JWT)
- Manages roles and groups
- `CommandLineRunner` initializes default roles on startup
- Security configuration is centralized here

### Common Module
- Not a runnable application (no `@SpringBootApplication`)
- Provides: `BaseEntity`, `PageResponse`, `SecurityUtils`, `AuditConfig`
- Must be scanned by all Spring Boot applications

### Exceptions Module
- Centralized exception handling
- Not a runnable application
- Must be scanned by all Spring Boot applications

## Development Workflow

1. **Start infrastructure**: `.\gradlew composeUp`
2. **Run desired service**: `.\gradlew :users:bootRun` (or businesses, categories)
3. **Access Swagger UI**: Each service exposes OpenAPI docs at `/swagger-ui.html`
4. **Test with MailDev**: Email testing UI at http://localhost:1080
5. **Keycloak Admin**: http://localhost:8080 (see `.env` for credentials)

## API Endpoint Patterns

### REST API Structure

All REST endpoints follow consistent patterns:

1. **Base path**: Resource name in plural (e.g., `/businesses`, `/categories`, `/users`)

2. **Standard CRUD operations**:
   ```
   POST   /businesses              - Create new business
   GET    /businesses/{id}         - Get by ID
   GET    /businesses              - Get all (paginated)
   PUT    /businesses/{id}         - Update (if implemented)
   DELETE /businesses/{id}         - Delete (if implemented)
   ```

3. **Nested/related resources**:
   ```
   GET /categories/{parentId}/children           - Get subcategories
   GET /businesses/category/{categoryId}         - Get businesses by category
   ```

4. **Query parameters for pagination**:
   - `page` (default: 0)
   - `size` (default: 10)

5. **HTTP status codes**:
   - `200 OK` - Successful GET/PUT/PATCH
   - `201 Created` - Successful POST (though current implementation returns 200)
   - `400 Bad Request` - Validation errors
   - `401 Unauthorized` - Authentication required
   - `403 Forbidden` - Insufficient permissions
   - `404 Not Found` - Resource doesn't exist
   - `500 Internal Server Error` - Server errors

### Swagger/OpenAPI

- Each service exposes Swagger UI at `/swagger-ui.html`
- Configured via `springdoc-openapi-starter-webmvc-ui` dependency
- Always add `@Operation` annotations to controller methods

## Error Handling Conventions

### Global Exception Handler

All services use `GlobalExceptionHandler` from the `exceptions` module with `@RestControllerAdvice`.

### Exception Response Format

All errors return `ExceptionResponse`:
```json
{
  "businessErrorCode": 4006,
  "businessErrorDescription": "The requested resource was not found",
  "error": "Business not found with id 123",
  "validationErrors": ["Name cannot be blank", "Email is invalid"]
}
```

### Business Error Codes

Use `BusinessErrorCodes` enum for consistent error codes:
- **1xxx**: Authentication/Authorization errors (1001-1006)
- **2xxx**: Payment/Quota errors (2001-2004)
- **3xxx**: Dependency errors (3001)
- **4xxx**: Client errors (4001-4007)
- **5xxx**: Server errors (5000-5003)

**Common codes:**
- `4006` - `RESOURCE_NOT_FOUND` (404)
- `4001` - `BAD_REQUEST_PARAMETERS` (400)
- `1005` - `INVALID_CREDENTIALS` (401)
- `4002` - `OPERATION_NOT_PERMITTED` (403)

### Exception Throwing Pattern

1. **Entity not found**: Use `ResourceNotFoundException` from exceptions module:
   ```java
   categoryRepository.findById(id)
       .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
   ```

2. **Authentication errors**: Use `UserAuthenticationException` from exceptions module

3. **Authorization errors**: Use `OperationNotPermittedException` from exceptions module

4. **Validation errors**: Use Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Email`, etc.) - automatically caught by `GlobalExceptionHandler`

**Important**: Always use custom exceptions from the `exceptions` module (not JPA's `EntityNotFoundException`) for consistency and proper error handling across all services.

### Custom Exception Guidelines

- Define new exceptions in `exceptions` module for reusability
- Add corresponding `@ExceptionHandler` method in `GlobalExceptionHandler`
- Map to appropriate `BusinessErrorCodes` value

## DTO Patterns

### Request DTOs

**Pattern**: Use Java `record` types for immutable request objects with validation:

```java
public record BusinessRequest(
    Long id,  // Optional for updates
    @NotNull(message = "Name cannot be null")
    @NotBlank
    String name,
    String description,
    // ... other fields
) {}
```

**Conventions**:
- Use `record` (immutable, auto-generates getters, equals, hashCode, toString)
- Add Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Email`, `@Pattern`, etc.)
- Include custom validation messages
- ID field is optional (used for updates if supported)

### Response DTOs

**Pattern**: Use Lombok `@Builder` pattern for flexible response construction:

```java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessResponse {
    private Long id;
    private String name;
    private Double rating;
    // ... other fields
}
```

**Conventions**:
- Use `@Builder` for flexible object construction
- Include all Lombok annotations: `@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor`
- Response objects can include computed fields not in entity (e.g., `imageUrl` extracted from list)
- **Never expose sensitive fields** (passwords, internal IDs across services unless needed)

## Mapper Patterns

### Mapper Service

**Pattern**: Create mapper as `@Service` with dependency injection if needed:

```java
@Service
@RequiredArgsConstructor  // If dependencies needed
public class BusinessMapper {
    // private final SomeDependency dependency;  // If needed
    
    public Business toBusiness(BusinessRequest request) {
        return Business.builder()
            .id(request.id())
            .name(request.name())
            // ... map fields
            .active(true)  // Set defaults
            .build();
    }
    
    public BusinessResponse toBusinessResponse(Business entity) {
        return BusinessResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            // ... map fields, can include computed values
            .build();
    }
}
```

**Conventions**:
- Mapper is a Spring `@Service` bean
- Method naming: `toEntity(Request)` and `toEntityResponse(Entity)`
- Can inject repositories if needed (e.g., CategoryMapper injects CategoryRepository to resolve parent)
- Set sensible defaults in request→entity mapping (e.g., `active = true`)
- Response mapping can include computed fields (e.g., extracting icon image from list)

**Do NOT use**:
- MapStruct or other annotation processors (manual mapping is preferred)
- Static utility classes (use Spring-managed services for testability)

## Validation Patterns

### Bean Validation

Use Jakarta Bean Validation annotations on request DTOs:

```java
@NotNull(message = "Name cannot be null")
@NotBlank(message = "Name cannot be blank")
String name;

@Email(message = "Invalid email address")
String email;

@Pattern(regexp = "^(\\+[1-9]\\d{1,14})?$", message = "Invalid phone number (expected E.164)")
String phoneNumber;

@URL(protocol = "https", message = "Invalid website URL")
String website;
```

**Common annotations**:
- `@NotNull` - Field must not be null
- `@NotBlank` - String must not be null, empty, or whitespace
- `@Email` - Valid email format
- `@Pattern` - Regex validation
- `@URL` - Valid URL format
- `@Min`, `@Max` - Numeric range validation

### Controller Validation

Add `@Valid` to request parameters:
```java
public ResponseEntity<BusinessResponse> create(@Valid @RequestBody BusinessRequest request) {
```

Validation errors are automatically caught by `GlobalExceptionHandler.handleMethodArgumentNotValidException()`.

## Kubernetes Deployment

Services are designed for Kubernetes deployment:
- Each module builds to a separate Docker image via `bootBuildImage`
- Services communicate via Kubernetes service discovery
- Each service connects to shared PostgreSQL instance (separate schemas recommended)
- Configuration externalized via ConfigMaps/Secrets

## Agent Workflow Instructions

### 1. Skills Review After Significant Updates

**When to review skills:**
After any significant update including:
- New feature or module added
- New technology or dependency introduced
- Architecture changes
- Major refactoring

**Review process:**
1. Review currently installed skills in `.agents/skills/`
2. Use the `find-skills` skill to search for relevant new skills based on the updates
3. **Present options to the user and ask for approval** before installing any new skills
4. Do NOT auto-install skills without user approval

**Example:**
```bash
# Search for relevant skills after adding Kafka support
skills find "kafka spring boot"
# Present results to user and ask which ones to install
```

### 2. Test Execution and Quality Assurance

**When to run tests:**
- After any significant update (as defined above)
- When explicitly requested by the user

**Test execution:**
```bash
# Run all tests for all modules, including integration tests
.\gradlew test

# This automatically:
# - Starts Docker Compose services (composeUp)
# - Runs unit and integration tests for all modules
# - Stops Docker Compose services (composeDownNoFail)
```

**Test failure handling:**
- **Auto-fix** obvious issues without asking:
  - Compilation errors
  - Missing imports
  - Syntax errors
  - Obvious typos in test code
  
- **Ask the user** when logic might be wrong:
  - Test assertions failing due to behavior changes
  - Unclear whether the issue is in production code or test code
  - Business logic validation failures
  - Any ambiguous failure where multiple fixes are possible

**Goal:** Tests must always be green (passing) after updates.

**Important:** If you're not sure whether a found issue is in the production code or in the test code, always ask the user before making changes.

### 3. Complete Test Coverage Policy

**Coverage requirement:** ALL code must be covered by tests - no exceptions.

**If tests fail or are difficult to implement:**
- **NEVER skip or exclude code from testing** (like attempting to exclude @Embeddable Address)
- **ALWAYS find a way to make tests work:**
  - Investigate root cause of test failures
  - Try different approaches (remove problematic plugins, adjust configuration)
  - Research solutions and alternatives
  - Ask the user for guidance if stuck
  - Document any trade-offs made to achieve working tests

**Examples of solutions:**
- If bytecode enhancement breaks @Embeddable tests → Remove the plugin
- If cross-module dependencies cause issues → Adjust module structure or mocking
- If external services are needed → Use testcontainers or embedded alternatives
- If validation frameworks conflict → Configure or replace them

**Unit tests coverage:**
- All entity fields and their validation rules
- All constructors, builders, setters, and getters
- All relationships (@ManyToOne, @OneToMany, @Embedded)
- All business logic methods
- Edge cases and boundary conditions

**Integration tests coverage:**
- Database interactions (save, update, delete, queries)
- Cross-module communication (Feign clients, REST calls)
- Transaction behavior
- Constraint enforcement (foreign keys, unique constraints)
- Security rules (authentication, authorization)

**Rationale:** Code without tests is untrusted code. If production code works, tests must work too. Technical obstacles are solvable - incomplete coverage is not acceptable.

## Critical Technical Discoveries (Spring Boot 4 + Hibernate 7 + Java 25)

These were hard-won discoveries during test implementation. They are essential knowledge for working on this project.

### Hibernate 7 Migration

1. **Dialect**: `org.hibernate.spatial.dialect.postgis.PostgisDialect` no longer exists. Use `org.hibernate.dialect.PostgreSQLDialect` — it has built-in spatial support in Hibernate 7.

2. **`@MappedSuperclass` for shared base classes**: Hibernate 7 enforces that a subclass in a `SINGLE_TABLE` hierarchy cannot have its own `@Table` annotation. If a base class (like `Keyword`) is only used for shared fields and not queried directly, use `@MappedSuperclass` instead of `@Entity`.

### Spring Boot 4 Package & API Changes

3. **`@WebMvcTest`** moved to `org.springframework.boot.webmvc.test.autoconfigure`
4. **`@AutoConfigureMockMvc`** moved to `org.springframework.boot.webmvc.test.autoconfigure`
5. **`@MockBean` is removed** — use `@MockitoBean` from `org.springframework.test.context.bean.override.mockito`
6. **`DataSourceAutoConfiguration`** moved to `org.springframework.boot.jdbc.autoconfigure`
7. **`HibernateJpaAutoConfiguration`** moved to `org.springframework.boot.hibernate.autoconfigure`
8. **Bean definition override** is not allowed by default in Spring Boot 4 (`BeanDefinitionOverrideException`)

### `@WebMvcTest` Does NOT Auto-Configure Jackson in Spring Boot 4

9. `@WebMvcTest` no longer includes `JacksonAutoConfiguration`. Neither `@AutoConfigureJson` nor `@ImportAutoConfiguration(JacksonAutoConfiguration.class)` reliably load it. Spring Boot 4's `JacksonAutoConfiguration` creates a `JsonMapper` bean (extends `ObjectMapper`), not `ObjectMapper` directly. **Workaround:** Create `ObjectMapper` manually in tests:
   ```java
   private final ObjectMapper objectMapper = JsonMapper.builder()
       .addModule(new JavaTimeModule())
       .build();
   ```

### `@ComponentScan` Breaks `@SpringBootApplication` Exclude Filters

10. Custom `@ComponentScan` on Application classes **overrides** `@SpringBootApplication`'s default `excludeFilters`, losing `AutoConfigurationExcludeFilter`. This causes `@AutoConfiguration` classes to load as regular `@Configuration`, breaking `@WebMvcTest` slice isolation. **Fix:** Always include:
    ```java
    @ComponentScan(
        basePackages = {...},
        excludeFilters = {
            @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
            @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
        }
    )
    ```

### `@AutoConfiguration` Must Be Conditional

11. `AuditConfig` (registered via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`) must have `@ConditionalOnBean(EntityManagerFactory.class)`. Without this, it loads JPA auditing even in non-JPA test contexts (e.g., Feign client tests), causing "JPA metamodel must not be empty" errors.

### Lombok `@SuperBuilder` and Collection Fields

12. `@SuperBuilder` (and `@Builder`) **ignores field initializers**. Collection fields like `Set<Long> categoryIds = new HashSet<>()` become `null` when built via the builder. **Fix:** Add `@Builder.Default` annotation and ensure `import lombok.Builder;` is present.

### Java 25 Reflection and Anonymous Classes

13. In Java 25, anonymous inner classes' methods are **not accessible** via `Class.getMethod()` / `Method.invoke()` from different packages. `SecurityUtils.getUserIdFromAuthenticationOrThrow()` uses reflection to call `getId()` on the principal. Anonymous `UserDetails` implementations in tests fail silently. **Fix:** Use a public named class (e.g., `TestUserDetails`) instead of anonymous classes.

### Testing Patterns Established

14. **Repository/Integration tests** need Testcontainers with `postgis/postgis:17-3.5` and `@ServiceConnection`. Always set `createdBy=999L` and `updatedBy=999L` on entities (no security context in tests).

15. **Integration tests** should use `@Transactional` for test isolation (each test rolls back) and to avoid `LazyInitializationException`.

16. **Feign client tests** should use a minimal `@Configuration` inner class with `@EnableFeignClients(clients = ...)` and `@EnableAutoConfiguration(exclude = ...)` — never the full application context.

17. **Controller tests** with `@WebMvcTest`: security IS auto-configured. Use `@AutoConfigureMockMvc(addFilters = false)` for public endpoints, or `@WithMockUser(roles = "ADMIN")` for secured endpoints.

18. **Spring Cloud OpenFeign** timeout properties use the format: `spring.cloud.openfeign.client.config.<name>.read-timeout` (not the old `feign.client.config` format).

### Technology Versions (as of Feb 2026)

- Spring Boot 4.0.2, Spring Framework 7.0.3
- Hibernate ORM 7.2.4.Final, Jackson 2.20.2
- Testcontainers 2.0.3, WireMock 3.13.2
- Spring Cloud 2025.1.1, GraalVM Native 0.11.4
- Java 25, Gradle 9.2.1
- Docker Desktop 4.60.1, PostgreSQL 17 + PostGIS 3.5

---

## Knowledge Preservation Pattern

**CRITICAL:** The `store_memory` tool frequently fails (HTTP 404 error). To ensure critical learnings are never lost, follow this pattern:

### When You Discover Critical Learnings

1. **ALWAYS try `store_memory` first** - Attempt to store in the global memory system
2. **IF it fails** - Immediately store in project documentation instead
3. **Store location:** Add to `TEST_COVERAGE.md` in the "Critical Technical Learnings" section

### What Qualifies as "Critical Learning"

Store knowledge that meets these criteria:
- Framework bugs, limitations, or compatibility issues
- Application bugs discovered through testing
- Configuration patterns that prevent hard-to-debug failures
- Security vulnerabilities found and fixed
- Testing patterns that are non-obvious or error-prone
- Build/deployment issues and their solutions

### Storage Format in TEST_COVERAGE.md

```markdown
### N. [Descriptive Title]
**Problem:** [What went wrong or what issue this prevents]
**Root Cause:** [Why it happens]
**Solution:** [How to fix it]
**Reference:** [File path and line number]

**Impact/Pattern:** [Why this matters for future work]
```

### Example

```markdown
### 18. @Async Methods Cannot Declare Checked Exceptions
**Problem:** EmailService.sendEmail() was `@Async` but declared `throws MessagingException`
**Root Cause:** Spring cannot propagate checked exceptions from void async methods
**Solution:** Remove `throws` clause, catch exceptions internally, wrap in RuntimeException
**Reference:** `users/src/main/java/eu/dec21/appointme/users/email/EmailService.java:35-75`

**Impact:** This is a fundamental Spring limitation causing extremely hard-to-debug silent failures. Always use try-catch within @Async methods.
```

### Why This Matters

- `store_memory` failures are tracked: HTTP 404 from `https://api.business.githubcopilot.com/agents/swe/internal/memory/v0/`
- Session files (`.copilot/session-state/`) are ephemeral - deleted between sessions
- Project documentation persists across all sessions and is version-controlled
- Future sessions can benefit from learnings even if memory system unavailable

**Rationale:** Knowledge is too valuable to lose. If the memory system is unavailable, project documentation ensures continuity and helps all developers on the team.
