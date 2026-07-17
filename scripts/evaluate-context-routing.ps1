param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$DatasetPath = "docs/semantic-router-test-questions.md",
    [string]$CsvPath = "docs/context-routing-evaluation-results.csv",
    [string]$ReportPath = "docs/context-routing-evaluation-report.md",
    [int]$MinScenario = 1
)

$ErrorActionPreference = "Stop"
$scenarios = New-Object System.Collections.Generic.List[object]
foreach ($line in Get-Content -LiteralPath $DatasetPath) {
    if ($line -match '^\| (C\d{2}) \| ([A-Z]+) \| (.+?) \| (.+?) \| ([A-Z]+) \|$') {
        if ([int]$Matches[1].Substring(1) -lt $MinScenario) {
            continue
        }
        $scenarios.Add([pscustomobject]@{
            Id = $Matches[1]
            SeedExpectedTask = $Matches[2]
            SeedQuestion = $Matches[3].Trim()
            FollowupQuestion = $Matches[4].Trim()
            FollowupExpectedTask = $Matches[5]
        })
    }
}
if ($scenarios.Count -eq 0) {
    throw "No context scenarios found in $DatasetPath"
}

$results = foreach ($scenario in $scenarios) {
    try {
        $conversationBody = @{ title = "Routing evaluation $($scenario.Id)" } | ConvertTo-Json -Compress
        $conversation = Invoke-RestMethod `
            -Uri "$BaseUrl/api/conversations" `
            -Method Post `
            -ContentType "application/json; charset=utf-8" `
            -Body $conversationBody `
            -TimeoutSec 30

        $seedBody = @{
            question = $scenario.SeedQuestion
            conversationId = $conversation.id
        } | ConvertTo-Json -Compress
        $seed = $null
        for ($attempt = 1; $attempt -le 3; $attempt++) {
            try {
                $seed = Invoke-RestMethod `
                    -Uri "$BaseUrl/api/agent/chat" `
                    -Method Post `
                    -ContentType "application/json; charset=utf-8" `
                    -Body $seedBody `
                    -TimeoutSec 90
                break
            } catch {
                if ($attempt -eq 3) {
                    throw
                }
                Start-Sleep -Seconds 1
            }
        }

        $followupBody = @{
            question = $scenario.FollowupQuestion
            conversationId = $conversation.id
        } | ConvertTo-Json -Compress
        $followup = Invoke-RestMethod `
            -Uri "$BaseUrl/api/agent/route" `
            -Method Post `
            -ContentType "application/json; charset=utf-8" `
            -Body $followupBody `
            -TimeoutSec 60

        [pscustomobject]@{
            Id = $scenario.Id
            ConversationId = $conversation.id
            SeedExpectedTask = $scenario.SeedExpectedTask
            SeedActualTask = [string]$seed.route.taskType
            SeedPassed = ([string]$seed.route.taskType -eq $scenario.SeedExpectedTask)
            FollowupExpectedTask = $scenario.FollowupExpectedTask
            FollowupActualTask = [string]$followup.taskType
            FollowupModel = [string]$followup.modelId
            FollowupPassed = ([string]$followup.taskType -eq $scenario.FollowupExpectedTask)
            FollowupConfidence = [double]$followup.confidence
            FollowupReason = [string]$followup.reason
            SeedQuestion = $scenario.SeedQuestion
            FollowupQuestion = $scenario.FollowupQuestion
            Error = ""
        }
    } catch {
        [pscustomobject]@{
            Id = $scenario.Id
            ConversationId = ""
            SeedExpectedTask = $scenario.SeedExpectedTask
            SeedActualTask = "ERROR"
            SeedPassed = $false
            FollowupExpectedTask = $scenario.FollowupExpectedTask
            FollowupActualTask = "ERROR"
            FollowupModel = "ERROR"
            FollowupPassed = $false
            FollowupConfidence = 0.0
            FollowupReason = ""
            SeedQuestion = $scenario.SeedQuestion
            FollowupQuestion = $scenario.FollowupQuestion
            Error = $_.Exception.Message
        }
    }
}

$results | Export-Csv -LiteralPath $CsvPath -NoTypeInformation -Encoding UTF8
$seedPassed = @($results | Where-Object SeedPassed).Count
$followupPassed = @($results | Where-Object FollowupPassed).Count
$seedAccuracy = [Math]::Round($seedPassed * 100.0 / $results.Count, 2)
$followupAccuracy = [Math]::Round($followupPassed * 100.0 / $results.Count, 2)

$report = New-Object System.Collections.Generic.List[string]
$report.Add("# 上下文感知路由评估")
$report.Add("")
$report.Add("- 场景数：$($results.Count)")
$report.Add("- 种子问题路由准确率：$seedPassed/$($results.Count)（$seedAccuracy%）")
$report.Add("- 追问路由准确率：$followupPassed/$($results.Count)（$followupAccuracy%）")
$report.Add("")
$report.Add("## 按预期任务统计追问准确率")
$report.Add("")
$report.Add("| 任务 | 场景数 | 通过数 | 准确率 |")
$report.Add("|---|---:|---:|---:|")
foreach ($group in $results | Group-Object FollowupExpectedTask | Sort-Object Name) {
    $passed = @($group.Group | Where-Object FollowupPassed).Count
    $accuracy = [Math]::Round($passed * 100.0 / $group.Count, 2)
    $report.Add("| $($group.Name) | $($group.Count) | $passed | $accuracy% |")
}
$report.Add("")
$report.Add("## 场景结果")
$report.Add("")
$report.Add("| ID | 种子预期/实际 | 追问预期/实际 | 是否通过 |")
$report.Add("|---|---|---|---|")
foreach ($result in $results) {
    $report.Add("| $($result.Id) | $($result.SeedExpectedTask)/$($result.SeedActualTask) | $($result.FollowupExpectedTask)/$($result.FollowupActualTask) | $($result.FollowupPassed) |")
}
$report.Add("")
$report.Add("---")
$report.Add("")
$report.Add("# Context-Aware Routing Evaluation")
$report.Add("")
$report.Add("> English Version")
$report.Add("")
$report.Add("- Scenarios: $($results.Count)")
$report.Add("- Seed route accuracy: $seedPassed/$($results.Count) ($seedAccuracy%)")
$report.Add("- Follow-up route accuracy: $followupPassed/$($results.Count) ($followupAccuracy%)")
$report.Add("")
$report.Add("## Follow-Up Accuracy By Expected Task")
$report.Add("")
$report.Add("| Task | Scenarios | Passed | Accuracy |")
$report.Add("|---|---:|---:|---:|")
foreach ($group in $results | Group-Object FollowupExpectedTask | Sort-Object Name) {
    $passed = @($group.Group | Where-Object FollowupPassed).Count
    $accuracy = [Math]::Round($passed * 100.0 / $group.Count, 2)
    $report.Add("| $($group.Name) | $($group.Count) | $passed | $accuracy% |")
}
$report.Add("")
$report.Add("## Scenario Results")
$report.Add("")
$report.Add("| ID | Seed expected/actual | Follow-up expected/actual | Passed |")
$report.Add("|---|---|---|---|")
foreach ($result in $results) {
    $report.Add("| $($result.Id) | $($result.SeedExpectedTask)/$($result.SeedActualTask) | $($result.FollowupExpectedTask)/$($result.FollowupActualTask) | $($result.FollowupPassed) |")
}
[IO.File]::WriteAllLines(
    [IO.Path]::GetFullPath($ReportPath),
    $report,
    (New-Object System.Text.UTF8Encoding($false)))

$taskSummary = foreach ($group in $results | Group-Object FollowupExpectedTask | Sort-Object Name) {
    $passed = @($group.Group | Where-Object FollowupPassed).Count
    [pscustomobject]@{
        Task = $group.Name
        Passed = $passed
        Scenarios = $group.Count
        AccuracyPercent = [Math]::Round($passed * 100.0 / $group.Count, 2)
    }
}
$taskSummary | Format-Table -AutoSize

[pscustomobject]@{
    Scenarios = $results.Count
    SeedAccuracyPercent = $seedAccuracy
    FollowupAccuracyPercent = $followupAccuracy
    CsvPath = $CsvPath
    ReportPath = $ReportPath
} | Format-List
