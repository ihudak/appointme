# Copilot Instructions for AppointMe

> **Detailed technical reference** for code conventions, error handling, testing patterns, and critical discoveries.
>
> 🚀 **For quick-start context and module-specific patterns**, see [`AGENTS.md`](../AGENTS.md)

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
   public ResponseEntity<PageResponse<EntityResponse>> findAll(...) {
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
       // fields
   }
   ```

3. **Cross-service references**: NEVER use `@ManyToOne` or `@OneToMany` across service boundaries. Use ID collections:
   ```java
   // In Business entity - stores category IDs, not foreign keys
   @ElementCollection
   @CollectionTable(name = "business_category_ids")
   @Column(name = "category_id")
   @Builder.Default
   private Set<Long> categoryIds = new HashSet<>();
   
   // In Feedback entity - stores user and business IDs
   private Long userId;
   private Long businessId;
   ```

4. **Embedded objects**: Use `@Embedded` for value objects:
   ```java
   @Embedded
   private Address address;  // Used in Business entity
   ```

5. **Hierarchical relationships** (within same service): Use self-referential `@ManyToOne`:
   ```java
   // In Category entity
   @ManyToOne(fetch = LAZY)
   @JoinColumn(name = "parent_id")
   private Category parent;
   
   @OneToMany(mappedBy = "parent", cascade = ALL, orphanRemoval = true)
   @Builder.Default
   private Set<Category> children = new HashSet<>();
   ```

6. **PostGIS support**: Business module uses PostGIS for geospatial data:
   ```java
   @Column(columnDefinition = "geography(Point,4326)")
   private Point location;
   ```

### Repository Patterns

1. **Spring Data JPA**: Use interface-based repositories extending `JpaRepository`

2. **Custom queries**: Use `@Query` with JPQL for complex queries:
   ```java
   // Querying @ElementCollection (Business -> categoryIds)
   @Query("SELECT b FROM Business b JOIN b.categoryIds c WHERE c = :categoryId")
   Page<Business> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
   
   // Hierarchical query (Category parent-child)
   List<Category> findByParentIsNull();
   List<Category> findByParentId(Long parentId);
   
   // Native query for PostGIS spatial search
   @Query(value = "SELECT * FROM businesses WHERE ST_DWithin(...)", nativeQuery = true)
   List<Business> findNearby(@Param("lat") double lat, @Param("lon") double lon);
   ```

3. **Pagination**: Always accept `Pageable` and return `Page<T>` for list queries

### Service Layer

1. **Service injection**: Use constructor injection with `@RequiredArgsConstructor` (Lombok):
   ```java
   @Service
   @RequiredArgsConstructor
   public class CategoryService {
       private final CategoryRepository repository;
       private final CategoryMapper mapper;
   }
   ```

2. **Security context access** (for authenticated endpoints): Use `SecurityUtils` from `common.util`:
   ```java
   // In Feedback service - get current authenticated user
   Long userId = SecurityUtils.getCurrentUserIdOrThrow();
   
   // In User service - extract from Authentication object
   Long userId = SecurityUtils.getUserIdFromAuthenticationOrThrow(authentication);
   ```

3. **Entity not found**: Throw `ResourceNotFoundException` from exceptions module:
   ```java
   // ✅ Correct - use custom exception
   .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id))
   
   // ❌ Wrong - don't use JPA exception
   .orElseThrow(() -> new EntityNotFoundException("..."))
   ```

4. **Inter-service communication**: Use Feign clients to call other services:
   ```java
   @Service
   @RequiredArgsConstructor
   public class BusinessService {
       private final BusinessRepository repository;
       private final CategoryClient categoryClient;  // Feign client
       
       public void validateCategories(Set<Long> categoryIds) {
           categoryIds.forEach(categoryClient::getById);  // Throws 404 if not found
       }
   }
   ```

### Controller Patterns

1. **Controller structure**:
   ```java
   @RestController
   @RequestMapping("categories")  // Plural resource name
   @RequiredArgsConstructor
   @Tag(name = "Categories", description = "Categories API")
   public class CategoryController {
       private final CategoryService service;
   }
   ```

2. **OpenAPI documentation**: Always add `@Operation` with `summary` and `description`:
   ```java
   @GetMapping("{id}")
   @Operation(summary = "Get category by ID", description = "Retrieves a category by its ID")
   public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
       return ResponseEntity.ok(service.findById(id));
   }
   ```

3. **Pagination parameters**: Use consistent defaults:
   ```java
   @GetMapping
   public ResponseEntity<PageResponse<CategoryResponse>> findAll(
       @RequestParam(defaultValue = "0") int page,
       @RequestParam(defaultValue = "10") int size
   ) {
       return ResponseEntity.ok(service.findAll(page, size));
   }
   ```

4. **Secured endpoints**: Use `@PreAuthorize` for role-based access:
   ```java
   @PostMapping
   @PreAuthorize("hasRole('USER')")
   @Operation(summary = "Submit feedback (authenticated users only)")
   public ResponseEntity<FeedbackResponse> create(@Valid @RequestBody FeedbackRequest request) {
       Long userId = SecurityUtils.getCurrentUserIdOrThrow();
       return ResponseEntity.status(HttpStatus.CREATED)
           .body(service.create(request, userId));
   }
   ```

5. **Response wrapping**: Always wrap responses in `ResponseEntity`:
   ```java
   return ResponseEntity.ok(service.findById(id));  // 200 OK
   return ResponseEntity.status(HttpStatus.CREATED).body(created);  // 201 Created
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

3. **Environment-specific configuration**: Each module has profiles for dev/test/stage/prod:
   ```yaml
   # application-dev.yml
   spring:
     datasource:
       url: jdbc:postgresql://${APPME_PG_SRV:localhost}:${APPME_PG_PORT:5532}/${APPME_PG_DBNAME:appme_users}
     jpa:
       hibernate:
         ddl-auto: update  # Auto-updates schema in dev
   
   # application-prod.yml
   spring:
     jpa:
       hibernate:
         ddl-auto: validate  # Validates only, requires migrations
   ```

4. **Database schema management**:
   - **dev**: `ddl-auto: update` - Auto-updates schema
   - **test**: `ddl-auto: create-drop` - Fresh schema each run
   - **stage/prod**: `ddl-auto: validate` - Validates only, requires migrations

5. **Environment variables** (use consistent naming across modules):
   - `APPME_PG_SRV` - PostgreSQL server (default: localhost)
   - `APPME_PG_PORT` - PostgreSQL port (5532 dev/test, 5432 stage/prod)
   - `APPME_PG_DBNAME` - Database name (per module: appme_users, appme_businesses, etc.)
   - `POSTGRES_USER` / `POSTGRES_PASSWORD` - Database credentials
   - `SRV_PORT` - Service port (per module in dev/test, 8080 in stage/prod)
   - Module-specific: `CATEGORIES_SERVICE_URL`, `MAIL_HOST`, `FRONTEND_URL`

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

When services need to communicate:

1. Use **OpenFeign** for REST client communication
2. Add dependency: `implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'`
3. Enable with `@EnableFeignClients` on main application class
4. Create Feign client interface:
   ```java
   // In businesses module - call categories service
   @FeignClient(name = "categories", url = "${categories.service.url}")
   public interface CategoryClient {
       @GetMapping("/categories/{id}")
       CategoryResponse getById(@PathVariable Long id);
   }
   
   // In feedback module - call businesses service
   @FeignClient(name = "businesses", url = "${businesses.service.url}")
   public interface BusinessClient {
       @GetMapping("/businesses/{id}")
       BusinessResponse getById(@PathVariable Long id);
   }
   ```
5. Configure service URLs in `application.yml`:
   ```yaml
   categories:
     service:
       url: http://localhost:8083
   ```
6. Services discover each other via Kubernetes service names in production

## Module-Specific Notes

### Users Module (Port 8081)
- **Authentication**: JWT-based authentication with `JwtService` and `JwtFilter`
- **Authorization**: Role-based access control (RBAC) with User-Role-Group relationships
- **Initialization**: `CommandLineRunner` seeds default roles on startup
- **Security**: SecurityConfig centralizes authentication/authorization rules
- **Key entities**: User, Role, Group, UserGroup

### Businesses Module (Port 8082)
- **Spatial data**: PostGIS support via `hibernate-spatial` and `jts-core`
- **Cross-service refs**: Stores `categoryIds` (Set<Long>) and `ownerId` (Long), not foreign keys
- **Rating system**: Bayesian averaging algorithm for business reputation scores
- **Key entities**: Business, Address (embedded), BusinessKeyword
- **Feign clients**: Calls Categories service to validate category IDs

### Categories Module (Port 8083)
- **Hierarchical structure**: Category has self-referential `parent` (@ManyToOne)
- **Root categories**: Have `parent = null`
- **Repository queries**: `findByParentId()`, `findByParentIsNull()`, `findByParentIdWithChildren()`
- **Key entities**: Category, CategoryKeyword
- **Tree operations**: Recursive queries for ancestors and full tree retrieval

### Feedback Module (Port 8084)
- **Cross-service refs**: Stores `userId` and `businessId` as Long fields (not foreign keys)
- **Validation**: Uses Feign clients to verify user and business exist before creating feedback
- **Authorization**: Users can only delete their own feedback
- **Rating aggregation**: Provides average rating and count for businesses
- **Key entities**: Feedback
- **Feign clients**: Calls Users and Businesses services

### Common Module (Shared Library)
- **Not a runnable application** (no `@SpringBootApplication`)
- **Base classes**: `BaseEntity` (with audit), `BaseBasicEntity` (without audit)
- **Response models**: `PageResponse<T>`, `ExceptionResponse`
- **Utilities**: `SecurityUtils` (get current user ID), `AuditConfig` (JPA auditing)
- **Must be scanned** by all Spring Boot applications via `@ComponentScan`

### Exceptions Module (Shared Library)
- **Not a runnable application**
- **Custom exceptions**: `ResourceNotFoundException`, `UserAuthenticationException`, `OperationNotPermittedException`
- **Error codes**: Each module has its own `ErrorCodes` enum (e.g., `UsersErrorCodes`)
- **Global handler**: `GlobalExceptionHandler` with `@RestControllerAdvice`
- **Must be scanned** by all Spring Boot applications

## Development Workflow

1. **Start infrastructure**: `.\gradlew composeUp`
2. **Run desired service**: `.\gradlew :users:bootRun` (or businesses, categories)
3. **Access Swagger UI**: Each service exposes OpenAPI docs at `/swagger-ui.html`
4. **Test with MailDev**: Email testing UI at http://localhost:1080
5. **Keycloak Admin**: http://localhost:8080 (see `.env` for credentials)

## API Endpoint Patterns

### REST API Structure

All REST endpoints follow consistent patterns:

1. **Base path**: Resource name in plural (e.g., `/users`, `/businesses`, `/categories`, `/feedback`)

2. **Standard CRUD operations**:
   ```
   POST   /categories              - Create new category
   GET    /categories/{id}         - Get by ID
   GET    /categories              - Get all (paginated)
   PUT    /categories/{id}         - Update (if implemented)
   DELETE /categories/{id}         - Delete (if implemented)
   ```

3. **Nested/related resources**:
   ```
   GET /categories/{parentId}/children           - Get subcategories
   GET /categories/{id}/ancestors                - Get parent path
   GET /businesses/category/{categoryId}         - Get businesses by category
   GET /businesses/nearby                        - Spatial search (lat, lon, radius)
   GET /feedback/business/{businessId}           - Get feedback for business
   GET /feedback/user/{userId}                   - Get user's feedback history
   ```

4. **Authentication endpoints** (Users module):
   ```
   POST /users/register                          - Register new user
   POST /users/login                             - Authenticate and get JWT
   ```

5. **Query parameters for pagination**:
   - `page` (default: 0)
   - `size` (default: 10)

6. **HTTP status codes**:
   - `200 OK` - Successful GET/PUT/PATCH
   - `201 Created` - Successful POST
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

### Error Codes by Module

Each module has its own `ErrorCodes` enum (e.g., `UsersErrorCodes`, `BusinessErrorCodes`, `CategoryErrorCodes`, `FeedbackErrorCodes`):

- **1xxx**: Authentication/Authorization errors (1001-1006)
- **2xxx**: Payment/Quota errors (2001-2004)
- **3xxx**: Dependency errors (3001)
- **4xxx**: Client errors (4001-4007)
- **5xxx**: Server errors (5000-5003)

**Common error codes across modules:**
- `4006` - `RESOURCE_NOT_FOUND` (404) - "User not found", "Category not found", etc.
- `4001` - `BAD_REQUEST_PARAMETERS` (400) - Validation failures
- `1005` - `INVALID_CREDENTIALS` (401) - Authentication failures
- `4002` - `OPERATION_NOT_PERMITTED` (403) - Authorization failures

### Exception Throwing Pattern

1. **Resource not found**: Use `ResourceNotFoundException` from exceptions module:
   ```java
   // Users module
   userRepository.findById(id)
       .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
   
   // Categories module
   categoryRepository.findById(id)
       .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
   ```

2. **Authentication errors**: Use `UserAuthenticationException` from exceptions module:
   ```java
   if (!passwordEncoder.matches(password, user.getPassword())) {
       throw new UserAuthenticationException("Invalid credentials");
   }
   ```

3. **Authorization errors**: Use `OperationNotPermittedException` from exceptions module:
   ```java
   if (!feedback.getUserId().equals(currentUserId)) {
       throw new OperationNotPermittedException("Cannot delete another user's feedback");
   }
   ```

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
// Users module
public record UserRequest(
    @NotNull @NotBlank String username,
    @Email String email,
    @NotNull @NotBlank String password
) {}

// Feedback module
public record FeedbackRequest(
    @NotNull Long businessId,
    @Min(1) @Max(5) Integer rating,
    @Size(max = 1000) String comment
) {}

// Categories module
public record CategoryRequest(
    @NotNull @NotBlank String name,
    Long parentId  // Optional - null for root categories
) {}
```

**Conventions**:
- Use `record` (immutable, auto-generates getters, equals, hashCode, toString)
- Add Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Email`, `@Pattern`, `@Min`, `@Max`, etc.)
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
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private List<CategoryResponse> children;  // For tree responses
}
```

**Conventions**:
- Use `@Builder` for flexible object construction
- Include all Lombok annotations: `@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor`
- Response objects can include computed fields not in entity
- For hierarchical data (Categories), responses can include nested collections
- **Never expose sensitive fields** (passwords, tokens, internal security data)

## Mapper Patterns

### Mapper Service

**Pattern**: Create mapper as `@Service` with dependency injection if needed:

```java
@Service
@RequiredArgsConstructor
public class CategoryMapper {
    private final CategoryRepository repository;  // Inject if needed for lookups
    
    public Category toCategory(CategoryRequest request) {
        CategoryBuilder builder = Category.builder()
            .name(request.name());
        
        // Resolve parent if parentId provided
        if (request.parentId() != null) {
            Category parent = repository.findById(request.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            builder.parent(parent);
        }
        
        return builder.build();
    }
    
    public CategoryResponse toCategoryResponse(Category entity) {
        return CategoryResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
            .build();
    }
    
    // For tree responses with nested children
    public CategoryResponse toCategoryTreeResponse(Category entity) {
        return CategoryResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .children(entity.getChildren().stream()
                .map(this::toCategoryTreeResponse)  // Recursive
                .toList())
            .build();
    }
}
```

**Conventions**:
- Mapper is a Spring `@Service` bean
- Method naming: `toEntity(Request)` and `toEntityResponse(Entity)`
- Can inject repositories for resolving references (CategoryMapper resolves parent, BusinessMapper validates categoryIds via Feign)
- Set sensible defaults in request→entity mapping
- Response mapping can include computed fields or nested structures
- For hierarchical data, provide both flat and tree response mappers

**Do NOT use**:
- MapStruct or other annotation processors (manual mapping is preferred for clarity)
- Static utility classes (use Spring-managed services for testability and DI)

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

**When tests fail, identify and fix root cause:**
- **Bug in production code?** → Fix the production code to match expected behavior
- **Bug in test code?** → Fix the test code to verify correct behavior
- **Configuration issue?** → Fix the configuration (database setup, dependencies, etc.)
- **Framework limitation?** → Find proper workaround (remove problematic plugins, adjust configuration, use alternative approach)
- **NEVER skip or exclude code from testing** (like attempting to exclude @Embeddable Address)
- **NEVER compromise correctness** to make tests pass (e.g., don't change assertions to expect errors when success is expected)
- **Investigate root cause** thoroughly before making changes
- **Ask the user for guidance** if unsure whether production code or test code is wrong
- **Document trade-offs** if a workaround affects design

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

## Security Patterns & Common Bugs

### Critical Security Validations

1. **Empty Password Bug**: BCrypt allows encoding empty strings but `matches()` returns false! Always validate at DTO level:
   ```java
   @NotEmpty(message = "Password must not be empty")
   @NotBlank(message = "Password must not be blank")
   @Size(min = 8, max = 72, message = "Password must be 8-72 characters")  // BCrypt limit
   @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).+$", 
            message = "Password must contain upper, lower, digit, and special character")
   private String password;
   ```

2. **Duplicate Resource Protection**: Check existence before creating to avoid 500 errors:
   ```java
   // In service layer (Users, Businesses, Categories)
   if (repository.existsByEmail(email)) {
       throw new DuplicateResourceException("Email already exists: " + email);
   }
   // Returns 409 Conflict, not 500 Internal Server Error
   ```

3. **Circular Reference Protection** (Categories module):
   ```java
   // Detect cycles in parent-child hierarchy
   private void validateNoCircularReference(Category category, Long newParentId) {
       Set<Long> visited = new HashSet<>();
       Category current = repository.findById(newParentId).orElse(null);
       
       while (current != null) {
           if (visited.contains(current.getId()) || current.getId().equals(category.getId())) {
               throw new CircularCategoryReferenceException("Circular reference detected");
           }
           visited.add(current.getId());
           current = current.getParent();
       }
   }
   ```

4. **Pagination Filtering Bug**: Filter in database, not in memory:
   ```java
   // ❌ Wrong - filters AFTER pagination (incorrect metadata)
   Page<Business> page = repository.findAll(pageable);
   List<Business> filtered = page.stream().filter(Business::isActive).toList();
   
   // ✅ Correct - filters in database query
   Page<Business> page = repository.findByActiveTrue(pageable);
   ```

5. **Authorization Checks**: Verify ownership before operations:
   ```java
   // In Feedback service - users can only delete their own feedback
   public void delete(Long feedbackId, Long currentUserId) {
       Feedback feedback = repository.findById(feedbackId).orElseThrow();
       
       if (!feedback.getUserId().equals(currentUserId)) {
           throw new OperationNotPermittedException("Cannot delete another user's feedback");
       }
       
       repository.delete(feedback);
   }
   ```

### Testing Abstract Base Classes

When testing abstract classes (like `AuthRegBaseRequest`), Lombok doesn't process annotations in test code:

```java
// Create concrete test implementation with manual builder
static class TestAuthRequest extends AuthRegBaseRequest {
    public TestAuthRequest() {
        super();
    }
    
    public TestAuthRequest(String email, String password) {
        super(email, password);
    }
    
    public static TestAuthRequestBuilder builder() {
        return new TestAuthRequestBuilder();
    }
    
    public static class TestAuthRequestBuilder {
        private String email;
        private String password;
        
        public TestAuthRequestBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public TestAuthRequestBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public TestAuthRequest build() {
            return new TestAuthRequest(email, password);
        }
    }
}

// Use in tests
@Test
void shouldValidatePasswordNotEmpty() {
    TestAuthRequest request = TestAuthRequest.builder()
        .email("test@example.com")
        .password("")  // Empty password
        .build();
    
    Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
    assertThat(violations).hasSizeGreaterThanOrEqualTo(1)
        .extracting(ConstraintViolation::getMessage)
        .contains("Password must not be empty");
}
```

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
