$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$source = Join-Path $projectRoot 'frontend/dist'
$destination = Join-Path $projectRoot 'backend/src/main/resources/static'
if (!(Test-Path -LiteralPath (Join-Path $source 'index.html'))) { throw 'Run npm run build in frontend first.' }
# Replacing generated output only; verify destination remains inside this project.
$resolvedDestination = [System.IO.Path]::GetFullPath($destination)
$expected = [System.IO.Path]::GetFullPath((Join-Path $projectRoot 'backend/src/main/resources/static'))
if ($resolvedDestination -ne $expected) { throw 'Unexpected destination.' }
if (Test-Path -LiteralPath $destination) { Remove-Item -LiteralPath $destination -Recurse -Force }
New-Item -ItemType Directory -Path $destination -Force | Out-Null
Copy-Item -Path (Join-Path $source '*') -Destination $destination -Recurse
Write-Output 'React build copied into Spring Boot static resources. Build backend next.'
