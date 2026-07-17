# Semantic Routing Evaluation Report

- Generated: 2026-07-16 23:45:06
- Samples: 680
- Passed: 655
- Accuracy: 96.32%
- Semantic accepted: 680 (100%)
- Request errors: 0
- Elapsed: 165.01 seconds
- Original failures fixed: 28/46 (60.87%)

## Accuracy By Task

| Expected task | Samples | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 136 | 129 | 94.85% |
| DAILY | 136 | 133 | 97.79% |
| GENERAL | 136 | 127 | 93.38% |
| LITERARY | 136 | 132 | 97.06% |
| MATH | 136 | 134 | 98.53% |

## Confusion Matrix

| Expected | Actual | Count |
|---|---|---:|
| CODING | CODING | 129 |
| CODING | DAILY | 3 |
| CODING | GENERAL | 2 |
| CODING | LITERARY | 1 |
| CODING | MATH | 1 |
| DAILY | CODING | 1 |
| DAILY | DAILY | 133 |
| DAILY | LITERARY | 1 |
| DAILY | MATH | 1 |
| GENERAL | CODING | 2 |
| GENERAL | DAILY | 5 |
| GENERAL | GENERAL | 127 |
| GENERAL | LITERARY | 2 |
| LITERARY | DAILY | 1 |
| LITERARY | GENERAL | 3 |
| LITERARY | LITERARY | 132 |
| MATH | CODING | 1 |
| MATH | DAILY | 1 |
| MATH | MATH | 134 |

## First 50 Misroutes

| ID | Expected | Actual | Confidence | Question |
|---:|---|---|---:|---|
| 10 | GENERAL | DAILY | 0.68 | 初次参加正式面试时应该注意哪些基本礼仪？ |
| 12 | GENERAL | CODING | 0.71 | 请说明开源软件通常是怎样运作的。 |
| 16 | GENERAL | DAILY | 0.72 | 请梳理一次有效沟通通常包含哪些环节。 |
| 80 | CODING | DAILY | 0.73 | 怎样为一个需要审批后才能执行的操作设计状态流转？ |
| 119 | GENERAL | LITERARY | 0.71 | 面对陌生人的批评时应该怎样理性回应？ |
| 127 | GENERAL | DAILY | 0.72 | 一次好的复盘应该回答哪些问题？ |
| 140 | GENERAL | DAILY | 0.72 | 怎样判断一个目标是否过于模糊？ |
| 174 | DAILY | LITERARY | 0.72 | 三天后需要交论文，现在只有标题，接下来怎么推进？ |
| 183 | DAILY | MATH | 0.77 | 一周读完一本三百页的书，每天大约读多少合适？ |
| 224 | LITERARY | GENERAL | 0.75 | 把产品介绍改得有温度，但不要夸张。 |
| 235 | LITERARY | GENERAL | 0.75 | 分析留白为什么能增强某些情感表达。 |
| 276 | CODING | GENERAL | 0.75 | 为什么浮点数不适合直接表示金额？ |
| 284 | CODING | DAILY | 0.72 | 怎样在不重启服务的情况下重新加载配置？ |
| 355 | GENERAL | CODING | 0.73 | Java 是什么？只做面向普通人的概念介绍，不分析程序。 |
| 371 | CODING | MATH | 0.78 | 如何用程序计算刚才那个概率问题？ |
| 373 | CODING | LITERARY | 0.71 | 这不是语言润色问题，我需要修复字符串替换后的乱码。 |
| 450 | CODING | GENERAL | 0.72 | 怎样记录外部模型请求耗时又不泄露请求内容？ |
| 485 | GENERAL | LITERARY | 0.71 | 对一个观点保持开放态度意味着什么？ |
| 543 | GENERAL | DAILY | 0.71 | 对比线上课堂和线下课堂的学习体验，但不要为我制定课程表。 |
| 564 | DAILY | CODING | 0.74 | 我有数学考试、代码提交和社团海报三个截止任务，未来四天怎么分配？ |
| 595 | LITERARY | GENERAL | 0.71 | 下面是事实信息，请保留事实但改成有温度的校史介绍：学校建于一九九八年。 |
| 604 | LITERARY | DAILY | 0.69 | 给下面的普通通知增加一点人情味，但仍保持正式：图书馆周日闭馆。 |
| 649 | CODING | DAILY | 0.81 | 不要给时间安排，我需要实现一个按优先级和截止时间排序待办事项的 comparator。 |
| 653 | MATH | CODING | 0.75 | 某线程池有八个线程，每个每秒处理五个任务，理想情况下十秒处理多少个？ |
| 662 | MATH | DAILY | 0.71 | 某任务先完成三分之一，又完成剩余部分的一半，现在完成了总量的几分之几？ |
