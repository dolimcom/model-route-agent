# 原始语义候选评估

- 样本数：380
- 策略处理前 Top-1 正确数：280（73.68%）
- 正确且被策略接受：60（15.79%）
- 耗时：40.3 秒

## 各任务 Top-1 准确率

| 任务 | 样本数 | Top-1 正确数 | 准确率 |
|---|---:|---:|---:|
| CODING | 76 | 58 | 76.32% |
| DAILY | 76 | 74 | 97.37% |
| GENERAL | 76 | 39 | 51.32% |
| LITERARY | 76 | 50 | 65.79% |
| MATH | 76 | 59 | 77.63% |

## 拒绝原因

| 原因 | 数量 |
|---|---:|
| LOW_CONFIDENCE | 300 |
| ACCEPTED | 65 |
| AMBIGUOUS | 12 |
| TIE | 2 |
| OUT_OF_DOMAIN | 1 |

## Top-1 混淆矩阵

| 预期 | 最高分候选 | 数量 |
|---|---|---:|
| CODING | CODING | 58 |
| CODING | DAILY | 7 |
| CODING | FOLLOWUP | 8 |
| CODING | GENERAL | 1 |
| CODING | MATH | 2 |
| DAILY | DAILY | 74 |
| DAILY | FOLLOWUP | 2 |
| GENERAL | CODING | 1 |
| GENERAL | DAILY | 16 |
| GENERAL | FOLLOWUP | 20 |
| GENERAL | GENERAL | 39 |
| LITERARY | CODING | 1 |
| LITERARY | DAILY | 3 |
| LITERARY | FOLLOWUP | 19 |
| LITERARY | GENERAL | 3 |
| LITERARY | LITERARY | 50 |
| MATH | CODING | 1 |
| MATH | DAILY | 5 |
| MATH | FOLLOWUP | 7 |
| MATH | GENERAL | 4 |
| MATH | MATH | 59 |

---

# Raw Semantic Candidate Evaluation

> English Version

- Samples: 380
- Top-1 correct before policy: 280 (73.68%)
- Correct and accepted by policy: 60 (15.79%)
- Elapsed: 40.3 seconds

## Top-1 Accuracy By Task

| Task | Samples | Top-1 correct | Accuracy |
|---|---:|---:|---:|
| CODING | 76 | 58 | 76.32% |
| DAILY | 76 | 74 | 97.37% |
| GENERAL | 76 | 39 | 51.32% |
| LITERARY | 76 | 50 | 65.79% |
| MATH | 76 | 59 | 77.63% |

## Rejection Reasons

| Reason | Count |
|---|---:|
| LOW_CONFIDENCE | 300 |
| ACCEPTED | 65 |
| AMBIGUOUS | 12 |
| TIE | 2 |
| OUT_OF_DOMAIN | 1 |

## Top-1 Confusion Matrix

| Expected | Top candidate | Count |
|---|---|---:|
| CODING | CODING | 58 |
| CODING | DAILY | 7 |
| CODING | FOLLOWUP | 8 |
| CODING | GENERAL | 1 |
| CODING | MATH | 2 |
| DAILY | DAILY | 74 |
| DAILY | FOLLOWUP | 2 |
| GENERAL | CODING | 1 |
| GENERAL | DAILY | 16 |
| GENERAL | FOLLOWUP | 20 |
| GENERAL | GENERAL | 39 |
| LITERARY | CODING | 1 |
| LITERARY | DAILY | 3 |
| LITERARY | FOLLOWUP | 19 |
| LITERARY | GENERAL | 3 |
| LITERARY | LITERARY | 50 |
| MATH | CODING | 1 |
| MATH | DAILY | 5 |
| MATH | FOLLOWUP | 7 |
| MATH | GENERAL | 4 |
| MATH | MATH | 59 |
