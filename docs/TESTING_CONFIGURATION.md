# Testing Configuration - Summary

## Current Setup

### ✅ What Works

1. **Docker Compose Integration**
   - Tests automatically run `composeUp` before execution
   - Docker containers are cleaned up after tests (composeDownNoFail)
   - Configured in `build.gradle` lines 92-98

2. **Database Configuration**
   - PostgreSQL runs on `localhost:5532` (from `.env`)
   - Test configs correctly point to this port
   - Each module has its own database (e.g., `appme_users`, `appme_businesses`)

3. **Test Profile**
   - Spring Boot automatically uses `application-test.yaml` when running tests
   - Uses `ddl-auto: create-drop` (fresh schema each test run)
   - Shows SQL for debugging (`show-sql: true`)

### ⚠️ Issues Fixed

1. **Missing Databases**
   - **FIXED**: Added `appme_businesses`, `appme_categories`, `appme_feedback` to init script
   - **FIXED**: Enabled PostGIS extension for businesses and categories modules
   - Location: `database/db-users/init/01-create-db.sql`

### 🔧 How It Works

#### When you run tests:

```bash
./gradlew test
# or
./gradlew :users:test
```

**Gradle automatically:**
1. Starts Docker Compose (`composeUp`)
   - PostgreSQL on port 5532
   - MailDev on ports 1080/1025
   - Keycloak on port 9090
   
2. Waits for services to be ready

3. Runs tests with `test` profile
   - Connects to `localhost:5532`
   - Creates fresh schema (`create-drop`)
   - Shows SQL queries
   
4. Cleans up Docker containers (`composeDownNoFail`)

#### Database Initialization

When PostgreSQL container starts, it runs:
```sql
-- database/db-users/init/01-create-db.sql
create database appme_users owner pguser;
create database appme_businesses owner pguser;
create database appme_categories owner pguser;
create database appme_feedback owner pguser;

-- Enable PostGIS for spatial data
\c appme_businesses
create extension if not exists postgis;

\c appme_categories
create extension if not exists postgis;
```

### 📋 Test Configuration by Module

#### Users Module
```yaml
# application-test.yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5532/appme_users
    username: pguser
    password: p@ssw0rD!
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

#### Businesses Module
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5532/appme_businesses
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

#### Categories Module
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5532/appme_categories
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

#### Feedback Module
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5532/appme_feedback
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

### 🚀 Running Tests

#### All modules:
```bash
./gradlew test
```

#### Specific module:
```bash
./gradlew :users:test
./gradlew :businesses:test
./gradlew :categories:test
./gradlew :feedback:test
```

#### Without Docker (if services already running):
```bash
./gradlew test -PwithDocker=false
```

#### Disable Docker via environment variable:
```bash
export WITH_DOCKER=false
./gradlew test
```

### 🔍 Troubleshooting

#### Tests fail with "database does not exist"

**Cause**: Docker container volume has old data without new databases

**Fix**:
```bash
./gradlew composeDownNoFail
docker volume prune -f
./gradlew composeUp
./gradlew test
```

#### Tests fail with "connection refused"

**Cause**: Docker not running or PostgreSQL not ready

**Fix**:
```bash
# Ensure Docker Desktop is running
docker ps

# Check PostgreSQL logs
docker logs postgres_container

# Wait a bit longer for PostgreSQL to initialize
sleep 10 && ./gradlew test
```

#### Tests pass but application doesn't connect

**Cause**: Different profiles (test vs dev)

**Check**: 
- Dev uses port 5532 (from .env)
- Test also uses port 5532
- Both should work with same Docker instance

### 📝 Best Practices

1. **Keep Docker running during development**
   ```bash
   ./gradlew composeUp
   # Leaves containers running for faster test cycles
   ```

2. **Clean database between test sessions**
   ```bash
   ./gradlew composeDownNoFail
   ```

3. **Check database was created**
   ```bash
   docker exec -it postgres_container psql -U pguser -l
   # Should list: keycloak, appme_users, appme_businesses, appme_categories, appme_feedback
   ```

4. **Verify PostGIS extension**
   ```bash
   docker exec -it postgres_container psql -U pguser -d appme_businesses -c "\dx"
   # Should show: postgis extension
   ```

### ⚠️ Important Notes

1. **First test run** after updating init script requires:
   ```bash
   ./gradlew composeDownNoFail
   docker volume rm $(docker volume ls -q | grep appointme) 2>/dev/null || true
   ./gradlew composeUp
   ```

2. **Tests use separate profile**: Spring Boot automatically loads `application-test.yaml` during tests

3. **Database schema is recreated**: `ddl-auto: create-drop` means tables are dropped/created for each test class

4. **No data persistence**: Test data is lost after each test run (by design)

### 🎯 Next Steps

- Add integration tests that verify:
  - Database connectivity
  - Entity creation/retrieval
  - Inter-service communication (Feign)
  - Security/authentication
  
- Consider using Testcontainers for true isolation (optional)

- Add test data fixtures for consistent testing
