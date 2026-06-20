@echo off
set BASE_DIR=%~dp0..
set JAR_PATH=%BASE_DIR%\target\aidex-admin.jar
set LOG_DIR=%BASE_DIR%\runtime\logs
cd /d %BASE_DIR%
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%JAR_PATH%" (
  echo jar not found: %JAR_PATH%
  exit /b 1
)
start "aidex-admin" java -jar "%JAR_PATH%" > "%LOG_DIR%\aidex-admin.log" 2>&1
echo aidex-admin started
