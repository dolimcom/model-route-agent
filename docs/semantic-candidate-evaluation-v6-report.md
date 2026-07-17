# 原始语义候选评估 V6

- 样本数：680
- 策略处理前 Top-1 正确数：634（93.24%）
- 正确且被策略接受：634（93.24%）
- 耗时：156.05 秒

## 各任务 Top-1 准确率

| 任务 | 样本数 | Top-1 正确数 | 准确率 |
|---|---:|---:|---:|
| CODING | 136 | 121 | 88.97% |
| DAILY | 136 | 133 | 97.79% |
| GENERAL | 136 | 122 | 89.71% |
| LITERARY | 136 | 129 | 94.85% |
| MATH | 136 | 129 | 94.85% |

## 拒绝原因

| 原因 | 数量 |
|---|---:|
| ACCEPTED | 680 |

## Top-1 混淆矩阵

| 预期 | 最高分候选 | 数量 |
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

---

# Raw Semantic Candidate Evaluation

> English Version

- Samples: 680
- Top-1 correct before policy: 634 (93.24%)
- Correct and accepted by policy: 634 (93.24%)
- Elapsed: 156.05 seconds

## Top-1 Accuracy By Task

| Task | Samples | Top-1 correct | Accuracy |
|---|---:|---:|---:|
| CODING | 136 | 121 | 88.97% |
| DAILY | 136 | 133 | 97.79% |
| GENERAL | 136 | 122 | 89.71% |
| LITERARY | 136 | 129 | 94.85% |
| MATH | 136 | 129 | 94.85% |

## Rejection Reasons

| Reason | Count |
|---|---:|
| ACCEPTED | 680 |

## Top-1 Confusion Matrix

| Expected | Top candidate | Count |
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
