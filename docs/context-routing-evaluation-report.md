# 上下文感知路由评估

- 场景数：20
- 种子问题路由准确率：16/20（80%）
- 追问路由准确率：15/20（75%）

## 按预期任务统计追问准确率

| 任务 | 场景数 | 通过数 | 准确率 |
|---|---:|---:|---:|
| CODING | 4 | 4 | 100% |
| DAILY | 4 | 3 | 75% |
| GENERAL | 3 | 1 | 33.33% |
| LITERARY | 5 | 3 | 60% |
| MATH | 4 | 4 | 100% |

## 场景结果

| ID | 种子预期/实际 | 追问预期/实际 | 是否通过 |
|---|---|---|---|
| C01 | CODING/CODING | CODING/CODING | True |
| C02 | CODING/CODING | CODING/CODING | True |
| C03 | CODING/CODING | CODING/CODING | True |
| C04 | CODING/CODING | CODING/CODING | True |
| C05 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C06 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C07 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C08 | LITERARY/ERROR | LITERARY/ERROR | False |
| C09 | MATH/MATH | MATH/MATH | True |
| C10 | MATH/MATH | MATH/MATH | True |
| C11 | MATH/MATH | MATH/MATH | True |
| C12 | MATH/MATH | MATH/MATH | True |
| C13 | DAILY/DAILY | DAILY/DAILY | True |
| C14 | DAILY/DAILY | DAILY/DAILY | True |
| C15 | DAILY/DAILY | DAILY/DAILY | True |
| C16 | DAILY/ERROR | DAILY/ERROR | False |
| C17 | GENERAL/GENERAL | GENERAL/GENERAL | True |
| C18 | GENERAL/LITERARY | GENERAL/LITERARY | False |
| C19 | CODING/CODING | LITERARY/CODING | False |
| C20 | LITERARY/ERROR | GENERAL/ERROR | False |

---

# Context-Aware Routing Evaluation

> English Version

- Scenarios: 20
- Seed route accuracy: 16/20 (80%)
- Follow-up route accuracy: 15/20 (75%)

## Follow-Up Accuracy By Expected Task

| Task | Scenarios | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 4 | 4 | 100% |
| DAILY | 4 | 3 | 75% |
| GENERAL | 3 | 1 | 33.33% |
| LITERARY | 5 | 3 | 60% |
| MATH | 4 | 4 | 100% |

## Scenario Results

| ID | Seed expected/actual | Follow-up expected/actual | Passed |
|---|---|---|---|
| C01 | CODING/CODING | CODING/CODING | True |
| C02 | CODING/CODING | CODING/CODING | True |
| C03 | CODING/CODING | CODING/CODING | True |
| C04 | CODING/CODING | CODING/CODING | True |
| C05 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C06 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C07 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C08 | LITERARY/ERROR | LITERARY/ERROR | False |
| C09 | MATH/MATH | MATH/MATH | True |
| C10 | MATH/MATH | MATH/MATH | True |
| C11 | MATH/MATH | MATH/MATH | True |
| C12 | MATH/MATH | MATH/MATH | True |
| C13 | DAILY/DAILY | DAILY/DAILY | True |
| C14 | DAILY/DAILY | DAILY/DAILY | True |
| C15 | DAILY/DAILY | DAILY/DAILY | True |
| C16 | DAILY/ERROR | DAILY/ERROR | False |
| C17 | GENERAL/GENERAL | GENERAL/GENERAL | True |
| C18 | GENERAL/LITERARY | GENERAL/LITERARY | False |
| C19 | CODING/CODING | LITERARY/CODING | False |
| C20 | LITERARY/ERROR | GENERAL/ERROR | False |
