# AGENTS.md

> AI Agent Context File for AppointMe

This file provides **quick-start context** and **module-specific patterns** for AI coding assistants (GitHub Copilot, Cursor, Cline, etc.) working on the AppointMe project.

> 📖 **For detailed conventions and critical technical learnings**, see [`.github/copilot-instructions.md`](.github/copilot-instructions.md)

## Project Overview

**AppointMe** is a microservices-based appointment management system built with:
- **Spring Boot 4.0.2** | **Spring Framework 7.0.3**
- **Java 25** | **Hibernate 7.2.4.Final**
- **PostgreSQL 17 + PostGIS 3.5**
- **Gradle 9.2.1** multi-module build

## Architecture

### Microservices Structure

Each module is a standalone Spring Boot application with its own database:

| Module | Purpose | Port | Database |
|--------|---------|------|----------|
| **users** | Authentication (JWT), roles, groups | 8081 | appme_users |
| **businesses** | Business entities, locations (PostGIS), ratings | 8082 | appme_businesses |
| **categories** | Hierarchical category tree | 8083 | appme_categories |
| **feedback** | User reviews and feedback | 8084 | appme_feedback |
| **common** | Shared utilities, base entities, response models | - | - |
| **exceptions** | Shared exception handling | - | - |

### Key Architectural Patterns

1. **Shared Modules**: `common` and `exceptions` are dependency modules (not runnable) imported by all services
2. **No Cross-Service JPA**: Services use ID references (`Set<Long> categoryIds`), not foreign keys
3. **Base Entity Hierarchy**: All entities extend `BaseEntity` (with audit) or `BaseBasicEntity`
4. **Standard Response Format**: All paginated endpoints use `PageResponse<T>`
5. **Component Scanning**: Each app scans its package + `eu.dec21.appointme.common` + `eu.dec21.appointme.exceptions`

## Quick Commands

```bash
# Build all modules
.\gradlew build

# Run tests (auto-starts Docker Compose)
.\gradlew test

# Run specific service
.\gradlew :users:bootRun

# Start infrastructure (PostgreSQL, Keycloak, MailDev, etc.)
.\gradlew composeUp

# Stop infrastructure
.\gradlew composeDownNoFail
```

## Code Conventions

### Entities

**Standard Entity** (extends `BaseEntity` for audit support):
```java
@Getter @Setter @SuperBuilder
@AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @NotNull @NotBlank
    private String username;
    
    @Email
    private String email;
}
```

**Entity with Cross-Service References** (use IDs, not foreign keys):
```java
@Entity
@Table(name = "businesses")
public class Business extends BaseEntity {
    // Cross-service references use IDs, not foreign keys
    @ElementCollection
    @CollectionTable(name = "business_category_ids")
    @Column(name = "category_id")
    @Builder.Default  // Required with @SuperBuilder for initialized collections
    private Set<Long> categoryIds = new HashSet<>();
    
    private Long ownerId;  // Reference to User, not @ManyToOne
}
```

**Hierarchical Entity** (parent-child relationships):
```java
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {
    private String name;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Category> children = new HashSet<>();
}
```

### DTOs

**Request** - Use `record` with validation:
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
```

**Response** - Use Lombok `@Builder`:
```java
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private List<CategoryResponse> children;
}
```

### Controllers

**Standard CRUD Controller**:
```java
@RestController
@RequestMapping("categories")
@RequiredArgsConstructor
@Tag(name = "Categories")
public class CategoryController {
    private final CategoryService service;
    
    @GetMapping("{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @GetMapping
    @Operation(summary = "List all categories with pagination")
    public ResponseEntity<PageResponse<CategoryResponse>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.findAll(page, size));
    }
    
    @PostMapping
    @Operation(summary = "Create new category")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(request));
    }
}
```

**Secured Controller** (requires authentication):
```java
@RestController
@RequestMapping("feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback")
public class FeedbackController {
    private final FeedbackService service;
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Submit feedback (authenticated users only)")
    public ResponseEntity<FeedbackResponse> create(@Valid @RequestBody FeedbackRequest request) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(request, userId));
    }
}
```

### Services

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;
    
    public UserResponse findById(Long id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
    
    @Transactional
    public UserResponse create(UserRequest request) {
        User user = mapper.toEntity(request);
        User saved = repository.save(user);
        return mapper.toResponse(saved);
    }
}
```

### Exception Handling

**Always use custom exceptions from `exceptions` module:**
```java
throw new ResourceNotFoundException("User not found: " + id);  // ✅
throw new EntityNotFoundException("...");  // ❌ Don't use JPA exception
```

**Common error codes:**
- `4006` - RESOURCE_NOT_FOUND (404)
- `4001` - BAD_REQUEST_PARAMETERS (400)
- `1005` - INVALID_CREDENTIALS (401)
- `4002` - OPERATION_NOT_PERMITTED (403)

**Module-specific error codes** are defined in each module's `ErrorCodes` enum:
- `UsersErrorCodes`, `BusinessErrorCodes`, `CategoryErrorCodes`, `FeedbackErrorCodes`

## Critical Technical Knowledge

### Spring Boot 4 + Hibernate 7 + Java 25 Gotchas

1. **PostGIS Dialect**: Use `org.hibernate.dialect.PostgreSQLDialect` (has built-in spatial support in Hibernate 7)

2. **`@ComponentScan` breaks `@SpringBootApplication` exclude filters** - Always include:
   ```java
   @ComponentScan(
       basePackages = {...},
       excludeFilters = {
           @Filter(type = CUSTOM, classes = TypeExcludeFilter.class),
           @Filter(type = CUSTOM, classes = AutoConfigurationExcludeFilter.class)
       }
   )
   ```

3. **`@MockBean` removed** - Use `@MockitoBean` from `org.springframework.test.context.bean.override.mockito`

4. **`@WebMvcTest` doesn't auto-configure Jackson** - Create ObjectMapper manually:
   ```java
   private final ObjectMapper objectMapper = JsonMapper.builder()
       .addModule(new JavaTimeModule()).build();
   ```

5. **`@SuperBuilder` ignores field initializers** - Use `@Builder.Default`:
   ```java
   @Builder.Default
   private Set<Long> categoryIds = new HashSet<>();
   ```

6. **Java 25 reflection breaks anonymous classes** - Use public named classes for test UserDetails

7. **`@AutoConfiguration` must be conditional** - Add `@ConditionalOnBean(EntityManagerFactory.class)` to AuditConfig

### Testing Quick Reference

**Test Types & Annotations:**

| Test Type | Annotation | Docker Required | Use Case |
|-----------|-----------|-----------------|----------|
| **Unit** | `@ExtendWith(MockitoExtension.class)` | No | Entities, DTOs, Services (mocked) |
| **Repository** | `@DataJpaTest` + `@Testcontainers` | Yes | Database queries, constraints |
| **Controller** | `@WebMvcTest(Controller.class)` | No | REST endpoints, validation |
| **Integration** | `@SpringBootTest` + `@Testcontainers` | Yes | Full application flow |

**Common Patterns:**

```java
// Repository tests (standard PostgreSQL)
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
class UserRepositoryTest {
    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
    
    @Test
    @Transactional  // For test isolation
    void testSave() {
        user.setCreatedBy(999L);  // No security context in tests
        user.setUpdatedBy(999L);
        repository.save(user);
    }
}

// Repository tests (PostGIS - businesses module only)
@Container @ServiceConnection
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgis/postgis:17-3.5");

// Controller tests (public endpoints)
@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable security
class CategoryControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean CategoryService service;
    
    // Spring Boot 4: Manual ObjectMapper creation required
    private final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new JavaTimeModule()).build();
}

// Controller tests (secured endpoints)
@WebMvcTest(FeedbackController.class)
@WithMockUser(username = "testuser", roles = "USER")
class SecuredControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean FeedbackService service;
}
```

**Security Testing Patterns:**
```java
// Test for duplicate resources (409 Conflict)
@Test
void shouldRejectDuplicateEmail() {
    when(repository.existsByEmail(email)).thenReturn(true);
    assertThatThrownBy(() -> service.create(request))
        .isInstanceOf(DuplicateResourceException.class);
}

// Test for authorization (user can only delete own feedback)
@Test
void shouldPreventDeletingOtherUsersFeedback() {
    Feedback feedback = createFeedback(userId: 100L);
    Long currentUserId = 200L;  // Different user
    
    assertThatThrownBy(() -> service.delete(feedbackId, currentUserId))
        .isInstanceOf(OperationNotPermittedException.class);
}

// Test for circular references (Categories module)
@Test
void shouldDetectCircularReference() {
    Category parent = createCategory(id: 1L);
    Category child = createCategory(id: 2L, parent: parent);
    
    // Try to set parent's parent to child (creates cycle)
    assertThatThrownBy(() -> service.update(1L, parentId: 2L))
        .isInstanceOf(CircularCategoryReferenceException.class);
}
```
```

## Development Workflow

### Docker Compose Lifecycle

**Option 1: Automatic (First Time / Single Service)**
```bash
# Starts Docker Compose automatically if not running
.\gradlew :users:bootRun

# Docker stays running after you stop the app (Ctrl+C)
```

**Option 2: Manual (Active Development / Multiple Services)**
```bash
# 1. Start infrastructure ONCE
.\gradlew composeUp

# 2. Run services (fast restarts!)
.\gradlew :users:bootRun
.\gradlew :businesses:bootRun
.\gradlew :categories:bootRun
.\gradlew :feedback:bootRun

# 3. When done for the day
.\gradlew composeDownNoFail
```

**Key Differences:**
- `bootRun`: Starts Docker if needed, **keeps it running** after app stops
- `test`: Starts Docker, runs tests, **stops Docker** automatically
- Manual `composeUp`: Best for active development (avoids Docker restart overhead)

### Service URLs

- **Users**: http://localhost:8081/swagger-ui.html
- **Businesses**: http://localhost:8082/swagger-ui.html
- **Categories**: http://localhost:8083/swagger-ui.html
- **Feedback**: http://localhost:8084/swagger-ui.html

### Infrastructure URLs

- **MailDev**: http://localhost:1080
- **Keycloak Admin**: http://localhost:8080
- **Grafana**: http://localhost:3000

### Cross-Platform Configuration

**Validate configs (all platforms):**
```bash
.\gradlew validateConfigs      # Check configs are in sync
.\gradlew syncConfigsHelp      # Show fix instructions
```

**Platform-specific scripts:**
- **Windows**: `.\scripts\sync-configs.ps1 -Fix`
- **Linux/Mac**: `./scripts/sync-configs.sh --fix`

## Security & Authentication

- **JWT-based** authentication in `users` module
- **JwtService** handles token generation/validation
- **JwtFilter** intercepts and validates requests
- **SecurityUtils** helper: `SecurityUtils.getCurrentUserIdOrThrow()`

## API Standards

### REST Endpoints by Module

**Users** (port 8081):
```
POST   /users/register          # Register new user
POST   /users/login             # Authenticate and get JWT
GET    /users/{id}              # Get user by ID
GET    /users                   # List users (paginated)
POST   /users/groups            # Create user group
GET    /users/groups/{id}       # Get group details
```

**Businesses** (port 8082):
```
POST   /businesses              # Create business
GET    /businesses/{id}         # Get business by ID
GET    /businesses              # List businesses (paginated)
GET    /businesses/category/{categoryId}  # Filter by category
GET    /businesses/nearby       # Spatial search (PostGIS)
PUT    /businesses/{id}/rating  # Update business rating
```

**Categories** (port 8083):
```
POST   /categories              # Create category
GET    /categories/{id}         # Get category by ID
GET    /categories              # List categories (paginated)
GET    /categories/tree         # Get full category tree
GET    /categories/{id}/children  # Get child categories
GET    /categories/{id}/ancestors # Get parent path
```

**Feedback** (port 8084):
```
POST   /feedback                # Submit feedback (authenticated)
GET    /feedback/{id}           # Get feedback by ID
GET    /feedback/business/{businessId}  # Get feedback for business
GET    /feedback/user/{userId}  # Get user's feedback history
DELETE /feedback/{id}           # Delete own feedback
```

### Pagination Parameters

- `page` (default: 0)
- `size` (default: 10)

### HTTP Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Validation errors
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Module-Specific Patterns

### Users Module - Authentication & Authorization

**JWT Authentication Flow:**
```java
// Login endpoint generates JWT
@PostMapping("login")
public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    User user = authService.authenticate(request.username(), request.password());
    String token = jwtService.generateToken(user);
    return ResponseEntity.ok(new LoginResponse(token, user.getId()));
}

// Protected endpoints validate JWT via JwtFilter
@GetMapping("profile")
@PreAuthorize("hasRole('USER')")
public ResponseEntity<UserResponse> getProfile() {
    Long userId = SecurityUtils.getCurrentUserIdOrThrow();
    return ResponseEntity.ok(userService.findById(userId));
}
```

**Getting Current User ID:**
```java
Long currentUserId = SecurityUtils.getCurrentUserIdOrThrow();  // Throws if not authenticated
Optional<Long> maybeUserId = SecurityUtils.getCurrentUserId(); // Returns Optional
```

### Categories Module - Hierarchical Data

**Parent-Child Relationships:**
```java
// Get full category tree
public List<CategoryResponse> getCategoryTree() {
    List<Category> roots = repository.findByParentIsNull();
    return roots.stream()
        .map(this::toCategoryTreeResponse)  // Recursively map children
        .toList();
}

// Get ancestors (breadcrumb path)
public List<CategoryResponse> getAncestors(Long categoryId) {
    Category category = findById(categoryId);
    List<Category> ancestors = new ArrayList<>();
    Category current = category.getParent();
    while (current != null) {
        ancestors.add(0, current);
        current = current.getParent();
    }
    return ancestors.stream().map(mapper::toResponse).toList();
}
```

### Businesses Module - Spatial Queries (PostGIS)

**Location-based Search:**
```java
@Entity
public class Business extends BaseEntity {
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;
}

// Repository query
@Query(value = """
    SELECT * FROM businesses b
    WHERE ST_DWithin(b.location::geography, 
                     ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                     :radiusMeters)
    ORDER BY ST_Distance(b.location::geography,
                         ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography)
    """, nativeQuery = true)
List<Business> findNearby(@Param("lat") double lat, 
                          @Param("lon") double lon, 
                          @Param("radiusMeters") double radiusMeters);
```

**Rating Management:**
```java
// Aggregate rating from feedback
public void updateBusinessRating(Long businessId) {
    Double avgRating = feedbackClient.getAverageRating(businessId);
    Integer count = feedbackClient.getRatingCount(businessId);
    
    Business business = repository.findById(businessId).orElseThrow();
    business.setAverageRating(avgRating);
    business.setRatingCount(count);
    repository.save(business);
}
```

### Feedback Module - Cross-Service References

**Storing References to Other Services:**
```java
@Entity
public class Feedback extends BaseEntity {
    private Long businessId;  // NOT @ManyToOne - different database
    private Long userId;      // NOT @ManyToOne - different database
    
    @Min(1) @Max(5)
    private Integer rating;
    
    @Size(max = 1000)
    private String comment;
}

// Service validates references via Feign clients
public FeedbackResponse create(FeedbackRequest request, Long userId) {
    // Validate business exists
    businessClient.getById(request.businessId());  // Throws 404 if not found
    
    Feedback feedback = Feedback.builder()
        .businessId(request.businessId())
        .userId(userId)
        .rating(request.rating())
        .comment(request.comment())
        .build();
    
    return mapper.toResponse(repository.save(feedback));
}
```

## Inter-Service Communication

Use **OpenFeign** for REST client communication:
```java
// In businesses module - call categories service
@FeignClient(name = "categories", url = "${categories.service.url}")
public interface CategoryClient {
    @GetMapping("/categories/{id}")
    CategoryResponse getById(@PathVariable Long id);
}

// In businesses module - call users service
@FeignClient(name = "users", url = "${users.service.url}")
public interface UserClient {
    @GetMapping("/users/{id}")
    UserResponse getById(@PathVariable Long id);
}
```

Enable in main class: `@EnableFeignClients`

**Configuration (application.yml):**
```yaml
categories:
  service:
    url: http://localhost:8083

users:
  service:
    url: http://localhost:8081
```

## Infrastructure Services (compose.yaml)

- **PostgreSQL** (5432) - Main database
- **Keycloak** (8080) - OAuth2/OIDC
- **MailDev** (1080/1025) - Email testing
- **Grafana LGTM** (3000) - Observability
- **MongoDB** (27017) - Future use

## Deployment

Services are Kubernetes-ready:
- Each module builds Docker image via `bootBuildImage`
- Service discovery via Kubernetes service names
- Configuration via ConfigMaps/Secrets
- Separate database schemas per service

## Documentation References

- **Detailed conventions & critical learnings**: [`.github/copilot-instructions.md`](.github/copilot-instructions.md)
- **Test coverage & bug fixes**: [`TEST_COVERAGE.md`](TEST_COVERAGE.md)
- **Configuration guide**: [`CONFIG_SUMMARY.md`](CONFIG_SUMMARY.md)
- **Cross-platform setup**: [`CROSS_PLATFORM_SYNC.md`](CROSS_PLATFORM_SYNC.md)
- **Local development**: [`docs/LOCAL_DEVELOPMENT.md`](docs/LOCAL_DEVELOPMENT.md)
- **Renovate setup**: [`docs/RENOVATE_SETUP.md`](docs/RENOVATE_SETUP.md)

## Agent Workflow Guidelines

### After Significant Updates

**Significant updates include:**
- New features/modules
- New dependencies
- Architecture changes
- Major refactoring

**Actions:**
1. ✅ Run full test suite: `.\gradlew test`
2. ✅ Fix any broken tests
3. 🔍 Review available skills (`.agents/skills/`)
4. 🔍 Search for relevant new skills
5. ❓ **Ask user for approval** before installing new skills

### Test Execution Policy

**When to run tests:**
- After significant updates
- When explicitly requested

**Auto-fix without asking:**
- Compilation errors
- Missing imports
- Syntax errors
- Obvious typos

**Ask user before fixing:**
- Test assertion failures
- Business logic validation errors
- Ambiguous failures with multiple solutions
- Unclear if issue is in production or test code

**Goal:** Tests must always be green after updates.

### Complete Test Coverage Policy

**Coverage requirement:** ALL code must be tested - NO exceptions.

**When tests fail, identify root cause:**
- ✅ **Bug in production code?** → Fix the production code
- ✅ **Bug in test code?** → Fix the test code
- ✅ **Configuration issue?** → Fix the configuration
- ✅ **Framework limitation?** → Find proper workaround (adjust config, remove problematic plugin)
- ❌ **NEVER compromise correctness** - don't change assertions to match wrong behavior
- ❌ **NEVER skip or exclude code from testing**
- ❓ **Unsure which code is wrong?** → Ask user for clarification

**Goal:** Tests must verify CORRECT behavior. Passing tests with wrong assertions is worse than failing tests.

**Rationale:** Code without tests is untrusted code. Tests that verify wrong behavior are worse than no tests. Technical obstacles are solvable - incomplete or incorrect coverage is not acceptable.

### Common Security Bugs to Check

When implementing new features, always verify:

1. **Duplicate resource checks** - Prevent database constraint violations (500 → 409)
2. **Pagination filtering** - Filter in database queries, not in memory
3. **Empty/null validation** - Especially for passwords and required fields
4. **Authorization checks** - Users can only modify their own resources
5. **Circular references** - For hierarchical data structures
6. **Cross-service validation** - Verify referenced entities exist via Feign clients

*See [`.github/copilot-instructions.md`](.github/copilot-instructions.md) for detailed security patterns.*

## Knowledge Preservation

**Critical learnings discovered during development should be stored in:**
1. Try `store_memory` tool (if available)
2. If it fails → Document in `TEST_COVERAGE.md` under "Critical Technical Learnings"

**Store knowledge about:**
- Framework bugs/limitations
- Hard-won configuration patterns
- Security vulnerabilities fixed
- Non-obvious testing patterns
- Build/deployment issue solutions

---

**Project Version:** 0.0.1-SNAPSHOT  
**Last Updated:** Feb 2026  
**Maintained by:** AppointMe Development Team
