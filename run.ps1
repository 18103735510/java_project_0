$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$port = 8080
if ($args.Count -ge 1) {
    $port = [int]$args[0]
}

if (-not (Test-Path (Join-Path $root 'out'))) {
    Write-Host '还没有编译产物，先执行 build.ps1'
    & (Join-Path $root 'build.ps1')
}

Write-Host ("启动：http://127.0.0.1:" + $port)
java -cp (Join-Path $root 'out') com.example.survey.App --port $port
