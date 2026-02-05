# Cross-Platform Configuration Sync

## Summary

Both **Windows** and **Linux/Mac** are now fully supported!

## Scripts Available

- **scripts/sync-configs.ps1** - PowerShell (Windows)
- **scripts/sync-configs.sh** - Bash (Linux/macOS)

## Usage

### Recommended: Use Gradle (works on all platforms)

\\\ash
./gradlew validateConfigs      # Check configs
./gradlew syncConfigsHelp      # Show fix instructions
\\\

### Direct Script Usage

**Windows:**
\\\powershell
./scripts/sync-configs.ps1
./scripts/sync-configs.ps1 -Fix
./scripts/sync-configs.ps1 -Module businesses
\\\

**Linux/Mac:**
\\\ash
./scripts/sync-configs.sh
./scripts/sync-configs.sh --fix
./scripts/sync-configs.sh --module businesses
\\\

## Features

✅ Same functionality on all platforms
✅ Gradle auto-detects your OS
✅ Colored terminal output
✅ Exit code 0 = success, 1 = issues found
✅ CI/CD ready

See **docs/CONFIGURATION.md** for full documentation.

