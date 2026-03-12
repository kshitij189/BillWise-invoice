$ErrorActionPreference = "Stop"

$projectBase = $PSScriptRoot
$wrapperJar = Join-Path $projectBase ".mvn\wrapper\maven-wrapper.jar"

# Find Java
$javaExe = $null
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaExe = "$env:JAVA_HOME\bin\java.exe"
} elseif (Test-Path "C:\Program Files\Java\jdk-17\bin\java.exe") {
    $javaExe = "C:\Program Files\Java\jdk-17\bin\java.exe"
} else {
    $javaExe = (Get-Command java -ErrorAction SilentlyContinue).Source
}

if (-not $javaExe) {
    Write-Error "Could not find java.exe. Please set JAVA_HOME or add java to your PATH."
    exit 1
}

Write-Host "Using Java: $javaExe"

& "$javaExe" `
    "-Dmaven.multiModuleProjectDirectory=$projectBase" `
    -cp "$wrapperJar" `
    org.apache.maven.wrapper.MavenWrapperMain `
    @args

exit $LASTEXITCODE
