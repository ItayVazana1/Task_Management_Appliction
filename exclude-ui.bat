@echo off
setlocal

REM Project root = this script's directory
set PROJ=%~dp0

REM Source UI dir and staging dir
set SRC_UI=%PROJ%src\taskmanagement\ui
set STAGE=%PROJ%_ui_excluded

if not exist "%SRC_UI%" (
  echo [INFO] UI folder not found at "%SRC_UI%". Nothing to exclude.
  goto :end
)

if not exist "%STAGE%" mkdir "%STAGE%"

REM Move the whole UI package out of source tree
echo [INFO] Excluding UI: moving "%SRC_UI%" to "%STAGE%\ui"
robocopy "%SRC_UI%" "%STAGE%\ui" /E /MOVE >nul
if errorlevel 8 (
  echo [ERROR] Failed to move UI folder. Aborting.
  exit /b 1
)

echo [OK] UI excluded. You can now compile/run ViewModel/DAO/tests without UI.

:end
endlocal
