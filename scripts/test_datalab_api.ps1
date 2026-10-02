# 测试 DataLab 完整接口闭环
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$loginBody = @{ username = "2020710101"; password = $env:LOCAL_STUDENT_PASSWORD } | ConvertTo-Json
$loginRes = Invoke-RestMethod -Uri "http://localhost:8080/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginRes.token

if (-not $token) {
    Write-Error "登录失败，未能获取 Token"
    exit 1
}
Write-Host "1. 学生登录成功, Token获取正常"

$headers = @{ "Authorization" = "Bearer $token" }

# 1. 验证题单概览
$overview = Invoke-RestMethod -Uri "http://localhost:8080/business/python-practice/student/overview" -Method Get -Headers $headers
Write-Host "2. 题单题目总数: $($overview.data.questions.Count)"
$targetQ = $overview.data.questions | Where-Object { $_.question_id -eq 999901 }
if ($targetQ) {
    Write-Host "   目标实验题找到: $($targetQ.question_title), practical_mode=$($targetQ.practical_mode)"
} else {
    Write-Host "   未找到 999901，检查班级或发布状态"
}

# 2. 验证获取实验配置
$config = Invoke-RestMethod -Uri "http://localhost:8080/business/dataLab/student/config/999901" -Method Get -Headers $headers
Write-Host "3. 实验题配置名称: $($config.data.experimentName), 数据文件名: $($config.data.excelFileName)"

# 3. 验证 Excel 下载
Invoke-WebRequest -Uri "http://localhost:8080/business/dataLab/student/excel/999901" -Headers $headers -OutFile "uploadPath/downloaded_sport.xlsx"
$dlFile = Get-Item "uploadPath/downloaded_sport.xlsx"
Write-Host "4. 下载的 Excel 文件大小: $($dlFile.Length) 字节 (预期约 43387 字节)"

# 4. 验证提交实验成果
$submitBody = @{
    questionId = 999901
    sourceType = "BASE_VERSION"
    sourceId = 1
    sourceCode = "import pandas as pd`nimport matplotlib.pyplot as plt`ndf = pd.read_excel('高中运动会成绩统计.xlsx')`nprint('数据行数:', len(df))"
    chartImageBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    outputText = "数据行数: 500`n[输出正常]"
} | ConvertTo-Json
$submitRes = Invoke-RestMethod -Uri "http://localhost:8080/business/dataLab/student/submit" -Method Post -Headers $headers -Body $submitBody -ContentType "application/json"
Write-Host "5. 提交实验成果响应码: $($submitRes.code), 提交ID: $($submitRes.data.submissionId), 图表路径: $($submitRes.data.chartImageUrl)"

# 5. 验证查询历史提交
$history = Invoke-RestMethod -Uri "http://localhost:8080/business/dataLab/student/submission?questionId=999901&sourceType=BASE_VERSION&sourceId=1" -Method Get -Headers $headers
Write-Host "6. 查询历史提交成功: submissionId=$($history.data.submissionId), studentId=$($history.data.studentId), chartImageUrl=$($history.data.chartImageUrl)"
