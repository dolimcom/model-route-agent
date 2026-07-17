param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$DatasetPath = "docs/semantic-router-test-questions.md",
    [string]$CsvPath = "docs/semantic-candidate-evaluation-results.csv",
    [string]$ReportPath = "docs/semantic-candidate-evaluation-report.md",
    [int]$Concurrency = 6,
    [int]$Limit = 0
)

$ErrorActionPreference = "Stop"
$samples = New-Object System.Collections.Generic.List[object]
$currentTask = $null
foreach ($line in Get-Content -LiteralPath $DatasetPath) {
    if ($line -match '^## (GENERAL|DAILY|LITERARY|CODING|MATH)\b') {
        $currentTask = $Matches[1]
        continue
    }
    if ($currentTask -and $line -match '^(\d+)\.\s+(.+)$') {
        $samples.Add([pscustomobject]@{
            Id = [int]$Matches[1]
            ExpectedTask = $currentTask
            Question = $Matches[2].Trim()
        })
    }
}
$samples = @($samples | Sort-Object Id)
if ($Limit -gt 0) {
    $samples = @($samples | Select-Object -First $Limit)
}

$worker = {
    param($ChunkJson, $WorkerBaseUrl)
    $chunk = $ChunkJson | ConvertFrom-Json
    foreach ($sample in $chunk) {
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            $body = @{ question = $sample.Question } | ConvertTo-Json -Compress
            $response = Invoke-RestMethod `
                -Uri "$WorkerBaseUrl/api/agent/route/semantic" `
                -Method Post `
                -ContentType "application/json; charset=utf-8" `
                -Body $body `
                -TimeoutSec 90
            $stopwatch.Stop()
            $top = @($response.topCandidates)[0]
            $second = @($response.topCandidates)[1]
            $topTask = if ($top) { ([string]$top.routeId).ToUpperInvariant() } else { "NONE" }
            [pscustomobject]@{
                Id = [int]$sample.Id
                ExpectedTask = [string]$sample.ExpectedTask
                TopTask = $topTask
                TopTarget = if ($top) { [string]$top.target } else { "" }
                TopSemanticScore = if ($top) { [double]$top.semanticScore } else { 0.0 }
                TopKeywordScore = if ($top) { [double]$top.keywordScore } else { 0.0 }
                TopFinalScore = if ($top) { [double]$top.finalScore } else { 0.0 }
                SecondTask = if ($second) { ([string]$second.routeId).ToUpperInvariant() } else { "NONE" }
                SecondFinalScore = if ($second) { [double]$second.finalScore } else { 0.0 }
                Margin = [double]$response.margin
                Status = [string]$response.status
                ReasonCode = [string]$response.trace.reasonCode
                TopPassed = ($topTask -eq [string]$sample.ExpectedTask)
                AcceptedAndPassed = (($topTask -eq [string]$sample.ExpectedTask) -and
                    (([string]$response.status -eq "ROUTED") -or ([string]$response.status -eq "OVERRIDDEN")))
                LatencyMs = [int]$stopwatch.ElapsedMilliseconds
                Question = [string]$sample.Question
                Error = ""
            }
        } catch {
            $stopwatch.Stop()
            [pscustomobject]@{
                Id = [int]$sample.Id
                ExpectedTask = [string]$sample.ExpectedTask
                TopTask = "ERROR"
                TopTarget = ""
                TopSemanticScore = 0.0
                TopKeywordScore = 0.0
                TopFinalScore = 0.0
                SecondTask = ""
                SecondFinalScore = 0.0
                Margin = 0.0
                Status = "ERROR"
                ReasonCode = "ERROR"
                TopPassed = $false
                AcceptedAndPassed = $false
                LatencyMs = [int]$stopwatch.ElapsedMilliseconds
                Question = [string]$sample.Question
                Error = $_.Exception.Message
            }
        }
    }
}

$chunks = @()
for ($workerIndex = 0; $workerIndex -lt [Math]::Min($Concurrency, $samples.Count); $workerIndex++) {
    $chunk = @()
    for ($sampleIndex = $workerIndex; $sampleIndex -lt $samples.Count; $sampleIndex += $Concurrency) {
        $chunk += $samples[$sampleIndex]
    }
    $chunks += ,$chunk
}

$startedAt = Get-Date
$jobs = foreach ($chunk in $chunks) {
    $chunkJson = $chunk | ConvertTo-Json -Depth 4 -Compress
    Start-Job -ScriptBlock $worker -ArgumentList $chunkJson, $BaseUrl
}
$results = @($jobs | Receive-Job -Wait -AutoRemoveJob | Sort-Object Id)
$elapsed = (Get-Date) - $startedAt
$results | Export-Csv -LiteralPath $CsvPath -NoTypeInformation -Encoding UTF8

$topPassed = @($results | Where-Object TopPassed).Count
$acceptedPassed = @($results | Where-Object AcceptedAndPassed).Count
$topAccuracy = [Math]::Round($topPassed * 100.0 / $results.Count, 2)
$acceptedAccuracy = [Math]::Round($acceptedPassed * 100.0 / $results.Count, 2)

$report = New-Object System.Collections.Generic.List[string]
$report.Add("# 原始语义候选评估")
$report.Add("")
$report.Add("- 样本数：$($results.Count)")
$report.Add("- 策略处理前 Top-1 正确数：$topPassed（$topAccuracy%）")
$report.Add("- 正确且被策略接受：$acceptedPassed（$acceptedAccuracy%）")
$report.Add("- 耗时：$([Math]::Round($elapsed.TotalSeconds, 2)) 秒")
$report.Add("")
$report.Add("## 各任务 Top-1 准确率")
$report.Add("")
$report.Add("| 任务 | 样本数 | Top-1 正确数 | 准确率 |")
$report.Add("|---|---:|---:|---:|")
foreach ($group in $results | Group-Object ExpectedTask | Sort-Object Name) {
    $correct = @($group.Group | Where-Object TopPassed).Count
    $accuracy = [Math]::Round($correct * 100.0 / $group.Count, 2)
    $report.Add("| $($group.Name) | $($group.Count) | $correct | $accuracy% |")
}
$report.Add("")
$report.Add("## 拒绝原因")
$report.Add("")
$report.Add("| 原因 | 数量 |")
$report.Add("|---|---:|")
foreach ($group in $results | Group-Object ReasonCode | Sort-Object Count -Descending) {
    $report.Add("| $($group.Name) | $($group.Count) |")
}
$report.Add("")
$report.Add("## Top-1 混淆矩阵")
$report.Add("")
$report.Add("| 预期 | 最高分候选 | 数量 |")
$report.Add("|---|---|---:|")
foreach ($group in $results | Group-Object ExpectedTask, TopTask | Sort-Object Name) {
    $report.Add("| $($group.Group[0].ExpectedTask) | $($group.Group[0].TopTask) | $($group.Count) |")
}
$report.Add("")
$report.Add("---")
$report.Add("")
$report.Add("# Raw Semantic Candidate Evaluation")
$report.Add("")
$report.Add("> English Version")
$report.Add("")
$report.Add("- Samples: $($results.Count)")
$report.Add("- Top-1 correct before policy: $topPassed ($topAccuracy%)")
$report.Add("- Correct and accepted by policy: $acceptedPassed ($acceptedAccuracy%)")
$report.Add("- Elapsed: $([Math]::Round($elapsed.TotalSeconds, 2)) seconds")
$report.Add("")
$report.Add("## Top-1 Accuracy By Task")
$report.Add("")
$report.Add("| Task | Samples | Top-1 correct | Accuracy |")
$report.Add("|---|---:|---:|---:|")
foreach ($group in $results | Group-Object ExpectedTask | Sort-Object Name) {
    $correct = @($group.Group | Where-Object TopPassed).Count
    $accuracy = [Math]::Round($correct * 100.0 / $group.Count, 2)
    $report.Add("| $($group.Name) | $($group.Count) | $correct | $accuracy% |")
}
$report.Add("")
$report.Add("## Rejection Reasons")
$report.Add("")
$report.Add("| Reason | Count |")
$report.Add("|---|---:|")
foreach ($group in $results | Group-Object ReasonCode | Sort-Object Count -Descending) {
    $report.Add("| $($group.Name) | $($group.Count) |")
}
$report.Add("")
$report.Add("## Top-1 Confusion Matrix")
$report.Add("")
$report.Add("| Expected | Top candidate | Count |")
$report.Add("|---|---|---:|")
foreach ($group in $results | Group-Object ExpectedTask, TopTask | Sort-Object Name) {
    $report.Add("| $($group.Group[0].ExpectedTask) | $($group.Group[0].TopTask) | $($group.Count) |")
}
[IO.File]::WriteAllLines(
    [IO.Path]::GetFullPath($ReportPath),
    $report,
    (New-Object System.Text.UTF8Encoding($false)))

[pscustomobject]@{
    Samples = $results.Count
    Top1Correct = $topPassed
    Top1AccuracyPercent = $topAccuracy
    CorrectAndAccepted = $acceptedPassed
    CorrectAndAcceptedPercent = $acceptedAccuracy
    ElapsedSeconds = [Math]::Round($elapsed.TotalSeconds, 2)
    CsvPath = $CsvPath
    ReportPath = $ReportPath
} | Format-List
