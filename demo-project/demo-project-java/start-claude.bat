@echo off
REM Loads GITHUB_PAT from the saved Windows user environment (setx) into this
REM session, then starts Claude Code. The token is never stored in this file.
for /f "delims=" %%i in ('powershell -NoProfile -Command "[Environment]::GetEnvironmentVariable('GITHUB_PAT','User')"') do set "GITHUB_PAT=%%i"

if not defined GITHUB_PAT (
    echo GITHUB_PAT is not set. Run: setx GITHUB_PAT "your-token"
    exit /b 1
)

cd /d "%~dp0"
claude %*
