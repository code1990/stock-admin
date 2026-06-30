@echo off
set BASE_DIR=%~dp0..
set JAR_PATH=%BASE_DIR%\target\stock-admin.jar
set LOG_DIR=%BASE_DIR%\runtime\logs
cd /d %BASE_DIR%
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%JAR_PATH%" (
  echo jar not found: %JAR_PATH%
  exit /b 1
)
if "%STOCK_ADMIN_DB_URL%"=="" if not "%STOCK_STAT_DB_PATH%"=="" set "STOCK_ADMIN_DB_URL=jdbc:sqlite:%STOCK_STAT_DB_PATH%"
start "stock-admin" java -jar "%JAR_PATH%" > "%LOG_DIR%\stock-admin.log" 2>&1
echo stock-admin started
