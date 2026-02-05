# GitHub Workflows

This directory contains CI/CD workflows for AppointMe.

## Workflows

### ci.yml - Continuous Integration

**Triggers:**
- Push to `main` or `develop`
- Pull requests to `main` or `develop`
- Manual workflow dispatch

**Jobs:**

1. **validate-configs** - Validates configuration file consistency across environments
2. **test** - Runs tests for all 4 modules in parallel using service containers
3. **build** - Builds JAR files after successful tests
4. **docker-build** - (Optional) Builds Docker images on push to main

**Service Containers:**
- PostgreSQL with PostGIS (version 16-3.4)
- MailDev for email testing

**Test Execution:**
Tests run in parallel using matrix strategy:
- users
- businesses  
- categories
- feedback

Each module tests against its own PostgreSQL database created during the workflow.

## Requirements

For Docker image building (optional):
- `DOCKER_USERNAME` - Docker Hub username (GitHub secret)
- `DOCKER_PASSWORD` - Docker Hub password/token (GitHub secret)

## Cost

**FREE** - All features use GitHub Actions free tier.

## Viewing Results

1. Go to the "Actions" tab in your GitHub repository
2. Click on a workflow run
3. Expand jobs to see individual module results
4. Download test artifacts for detailed reports

See `docs/COMPLETE_TESTING_STRATEGY.md` for more information.
