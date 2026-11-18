@echo off
title SOUL Web - Local Preview
setlocal
chcp 65001 >nul
cd /d "%~dp0"

set PORT=8000
set DIR=%~dp0dist-standalone

if not exist "%DIR%\index.html" (
  echo.
  echo [ERROR] "dist-standalone" folder not found next to this file.
  echo Keep this .bat in the same folder as dist-standalone.
  echo.
  pause
  exit /b 1
)

echo.
echo ============================================
echo   SOUL Web - local preview
echo   URL:    http://localhost:%PORT%
echo   Folder: %DIR%
echo.
echo   IMPORTANT:
echo   - Use the localhost URL, do NOT open index.html directly
echo   - Allow camera / microphone when the browser asks
echo   - Press Ctrl+C to stop
echo ============================================
echo.

where python >nul 2>nul
if not errorlevel 1 (
  echo Starting with Python...
  start "" "http://localhost:%PORT%"
  python -m http.server %PORT% --directory "%DIR%"
  goto :end
)

where node >nul 2>nul
if not errorlevel 1 (
  echo Starting with Node.js / npx serve...
  start "" "http://localhost:%PORT%"
  call npx --yes serve -l %PORT% "%DIR%"
  goto :end
)

echo [ERROR] Neither Python nor Node.js was found.
echo.
echo Install ONE of them, then run this file again:
echo   Python 3.8+  https://www.python.org/downloads/
echo   Node.js 20+  https://nodejs.org/
echo.
pause

:end
endlocal
