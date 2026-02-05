# Configuration Management

## Overview

This project uses environment-specific YAML configuration files for each microservice module. To ensure consistency across environments, we provide automated validation tools that work on **Windows, Linux, and macOS**.

## Configuration Files

Each module has 4 environment configs:
- `application-dev.yaml` - Development (localhost, debug logging)
- `application-test.yaml` - Automated testing (create-drop schema, verbose SQL)
- `application-stage.yaml` - Staging environment (validate schema, info logging)
- `application-prod.yaml` - Production environment (validate schema, warn logging)

## Configuration Sync Tool

### Quick Check

To validate that all configurations are synchronized:

```bash
# Using Gradle (recommended - works on all platforms)
./gradlew validateConfigs

# Or directly with scripts:
# Windows
./scripts/sync-configs.ps1

# Linux/Mac
./scripts/sync-configs.sh
```

### Detailed Fix Instructions

If validation fails, get detailed instructions:

```bash
# Using Gradle (all platforms)
./gradlew syncConfigsHelp

# Or directly with scripts:
# Windows
./scripts/sync-configs.ps1 -Fix

# Linux/Mac
./scripts/sync-configs.sh --fix
```

### Check Specific Module

To check only one module:

```bash
# Windows
./scripts/sync-configs.ps1 -Module businesses

# Linux/Mac
./scripts/sync-configs.sh --module businesses
```

## Platform Support

The Gradle tasks automatically detect your platform and use the appropriate script:
- **Windows**: Uses PowerShell script (`sync-configs.ps1`)
- **Linux/macOS**: Uses Bash script (`sync-configs.sh`)

Both scripts provide identical functionality and output.

## Configuration Guidelines

### When Adding New Configuration

1. **Always start with dev config** - Add new configuration to `application-dev.yaml` first
2. **Run validation** - Execute `./gradlew validateConfigs` to check consistency
3. **Copy to other environments** - If validation fails, copy the new sections to test/stage/prod
4. **Adjust environment-specific values**:
   - **URLs**: localhost in dev/test, service names in stage/prod
   - **Credentials**: defaults in dev/test, required env vars in prod
   - **Logging**: debug in dev/test, info in stage, warn in prod
   - **Database**: `ddl-auto: update` in dev, `create-drop` in test, `validate` in stage/prod
5. **Run validation again** - Confirm all configs are synchronized

### Environment-Specific Patterns

See full examples in the documentation for database, service URLs, and logging patterns.

## Integration with CI/CD

Add to your CI pipeline to prevent config drift:

```yaml
- name: Validate configurations
  run: ./gradlew validateConfigs
```

## Best Practices

1. **Keep dev config as source of truth** - Other environments should mirror dev structure
2. **Use environment variables** - Never hardcode production credentials
3. **Validate regularly** - Run `./gradlew validateConfigs` before committing
4. **Document new configs** - Add comments explaining purpose of new configuration
5. **Test changes** - Verify new configs work in dev before deploying
