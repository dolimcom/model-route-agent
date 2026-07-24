param(
    [string]$OutputPath = "docs/semantic-router-medium-long-250.md"
)

$ErrorActionPreference = "Stop"

function Build-Questions {
    param(
        [string]$Task,
        [string[]]$BaseIntents
    )

    $mediumTemplates = @(
        '我前面看了不少背景信息，但这次真正需要你直接处理的是：{0}',
        '上下文里可能会提到别的话题，不过这次请只围绕这个明确请求回答：{0}',
        '为了避免被前面的说明带偏，我把这次的核心需求单独写在最后：{0}'
    )

    $longTemplates = @(
        '我这两天一直在同时整理课程任务、项目笔记和生活安排，脑子里有很多互相干扰的信息，所以提问会写得比较长。但这一次你不用处理所有背景，只需要抓住最后这个明确目标并直接回答：{0}。如果理解了，就不要扩展到别的方向。',
        '为了更接近真实用户输入，我把背景说完整一点：前面可能会出现学习、开发、写作、数据或计划这些词，可它们都只是铺垫，不应该改变你对当前任务类型的判断。真正决定这次路由的是下面这句明确请求，请只围绕它作答：{0}，其余内容都可以忽略。'
    )

    $questions = New-Object System.Collections.Generic.List[string]
    foreach ($intent in $BaseIntents) {
        foreach ($template in $mediumTemplates) {
            $questions.Add(([string]::Format($template, $intent)).Trim())
        }
        foreach ($template in $longTemplates) {
            $questions.Add(([string]::Format($template, $intent)).Trim())
        }
    }
    return $questions
}

$generalIntents = @(
    '请解释为什么人们在面对不确定信息时更容易依赖熟悉的判断框架。',
    '请比较长期目标和短期目标分别适合什么样的现实情境。',
    '请说明为什么同一个建议对不同的人可能会产生完全不同的效果。',
    '请分析公共图书馆除了借书之外还具有什么社会价值。',
    '请解释为什么确认偏误会影响一个人对信息真伪的判断。',
    '请客观评价短视频对知识获取效率和注意力分配的双重影响。',
    '请介绍开源协作为什么会对普通软件使用者也产生实际影响。',
    '请说明为什么礼貌表达并不一定代表赞同对方的观点。',
    '请解释面对失败经历时，复盘为什么比单纯自责更有价值。',
    '请比较线上讨论和面对面讨论在信息传递上的差异。'
)

$dailyIntents = @(
    '请帮我安排明天下午两点到六点之间的学习、吃饭和休息顺序。',
    '请把准备面试、复习课程和整理房间拆成今晚可以执行的步骤。',
    '请根据我上午精力更好、晚上容易疲惫的特点安排一天的任务顺序。',
    '请给我一份适合周日晚上快速查看的下周重点清单模板。',
    '请在我今天状态一般的前提下，安排一个低负担但能推进事情的晚上流程。',
    '请把买东西、取快递、打印材料和去教室这几件事排出先后顺序。',
    '请为一周内完成三项截止任务设计一个现实一点的推进节奏。',
    '请把旅行出发前一天晚上需要准备的事项整理成简洁清单。',
    '请为我设计一个三十分钟以内、尽量不费力的睡前整理流程。',
    '请安排一个包含复习、运动和独处时间的周六半日计划。'
)

$literaryIntents = @(
    '请把“雨后的操场空了下来”扩写成一段更有画面感的场景描写。',
    '请为一个关于旧车站重逢的短篇故事设计一个克制的开头。',
    '请把这句普通表达改得更含蓄一些，但保留原来的情绪强度。',
    '请分析一个角色不断看表这个细节可能暗示的心理状态。',
    '请为一家临河的小书店写一句简短但有记忆点的宣传语。',
    '请用第一人称写一段多年后回到故乡时复杂但克制的感受。',
    '请把“她很难过，但没有哭”改成展示而不是直接说明的写法。',
    '请为一个以“最后一班车已经开走”为起点的故事继续写一小段。',
    '请比较直接抒情和留白表达在情感描写上的不同效果。',
    '请把一段正式通知改得更有温度，但不要丢掉原本的正式感。'
)

$codingIntents = @(
    '请分析一个 Spring Boot 接口偶发返回 500 时应该优先排查哪些方向。',
    '请设计一个支持幂等提交的后端接口，避免重复请求创建多条记录。',
    '请说明为什么同一个类内部调用带事务注解的方法时事务可能不生效。',
    '请设计一个可回滚的文件修改链路，并说明执行前后应该记录哪些信息。',
    '请解释为什么用户传来的路径必须做规范化和授权目录边界校验。',
    '请分析数据库查询在数据量上来后变慢时应该从哪些方面定位。',
    '请设计一个统一异常处理机制，让接口错误响应结构保持一致。',
    '请说明为什么线程池已经存在时，请求仍然可能出现排队和堆积问题。',
    '请设计一个模型接入层，让不同供应商协议对上层业务透明。',
    '请解释为什么日志里不能直接打印访问令牌、原始路径和完整敏感请求体。'
)

$mathIntents = @(
    '请计算一个商品先涨价百分之二十再打九折之后，相当于原价的百分之多少。',
    '请求解方程 3x 加 7 等于 25，并给出清晰的计算过程。',
    '请计算从十个人中选出两个人合作一组，一共有多少种选法。',
    '请说明一个事件成功率为百分之八十时，连续两次都失败的概率是多少。',
    '请计算每天处理八十条记录，一周七天总共可以处理多少条。',
    '请求一个长为十二、宽为七的长方形的面积，以及周长分别是多少。',
    '请计算一辆车三小时行驶二百四十公里时，它的平均速度是多少。',
    '请求导函数 y 等于 x 的三次方减去 3x 在 x 等于 1 处的导数值。',
    '请计算五个红球三个蓝球的袋子里随机取一个球，取到红球的概率是多少。',
    '请说明连续抛三次硬币时，恰好出现两次正面的概率是多少。'
)

$sections = @(
    @{ Task = "GENERAL"; Questions = (Build-Questions -Task "GENERAL" -BaseIntents $generalIntents) }
    @{ Task = "DAILY"; Questions = (Build-Questions -Task "DAILY" -BaseIntents $dailyIntents) }
    @{ Task = "LITERARY"; Questions = (Build-Questions -Task "LITERARY" -BaseIntents $literaryIntents) }
    @{ Task = "CODING"; Questions = (Build-Questions -Task "CODING" -BaseIntents $codingIntents) }
    @{ Task = "MATH"; Questions = (Build-Questions -Task "MATH" -BaseIntents $mathIntents) }
)

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("# Semantic Router Medium/Long Length Dataset")
$lines.Add("")
$lines.Add("This dataset contains 250 single-turn questions focused on 50-100 character medium inputs and 100+ character long inputs.")
$lines.Add("")

$id = 1
foreach ($section in $sections) {
    $lines.Add("## $($section.Task)")
    $lines.Add("")
    foreach ($question in $section.Questions) {
        $lines.Add("$id. $question")
        $id++
    }
    $lines.Add("")
}

$fullPath = [IO.Path]::GetFullPath($OutputPath)
[IO.File]::WriteAllLines($fullPath, $lines, (New-Object System.Text.UTF8Encoding($false)))
Write-Output $fullPath
