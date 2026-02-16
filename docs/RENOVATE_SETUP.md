# Renovate Bot Setup

## What is Renovate?

Renovate is an automated dependency update bot that:
- Scans your `build.gradle` files for outdated dependencies
- Creates pull requests to update them
- Groups related dependencies together
- Auto-merges minor/patch updates if tests pass
- Requires manual approval for major version updates

## Setup Options

### Option 1: GitHub App (Recommended) ⭐

This is the easiest and most reliable method.

1. **Install the Renovate GitHub App:**
   - Go to: https://github.com/apps/renovate
   - Click "Install"
   - Select your repository (or organization)
   - Authorize the app

2. **That's it!** Renovate will automatically:
   - Detect the `renovate.json` configuration in your repository
   - Start scanning for dependency updates
   - Create an initial "Dependency Dashboard" issue
   - Open pull requests for updates

### Option 2: Self-Hosted with GitHub Actions

If you prefer to run Renovate as a GitHub Action (more control, but requires setup):

1. **Create a GitHub App for Renovate:**
   - Go to: Settings → Developer settings → GitHub Apps → New GitHub App
   - Name: "Renovate Bot" (or your choice)
   - Homepage URL: `https://github.com/renovatebot/renovate`
   - Uncheck "Webhook" → "Active"
   - Repository permissions:
     - Contents: Read & Write
     - Issues: Read & Write
     - Metadata: Read-only
     - Pull requests: Read & Write
     - Workflows: Read & Write
   - Click "Create GitHub App"

2. **Generate a private key:**
   - In your GitHub App settings, scroll to "Private keys"
   - Click "Generate a private key"
   - Save the downloaded `.pem` file

3. **Add secrets to your repository:**
   - Go to: Repository Settings → Secrets and variables → Actions
   - Add two secrets:
     - `RENOVATE_APP_ID`: Your GitHub App ID (found on the app's settings page)
     - `RENOVATE_PRIVATE_KEY`: Paste the entire content of the `.pem` file

4. **The workflow is already created:** `.github/workflows/renovate.yml`

## Configuration

The `renovate.json` file configures how Renovate behaves:

### Key Features:

✅ **Scheduled Updates**: Runs before 6am on Mondays  
✅ **Grouped Dependencies**: Spring Boot, Hibernate, and testing libs are grouped  
✅ **Auto-merge**: Minor and patch updates auto-merge if tests pass  
✅ **Manual Approval**: Major version updates require approval  
✅ **Security Alerts**: CVE fixes are labeled and prioritized  
✅ **Rate Limiting**: Max 5 concurrent PRs to avoid spam  

### Branch Protection (Important!)

To block PRs with failing tests, set up branch protection:

1. Go to: Repository Settings → Branches → Branch protection rules
2. Add rule for `main` branch:
   - ✅ Require status checks to pass before merging
   - Select required checks:
     - ✅ `test (users)`
     - ✅ `test (businesses)`
     - ✅ `test (categories)`
     - ✅ `test (feedback)`
     - ✅ `build`
   - ✅ Require branches to be up to date before merging
   - ✅ Require linear history (optional but recommended)

## How It Works

1. **Renovate scans** your repository every Monday at 5am
2. **Finds outdated dependencies** in `build.gradle` files
3. **Creates PRs** with updates (grouped by type)
4. **GitHub Actions runs** your CI workflow (tests)
5. **If tests pass:**
   - Minor/patch updates → Auto-merged ✅
   - Major updates → Wait for manual review 👀
6. **If tests fail:**
   - Branch protection blocks merge ❌
   - You can review and fix issues

## Example PR Titles

- `chore(deps): update spring boot to v4.0.3`
- `chore(deps): update testing group`
- `chore(deps): update dependency org.postgresql:postgresql to v42.7.4`

## Customization

Edit `renovate.json` to customize:
- `schedule`: Change update frequency
- `automerge`: Disable auto-merge if you want manual review for everything
- `packageRules`: Add more grouping rules
- `assignees`/`reviewers`: Add team members to be notified

## Testing Renovate

After setup, you can trigger a manual run:
- GitHub App: Wait for next scheduled run, or trigger via Dependency Dashboard issue
- GitHub Action: Go to Actions → Renovate → Run workflow

## Troubleshooting

- **No PRs created?** Check the Dependency Dashboard issue for logs
- **Workflow failing?** Ensure secrets are set correctly for Option 2
- **Want to test locally?** Run: `npx renovate --platform=local`

