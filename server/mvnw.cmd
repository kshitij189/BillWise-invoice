@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

@REM Try JAVA_HOME first
if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
    if exist "%JAVA_HOME%\bin\java.exe" goto foundJava
)

@REM Try common install locations
if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
    set "JAVA_EXE=C:\Program Files\Java\jdk-17\bin\java.exe"
    goto foundJava
)

@REM Try java on PATH
where java >nul 2>nul
if not ERRORLEVEL 1 (
    set "JAVA_EXE=java"
    goto foundJava
)

echo Error: Could not find java.exe.
echo Please set JAVA_HOME or add java to your PATH.
exit /b 1

:foundJava
echo Using Java: %JAVA_EXE%

"%JAVA_EXE%" ^
  -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" ^
  -cp "%WRAPPER_JAR%" ^
  org.apache.maven.wrapper.MavenWrapperMain ^
  %*

if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=%ERRORLEVEL%

:end
endlocal & exit /b %ERROR_CODE%
