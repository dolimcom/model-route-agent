param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$DatasetPath = "docs/semantic-router-test-questions.md",
    [string]$CsvPath = "docs/routing-evaluation-results.csv",
    [string]$ReportPath = "docs/routing-evaluation-report.md",
    [string]$BaselineCsvPath = "docs/routing-evaluation-results.csv",
    [int]$Concurrency = 4,
    [int]$Limit = 0
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $DatasetPath)) {
    throw "Dataset not found: $DatasetPath"
}

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
            ExpectedModel = switch ($currentTask) {
                "GENERAL" { "general" }
                "DAILY" { "general" }
                "LITERARY" { "literary" }
                "CODING" { "coding" }
                "MATH" { "math" }
            }
            Question = $Matches[2].Trim()
        })
    }
}

$samples = @($samples | Sort-Object Id)
if ($Limit -gt 0) {
    $samples = @($samples | Select-Object -First $Limit)
}
if ($samples.Count -eq 0) {
    throw "No numbered routing samples were found in $DatasetPath"
}

$worker = {
    param($ChunkJson, $WorkerBaseUrl)

    $Chunk = $ChunkJson | ConvertFrom-Json

    $workerResults = foreach ($sample in $Chunk) {
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            $body = @{ question = $sample.Question } | ConvertTo-Json -Compress
            $response = Invoke-RestMethod `
                -Uri "$WorkerBaseUrl/api/agent/route" `
                -Method Post `
                -ContentType "application/json; charset=utf-8" `
                -Body $body `
                -TimeoutSec 90
            $stopwatch.Stop()
            [pscustomobject]@{
                Id = [int]$sample.Id
                ExpectedTask = [string]$sample.ExpectedTask
                ExpectedModel = [string]$sample.ExpectedModel
                ActualTask = [string]$response.taskType
                ActualModel = [string]$response.modelId
                Confidence = [double]$response.confidence
                FallbackUsed = [bool]$response.fallbackUsed
                SemanticAccepted = ([string]$response.reason).StartsWith("Semantic router selected")
                Passed = ([string]$response.taskType -eq [string]$sample.ExpectedTask) -and
                         ([string]$response.modelId -eq [string]$sample.ExpectedModel)
                LatencyMs = [int]$stopwatch.ElapsedMilliseconds
                Question = [string]$sample.Question
                Reason = [string]$response.reason
                Error = ""
            }
        } catch {
            $stopwatch.Stop()
            [pscustomobject]@{
                Id = [int]$sample.Id
                ExpectedTask = [string]$sample.ExpectedTask
                ExpectedModel = [string]$sample.ExpectedModel
                ActualTask = "ERROR"
                ActualModel = "ERROR"
                Confidence = 0.0
                FallbackUsed = $true
                SemanticAccepted = $false
                Passed = $false
                LatencyMs = [int]$stopwatch.ElapsedMilliseconds
                Question = [string]$sample.Question
                Reason = ""
                Error = $_.Exception.Message
            }
        }
    }
    return $workerResults
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

$baselineResults = if (Test-Path -LiteralPath $BaselineCsvPath) {
    @(Import-Csv -LiteralPath $BaselineCsvPath)
} else {
    @()
}
$results | Export-Csv -LiteralPath $CsvPath -NoTypeInformation -Encoding UTF8

$passed = @($results | Where-Object Passed).Count
$semanticAccepted = @($results | Where-Object SemanticAccepted).Count
$errors = @($results | Where-Object { $_.ActualTask -eq "ERROR" }).Count
$accuracy = [Math]::Round(($passed * 100.0) / $results.Count, 2)
$semanticRate = [Math]::Round(($semanticAccepted * 100.0) / $results.Count, 2)

$oldFailureRecovery = $null
$oldFailuresFixed = 0
$oldFailuresRetestedCount = 0
if ($baselineResults.Count -gt 0) {
    $oldFailedIds = @($baselineResults | Where-Object { $_.Passed -ne "True" } | ForEach-Object Id)
    $oldFailuresRetested = @($results | Where-Object { $oldFailedIds -contains $_.Id })
    $oldFailuresRetestedCount = $oldFailuresRetested.Count
    $oldFailuresFixed = @($oldFailuresRetested | Where-Object Passed).Count
    if ($oldFailuresRetestedCount -gt 0) {
        $oldFailureRecovery = [Math]::Round($oldFailuresFixed * 100.0 / $oldFailuresRetestedCount, 2)
    }
}

$report = New-Object System.Collections.Generic.List[string]
$report.Add("# 语义路由评估报告")
$report.Add("")
$report.Add("- 生成时间：$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')")
$report.Add("- 样本数：$($results.Count)")
$report.Add("- 通过数：$passed")
$report.Add("- 准确率：$accuracy%")
$report.Add("- 语义路由接受数：$semanticAccepted（$semanticRate%）")
$report.Add("- 请求错误数：$errors")
$report.Add("- 耗时：$([Math]::Round($elapsed.TotalSeconds, 2)) 秒")
if ($null -ne $oldFailureRecovery) {
    $report.Add("- 原始失败样本修复数：$oldFailuresFixed/$oldFailuresRetestedCount（$oldFailureRecovery%）")
}
$report.Add("")
$report.Add("## 按任务统计准确率")
$report.Add("")
$report.Add("| 预期任务 | 样本数 | 通过数 | 准确率 |")
$report.Add("|---|---:|---:|---:|")
foreach ($group in $results | Group-Object ExpectedTask | Sort-Object Name) {
    $groupPassed = @($group.Group | Where-Object Passed).Count
    $groupAccuracy = [Math]::Round(($groupPassed * 100.0) / $group.Count, 2)
    $report.Add("| $($group.Name) | $($group.Count) | $groupPassed | $groupAccuracy% |")
}

$report.Add("")
$report.Add("## 混淆矩阵")
$report.Add("")
$report.Add("| 预期任务 | 实际任务 | 数量 |")
$report.Add("|---|---|---:|")
foreach ($group in $results | Group-Object ExpectedTask, ActualTask | Sort-Object Name) {
    $report.Add("| $($group.Group[0].ExpectedTask) | $($group.Group[0].ActualTask) | $($group.Count) |")
}

$report.Add("")
$report.Add("## 前 50 个错误路由")
$report.Add("")
$report.Add("| ID | 预期任务 | 实际任务 | 置信度 | 问题 |")
$report.Add("|---:|---|---|---:|---|")
foreach ($failure in $results | Where-Object { -not $_.Passed } | Select-Object -First 50) {
    $safeQuestion = $failure.Question.Replace("|", "\|")
    $report.Add("| $($failure.Id) | $($failure.ExpectedTask) | $($failure.ActualTask) | $($failure.Confidence) | $safeQuestion |")
}

$report.Add("")
$report.Add("---")
$report.Add("")
$report.Add("# Semantic Routing Evaluation Report")
$report.Add("")
$report.Add("> English Version")
$report.Add("")
$report.Add("- Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')")
$report.Add("- Samples: $($results.Count)")
$report.Add("- Passed: $passed")
$report.Add("- Accuracy: $accuracy%")
$report.Add("- Semantic accepted: $semanticAccepted ($semanticRate%)")
$report.Add("- Request errors: $errors")
$report.Add("- Elapsed: $([Math]::Round($elapsed.TotalSeconds, 2)) seconds")
if ($null -ne $oldFailureRecovery) {
    $report.Add("- Original failures fixed: $oldFailuresFixed/$oldFailuresRetestedCount ($oldFailureRecovery%)")
}
$report.Add("")
$report.Add("## Accuracy By Task")
$report.Add("")
$report.Add("| Expected task | Samples | Passed | Accuracy |")
$report.Add("|---|---:|---:|---:|")
foreach ($group in $results | Group-Object ExpectedTask | Sort-Object Name) {
    $groupPassed = @($group.Group | Where-Object Passed).Count
    $groupAccuracy = [Math]::Round(($groupPassed * 100.0) / $group.Count, 2)
    $report.Add("| $($group.Name) | $($group.Count) | $groupPassed | $groupAccuracy% |")
}

$report.Add("")
$report.Add("## Confusion Matrix")
$report.Add("")
$report.Add("| Expected | Actual | Count |")
$report.Add("|---|---|---:|")
foreach ($group in $results | Group-Object ExpectedTask, ActualTask | Sort-Object Name) {
    $report.Add("| $($group.Group[0].ExpectedTask) | $($group.Group[0].ActualTask) | $($group.Count) |")
}

$report.Add("")
$report.Add("## First 50 Misroutes")
$report.Add("")
$report.Add("| ID | Expected | Actual | Confidence | Question |")
$report.Add("|---:|---|---|---:|---|")
foreach ($failure in $results | Where-Object { -not $_.Passed } | Select-Object -First 50) {
    $safeQuestion = $failure.Question.Replace("|", "\|")
    $report.Add("| $($failure.Id) | $($failure.ExpectedTask) | $($failure.ActualTask) | $($failure.Confidence) | $safeQuestion |")
}

[IO.File]::WriteAllLines(
    [IO.Path]::GetFullPath($ReportPath),
    $report,
    (New-Object System.Text.UTF8Encoding($false)))

$taskSummary = foreach ($group in $results | Group-Object ExpectedTask | Sort-Object Name) {
    $groupPassed = @($group.Group | Where-Object Passed).Count
    [pscustomobject]@{
        Task = $group.Name
        Passed = $groupPassed
        Samples = $group.Count
        AccuracyPercent = [Math]::Round($groupPassed * 100.0 / $group.Count, 2)
    }
}
$taskSummary | Format-Table -AutoSize

[pscustomobject]@{
    Samples = $results.Count
    Passed = $passed
    AccuracyPercent = $accuracy
    SemanticAccepted = $semanticAccepted
    SemanticAcceptedPercent = $semanticRate
    Errors = $errors
    OriginalFailureRecoveryPercent = $oldFailureRecovery
    ElapsedSeconds = [Math]::Round($elapsed.TotalSeconds, 2)
    CsvPath = $CsvPath
    ReportPath = $ReportPath
} | Format-List
