# Semantic Routing Evaluation Report

- Generated: 2026-07-16 20:53:51
- Samples: 530
- Passed: 502
- Accuracy: 94.72%
- Semantic accepted: 530 (100%)
- Request errors: 0
- Elapsed: 107.39 seconds
- Original failures fixed: 212/226 (93.81%)

## Accuracy By Task

| Expected task | Samples | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 106 | 98 | 92.45% |
| DAILY | 106 | 105 | 99.06% |
| GENERAL | 106 | 93 | 87.74% |
| LITERARY | 106 | 102 | 96.23% |
| MATH | 106 | 104 | 98.11% |

## Confusion Matrix

| Expected | Actual | Count |
|---|---|---:|
| CODING | CODING | 98 |
| CODING | DAILY | 2 |
| CODING | GENERAL | 2 |
| CODING | LITERARY | 1 |
| CODING | MATH | 3 |
| DAILY | DAILY | 105 |
| DAILY | LITERARY | 1 |
| GENERAL | CODING | 3 |
| GENERAL | DAILY | 6 |
| GENERAL | GENERAL | 93 |
| GENERAL | LITERARY | 4 |
| LITERARY | DAILY | 1 |
| LITERARY | GENERAL | 2 |
| LITERARY | LITERARY | 102 |
| LITERARY | MATH | 1 |
| MATH | DAILY | 1 |
| MATH | GENERAL | 1 |
| MATH | MATH | 104 |

## First 50 Misroutes

| ID | Expected | Actual | Confidence | Question |
|---:|---|---|---:|---|
| 8 | GENERAL | CODING | 0.71 | 如何判断一条网络信息是否值得相信？ |
| 10 | GENERAL | DAILY | 0.7 | 初次参加正式面试时应该注意哪些基本礼仪？ |
| 12 | GENERAL | CODING | 0.71 | 请说明开源软件通常是怎样运作的。 |
| 20 | GENERAL | DAILY | 0.7 | 如果只能保留一个好习惯，你认为哪个最值得保留？ |
| 75 | CODING | GENERAL | 0.68 | 请说明乐观锁和悲观锁分别适合什么场景。 |
| 127 | GENERAL | DAILY | 0.72 | 一次好的复盘应该回答哪些问题？ |
| 137 | GENERAL | CODING | 0.69 | 如何看待失败经历对个人成长的作用？ |
| 140 | GENERAL | DAILY | 0.74 | 怎样判断一个目标是否过于模糊？ |
| 174 | DAILY | LITERARY | 0.72 | 三天后需要交论文，现在只有标题，接下来怎么推进？ |
| 224 | LITERARY | GENERAL | 0.75 | 把产品介绍改得有温度，但不要夸张。 |
| 235 | LITERARY | GENERAL | 0.75 | 分析留白为什么能增强某些情感表达。 |
| 260 | CODING | MATH | 0.75 | 为什么在构造函数里执行耗时网络请求会影响应用启动？ |
| 276 | CODING | GENERAL | 0.73 | 为什么浮点数不适合直接表示金额？ |
| 284 | CODING | DAILY | 0.72 | 怎样在不重启服务的情况下重新加载配置？ |
| 354 | GENERAL | DAILY | 0.74 | 我不需要日程，只想知道为什么人会拖延。 |
| 366 | LITERARY | MATH | 0.7 | 不要修复下面的程序，只为它写一段幽默旁白。 |
| 367 | LITERARY | DAILY | 0.7 | 将“按时完成所有事项”改成适合海报的句子。 |
| 371 | CODING | MATH | 0.79 | 如何用程序计算刚才那个概率问题？ |
| 373 | CODING | LITERARY | 0.71 | 这不是语言润色问题，我需要修复字符串替换后的乱码。 |
| 375 | MATH | DAILY | 0.75 | 不需要安排复习，请直接算每天完成二十页需要几天读完三百页。 |
| 377 | MATH | GENERAL | 0.76 | 不要润色题目，计算两种方案的成本差额。 |
| 384 | GENERAL | LITERARY | 0.7 | 一场有效的公开讨论需要遵守哪些基本原则？ |
| 396 | GENERAL | DAILY | 0.7 | What is the difference between being busy and being productive? |
| 399 | GENERAL | LITERARY | 0.69 | 如果没有明确答案，应该怎样表达不确定性？ |
| 450 | CODING | MATH | 0.71 | 怎样记录外部模型请求耗时又不泄露请求内容？ |
| 485 | GENERAL | LITERARY | 0.71 | 对一个观点保持开放态度意味着什么？ |
| 486 | GENERAL | LITERARY | 0.71 | 请比较口头沟通和书面沟通的特点。 |
| 520 | CODING | DAILY | 0.72 | 直接给出排查 CPU 持续满载的步骤。 |
