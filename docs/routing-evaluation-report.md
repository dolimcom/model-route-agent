# Semantic Routing Evaluation Report

- Generated: 2026-07-16 20:02:37
- Samples: 380
- Passed: 154
- Accuracy: 40.53%
- Semantic accepted: 63 (16.58%)
- Request errors: 0
- Elapsed: 43.46 seconds

## Accuracy By Task

| Expected task | Samples | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 76 | 4 | 5.26% |
| DAILY | 76 | 55 | 72.37% |
| GENERAL | 76 | 72 | 94.74% |
| LITERARY | 76 | 3 | 3.95% |
| MATH | 76 | 20 | 26.32% |

## Confusion Matrix

| Expected | Actual | Count |
|---|---|---:|
| CODING | CODING | 4 |
| CODING | DAILY | 1 |
| CODING | GENERAL | 69 |
| CODING | LITERARY | 1 |
| CODING | MATH | 1 |
| DAILY | DAILY | 55 |
| DAILY | GENERAL | 21 |
| GENERAL | CODING | 2 |
| GENERAL | DAILY | 1 |
| GENERAL | GENERAL | 72 |
| GENERAL | MATH | 1 |
| LITERARY | GENERAL | 73 |
| LITERARY | LITERARY | 3 |
| MATH | CODING | 1 |
| MATH | DAILY | 1 |
| MATH | GENERAL | 53 |
| MATH | LITERARY | 1 |
| MATH | MATH | 20 |

## First 50 Misroutes

| ID | Expected | Actual | Confidence | Question |
|---:|---|---|---:|---|
| 27 | DAILY | GENERAL | 0 | 怎样在一周内养成每天喝足够水的习惯？ |
| 32 | DAILY | GENERAL | 0 | 帮我制定一份宿舍大扫除的执行顺序。 |
| 37 | DAILY | GENERAL | 0 | 我想坚持每晚阅读二十分钟，应该如何开始？ |
| 41 | LITERARY | GENERAL | 0 | 把“夕阳落在旧屋顶上”扩写成一段有画面感的描写。 |
| 42 | LITERARY | GENERAL | 0 | 请为一个发生在雨夜车站的故事设计开头。 |
| 44 | LITERARY | GENERAL | 0 | 写四行文字表达离别，但不要直接出现“离开”二字。 |
| 45 | LITERARY | GENERAL | 0 | 为一间临河的小书店取一个富有意境的名字。 |
| 46 | LITERARY | GENERAL | 0 | 分析《老人与海》中主人公面对失败时的态度。 |
| 47 | LITERARY | GENERAL | 0 | 让“她走进空房间”这句话带上一种悬疑感。 |
| 48 | LITERARY | GENERAL | 0 | 请以第一人称描述多年后回到故乡的感受。 |
| 49 | LITERARY | GENERAL | 0 | 为校园故事设计两个性格互补的主要人物。 |
| 50 | LITERARY | GENERAL | 0 | 怎样让一个反派角色显得真实而不是单纯邪恶？ |
| 51 | LITERARY | GENERAL | 0 | 请评价这句话的节奏：“风穿过长巷，灯一盏盏熄灭。” |
| 52 | LITERARY | GENERAL | 0 | 用克制的语气写一段收到坏消息后的心理活动。 |
| 53 | LITERARY | GENERAL | 0 | 为科幻故事构思一个出人意料但合理的结局。 |
| 54 | LITERARY | GENERAL | 0 | 将“今天下雨了，我没有出门”改得更具氛围感。 |
| 55 | LITERARY | GENERAL | 0 | 解释一个故事采用倒叙开场可能产生什么效果。 |
| 56 | LITERARY | GENERAL | 0 | 请写一段适合作为咖啡店海报标题的短句。 |
| 57 | LITERARY | GENERAL | 0 | 比较热烈表达和留白表达在情感描写中的差异。 |
| 58 | LITERARY | GENERAL | 0 | 给一篇关于童年夏天的文章拟三个标题。 |
| 59 | LITERARY | GENERAL | 0 | 以“最后一班车已经开走”为首句继续写一小段。 |
| 60 | LITERARY | GENERAL | 0 | 分析一个角色反复看表这一细节可以暗示什么。 |
| 61 | CODING | GENERAL | 0 | 一个服务启动后立刻退出，却没有明显报错，应该从哪些方向定位？ |
| 62 | CODING | GENERAL | 0 | 为什么对象明明已经创建，调用方法时仍可能出现空指针异常？ |
| 63 | CODING | GENERAL | 0 | 设计一个接口，使客户端可以分页查询历史消息。 |
| 64 | CODING | GENERAL | 0 | 两个线程同时修改同一条记录时，怎样避免数据互相覆盖？ |
| 65 | CODING | GENERAL | 0 | 一个查询在数据量变大后越来越慢，应该怎样排查？ |
| 66 | CODING | GENERAL | 0 | 请解释依赖注入为什么有利于单元测试。 |
| 67 | CODING | GENERAL | 0 | 怎样让文件路径校验避免用户访问授权目录之外的位置？ |
| 68 | CODING | GENERAL | 0 | 一个请求偶尔返回 500，但重新请求又成功，如何定位原因？ |
| 69 | CODING | GENERAL | 0 | 如何设计可回滚的文件修改操作？ |
| 70 | CODING | GENERAL | 0 | 请给出一个适合存储聊天消息的表结构。 |
| 71 | CODING | GENERAL | 0 | 为什么在循环中频繁拼接字符串可能影响性能？ |
| 72 | CODING | GENERAL | 0 | 如果第三方接口响应很慢，后端应当设置哪些保护措施？ |
| 73 | CODING | GENERAL | 0 | 怎样测试控制器在请求参数为空时返回正确的错误信息？ |
| 74 | CODING | GENERAL | 0 | 一个事务方法内部调用另一个事务方法时可能遇到什么问题？ |
| 75 | CODING | GENERAL | 0 | 请说明乐观锁和悲观锁分别适合什么场景。 |
| 76 | CODING | GENERAL | 0 | 如何把模型供应商的不同调用协议封装成可扩展结构？ |
| 77 | CODING | GENERAL | 0 | 程序读取中文文件后出现乱码，应该检查哪些编码设置？ |
| 78 | CODING | GENERAL | 0 | 为什么把密钥直接返回给前端是危险的？ |
| 79 | CODING | GENERAL | 0 | 设计一个策略，让主路由失败后自动切换到备用方式。 |
| 80 | CODING | GENERAL | 0 | 怎样为一个需要审批后才能执行的操作设计状态流转？ |
| 81 | MATH | GENERAL | 0 | 连续抛三次硬币，恰好两次正面朝上的可能性是多少？ |
| 82 | MATH | GENERAL | 0 | 一个商品先涨价百分之二十再降价百分之二十，最终价格如何变化？ |
| 83 | MATH | GENERAL | 0 | 某班有四十人，其中二十四人会游泳，占全班的几成？ |
| 84 | MATH | GENERAL | 0 | 已知一个直角三角形的两条直角边分别为三和四，斜边多长？ |
| 85 | MATH | GENERAL | 0 | 从一副扑克牌中随机抽一张，抽到红桃的可能性是多少？ |
| 86 | MATH | GENERAL | 0 | 若一个数的三倍减去五等于十，这个数是多少？ |
| 87 | MATH | GENERAL | 0 | 一辆车以每小时六十公里行驶两个半小时，共行驶多远？ |
| 88 | MATH | GENERAL | 0 | 请推导等差数列前 n 项和的表达式。 |
