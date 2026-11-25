@echo off
setlocal
cd /d "%~dp0"
echo SoniCraft Legacy 0.1.27 (Minecraft 1.12.2)
echo Java 8 and reachable Gradle and Forge repositories are required.
java -version
call gradlew.bat --no-daemon build batch019Check batch020Check batch021Check batch022Check batch023Check batch024Check batch025Check batch026Check batch027Check
if errorlevel 1 exit /b 1
echo Built jars:
dir /b build\libs\*.jar
