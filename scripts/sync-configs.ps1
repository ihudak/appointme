#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Validates and synchronizes application configuration files across environments.

.DESCRIPTION
    Ensures that all modules have consistent configuration keys across dev/test/stage/prod.
    Detects missing keys in test/stage/prod that exist in dev configs.

.PARAMETER Fix
    If specified, shows detailed instructions for fixing missing keys.

.PARAMETER Module
    Optional. Specific module to check (businesses, categories, users, feedback). If not specified, checks all.

.EXAMPLE
    .\sync-configs.ps1
    Validates all configs and reports issues

.EXAMPLE
    .\sync-configs.ps1 -Fix
    Shows detailed fix instructions

.EXAMPLE
    .\sync-configs.ps1 -Module businesses
    Checks only the businesses module
#>

param(
    [switch]$Fix,
    [string]$Module
)

$ErrorActionPreference = "Stop"
$scriptRoot = Split-Path -Parent $PSScriptRoot

# ANSI colors for output
$RED = "`e[31m"
$GREEN = "`e[32m"
$YELLOW = "`e[33m"
$BLUE = "`e[34m"
$RESET = "`e[0m"

$modules = if ($Module) { @($Module) } else { @("businesses", "categories", "users", "feedback") }
$environments = @("test", "stage", "prod")

function Get-YamlKeys {
    param([string]$FilePath)
    
    if (-not (Test-Path $FilePath)) {
        return @()
    }
    
    $content = Get-Content $FilePath -Raw
    $keys = @()
    
    # Match top-level keys (no indentation)
    $matches = [regex]::Matches($content, '(?m)^([a-z][a-z0-9-_]*):')
    foreach ($match in $matches) {
        $keys += $match.Groups[1].Value
    }
    
    # Also get second-level keys under spring, application, etc.
    $matches = [regex]::Matches($content, '(?m)^  ([a-z][a-z0-9-_]*):')
    foreach ($match in $matches) {
        $keys += "  " + $match.Groups[1].Value
    }
    
    return $keys | Sort-Object -Unique
}

function Compare-Configs {
    param(
        [string]$ModuleName,
        [string]$DevPath,
        [string]$EnvPath,
        [string]$Environment
    )
    
    $devKeys = Get-YamlKeys -FilePath $DevPath
    $envKeys = Get-YamlKeys -FilePath $EnvPath
    
    $missing = $devKeys | Where-Object { $_ -notin $envKeys }
    
    return @{
        Module = $ModuleName
        Environment = $Environment
        DevPath = $DevPath
        EnvPath = $EnvPath
        Missing = $missing
    }
}

Write-Host "${BLUE}════════════════════════════════════════════════════${RESET}"
Write-Host "${BLUE}  AppointMe Configuration Sync Tool${RESET}"
Write-Host "${BLUE}════════════════════════════════════════════════════${RESET}"
Write-Host ""

$issues = @()
$totalChecks = 0

foreach ($mod in $modules) {
    $devConfig = Join-Path $scriptRoot "$mod\src\main\resources\application-dev.yaml"
    
    if (-not (Test-Path $devConfig)) {
        Write-Host "${YELLOW}⚠${RESET}  Module '$mod' - dev config not found: $devConfig"
        continue
    }
    
    foreach ($env in $environments) {
        $envConfig = Join-Path $scriptRoot "$mod\src\main\resources\application-$env.yaml"
        $totalChecks++
        
        $result = Compare-Configs -ModuleName $mod -DevPath $devConfig -EnvPath $envConfig -Environment $env
        
        if ($result.Missing.Count -gt 0) {
            $issues += $result
            Write-Host "${RED}✗${RESET} ${mod}/${env}: ${RED}$($result.Missing.Count) missing keys${RESET}"
            foreach ($key in $result.Missing) {
                Write-Host "    - $key"
            }
        } else {
            Write-Host "${GREEN}✓${RESET} ${mod}/${env}: All keys present"
        }
    }
}

Write-Host ""
Write-Host "${BLUE}════════════════════════════════════════════════════${RESET}"

if ($issues.Count -eq 0) {
    Write-Host "${GREEN}✓ All configurations are synchronized!${RESET}"
    Write-Host "  Checked: $totalChecks environment configs"
    exit 0
}

Write-Host "${YELLOW}⚠ Found $($issues.Count) configuration files with missing keys${RESET}"

if ($Fix) {
    Write-Host ""
    Write-Host "${YELLOW}Detailed fix instructions:${RESET}"
    Write-Host ""
    
    foreach ($issue in $issues) {
        Write-Host "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
        Write-Host "${BLUE}Module: $($issue.Module) | Environment: $($issue.Environment)${RESET}"
        Write-Host "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
        Write-Host ""
        Write-Host "  Dev config:  ${GREEN}$($issue.DevPath)${RESET}"
        Write-Host "  Env config:  ${YELLOW}$($issue.EnvPath)${RESET}"
        Write-Host ""
        Write-Host "  Missing keys:"
        foreach ($key in $issue.Missing) {
            Write-Host "    ${RED}✗${RESET} $key"
        }
        Write-Host ""
    }
    
    Write-Host "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
    Write-Host "${YELLOW}Action Required:${RESET}"
    Write-Host ""
    Write-Host "  1. Review the dev config and locate the missing sections"
    Write-Host "  2. Copy those sections to test/stage/prod configs"
    Write-Host "  3. Adjust environment-specific values:"
    Write-Host "     - Database URLs and credentials"
    Write-Host "     - Service URLs (localhost vs service names)"
    Write-Host "     - Logging levels (debug/info/warn)"
    Write-Host "     - File upload paths"
    Write-Host "  4. Run ${BLUE}.\scripts\sync-configs.ps1${RESET} again to verify"
    Write-Host ""
    
} else {
    Write-Host ""
    Write-Host "${YELLOW}Run with -Fix to see detailed instructions:${RESET}"
    Write-Host "  ${BLUE}.\scripts\sync-configs.ps1 -Fix${RESET}"
    Write-Host ""
}

exit 1
