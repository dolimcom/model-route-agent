# 原始语义候选评估 V3

- 样本数：380
- 策略处理前 Top-1 正确数：313（82.37%）
- 正确且被策略接受：274（72.11%）
- 耗时：48.66 秒

## 各任务 Top-1 准确率

| 任务 | 样本数 | Top-1 正确数 | 准确率 |
|---|---:|---:|---:|
| CODING | 76 | 58 | 76.32% |
| DAILY | 76 | 76 | 100% |
| GENERAL | 76 | 63 | 82.89% |
| LITERARY | 76 | 56 | 73.68% |
| MATH | 76 | 60 | 78.95% |

## 拒绝原因

| 原因 | 数量 |
|---|---:|
| ACCEPTED | 310 |
| AMBIGUOUS | 39 |
| LOW_CONFIDENCE | 19 |
| TIE | 12 |

## Top-1 混淆矩阵

| 预期 | 最高分候选 | 数量 |
|---|---|---:|
| CODING | CODING | 58 |
| CODING | DAILY | 9 |
| CODING | GENERAL | 7 |
| CODING | LITERARY | 1 |
| CODING | MATH | 1 |
| DAILY | DAILY | 76 |
| GENERAL | CODING | 2 |
| GENERAL | DAILY | 11 |
| GENERAL | GENERAL | 63 |
| LITERARY | CODING | 3 |
| LITERARY | DAILY | 5 |
| LITERARY | GENERAL | 12 |
| LITERARY | LITERARY | 56 |
| MATH | CODING | 2 |
| MATH | DAILY | 6 |
| MATH | GENERAL | 8 |
| MATH | MATH | 60 |

---

# Raw Semantic Candidate Evaluation

> English Version

- Samples: 380
- Top-1 correct before policy: 313 (82.37%)
- Correct and accepted by policy: 274 (72.11%)
- Elapsed: 48.66 seconds

## Top-1 Accuracy By Task

| Task | Samples | Top-1 correct | Accuracy |
|---|---:|---:|---:|
| CODING | 76 | 58 | 76.32% |
| DAILY | 76 | 76 | 100% |
| GENERAL | 76 | 63 | 82.89% |
| LITERARY | 76 | 56 | 73.68% |
| MATH | 76 | 60 | 78.95% |

## Rejection Reasons

| Reason | Count |
|---|---:|
| ACCEPTED | 310 |
| AMBIGUOUS | 39 |
| LOW_CONFIDENCE | 19 |
| TIE | 12 |

## Top-1 Confusion Matrix

| Expected | Top candidate | Count |
|---|---|---:|
| CODING | CODING | 58 |
| CODING | DAILY | 9 |
| CODING | GENERAL | 7 |
| CODING | LITERARY | 1 |
| CODING | MATH | 1 |
| DAILY | DAILY | 76 |
| GENERAL | CODING | 2 |
| GENERAL | DAILY | 11 |
| GENERAL | GENERAL | 63 |
| LITERARY | CODING | 3 |
| LITERARY | DAILY | 5 |
| LITERARY | GENERAL | 12 |
| LITERARY | LITERARY | 56 |
| MATH | CODING | 2 |
| MATH | DAILY | 6 |
| MATH | GENERAL | 8 |
| MATH | MATH | 60 |
