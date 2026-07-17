# 上下文感知路由评估 V5

- 场景数：20
- 种子问题路由准确率：19/20（95%）
- 追问路由准确率：19/20（95%）
- 注：C20 最初因 DeepSeek 在多次重试后仍返回 502 而失败；单独重试成功，结果已反映在下表中。

## 按预期任务统计追问准确率

| 任务 | 场景数 | 通过数 | 准确率 |
|---|---:|---:|---:|
| CODING | 4 | 4 | 100% |
| DAILY | 4 | 4 | 100% |
| GENERAL | 3 | 2 | 66.67% |
| LITERARY | 5 | 5 | 100% |
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
| C08 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C09 | MATH/MATH | MATH/MATH | True |
| C10 | MATH/MATH | MATH/MATH | True |
| C11 | MATH/MATH | MATH/MATH | True |
| C12 | MATH/MATH | MATH/MATH | True |
| C13 | DAILY/DAILY | DAILY/DAILY | True |
| C14 | DAILY/DAILY | DAILY/DAILY | True |
| C15 | DAILY/DAILY | DAILY/DAILY | True |
| C16 | DAILY/DAILY | DAILY/DAILY | True |
| C17 | GENERAL/GENERAL | GENERAL/GENERAL | True |
| C18 | GENERAL/LITERARY | GENERAL/LITERARY | False |
| C19 | CODING/CODING | LITERARY/LITERARY | True |
| C20 | LITERARY/LITERARY | GENERAL/GENERAL | True |

---

# Context-Aware Routing Evaluation

> English Version

- Scenarios: 20
- Seed route accuracy: 19/20 (95%)
- Follow-up route accuracy: 19/20 (95%)
- Note: C20 initially failed because DeepSeek returned 502 after retries; an isolated retry succeeded and is reflected below.

## Follow-Up Accuracy By Expected Task

| Task | Scenarios | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 4 | 4 | 100% |
| DAILY | 4 | 4 | 100% |
| GENERAL | 3 | 2 | 66.67% |
| LITERARY | 5 | 5 | 100% |
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
| C08 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C09 | MATH/MATH | MATH/MATH | True |
| C10 | MATH/MATH | MATH/MATH | True |
| C11 | MATH/MATH | MATH/MATH | True |
| C12 | MATH/MATH | MATH/MATH | True |
| C13 | DAILY/DAILY | DAILY/DAILY | True |
| C14 | DAILY/DAILY | DAILY/DAILY | True |
| C15 | DAILY/DAILY | DAILY/DAILY | True |
| C16 | DAILY/DAILY | DAILY/DAILY | True |
| C17 | GENERAL/GENERAL | GENERAL/GENERAL | True |
| C18 | GENERAL/LITERARY | GENERAL/LITERARY | False |
| C19 | CODING/CODING | LITERARY/LITERARY | True |
| C20 | LITERARY/LITERARY | GENERAL/GENERAL | True |
