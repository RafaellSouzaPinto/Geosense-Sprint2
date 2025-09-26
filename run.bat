@echo off
echo Configurando Java 17 automaticamente...

REM Verificar se Java 17 esta disponivel via SDKMAN ou chocolatey
where /q choco
if %ERRORLEVEL% EQU 0 (
    echo Instalando Java 17 via Chocolatey...
    choco install openjdk17 -y
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot"
    set "PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot\bin;%PATH%"
) else (
    echo Baixando Java 17 automaticamente...
    curl -L -o openjdk-17.msi "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.msi"
    start /wait msiexec /i openjdk-17.msi /quiet
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot"
    set "PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot\bin;%PATH%"
)

echo Verificando Java...
java -version

echo Compilando e executando projeto...
.\mvnw clean spring-boot:run

pause
