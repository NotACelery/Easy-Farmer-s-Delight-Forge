$ErrorActionPreference = 'Stop'
$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location -LiteralPath $ProjectDir
$LogPath = Join-Path $ProjectDir 'build.log'

Set-Content -LiteralPath $LogPath -Encoding UTF8 -Value @(
    "Easy Farmer's Delight Compat - Forge 1.20.1 build log"
    "Started: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    "Project: $ProjectDir"
)

function Write-Log([string]$Text) {
    Write-Host $Text
    Add-Content -LiteralPath $LogPath -Encoding UTF8 -Value $Text
}

function Test-Jdk17Home([string]$JdkPath) {
    if ([string]::IsNullOrWhiteSpace($JdkPath) -or -not (Test-Path -LiteralPath $JdkPath -PathType Container)) { return $null }

    $javaExe = Join-Path $JdkPath 'bin\java.exe'
    $javacExe = Join-Path $JdkPath 'bin\javac.exe'
    $releaseFile = Join-Path $JdkPath 'release'
    if (-not (Test-Path -LiteralPath $javaExe -PathType Leaf)) { return $null }
    if (-not (Test-Path -LiteralPath $javacExe -PathType Leaf)) { return $null }
    if (-not (Test-Path -LiteralPath $releaseFile -PathType Leaf)) { return $null }

    try {
        $releaseText = Get-Content -LiteralPath $releaseFile -Raw -ErrorAction Stop
    } catch {
        return $null
    }

    if ($releaseText -notmatch '(?m)^JAVA_VERSION="17(?:\.|\")') { return $null }

    try {
        return (Resolve-Path -LiteralPath $JdkPath -ErrorAction Stop).Path
    } catch {
        return $JdkPath
    }
}

function Find-Jdk17UnderRoot([string]$Root) {
    if ([string]::IsNullOrWhiteSpace($Root) -or -not (Test-Path -LiteralPath $Root -PathType Container)) { return $null }

    $direct = Test-Jdk17Home $Root
    if ($direct) { return $direct }

    try {
        $candidates = @(Get-ChildItem -LiteralPath $Root -Filter 'javac.exe' -File -Recurse -ErrorAction SilentlyContinue)
    } catch {
        $candidates = @()
    }

    foreach ($candidate in $candidates) {
        if ($candidate.Directory.Name -ne 'bin') { continue }
        $jdkCandidatePath = Split-Path -Parent $candidate.Directory.FullName
        $hit = Test-Jdk17Home $jdkCandidatePath
        if ($hit) { return $hit }
    }

    return $null
}

function Find-Jdk17 {
    if ($env:JAVA_HOME) {
        $hit = Test-Jdk17Home $env:JAVA_HOME
        if ($hit) { return $hit }
    }

    $roots = @(
        (Join-Path $ProjectDir '.jdk17'),
        (Join-Path $env:ProgramFiles 'Eclipse Adoptium'),
        (Join-Path $env:ProgramFiles 'Microsoft'),
        (Join-Path $env:ProgramFiles 'Java'),
        (Join-Path $env:ProgramFiles 'Zulu'),
        (Join-Path $env:LOCALAPPDATA 'Programs\Eclipse Adoptium'),
        (Join-Path $env:APPDATA 'PrismLauncher\java'),
        (Join-Path $env:LOCALAPPDATA 'PrismLauncher\java')
    )

    if (${env:ProgramFiles(x86)}) {
        $roots += (Join-Path ${env:ProgramFiles(x86)} 'Eclipse Adoptium')
        $roots += (Join-Path ${env:ProgramFiles(x86)} 'Java')
    }

    foreach ($root in $roots) {
        if ([string]::IsNullOrWhiteSpace($root)) { continue }
        $hit = Find-Jdk17UnderRoot $root
        if ($hit) { return $hit }
    }
    return $null
}

function Bootstrap-Jdk17 {
    $jdkRoot = Join-Path $ProjectDir '.jdk17'
    $zipPath = Join-Path $jdkRoot 'jdk17.zip'
    New-Item -ItemType Directory -Force -Path $jdkRoot | Out-Null
    if (Test-Path -LiteralPath $zipPath) { Remove-Item -LiteralPath $zipPath -Force }

    Write-Log 'No encontre un JDK 17 instalado. Descargando Eclipse Temurin JDK 17 portatil...'
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    $url = 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse'
    Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $zipPath
    Write-Log 'Extrayendo Temurin JDK 17...'
    Expand-Archive -LiteralPath $zipPath -DestinationPath $jdkRoot -Force
    Remove-Item -LiteralPath $zipPath -Force -ErrorAction SilentlyContinue
    $hit = Find-Jdk17UnderRoot $jdkRoot
    if (-not $hit) {
        Write-Log 'No pude detectar el JDK luego de extraerlo. Buscando javac.exe para diagnostico...'
        $found = @(Get-ChildItem -LiteralPath $jdkRoot -Filter 'javac.exe' -File -Recurse -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName)
        if ($found.Count -gt 0) { foreach ($item in $found) { Write-Log "javac encontrado: $item" } } else { Write-Log 'No aparecio ningun javac.exe dentro de .jdk17.' }
    }
    return $hit
}

function Resolve-Gradle {
    $wrapper = Join-Path $ProjectDir 'gradlew.bat'
    if (Test-Path -LiteralPath $wrapper -PathType Leaf) { return $wrapper }

    $installed = Get-Command gradle.bat -ErrorAction SilentlyContinue
    if ($installed) { return $installed.Source }

    $version = '8.8'
    $distRoot = Join-Path $ProjectDir '.gradle-dist'
    $gradleHome = Join-Path $distRoot "gradle-$version"
    $gradleBat = Join-Path $gradleHome 'bin\gradle.bat'
    if (Test-Path -LiteralPath $gradleBat -PathType Leaf) { return $gradleBat }

    New-Item -ItemType Directory -Force -Path $distRoot | Out-Null
    $zipPath = Join-Path $distRoot 'gradle.zip'
    if (Test-Path -LiteralPath $zipPath) { Remove-Item -LiteralPath $zipPath -Force }
    Write-Log "Gradle no encontrado. Descargando Gradle $version..."
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -UseBasicParsing -Uri "https://services.gradle.org/distributions/gradle-$version-bin.zip" -OutFile $zipPath
    Write-Log 'Extrayendo Gradle...'
    Expand-Archive -LiteralPath $zipPath -DestinationPath $distRoot -Force
    Remove-Item -LiteralPath $zipPath -Force -ErrorAction SilentlyContinue
    if (-not (Test-Path -LiteralPath $gradleBat -PathType Leaf)) { throw "No se encontro Gradle luego de extraer: $gradleBat" }
    return $gradleBat
}

try {
    $jdkHome = Find-Jdk17
    if (-not $jdkHome) { $jdkHome = Bootstrap-Jdk17 }
    if (-not $jdkHome) { throw 'No pude preparar ni detectar un JDK 17 valido.' }

    $env:JAVA_HOME = $jdkHome
    $env:Path = (Join-Path $jdkHome 'bin') + ';' + $env:Path
    Write-Log "JDK 17: $jdkHome"
    $releaseInfo = Get-Content -LiteralPath (Join-Path $jdkHome 'release') -ErrorAction Stop
    $javaVersionLine = $releaseInfo | Where-Object { $_ -match '^JAVA_VERSION=' } | Select-Object -First 1
    if ($javaVersionLine) { Write-Log $javaVersionLine }

    $gradle = Resolve-Gradle
    Write-Log "Gradle: $gradle"
    Write-Log 'Ejecutando: clean build --no-daemon --stacktrace --console=plain'
    Write-Host ''

    $cmdLine = 'call "' + $gradle + '" clean build --no-daemon --stacktrace --console=plain 2>&1'
    & $env:ComSpec /d /s /c $cmdLine | Tee-Object -FilePath $LogPath -Append
    $rc = $LASTEXITCODE
    if ($null -eq $rc) { $rc = 1 }
    if ($rc -ne 0) {
        Write-Log "Gradle termino con codigo $rc"
        exit [int]$rc
    }

    $libs = Join-Path $ProjectDir 'build\libs'
    if (-not (Test-Path -LiteralPath $libs -PathType Container)) { throw 'Gradle termino sin error, pero build\libs no existe.' }
    $jar = Get-ChildItem -LiteralPath $libs -Filter '*.jar' -File | Where-Object { $_.Name -notmatch '(sources|javadoc|dev|shadow)' } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $jar) { throw 'No encontre el JAR runtime en build\libs.' }
    Write-Log "JAR generado: $($jar.FullName)"
    exit 0
}
catch {
    $msg = "ERROR: $($_.Exception.Message)"
    Write-Host $msg -ForegroundColor Red
    Add-Content -LiteralPath $LogPath -Encoding UTF8 -Value $msg
    exit 10
}
