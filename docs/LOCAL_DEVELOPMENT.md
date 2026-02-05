# Local Development Guide

## 🚀 Running the Application Locally

### Quick Start

#### Option 1: Automatic (Recommended for first time)

```bash
# Runs ONE service (e.g., users)
./gradlew :users:bootRun

# This will:
# 1. Start Docker Compose automatically (if not running)
# 2. Start the Users service
# 3. Keep Docker running when you stop the app
```

#### Option 2: Manual Control (Better for active development)

```bash
# 1. Start Docker Compose ONCE
./gradlew composeUp

# 2. Run any service (multiple times, fast restarts!)
./gradlew :users:bootRun
./gradlew :businesses:bootRun
./gradlew :categories:bootRun
./gradlew :feedback:bootRun

# 3. When done for the day
./gradlew composeDownNoFail
```

---

## 📊 How It Works

### Docker Compose Lifecycle

**For `bootRun` (running application):**
```
./gradlew :users:bootRun
    ↓
Checks if Docker Compose is running
    ↓
If not running: starts Docker Compose
    ↓
Starts the application
    ↓
You stop the app (Ctrl+C)
    ↓
Docker Compose STAYS RUNNING ✅
```

**For `test` (running tests):**
```
./gradlew test
    ↓
Starts Docker Compose
    ↓
Runs all tests
    ↓
Cleans up Docker Compose ✅
```

---

## 🎯 Recommended Workflows

### Scenario 1: First Time Setup / After Long Break

```bash
# Clean start
./gradlew composeDownNoFail
docker volume prune -f

# Let Gradle manage everything
./gradlew :users:bootRun

# Access the application
# http://localhost:8081/api/v1/...
```

### Scenario 2: Active Development (Best!)

```bash
# Morning: Start Docker ONCE
./gradlew composeUp

# Check it's running
docker ps
# Should see: postgres_container, maildev_container, etc.

# Develop all day with fast restarts
./gradlew :users:bootRun          # Start users
# Ctrl+C to stop
./gradlew :users:bootRun          # Restart users (FAST!)
./gradlew :businesses:bootRun     # Try another service
# Ctrl+C to stop

# Evening: Stop Docker
./gradlew composeDownNoFail
```

### Scenario 3: Running Multiple Services

```bash
# Start Docker once
./gradlew composeUp

# Terminal 1: Users service
./gradlew :users:bootRun

# Terminal 2: Categories service (new terminal)
./gradlew :categories:bootRun

# Terminal 3: Businesses service (new terminal)
./gradlew :businesses:bootRun

# Now you can test inter-service communication (Feign)!
```

### Scenario 4: Clean Database Reset

```bash
# Stop everything
./gradlew composeDownNoFail

# Remove volumes (deletes all data)
docker volume prune -f

# Start fresh
./gradlew composeUp

# Database will be re-initialized with init scripts
```

---

## 🔍 Service Ports

| Service | Port | URL |
|---------|------|-----|
| **Users** | 8081 | http://localhost:8081/api/v1/ |
| **Businesses** | 8082 | http://localhost:8082/api/v1/ |
| **Categories** | 8083 | http://localhost:8083/api/v1/ |
| **Feedback** | 8084 | http://localhost:8084/api/v1/ |
| **PostgreSQL** | 5532 | localhost:5532 |
| **MailDev UI** | 1080 | http://localhost:1080 |
| **MailDev SMTP** | 1025 | localhost:1025 |
| **Keycloak** | 9090 | http://localhost:9090 |

---

## 🛠️ Useful Commands

### Docker Management

```bash
# Start Docker Compose
./gradlew composeUp

# Stop Docker Compose (keeps data)
./gradlew composeDownNoFail

# Check running containers
docker ps

# View logs
docker logs postgres_container
docker logs maildev_container

# Connect to database
docker exec -it postgres_container psql -U pguser -d appme_users

# List all databases
docker exec -it postgres_container psql -U pguser -l
```

### Application Management

```bash
# Run specific service
./gradlew :users:bootRun
./gradlew :businesses:bootRun
./gradlew :categories:bootRun
./gradlew :feedback:bootRun

# Run with custom profile
./gradlew :users:bootRun --args='--spring.profiles.active=dev'

# Run with custom port
./gradlew :users:bootRun --args='--server.port=8181'

# Run without Docker (if already running)
./gradlew :users:bootRun -PwithDocker=false
```

### Testing

```bash
# Run tests (with Docker lifecycle management)
./gradlew test

# Run tests for specific module
./gradlew :users:test

# Run tests without Docker management (if Docker already running)
./gradlew test -PwithDocker=false

# Run integration tests (Testcontainers)
./gradlew integrationTest
```

---

## 🐛 Troubleshooting

### "Port already in use"

**Problem:** Docker or app is already running on that port

**Solution:**
```bash
# Check what's using the port
# Windows:
netstat -ano | findstr :8081

# Linux/Mac:
lsof -i :8081

# Stop the process or change the port
./gradlew :users:bootRun --args='--server.port=8181'
```

### "Database connection refused"

**Problem:** PostgreSQL not ready or not running

**Solution:**
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# If not running, start compose
./gradlew composeUp

# Wait a few seconds for PostgreSQL to be ready
# Then try again
```

### "Database does not exist"

**Problem:** Docker volume has old data without new databases

**Solution:**
```bash
# Stop and clean
./gradlew composeDownNoFail
docker volume prune -f

# Start fresh (will run init scripts)
./gradlew composeUp
```

### App starts but no response

**Problem:** Wrong port or context path

**Solution:**
```bash
# Users service URL should be:
http://localhost:8081/api/v1/

# Not:
http://localhost:8081/  # Missing context path!
```

### "Out of memory" errors

**Problem:** Too many services running

**Solution:**
```bash
# Stop services you're not using
# Ctrl+C in their terminals

# Or increase Docker memory
# Docker Desktop → Settings → Resources → Memory
```

---

## 💡 Pro Tips

### 1. Keep Docker Running

```bash
# Start once in the morning
./gradlew composeUp

# Use all day with fast app restarts
./gradlew :users:bootRun  # Fast!

# Stop once at end of day
./gradlew composeDownNoFail
```

**Why?** Starting Docker Compose takes ~10 seconds. Keeping it running = instant app restarts!

### 2. Use Docker Desktop Dashboard

- Visual view of running containers
- Easy access to logs
- Quick container restart
- Resource monitoring

### 3. Database GUI Tools

Connect with your favorite tool:
```
Host: localhost
Port: 5532
User: pguser
Password: p@ssw0rD!
Database: appme_users (or appme_businesses, etc.)
```

Recommended tools:
- **DBeaver** (Free, cross-platform)
- **pgAdmin** (Free, PostgreSQL-specific)
- **DataGrip** (Paid, JetBrains)

### 4. Email Testing

Access MailDev UI: http://localhost:1080
- See all emails sent by the application
- No real emails sent (safe for testing)
- View HTML/text versions
- Check email headers

### 5. Hot Reload (Spring DevTools)

Already included! Changes to Java files trigger automatic restart:
```bash
./gradlew :users:bootRun
# Edit Java file
# App automatically restarts (in ~2 seconds)
```

### 6. Multiple Profiles

```bash
# Development (default)
./gradlew :users:bootRun

# Test profile
./gradlew :users:bootRun --args='--spring.profiles.active=test'

# Custom profile
./gradlew :users:bootRun --args='--spring.profiles.active=local'
```

---

## 📚 API Testing Tools

### Swagger UI (Built-in)

Each service has Swagger UI:
- Users: http://localhost:8081/api/v1/swagger-ui.html
- Businesses: http://localhost:8082/api/v1/swagger-ui.html
- Categories: http://localhost:8083/api/v1/swagger-ui.html
- Feedback: http://localhost:8084/api/v1/swagger-ui.html

### Postman / Insomnia

Save your API requests for quick testing:
```
POST http://localhost:8081/api/v1/auth/register
GET http://localhost:8083/api/v1/categories
GET http://localhost:8082/api/v1/businesses?page=0&size=10
```

### cURL Examples

```bash
# Health check
curl http://localhost:8081/actuator/health

# Get categories
curl http://localhost:8083/api/v1/categories

# Create category (as admin)
curl -X POST http://localhost:8083/api/v1/categories/admin \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"name":"New Category","description":"Test"}'
```

---

## 🎯 Quick Reference

### Start Development Session
```bash
./gradlew composeUp
./gradlew :users:bootRun
```

### End Development Session
```bash
# Ctrl+C to stop app
./gradlew composeDownNoFail
```

### Clean Reset
```bash
./gradlew composeDownNoFail
docker volume prune -f
./gradlew composeUp
```

### Check Status
```bash
docker ps
./gradlew validateConfigs
```

---

## 🔄 Comparison: bootRun vs Test

| Feature | bootRun | test |
|---------|---------|------|
| **Purpose** | Manual testing | Automated testing |
| **Docker startup** | ✅ Yes | ✅ Yes |
| **Docker cleanup** | ❌ No (stays running) | ✅ Yes |
| **When to use** | Active development | Before commit |
| **Speed** | Fast restarts | Slower (cleanup) |
| **Data persistence** | ✅ Yes | ❌ No (create-drop) |

---

## 📖 See Also

- `docs/COMPLETE_TESTING_STRATEGY.md` - Testing guide
- `docs/CONFIGURATION.md` - Configuration management
- `CROSS_PLATFORM_SYNC.md` - Config validation tools
- `.github/workflows/ci.yml` - CI/CD setup

---

## 🎉 Summary

**Best practice for local development:**

1. Start Docker Compose once: `./gradlew composeUp`
2. Run your service: `./gradlew :users:bootRun`
3. Restart as needed (fast!)
4. Stop Docker when done: `./gradlew composeDownNoFail`

**Docker will NOT stop automatically when you stop the app!** 🎊

This gives you:
- ✅ Fast app restarts
- ✅ Persistent data during development
- ✅ Ability to run multiple services
- ✅ Full control over Docker lifecycle
