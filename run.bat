@echo off
echo ==============================================================
echo   OptiLogix - Logistics Optimization System (MCMF Engine)
echo ==============================================================
echo Starting Spring Boot backend...
echo Web UI will be accessible at: http://localhost:8080
echo ==============================================================

if exist target\logistics-optimization-system-1.0.0.jar (
    java -jar target\logistics-optimization-system-1.0.0.jar
) else (
    echo Building project with Maven wrapper...
    call mvnw.cmd package -DskipTests
    java -jar target\logistics-optimization-system-1.0.0.jar
)
pause
