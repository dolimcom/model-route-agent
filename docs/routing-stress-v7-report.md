# Semantic Routing Evaluation Report

- Generated: 2026-07-16 23:50:25
- Samples: 1000
- Passed: 938
- Accuracy: 93.8%
- Semantic accepted: 1000 (100%)
- Request errors: 0
- Elapsed: 254.16 seconds

## Accuracy By Task

| Expected task | Samples | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 200 | 175 | 87.5% |
| DAILY | 200 | 200 | 100% |
| GENERAL | 200 | 193 | 96.5% |
| LITERARY | 200 | 188 | 94% |
| MATH | 200 | 182 | 91% |

## Confusion Matrix

| Expected | Actual | Count |
|---|---|---:|
| CODING | CODING | 175 |
| CODING | DAILY | 1 |
| CODING | GENERAL | 8 |
| CODING | MATH | 16 |
| DAILY | DAILY | 200 |
| GENERAL | CODING | 1 |
| GENERAL | DAILY | 3 |
| GENERAL | GENERAL | 193 |
| GENERAL | LITERARY | 3 |
| LITERARY | DAILY | 1 |
| LITERARY | GENERAL | 11 |
| LITERARY | LITERARY | 188 |
| MATH | CODING | 8 |
| MATH | DAILY | 9 |
| MATH | GENERAL | 1 |
| MATH | MATH | 182 |

## First 50 Misroutes

| ID | Expected | Actual | Confidence | Question |
|---:|---|---|---:|---|
| 66 | GENERAL | LITERARY | 0.7 | 请只处理最后这个请求：比较口头承诺和书面承诺的特点。 |
| 74 | GENERAL | DAILY | 0.73 | 请只处理最后这个请求：为什么人在熟悉环境中更容易放松？ |
| 75 | GENERAL | DAILY | 0.72 | 有人刚才讨论了 Java 代码、小说、日程和概率，不过我现在只想问：为什么人在熟悉环境中更容易放松？ |
| 110 | GENERAL | LITERARY | 0.74 | 请在三句话内完成下面要求，不要扩展到其他领域：说明规律生活为什么可能改善精神状态，但不要制定作息。 |
| 162 | GENERAL | CODING | 0.73 | 请只处理最后这个请求：评价效率和公平发生冲突时需要考虑什么。 |
| 182 | GENERAL | LITERARY | 0.74 | 请在三句话内完成下面要求，不要扩展到其他领域：说明复盘的价值，但不要给出执行清单。 |
| 186 | GENERAL | DAILY | 0.71 | 请只处理最后这个请求：比较拥有更多选择和更容易做决定之间的关系。 |
| 471 | LITERARY | GENERAL | 0.71 | This is the actual request, unrelated to the quoted examples: 为一句感谢的话增加隐约的告别意味。 |
| 473 | LITERARY | GENERAL | 0.72 | 分析反复整理衣领这个动作可以表现人物怎样的心理。 |
| 474 | LITERARY | GENERAL | 0.72 | 请只处理最后这个请求：分析反复整理衣领这个动作可以表现人物怎样的心理。 |
| 475 | LITERARY | GENERAL | 0.73 | 有人刚才讨论了 Java 代码、小说、日程和概率，不过我现在只想问：分析反复整理衣领这个动作可以表现人物怎样的心理。 |
| 476 | LITERARY | GENERAL | 0.73 | 不要被前面的技术名词和数字干扰，实际任务是：分析反复整理衣领这个动作可以表现人物怎样的心理。 |
| 477 | LITERARY | GENERAL | 0.72 | 上文是一段程序和数学公式；换个任务：分析反复整理衣领这个动作可以表现人物怎样的心理。 |
| 478 | LITERARY | GENERAL | 0.72 | 请在三句话内完成下面要求，不要扩展到其他领域：分析反复整理衣领这个动作可以表现人物怎样的心理。 |
| 479 | LITERARY | GENERAL | 0.72 | This is the actual request, unrelated to the quoted examples: 分析反复整理衣领这个动作可以表现人物怎样的心理。 |
| 480 | LITERARY | GENERAL | 0.73 | 先忽略日程、代码、文学和计算示例，真正的问题是：分析反复整理衣领这个动作可以表现人物怎样的心理。 |
| 511 | LITERARY | GENERAL | 0.69 | This is the actual request, unrelated to the quoted examples: 用第一人称写多年后回到海边小镇的感受。 |
| 559 | LITERARY | GENERAL | 0.7 | This is the actual request, unrelated to the quoted examples: 为一家深夜书店取三个有意境的名字。 |
| 567 | LITERARY | DAILY | 0.73 | This is the actual request, unrelated to the quoted examples: 把会议日程作为背景写一个微型故事。 |
| 631 | CODING | DAILY | 0.73 | This is the actual request, unrelated to the quoted examples: 为提醒文本实现日期时间解析的服务层。 |
| 689 | CODING | MATH | 0.72 | 定位 CPU 长时间满载但请求量不高的问题。 |
| 690 | CODING | MATH | 0.72 | 请只处理最后这个请求：定位 CPU 长时间满载但请求量不高的问题。 |
| 691 | CODING | MATH | 0.72 | 有人刚才讨论了 Java 代码、小说、日程和概率，不过我现在只想问：定位 CPU 长时间满载但请求量不高的问题。 |
| 692 | CODING | MATH | 0.72 | 不要被前面的技术名词和数字干扰，实际任务是：定位 CPU 长时间满载但请求量不高的问题。 |
| 693 | CODING | MATH | 0.72 | 上文是一段程序和数学公式；换个任务：定位 CPU 长时间满载但请求量不高的问题。 |
| 694 | CODING | MATH | 0.72 | 请在三句话内完成下面要求，不要扩展到其他领域：定位 CPU 长时间满载但请求量不高的问题。 |
| 695 | CODING | MATH | 0.72 | This is the actual request, unrelated to the quoted examples: 定位 CPU 长时间满载但请求量不高的问题。 |
| 696 | CODING | MATH | 0.72 | 先忽略日程、代码、文学和计算示例，真正的问题是：定位 CPU 长时间满载但请求量不高的问题。 |
| 713 | CODING | MATH | 0.72 | 为什么构造函数中发送网络请求会拖慢服务启动？ |
| 714 | CODING | MATH | 0.72 | 请只处理最后这个请求：为什么构造函数中发送网络请求会拖慢服务启动？ |
| 715 | CODING | MATH | 0.72 | 有人刚才讨论了 Java 代码、小说、日程和概率，不过我现在只想问：为什么构造函数中发送网络请求会拖慢服务启动？ |
| 716 | CODING | MATH | 0.72 | 不要被前面的技术名词和数字干扰，实际任务是：为什么构造函数中发送网络请求会拖慢服务启动？ |
| 717 | CODING | MATH | 0.72 | 上文是一段程序和数学公式；换个任务：为什么构造函数中发送网络请求会拖慢服务启动？ |
| 718 | CODING | MATH | 0.72 | 请在三句话内完成下面要求，不要扩展到其他领域：为什么构造函数中发送网络请求会拖慢服务启动？ |
| 719 | CODING | MATH | 0.72 | This is the actual request, unrelated to the quoted examples: 为什么构造函数中发送网络请求会拖慢服务启动？ |
| 720 | CODING | MATH | 0.72 | 先忽略日程、代码、文学和计算示例，真正的问题是：为什么构造函数中发送网络请求会拖慢服务启动？ |
| 745 | CODING | GENERAL | 0.72 | 解释乐观锁与悲观锁在库存更新中的取舍。 |
| 746 | CODING | GENERAL | 0.71 | 请只处理最后这个请求：解释乐观锁与悲观锁在库存更新中的取舍。 |
| 747 | CODING | GENERAL | 0.72 | 有人刚才讨论了 Java 代码、小说、日程和概率，不过我现在只想问：解释乐观锁与悲观锁在库存更新中的取舍。 |
| 748 | CODING | GENERAL | 0.72 | 不要被前面的技术名词和数字干扰，实际任务是：解释乐观锁与悲观锁在库存更新中的取舍。 |
| 749 | CODING | GENERAL | 0.72 | 上文是一段程序和数学公式；换个任务：解释乐观锁与悲观锁在库存更新中的取舍。 |
| 750 | CODING | GENERAL | 0.72 | 请在三句话内完成下面要求，不要扩展到其他领域：解释乐观锁与悲观锁在库存更新中的取舍。 |
| 751 | CODING | GENERAL | 0.74 | This is the actual request, unrelated to the quoted examples: 解释乐观锁与悲观锁在库存更新中的取舍。 |
| 752 | CODING | GENERAL | 0.72 | 先忽略日程、代码、文学和计算示例，真正的问题是：解释乐观锁与悲观锁在库存更新中的取舍。 |
| 801 | MATH | CODING | 0.77 | 线程池有十二个线程，每个每秒处理四项任务，五秒共处理多少项？ |
| 802 | MATH | CODING | 0.76 | 请只处理最后这个请求：线程池有十二个线程，每个每秒处理四项任务，五秒共处理多少项？ |
| 803 | MATH | CODING | 0.76 | 有人刚才讨论了 Java 代码、小说、日程和概率，不过我现在只想问：线程池有十二个线程，每个每秒处理四项任务，五秒共处理多少项？ |
| 804 | MATH | CODING | 0.76 | 不要被前面的技术名词和数字干扰，实际任务是：线程池有十二个线程，每个每秒处理四项任务，五秒共处理多少项？ |
| 805 | MATH | CODING | 0.77 | 上文是一段程序和数学公式；换个任务：线程池有十二个线程，每个每秒处理四项任务，五秒共处理多少项？ |
| 806 | MATH | CODING | 0.77 | 请在三句话内完成下面要求，不要扩展到其他领域：线程池有十二个线程，每个每秒处理四项任务，五秒共处理多少项？ |
