# Semantic Routing Evaluation Report

- Generated: 2026-07-16 20:40:09
- Samples: 480
- Passed: 428
- Accuracy: 89.17%
- Semantic accepted: 480 (100%)
- Request errors: 0
- Elapsed: 95.82 seconds
- Original failures fixed: 193/226 (85.4%)

## Accuracy By Task

| Expected task | Samples | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 96 | 76 | 79.17% |
| DAILY | 96 | 95 | 98.96% |
| GENERAL | 96 | 84 | 87.5% |
| LITERARY | 96 | 85 | 88.54% |
| MATH | 96 | 88 | 91.67% |

## Confusion Matrix

| Expected | Actual | Count |
|---|---|---:|
| CODING | CODING | 76 |
| CODING | DAILY | 7 |
| CODING | GENERAL | 5 |
| CODING | LITERARY | 1 |
| CODING | MATH | 7 |
| DAILY | DAILY | 95 |
| DAILY | LITERARY | 1 |
| GENERAL | CODING | 2 |
| GENERAL | DAILY | 7 |
| GENERAL | GENERAL | 84 |
| GENERAL | LITERARY | 2 |
| GENERAL | MATH | 1 |
| LITERARY | CODING | 2 |
| LITERARY | DAILY | 1 |
| LITERARY | GENERAL | 7 |
| LITERARY | LITERARY | 85 |
| LITERARY | MATH | 1 |
| MATH | DAILY | 2 |
| MATH | GENERAL | 6 |
| MATH | MATH | 88 |

## First 50 Misroutes

| ID | Expected | Actual | Confidence | Question |
|---:|---|---|---:|---|
| 10 | GENERAL | DAILY | 0.7 | 初次参加正式面试时应该注意哪些基本礼仪？ |
| 12 | GENERAL | CODING | 0.72 | 请说明开源软件通常是怎样运作的。 |
| 20 | GENERAL | DAILY | 0.71 | 如果只能保留一个好习惯，你认为哪个最值得保留？ |
| 55 | LITERARY | GENERAL | 0.73 | 解释一个故事采用倒叙开场可能产生什么效果。 |
| 60 | LITERARY | GENERAL | 0.72 | 分析一个角色反复看表这一细节可以暗示什么。 |
| 69 | CODING | DAILY | 0.72 | 如何设计可回滚的文件修改操作？ |
| 74 | CODING | MATH | 0.71 | 一个事务方法内部调用另一个事务方法时可能遇到什么问题？ |
| 75 | CODING | GENERAL | 0.68 | 请说明乐观锁和悲观锁分别适合什么场景。 |
| 96 | MATH | GENERAL | 0.71 | 解释为什么任意奇数的平方仍然是奇数。 |
| 101 | GENERAL | DAILY | 0.74 | 为什么有些人更喜欢在安静环境中工作？ |
| 127 | GENERAL | DAILY | 0.72 | 一次好的复盘应该回答哪些问题？ |
| 140 | GENERAL | DAILY | 0.74 | 怎样判断一个目标是否过于模糊？ |
| 174 | DAILY | LITERARY | 0.72 | 三天后需要交论文，现在只有标题，接下来怎么推进？ |
| 213 | LITERARY | GENERAL | 0.71 | 把“他很生气”改成展示而非告知的写法。 |
| 216 | LITERARY | GENERAL | 0.71 | 评价一个故事中重复出现钟声可能具有的意义。 |
| 224 | LITERARY | GENERAL | 0.75 | 把产品介绍改得有温度，但不要夸张。 |
| 235 | LITERARY | GENERAL | 0.77 | 分析留白为什么能增强某些情感表达。 |
| 260 | CODING | MATH | 0.75 | 为什么在构造函数里执行耗时网络请求会影响应用启动？ |
| 261 | CODING | DAILY | 0.76 | 怎样实现聊天记录按时间稳定排序？ |
| 267 | CODING | DAILY | 0.75 | 一个定时任务在多实例部署时重复执行，怎么处理？ |
| 276 | CODING | GENERAL | 0.73 | 为什么浮点数不适合直接表示金额？ |
| 280 | CODING | MATH | 0.77 | 如何限制一次读取的文件大小，防止内存被占满？ |
| 282 | CODING | GENERAL | 0.73 | 为什么事务注解放在 private 方法上通常不生效？ |
| 284 | CODING | DAILY | 0.73 | 怎样在不重启服务的情况下重新加载配置？ |
| 289 | CODING | DAILY | 0.76 | 怎样将一段同步调用改造成异步任务并查询进度？ |
| 318 | MATH | GENERAL | 0.72 | 解释矩阵乘法为什么通常不满足交换律。 |
| 330 | MATH | GENERAL | 0.7 | 为什么零不能作为除数？ |
| 346 | MATH | DAILY | 0.68 | 3个人排队有多少种 permutation？ |
| 354 | GENERAL | DAILY | 0.74 | 我不需要日程，只想知道为什么人会拖延。 |
| 363 | LITERARY | CODING | 0.71 | 不分析技术原理，把“程序终于成功运行”写得像故事结尾。 |
| 365 | LITERARY | CODING | 0.73 | 把数据库崩溃写成一段拟人化的小故事。 |
| 366 | LITERARY | MATH | 0.7 | 不要修复下面的程序，只为它写一段幽默旁白。 |
| 367 | LITERARY | DAILY | 0.7 | 将“按时完成所有事项”改成适合海报的句子。 |
| 369 | CODING | GENERAL | 0.7 | 不要写得有文采，请直接说明这个文本解析器为何丢失换行。 |
| 371 | CODING | MATH | 0.79 | 如何用程序计算刚才那个概率问题？ |
| 373 | CODING | LITERARY | 0.71 | 这不是语言润色问题，我需要修复字符串替换后的乱码。 |
| 374 | CODING | DAILY | 0.76 | 给一个自动生成待办事项编号的数据结构设计。 |
| 375 | MATH | DAILY | 0.75 | 不需要安排复习，请直接算每天完成二十页需要几天读完三百页。 |
| 377 | MATH | GENERAL | 0.76 | 不要润色题目，计算两种方案的成本差额。 |
| 384 | GENERAL | LITERARY | 0.67 | 一场有效的公开讨论需要遵守哪些基本原则？ |
| 387 | GENERAL | CODING | 0.67 | 什么叫信息茧房，它可能怎样形成？ |
| 392 | GENERAL | MATH | 0.67 | 请说明信用在商业活动中的作用。 |
| 396 | GENERAL | DAILY | 0.71 | What is the difference between being busy and being productive? |
| 400 | GENERAL | LITERARY | 0.68 | 请用两句话说明终身学习的价值。 |
| 439 | LITERARY | GENERAL | 0.76 | 保持事实不变，把这段介绍写得更打动人。 |
| 442 | CODING | DAILY | 0.8 | 如何避免两个定时任务同时处理同一批数据？ |
| 445 | CODING | MATH | 0.7 | 怎样验证上传的文件确实是声明的格式，而不只看扩展名？ |
| 448 | CODING | GENERAL | 0.69 | 为什么把所有业务逻辑都放进 Controller 会难以维护？ |
| 450 | CODING | MATH | 0.72 | 怎样记录外部模型请求耗时又不泄露请求内容？ |
| 451 | CODING | MATH | 0.73 | 一个队列消费者处理速度低于生产速度时应该怎么办？ |
