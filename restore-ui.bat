@echo off
setlocal

set PROJ=%~dp0
set SRC_ROOT=%PROJ%src\taskmanagement
set STAGE=%PROJ%_ui_excluded\ui

if not exist "%STAGE%" (
  echo [INFO] Nothing to restore. "%STAGE%" does not exist.
  goto :end
)

if not exist "%SRC_ROOT%" mkdir "%SRC_ROOT%"

echo [INFO] Restoring UI back to "%SRC_ROOT%\ui"
robocopy "%STAGE%" "%SRC_ROOT%\ui" /E /MOVE >nul
if errorlevel 8 (
  echo [ERROR] Failed to restore UI folder. Aborting.
  exit /b 1
)

echo [OK] UI restored under src\taskmanagement\ui

:end
endlocal
