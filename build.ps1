$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$out = Join-Path $root 'out'
if (Test-Path $out) {
    Remove-Item -Recurse -Force $out
}
New-Item -ItemType Directory -Path $out | Out-Null

$sources = @(Get-ChildItem -Path (Join-Path $root 'src') -Recurse -Filter *.java | ForEach-Object { $_.FullName })
Write-Host ("编译 " + $sources.Count + " 个 Java 文件……")

& javac --release 21 -encoding UTF-8 -d $out $sources
if ($LASTEXITCODE -ne 0) {
    throw '编译失败'
}

& jar --create --file (Join-Path $root 'survey.jar') --main-class com.example.survey.App -C $out .
if ($LASTEXITCODE -ne 0) {
    throw '打包失败'
}

Write-Host '构建完成：out\ 目录 + survey.jar'
