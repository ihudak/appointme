#!/usr/bin/env bash
#
# Validates and synchronizes application configuration files across environments.
#
# Usage:
#   ./sync-configs.sh              - Validate all configs
#   ./sync-configs.sh --fix        - Show detailed fix instructions
#   ./sync-configs.sh --module businesses - Check specific module only
#

set -euo pipefail

# Colors (ANSI escape codes)
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
RESET='\033[0m'

# Script directory (project root is parent)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Parse arguments
SHOW_FIX=false
MODULE=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --fix|-f)
            SHOW_FIX=true
            shift
            ;;
        --module|-m)
            MODULE="$2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --fix, -f              Show detailed fix instructions"
            echo "  --module, -m MODULE    Check specific module only"
            echo "  --help, -h             Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                     Validate all configs"
            echo "  $0 --fix               Show detailed fix instructions"
            echo "  $0 --module businesses Check only businesses module"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Determine modules to check
if [[ -n "$MODULE" ]]; then
    MODULES=("$MODULE")
else
    MODULES=("businesses" "categories" "users" "feedback")
fi

ENVIRONMENTS=("test" "stage" "prod")

# Extract YAML keys (top-level and second-level only)
get_yaml_keys() {
    local file="$1"
    
    if [[ ! -f "$file" ]]; then
        return
    fi
    
    # Extract top-level keys (no indentation)
    grep -E '^[a-z][a-z0-9_-]*:' "$file" | cut -d: -f1 | sort -u
    
    # Extract second-level keys (2 spaces indentation)
    grep -E '^  [a-z][a-z0-9_-]*:' "$file" | sed 's/^  /  /' | cut -d: -f1 | sort -u
}

# Compare configs and find missing keys
compare_configs() {
    local module="$1"
    local env="$2"
    local dev_path="$PROJECT_ROOT/$module/src/main/resources/application-dev.yaml"
    local env_path="$PROJECT_ROOT/$module/src/main/resources/application-$env.yaml"
    
    if [[ ! -f "$dev_path" ]]; then
        return 1
    fi
    
    local dev_keys=$(get_yaml_keys "$dev_path")
    local env_keys=$(get_yaml_keys "$env_path")
    
    # Find missing keys (in dev but not in env)
    local missing=""
    while IFS= read -r key; do
        if ! echo "$env_keys" | grep -Fxq "$key"; then
            missing="${missing}${key}"$'\n'
        fi
    done <<< "$dev_keys"
    
    echo "$missing"
}

# Header
echo -e "${BLUE}═══════════════════════════════════════════════════${RESET}"
echo -e "${BLUE}  AppointMe Configuration Sync Tool${RESET}"
echo -e "${BLUE}═══════════════════════════════════════════════════${RESET}"
echo ""

# Check all configurations
declare -a issues
total_checks=0

for module in "${MODULES[@]}"; do
    dev_config="$PROJECT_ROOT/$module/src/main/resources/application-dev.yaml"
    
    if [[ ! -f "$dev_config" ]]; then
        echo -e "${YELLOW}⚠${RESET}  Module '$module' - dev config not found: $dev_config"
        continue
    fi
    
    for env in "${ENVIRONMENTS[@]}"; do
        ((total_checks++)) || true
        
        missing=$(compare_configs "$module" "$env")
        missing_count=$(echo "$missing" | grep -c . || echo 0)
        
        if [[ $missing_count -gt 0 ]]; then
            issues+=("$module:$env:$missing")
            echo -e "${RED}✗${RESET} ${module}/${env}: ${RED}${missing_count} missing keys${RESET}"
            while IFS= read -r key; do
                [[ -n "$key" ]] && echo "    - $key"
            done <<< "$missing"
        else
            echo -e "${GREEN}✓${RESET} ${module}/${env}: All keys present"
        fi
    done
done

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════${RESET}"

# Summary
if [[ ${#issues[@]} -eq 0 ]]; then
    echo -e "${GREEN}✓ All configurations are synchronized!${RESET}"
    echo "  Checked: $total_checks environment configs"
    exit 0
fi

echo -e "${YELLOW}⚠ Found ${#issues[@]} configuration files with missing keys${RESET}"

if [[ "$SHOW_FIX" == true ]]; then
    echo ""
    echo -e "${YELLOW}Detailed fix instructions:${RESET}"
    echo ""
    
    for issue in "${issues[@]}"; do
        IFS=':' read -r module env missing <<< "$issue"
        
        echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
        echo -e "${BLUE}Module: $module | Environment: $env${RESET}"
        echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
        echo ""
        echo -e "  Dev config:  ${GREEN}$PROJECT_ROOT/$module/src/main/resources/application-dev.yaml${RESET}"
        echo -e "  Env config:  ${YELLOW}$PROJECT_ROOT/$module/src/main/resources/application-$env.yaml${RESET}"
        echo ""
        echo "  Missing keys:"
        while IFS= read -r key; do
            [[ -n "$key" ]] && echo -e "    ${RED}✗${RESET} $key"
        done <<< "$missing"
        echo ""
    done
    
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
    echo -e "${YELLOW}Action Required:${RESET}"
    echo ""
    echo "  1. Review the dev config and locate the missing sections"
    echo "  2. Copy those sections to test/stage/prod configs"
    echo "  3. Adjust environment-specific values:"
    echo "     - Database URLs and credentials"
    echo "     - Service URLs (localhost vs service names)"
    echo "     - Logging levels (debug/info/warn)"
    echo "     - File upload paths"
    echo -e "  4. Run ${BLUE}./scripts/sync-configs.sh${RESET} again to verify"
    echo ""
else
    echo ""
    echo -e "${YELLOW}Run with --fix to see detailed instructions:${RESET}"
    echo -e "  ${BLUE}./scripts/sync-configs.sh --fix${RESET}"
    echo ""
fi

exit 1
