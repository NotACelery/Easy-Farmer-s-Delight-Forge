@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "NO_PAUSE=0"
if /I "%~1"=="--no-pause" set "NO_PAUSE=1"

echo ============================================================
echo Easy Farmer's Delight Compat - Forge 1.20.1
echo Build de desarrollo
echo ============================================================
echo.

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-dev.ps1"
set "RC=%ERRORLEVEL%"

echo.
if "%RC%"=="0" (
    echo ============================================================
    echo BUILD COMPLETADO CORRECTAMENTE
    echo Revisa: "%~dp0build\libs"
    echo ============================================================
) else (
    echo ============================================================
    echo BUILD FALLIDO - codigo %RC%
    echo La ventana quedara abierta para que puedas leer el error.
    echo Log: "%~dp0build-dev.log"
    echo ============================================================
)

if "%NO_PAUSE%"=="0" (
    echo.
    pause
)
exit /b %RC%
