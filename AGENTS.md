# AGENTS.md

> AI Agent Context File for AppointMe

This file provides context for AI coding assistants (GitHub Copilot, Cursor, Cline, etc.) working on the AppointMe project.

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

```java
@Getter @Setter @SuperBuilder
@AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "businesses")
public class Business extends BaseEntity {
    // Cross-service references use IDs, not foreign keys
    @ElementCollection
    @CollectionTable(name = "business_category_ids")
    @Column(name = "category_id")
    @Builder.Default  // Required with @SuperBuilder for initialized collections
    private Set<Long> categoryIds = new HashSet<>();
}
```

### DTOs

**Request** - Use `record` with validation:
```java
public record BusinessRequest(
    @NotNull @NotBlank String name,
    @Email String email
) {}
```

**Response** - Use Lombok `@Builder`:
```java
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class BusinessResponse {
    private Long id;
    private String name;
}
```

### Controllers

```java
@RestController
@RequestMapping("businesses")
@RequiredArgsConstructor
@Tag(name = "Businesses")
public class BusinessController {
    
    @GetMapping("{id}")
    @Operation(summary = "Get business by ID")
    public ResponseEntity<BusinessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @GetMapping
    public ResponseEntity<PageResponse<BusinessResponse>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.findAll(page, size));
    }
}
```

### Services

```java
@Service
@RequiredArgsConstructor
public class BusinessService {
    private final BusinessRepository repository;
    private final BusinessMapper mapper;
    
    public BusinessResponse findById(Long id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + id));
    }
}
```

### Exception Handling

**Always use custom exceptions from `exceptions` module:**
```java
throw new ResourceNotFoundException("Business not found: " + id);  // ✅
throw new EntityNotFoundException("...");  // ❌ Don't use JPA exception
```

**Error codes from `BusinessErrorCodes` enum:**
- `4006` - RESOURCE_NOT_FOUND (404)
- `4001` - BAD_REQUEST_PARAMETERS (400)
- `1005` - INVALID_CREDENTIALS (401)
- `4002` - OPERATION_NOT_PERMITTED (403)

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

### Testing Patterns

```java
// Repository tests
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
class BusinessRepositoryTest {
    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgis/postgis:17-3.5");
    
    @Test
    @Transactional  // For test isolation
    void testSave() {
        business.setCreatedBy(999L);  // No security context in tests
        business.setUpdatedBy(999L);
        repository.save(business);
    }
}

// Controller tests
@WebMvcTest(BusinessController.class)
@AutoConfigureMockMvc(addFilters = false)  // For public endpoints
class BusinessControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean BusinessService service;
    
    private final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new JavaTimeModule()).build();
}

// Secured endpoints
@WebMvcTest(SecuredController.class)
@WithMockUser(roles = "ADMIN")
class SecuredControllerTest { }
```

## Development Workflow

1. **Start infrastructure**: `.\gradlew composeUp`
2. **Run service**: `.\gradlew :users:bootRun`
3. **Access Swagger UI**: http://localhost:8081/swagger-ui.html
4. **Test emails**: http://localhost:1080 (MailDev)
5. **Keycloak Admin**: http://localhost:8080

## Security & Authentication

- **JWT-based** authentication in `users` module
- **JwtService** handles token generation/validation
- **JwtFilter** intercepts and validates requests
- **SecurityUtils** helper: `SecurityUtils.getCurrentUserIdOrThrow()`

## API Standards

### REST Endpoints

```
POST   /businesses              # Create
GET    /businesses/{id}         # Get by ID
GET    /businesses              # List (paginated)
GET    /businesses/category/{categoryId}  # Filtered list
```

### Pagination Parameters

- `page` (default: 0)
- `size` (default: 10)

### HTTP Status Codes

- `200 OK` - Success
- `400 Bad Request` - Validation errors
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Inter-Service Communication

Use **OpenFeign** for REST client communication:
```java
@FeignClient(name = "categories", url = "${categories.service.url}")
public interface CategoryClient {
    @GetMapping("/categories/{id}")
    CategoryResponse getById(@PathVariable Long id);
}
```

Enable in main class: `@EnableFeignClients`

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

- **Full conventions**: `.github/copilot-instructions.md`
- **Test coverage**: `TEST_COVERAGE.md`
- **Cross-platform**: `CROSS_PLATFORM_SYNC.md`
- **Renovate setup**: `docs/RENOVATE_SETUP.md`

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
