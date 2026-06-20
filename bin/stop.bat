@echo off
for /f "tokens=2" %%i in ('tasklist ^| findstr "java.exe"') do taskkill /PID %%i /F
echo requested java process shutdown
