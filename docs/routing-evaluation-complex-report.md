# Semantic Routing Evaluation Report

- Generated: 2026-07-16 21:10:29
- Samples: 680
- Passed: 612
- Accuracy: 90%
- Semantic accepted: 680 (100%)
- Request errors: 0
- Elapsed: 122.43 seconds
- Original failures fixed: 212/226 (93.81%)

## Accuracy By Task

| Expected task | Samples | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 136 | 121 | 88.97% |
| DAILY | 136 | 132 | 97.06% |
| GENERAL | 136 | 111 | 81.62% |
| LITERARY | 136 | 123 | 90.44% |
| MATH | 136 | 125 | 91.91% |

## Confusion Matrix

| Expected | Actual | Count |
|---|---|---:|
| CODING | CODING | 121 |
| CODING | DAILY | 6 |
| CODING | GENERAL | 3 |
| CODING | LITERARY | 1 |
| CODING | MATH | 5 |
| DAILY | CODING | 3 |
| DAILY | DAILY | 132 |
| DAILY | LITERARY | 1 |
| GENERAL | CODING | 5 |
| GENERAL | DAILY | 10 |
| GENERAL | GENERAL | 111 |
| GENERAL | LITERARY | 7 |
| GENERAL | MATH | 3 |
| LITERARY | CODING | 2 |
| LITERARY | DAILY | 3 |
| LITERARY | GENERAL | 7 |
| LITERARY | LITERARY | 123 |
| LITERARY | MATH | 1 |
| MATH | CODING | 6 |
| MATH | DAILY | 3 |
| MATH | GENERAL | 1 |
| MATH | LITERARY | 1 |
| MATH | MATH | 125 |

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
| 535 | GENERAL | CODING | 0.71 | 有人说“每天写代码十小时才算努力”，请评价这种观点，而不是分析代码。 |
| 536 | GENERAL | DAILY | 0.74 | 请区分“理解一个概念”和“记住一个答案”，不要给学习计划。 |
| 537 | GENERAL | MATH | 0.75 | 数学公式先放在一边，用直觉说明为什么概率不能预测单次结果。 |
| 538 | GENERAL | LITERARY | 0.73 | 这句话来自小说，但我不需要文学分析：现实生活中为什么怀旧会让人感到温暖？ |
| 541 | GENERAL | DAILY | 0.7 | 我不是要安排旅行，请说明旅行保险通常解决什么问题。 |
| 542 | GENERAL | CODING | 0.68 | “开源”“免费”“公开透明”是同一个概念吗？请辨析。 |
| 543 | GENERAL | DAILY | 0.72 | 对比线上课堂和线下课堂的学习体验，但不要为我制定课程表。 |
| 547 | GENERAL | LITERARY | 0.74 | What makes a source credible even when it confirms what we already believe? |
| 549 | GENERAL | DAILY | 0.73 | 解释 work-life balance 的含义，不要给具体时间安排。 |
| 550 | GENERAL | LITERARY | 0.72 | AI、小说和数学都可以先不谈，我只是想知道什么叫同理心。 |
| 551 | GENERAL | MATH | 0.67 | 请分析“选择越多越自由”这句话可能忽略了什么。 |
| 558 | GENERAL | MATH | 0.71 | 如果没有足够证据，怎样区分合理猜测和武断结论？ |
| 561 | DAILY | CODING | 0.75 | 我今晚既要修改 Java 作业、读一篇小说又要做两道题，请只安排顺序，不解决内容。 |
| 567 | DAILY | CODING | 0.71 | 我不需要你回答“什么是 REST”，只需要把阅读资料和练习安排到周末。 |
| 580 | DAILY | CODING | 0.72 | 帮我为一次三十分钟的线上汇报设计会前、会中、会后事项。 |
| 591 | LITERARY | CODING | 0.72 | 不要修复这段代码，把 `NullPointerException` 写成一个侦探故事的线索。 |
| 595 | LITERARY | GENERAL | 0.74 | 下面是事实信息，请保留事实但改成有温度的校史介绍：学校建于一九九八年。 |
| 597 | LITERARY | DAILY | 0.76 | 为一个每天按日程生活、某天突然偏离计划的人设计故事冲突。 |
| 600 | LITERARY | CODING | 0.72 | 用两个程序线程彼此等待的情景比喻一段僵持的关系。 |
| 604 | LITERARY | DAILY | 0.7 | 给下面的普通通知增加一点人情味，但仍保持正式：图书馆周日闭馆。 |
| 607 | LITERARY | GENERAL | 0.72 | 用环境变化暗示两位朋友已经无话可说，不要直接说明关系破裂。 |
| 608 | LITERARY | GENERAL | 0.72 | 让这句台词同时带有感谢和告别：“这段时间麻烦你了。” |
