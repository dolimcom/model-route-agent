# 原始语义候选评估 V4

- 样本数：480
- 策略处理前 Top-1 正确数：428（89.17%）
- 正确且被策略接受：428（89.17%）
- 耗时：108.25 秒

## 各任务 Top-1 准确率

| 任务 | 样本数 | Top-1 正确数 | 准确率 |
|---|---:|---:|---:|
| CODING | 96 | 76 | 79.17% |
| DAILY | 96 | 95 | 98.96% |
| GENERAL | 96 | 84 | 87.5% |
| LITERARY | 96 | 85 | 88.54% |
| MATH | 96 | 88 | 91.67% |

## 拒绝原因

| 原因 | 数量 |
|---|---:|
| ACCEPTED | 480 |

## Top-1 混淆矩阵

| 预期 | 最高分候选 | 数量 |
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

---

# Raw Semantic Candidate Evaluation

> English Version

- Samples: 480
- Top-1 correct before policy: 428 (89.17%)
- Correct and accepted by policy: 428 (89.17%)
- Elapsed: 108.25 seconds

## Top-1 Accuracy By Task

| Task | Samples | Top-1 correct | Accuracy |
|---|---:|---:|---:|
| CODING | 96 | 76 | 79.17% |
| DAILY | 96 | 95 | 98.96% |
| GENERAL | 96 | 84 | 87.5% |
| LITERARY | 96 | 85 | 88.54% |
| MATH | 96 | 88 | 91.67% |

## Rejection Reasons

| Reason | Count |
|---|---:|
| ACCEPTED | 480 |

## Top-1 Confusion Matrix

| Expected | Top candidate | Count |
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
