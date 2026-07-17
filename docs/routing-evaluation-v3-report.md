# Semantic Routing Evaluation Report

- Generated: 2026-07-16 20:21:11
- Samples: 380
- Passed: 292
- Accuracy: 76.84%
- Semantic accepted: 310 (81.58%)
- Request errors: 0
- Elapsed: 47.77 seconds

## Accuracy By Task

| Expected task | Samples | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 76 | 50 | 65.79% |
| DAILY | 76 | 75 | 98.68% |
| GENERAL | 76 | 68 | 89.47% |
| LITERARY | 76 | 45 | 59.21% |
| MATH | 76 | 54 | 71.05% |

## Confusion Matrix

| Expected | Actual | Count |
|---|---|---:|
| CODING | CODING | 50 |
| CODING | DAILY | 4 |
| CODING | GENERAL | 20 |
| CODING | LITERARY | 1 |
| CODING | MATH | 1 |
| DAILY | DAILY | 75 |
| DAILY | GENERAL | 1 |
| GENERAL | CODING | 1 |
| GENERAL | DAILY | 7 |
| GENERAL | GENERAL | 68 |
| LITERARY | CODING | 2 |
| LITERARY | DAILY | 2 |
| LITERARY | GENERAL | 27 |
| LITERARY | LITERARY | 45 |
| MATH | CODING | 2 |
| MATH | DAILY | 4 |
| MATH | GENERAL | 16 |
| MATH | MATH | 54 |

## First 50 Misroutes

| ID | Expected | Actual | Confidence | Question |
|---:|---|---|---:|---|
| 7 | GENERAL | DAILY | 0.73 | 分析远程办公对个人生活可能产生的影响。 |
| 11 | GENERAL | DAILY | 0.72 | 为什么良好的睡眠会影响第二天的注意力？ |
| 12 | GENERAL | CODING | 0.72 | 请说明开源软件通常是怎样运作的。 |
| 20 | GENERAL | DAILY | 0.71 | 如果只能保留一个好习惯，你认为哪个最值得保留？ |
| 47 | LITERARY | GENERAL | 0 | 让“她走进空房间”这句话带上一种悬疑感。 |
| 48 | LITERARY | GENERAL | 0.68 | 请以第一人称描述多年后回到故乡的感受。 |
| 50 | LITERARY | GENERAL | 0 | 怎样让一个反派角色显得真实而不是单纯邪恶？ |
| 55 | LITERARY | GENERAL | 0 | 解释一个故事采用倒叙开场可能产生什么效果。 |
| 59 | LITERARY | GENERAL | 0 | 以“最后一班车已经开走”为首句继续写一小段。 |
| 60 | LITERARY | GENERAL | 0 | 分析一个角色反复看表这一细节可以暗示什么。 |
| 70 | CODING | DAILY | 0.71 | 请给出一个适合存储聊天消息的表结构。 |
| 71 | CODING | GENERAL | 0 | 为什么在循环中频繁拼接字符串可能影响性能？ |
| 75 | CODING | GENERAL | 0 | 请说明乐观锁和悲观锁分别适合什么场景。 |
| 78 | CODING | GENERAL | 0 | 为什么把密钥直接返回给前端是危险的？ |
| 82 | MATH | GENERAL | 0 | 一个商品先涨价百分之二十再降价百分之二十，最终价格如何变化？ |
| 83 | MATH | GENERAL | 0 | 某班有四十人，其中二十四人会游泳，占全班的几成？ |
| 87 | MATH | GENERAL | 0 | 一辆车以每小时六十公里行驶两个半小时，共行驶多远？ |
| 96 | MATH | GENERAL | 0.71 | 解释为什么任意奇数的平方仍然是奇数。 |
| 101 | GENERAL | DAILY | 0.74 | 为什么有些人更喜欢在安静环境中工作？ |
| 114 | GENERAL | DAILY | 0.71 | 为什么规律运动可能改善人的情绪？ |
| 140 | GENERAL | DAILY | 0.74 | 怎样判断一个目标是否过于模糊？ |
| 201 | LITERARY | GENERAL | 0 | 写一段清晨雾气笼罩河面的场景。 |
| 202 | LITERARY | GENERAL | 0.73 | 让一句普通的道歉显得真诚但不过分煽情。 |
| 204 | LITERARY | GENERAL | 0 | 用旁观者视角描写一场久别重逢。 |
| 211 | LITERARY | GENERAL | 0 | 怎样用动作而不是心理独白表现犹豫？ |
| 213 | LITERARY | GENERAL | 0 | 把“他很生气”改成展示而非告知的写法。 |
| 214 | LITERARY | GENERAL | 0 | 写一小段带有荒诞色彩的办公室场景。 |
| 217 | LITERARY | GENERAL | 0.71 | 用儿童视角描写第一次看到大海。 |
| 219 | LITERARY | DAILY | 0.73 | 将一段正式通知改成亲切自然的宣传语。 |
| 220 | LITERARY | GENERAL | 0 | 写一段两个人都没有说出真实想法的对话。 |
| 222 | LITERARY | GENERAL | 0 | 为“时间停止十分钟”构思一个短篇核心冲突。 |
| 224 | LITERARY | GENERAL | 0.75 | 把产品介绍改得有温度，但不要夸张。 |
| 233 | LITERARY | GENERAL | 0 | 用第二人称写一段关于错过的文字。 |
| 235 | LITERARY | GENERAL | 0 | 分析留白为什么能增强某些情感表达。 |
| 236 | LITERARY | GENERAL | 0 | 写一段看似温馨但隐约令人不安的家庭晚餐。 |
| 238 | LITERARY | GENERAL | 0 | 把这句话变得更轻盈：“春天终于到来了。” |
| 239 | LITERARY | GENERAL | 0 | 设计一封多年后才被读到的信的内容线索。 |
| 240 | LITERARY | GENERAL | 0 | 用三句话塑造一个嘴硬心软的人。 |
| 247 | LITERARY | GENERAL | 0.76 | 不要解释，直接写一段海边黄昏。 |
| 248 | LITERARY | GENERAL | 0 | 改得委婉一点：“你写的内容很空洞。” |
| 250 | LITERARY | GENERAL | 0 | 只给标题：一个关于机器人学会遗忘的故事。 |
| 255 | CODING | GENERAL | 0 | 为什么使用线程池后仍可能出现任务堆积？ |
| 256 | CODING | GENERAL | 0 | 如何安全地处理用户上传文件的名称和路径？ |
| 261 | CODING | DAILY | 0.74 | 怎样实现聊天记录按时间稳定排序？ |
| 264 | CODING | GENERAL | 0 | 文件修改前应该保存哪些信息才能支持撤销？ |
| 265 | CODING | GENERAL | 0 | 为什么直接信任前端传来的文件绝对路径不安全？ |
| 266 | CODING | GENERAL | 0.71 | 请解释连接池耗尽时通常会出现哪些现象。 |
| 268 | CODING | GENERAL | 0 | 怎样避免日志中打印访问令牌和敏感参数？ |
| 272 | CODING | GENERAL | 0 | 请设计一个审批操作从待处理到执行完成的状态机。 |
| 273 | CODING | GENERAL | 0 | 怎样保证创建文件和写审计记录具有一致性？ |
