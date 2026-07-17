# Semantic Routing Evaluation Report

- Generated: 2026-07-16 23:10:16
- Samples: 680
- Passed: 634
- Accuracy: 93.24%
- Semantic accepted: 680 (100%)
- Request errors: 0
- Elapsed: 164.33 seconds
- Original failures fixed: 27/68 (39.71%)

## Accuracy By Task

| Expected task | Samples | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 136 | 121 | 88.97% |
| DAILY | 136 | 133 | 97.79% |
| GENERAL | 136 | 122 | 89.71% |
| LITERARY | 136 | 129 | 94.85% |
| MATH | 136 | 129 | 94.85% |

## Confusion Matrix

| Expected | Actual | Count |
|---|---|---:|
| CODING | CODING | 121 |
| CODING | DAILY | 5 |
| CODING | GENERAL | 4 |
| CODING | LITERARY | 1 |
| CODING | MATH | 5 |
| DAILY | CODING | 1 |
| DAILY | DAILY | 133 |
| DAILY | GENERAL | 1 |
| DAILY | LITERARY | 1 |
| GENERAL | CODING | 2 |
| GENERAL | DAILY | 6 |
| GENERAL | GENERAL | 122 |
| GENERAL | LITERARY | 5 |
| GENERAL | MATH | 1 |
| LITERARY | DAILY | 3 |
| LITERARY | GENERAL | 4 |
| LITERARY | LITERARY | 129 |
| MATH | CODING | 3 |
| MATH | DAILY | 3 |
| MATH | LITERARY | 1 |
| MATH | MATH | 129 |

## First 50 Misroutes

| ID | Expected | Actual | Confidence | Question |
|---:|---|---|---:|---|
| 10 | GENERAL | DAILY | 0.7 | 初次参加正式面试时应该注意哪些基本礼仪？ |
| 12 | GENERAL | CODING | 0.71 | 请说明开源软件通常是怎样运作的。 |
| 20 | GENERAL | DAILY | 0.7 | 如果只能保留一个好习惯，你认为哪个最值得保留？ |
| 75 | CODING | GENERAL | 0.68 | 请说明乐观锁和悲观锁分别适合什么场景。 |
| 127 | GENERAL | DAILY | 0.72 | 一次好的复盘应该回答哪些问题？ |
| 137 | GENERAL | CODING | 0.69 | 如何看待失败经历对个人成长的作用？ |
| 140 | GENERAL | DAILY | 0.72 | 怎样判断一个目标是否过于模糊？ |
| 150 | GENERAL | LITERARY | 0.68 | 只回复一句话：什么叫长期主义？ |
| 174 | DAILY | LITERARY | 0.72 | 三天后需要交论文，现在只有标题，接下来怎么推进？ |
| 224 | LITERARY | GENERAL | 0.75 | 把产品介绍改得有温度，但不要夸张。 |
| 235 | LITERARY | GENERAL | 0.75 | 分析留白为什么能增强某些情感表达。 |
| 252 | CODING | MATH | 0.71 | 如何保证重复提交同一个请求不会创建两条数据？ |
| 275 | CODING | MATH | 0.72 | 如何测试并发请求下的自增序列是否唯一？ |
| 276 | CODING | GENERAL | 0.73 | 为什么浮点数不适合直接表示金额？ |
| 284 | CODING | DAILY | 0.72 | 怎样在不重启服务的情况下重新加载配置？ |
| 367 | LITERARY | DAILY | 0.7 | 将“按时完成所有事项”改成适合海报的句子。 |
| 371 | CODING | MATH | 0.79 | 如何用程序计算刚才那个概率问题？ |
| 373 | CODING | LITERARY | 0.71 | 这不是语言润色问题，我需要修复字符串替换后的乱码。 |
| 375 | MATH | DAILY | 0.71 | 不需要安排复习，请直接算每天完成二十页需要几天读完三百页。 |
| 384 | GENERAL | LITERARY | 0.7 | 一场有效的公开讨论需要遵守哪些基本原则？ |
| 450 | CODING | GENERAL | 0.72 | 怎样记录外部模型请求耗时又不泄露请求内容？ |
| 485 | GENERAL | LITERARY | 0.71 | 对一个观点保持开放态度意味着什么？ |
| 486 | GENERAL | LITERARY | 0.71 | 请比较口头沟通和书面沟通的特点。 |
| 518 | CODING | MATH | 0.73 | 怎样测试两个并发审批请求只能成功一个？ |
| 520 | CODING | DAILY | 0.72 | 直接给出排查 CPU 持续满载的步骤。 |
| 537 | GENERAL | MATH | 0.73 | 数学公式先放在一边，用直觉说明为什么概率不能预测单次结果。 |
| 541 | GENERAL | DAILY | 0.69 | 我不是要安排旅行，请说明旅行保险通常解决什么问题。 |
| 543 | GENERAL | DAILY | 0.72 | 对比线上课堂和线下课堂的学习体验，但不要为我制定课程表。 |
| 547 | GENERAL | LITERARY | 0.72 | What makes a source credible even when it confirms what we already believe? |
| 571 | DAILY | GENERAL | 0.72 | 今天身体不舒服，但有两个必须交的任务，请给最低负担版本。 |
| 580 | DAILY | CODING | 0.72 | 帮我为一次三十分钟的线上汇报设计会前、会中、会后事项。 |
| 595 | LITERARY | GENERAL | 0.73 | 下面是事实信息，请保留事实但改成有温度的校史介绍：学校建于一九九八年。 |
| 597 | LITERARY | DAILY | 0.76 | 为一个每天按日程生活、某天突然偏离计划的人设计故事冲突。 |
| 604 | LITERARY | DAILY | 0.7 | 给下面的普通通知增加一点人情味，但仍保持正式：图书馆周日闭馆。 |
| 619 | LITERARY | GENERAL | 0.72 | 为儿童读者解释离别，使用故事而不是抽象说理。 |
| 625 | CODING | MATH | 0.75 | 我需要用程序求方程，但不要手算；请给出 Java 中的数值求解思路。 |
| 627 | CODING | DAILY | 0.73 | 两个审批请求几乎同时到达，都读到 PENDING，怎样保证只有一个执行成功？ |
| 636 | CODING | GENERAL | 0.7 | 为什么使用 `parallelStream()` 后速度反而下降，应该测量哪些指标？ |
| 643 | CODING | DAILY | 0.78 | “提醒我明天开会”是输入样例，请实现解析提醒时间的服务层结构。 |
| 649 | CODING | DAILY | 0.83 | 不要给时间安排，我需要实现一个按优先级和截止时间排序待办事项的 comparator。 |
| 653 | MATH | CODING | 0.75 | 某线程池有八个线程，每个每秒处理五个任务，理想情况下十秒处理多少个？ |
| 654 | MATH | CODING | 0.76 | 一个数据库表有两万行，删除四分之一后还剩多少行？ |
| 655 | MATH | DAILY | 0.71 | 我不是要设计日程：每天读三十五页，四百二十页需要几天？ |
| 657 | MATH | LITERARY | 0.74 | 一个故事有五个角色，每两人之间安排一次对话，共需要多少组对话？ |
| 660 | MATH | CODING | 0.73 | 缓存命中率从百分之六十提高到百分之七十五，提升了多少个百分点？ |
| 663 | MATH | DAILY | 0.67 | 一个圆形操场周长为 100π 米，半径是多少？ |
