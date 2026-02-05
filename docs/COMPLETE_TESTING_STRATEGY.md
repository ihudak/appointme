# Complete Testing Strategy

## Overview

AppointMe uses a **multi-layered testing approach**:
1. **Local Development**: Docker Compose with real PostgreSQL
2. **CI/CD**: GitHub Actions service containers
3. **Integration Tests**: Testcontainers for isolated testing

## 🔧 Configuration Changes

### 1. Build.gradle - Smart Docker Detection

```groovy
// Automatically detects CI environment
def isCiEnvironment = System.getenv("CI") != null
def withDocker = !isCiEnvironment && 
                 (project.findProperty("withDocker") != "false") && 
                 (System.getenv("WITH_DOCKER") != "false")
```

**Behavior:**
- **Local dev**: Uses Docker Compose (default)
- **CI (GitHub Actions)**: Skips Docker Compose (uses service containers instead)
- **Manual override**: `./gradlew test -PwithDocker=false`

### 2. Testcontainers Dependencies Added

```groovy
dependencies {
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
}
```

## 🚀 Running Tests

### Local Development

#### Option 1: With Docker Compose (Default)
```bash
./gradlew test
# Automatically starts Docker Compose, runs tests, cleans up
```

#### Option 2: Without Docker Compose
```bash
# Start docker compose manually first
./gradlew composeUp

# Run tests without compose lifecycle
./gradlew test -PwithDocker=false

# Stop compose when done
./gradlew composeDownNoFail
```

#### Option 3: Using Testcontainers
```bash
# Testcontainers start their own containers
./gradlew integrationTest
```

### In CI/CD (GitHub Actions)

Tests automatically use **service containers** - no docker-compose needed!

```yaml
services:
  postgres:
    image: postgis/postgis:16-3.4
    env:
      POSTGRES_USER: pguser
      POSTGRES_PASSWORD: p@ssw0rD!
```

## 📊 Test Types Comparison

| Test Type | Database | Startup Time | Isolation | Use Case |
|-----------|----------|--------------|-----------|----------|
| **Unit Tests** | H2/None | < 1s | Perfect | Business logic |
| **Docker Compose** | PostgreSQL | ~10s | Shared | Local development |
| **Service Containers** | PostgreSQL | ~5s | Shared | CI/CD pipeline |
| **Testcontainers** | PostgreSQL | ~5s | Per test class | Integration tests |

## 🎯 GitHub Actions Workflow

### Jobs Overview

1. **validate-configs** - Ensures all config files are synchronized
2. **test** - Runs unit/integration tests (matrix: 4 modules in parallel)
3. **build** - Builds JAR files
4. **docker-build** - Builds Docker images (optional, on push to main)

### Test Job Details

```yaml
strategy:
  matrix:
    module: [users, businesses, categories, feedback]

services:
  postgres:
    image: postgis/postgis:16-3.4
    # ... health checks, ports

steps:
  - Create databases via psql
  - Run tests: ./gradlew :${{ matrix.module }}:test
  - Upload test results
```

**Benefits:**
- ✅ Tests run in parallel (4 modules simultaneously)
- ✅ Fast (~2-3 minutes total)
- ✅ FREE on GitHub Actions
- ✅ No docker-compose complexity

## 📝 Writing Integration Tests with Testcontainers

### Example: Category Module

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CategoryIntegrationTest {

    @Container
    @ServiceConnection  // Spring Boot 3.1+ feature
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgis/postgis:16-3.4")
            .withDatabaseName("appme_categories")
            .withUsername("pguser")
            .withPassword("p@ssw0rD!");

    @Autowired
    private CategoryRepository repository;

    @Test
    void shouldCreateCategory() {
        Category category = Category.builder()
            .name("Test")
            .build();
        
        Category saved = repository.save(category);
        
        assertThat(saved.getId()).isNotNull();
    }
}
```

### Key Features

1. **@ServiceConnection**: Automatically configures Spring Boot datasource
2. **Static container**: Reused across all tests in class (faster)
3. **Isolated**: Each test class gets its own database
4. **Real PostgreSQL**: Catches database-specific issues

## 💰 Cost Analysis

### GitHub Actions Free Tier

| Resource | Free Allowance | Our Usage |
|----------|----------------|-----------|
| Minutes (public repos) | Unlimited | Unlimited |
| Minutes (private repos) | 2000/month | ~50/month |
| Storage | 500 MB | ~100 MB |
| Concurrent jobs | 20 | 4 (parallel tests) |

**Estimated cost for private repo**: **FREE** (well under limits)

### Local Development

| Resource | Cost | Notes |
|----------|------|-------|
| Docker Desktop | Free (personal use) | Required |
| PostgreSQL container | Free | Minimal resources |
| Disk space | ~500 MB | Docker images |

## 🔍 Troubleshooting

### Tests fail locally with "database does not exist"

**Solution:**
```bash
./gradlew composeDownNoFail
docker volume prune -f
./gradlew composeUp
./gradlew test
```

### Tests fail in CI with "connection refused"

**Check:**
1. Service container health checks
2. Database creation step succeeded
3. Port mapping (5432 not 5532 in CI)

### Testcontainers fails to start

**Check:**
1. Docker is running: `docker ps`
2. Docker socket accessible
3. Sufficient disk space

### Different test results local vs CI

**Common causes:**
1. Port differences (5532 local, 5432 CI)
2. Database initialization timing
3. Environment variables not set

## 📚 Best Practices

### 1. Use Testcontainers for Critical Integration Tests

```java
// Good: Real database for integration tests
@Testcontainers
class BusinessRepositoryIntegrationTest { ... }

// Also good: H2 for fast unit tests
@DataJpaTest
class BusinessValidationTest { ... }
```

### 2. Keep Docker Compose Running During Development

```bash
# Start once in the morning
./gradlew composeUp

# Run tests multiple times (fast!)
./gradlew test -PwithDocker=false

# Stop at end of day
./gradlew composeDownNoFail
```

### 3. Use Matrix Strategy in CI

```yaml
strategy:
  matrix:
    module: [users, businesses, categories, feedback]
```
Runs 4 modules in parallel = 4x faster!

### 4. Clean Up Test Data

```java
@AfterEach
void cleanup() {
    repository.deleteAll();
}
```

### 5. Use @DirtiesContext Sparingly

Only when absolutely necessary - it's slow!

## 🎓 Next Steps

1. **Add more integration tests** using Testcontainers
2. **Set up code coverage** with JaCoCo
3. **Add mutation testing** with Pitest
4. **Configure Dependabot** for security updates
5. **Add performance tests** for critical endpoints

## 📖 References

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [GitHub Actions Service Containers](https://docs.github.com/en/actions/using-containerized-services/about-service-containers)
- [PostGIS Docker Image](https://hub.docker.com/r/postgis/postgis/)

## 📁 Files Modified

1. ✅ `build.gradle` - Smart Docker detection, Testcontainers dependencies
2. ✅ `gradle/libs.versions.toml` - Testcontainers version catalog
3. ✅ `.github/workflows/ci.yml` - Complete CI/CD workflow
4. ✅ `categories/.../CategoryIntegrationTest.java` - Example Testcontainers test
5. ✅ `docs/COMPLETE_TESTING_STRATEGY.md` - This document

## 🎉 Summary

You now have:
- ✅ **Fast local development** with Docker Compose
- ✅ **Efficient CI/CD** with GitHub Actions service containers
- ✅ **Isolated integration tests** with Testcontainers
- ✅ **Zero additional costs** (all free!)
- ✅ **Comprehensive documentation** for team onboarding
