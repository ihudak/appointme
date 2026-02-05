# Configuration Sync Scripts

This folder contains scripts to validate configuration consistency across environments.

## Files

- **sync-configs.ps1** - PowerShell version (Windows)
- **sync-configs.sh** - Bash version (Linux/macOS)

## Usage

### Via Gradle (Recommended)

Works on all platforms automatically:

```bash
./gradlew validateConfigs      # Check configs
./gradlew syncConfigsHelp      # Show fix instructions
```

### Direct Script Usage

**Windows (PowerShell):**
```powershell
./scripts/sync-configs.ps1                  # Validate all
./scripts/sync-configs.ps1 -Fix             # Show fix instructions
./scripts/sync-configs.ps1 -Module users    # Check specific module
```

**Linux/Mac (Bash):**
```bash
./scripts/sync-configs.sh                   # Validate all
./scripts/sync-configs.sh --fix             # Show fix instructions
./scripts/sync-configs.sh --module users    # Check specific module
```

## What It Does

1. Compares each module's dev config to test/stage/prod configs
2. Reports missing configuration keys
3. Provides colored output for easy scanning
4. Returns exit code 0 if all synced, 1 if issues found

## When to Use

- After adding new configuration to dev
- Before committing changes
- In CI/CD pipelines to prevent config drift
- When troubleshooting environment-specific issues

## See Also

Full documentation: `docs/CONFIGURATION.md`
