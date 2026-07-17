# 上下文感知路由复杂集评估

- 场景数：30
- 种子问题路由准确率：27/30（90%）
- 追问路由准确率：21/30（70%）
- 注：C31 最初因 DeepSeek 返回 502 而失败；单独重试成功，结果已反映在下表中。

## 按预期任务统计追问准确率

| 任务 | 场景数 | 通过数 | 准确率 |
|---|---:|---:|---:|
| CODING | 7 | 5 | 71.43% |
| DAILY | 5 | 4 | 80% |
| GENERAL | 6 | 2 | 33.33% |
| LITERARY | 6 | 6 | 100% |
| MATH | 6 | 4 | 66.67% |

## 场景结果

| ID | 种子预期/实际 | 追问预期/实际 | 是否通过 |
|---|---|---|---|
| C21 | CODING/CODING | CODING/CODING | True |
| C22 | CODING/CODING | CODING/CODING | True |
| C23 | CODING/CODING | DAILY/DAILY | True |
| C24 | CODING/CODING | GENERAL/CODING | False |
| C25 | CODING/CODING | LITERARY/LITERARY | True |
| C26 | CODING/MATH | MATH/MATH | True |
| C27 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C28 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C29 | LITERARY/LITERARY | CODING/CODING | True |
| C30 | LITERARY/LITERARY | GENERAL/DAILY | False |
| C31 | LITERARY/LITERARY | DAILY/DAILY | True |
| C32 | LITERARY/LITERARY | MATH/LITERARY | False |
| C33 | MATH/MATH | MATH/MATH | True |
| C34 | MATH/MATH | MATH/MATH | True |
| C35 | MATH/MATH | CODING/MATH | False |
| C36 | MATH/MATH | DAILY/MATH | False |
| C37 | MATH/MATH | GENERAL/MATH | False |
| C38 | MATH/MATH | LITERARY/LITERARY | True |
| C39 | DAILY/DAILY | DAILY/DAILY | True |
| C40 | DAILY/DAILY | CODING/DAILY | False |
| C41 | DAILY/DAILY | GENERAL/DAILY | False |
| C42 | DAILY/DAILY | LITERARY/LITERARY | True |
| C43 | DAILY/DAILY | MATH/DAILY | False |
| C44 | GENERAL/GENERAL | GENERAL/GENERAL | True |
| C45 | GENERAL/GENERAL | GENERAL/GENERAL | True |
| C46 | GENERAL/GENERAL | CODING/CODING | True |
| C47 | GENERAL/DAILY | DAILY/DAILY | True |
| C48 | GENERAL/LITERARY | LITERARY/LITERARY | True |
| C49 | GENERAL/GENERAL | MATH/MATH | True |
| C50 | CODING/CODING | CODING/CODING | True |

---

# Context-Aware Routing Evaluation

> English Version

- Scenarios: 30
- Seed route accuracy: 27/30 (90%)
- Follow-up route accuracy: 21/30 (70%)
- Note: C31 initially failed because DeepSeek returned 502; an isolated retry succeeded and is reflected below.

## Follow-Up Accuracy By Expected Task

| Task | Scenarios | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 7 | 5 | 71.43% |
| DAILY | 5 | 4 | 80% |
| GENERAL | 6 | 2 | 33.33% |
| LITERARY | 6 | 6 | 100% |
| MATH | 6 | 4 | 66.67% |

## Scenario Results

| ID | Seed expected/actual | Follow-up expected/actual | Passed |
|---|---|---|---|
| C21 | CODING/CODING | CODING/CODING | True |
| C22 | CODING/CODING | CODING/CODING | True |
| C23 | CODING/CODING | DAILY/DAILY | True |
| C24 | CODING/CODING | GENERAL/CODING | False |
| C25 | CODING/CODING | LITERARY/LITERARY | True |
| C26 | CODING/MATH | MATH/MATH | True |
| C27 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C28 | LITERARY/LITERARY | LITERARY/LITERARY | True |
| C29 | LITERARY/LITERARY | CODING/CODING | True |
| C30 | LITERARY/LITERARY | GENERAL/DAILY | False |
| C31 | LITERARY/LITERARY | DAILY/DAILY | True |
| C32 | LITERARY/LITERARY | MATH/LITERARY | False |
| C33 | MATH/MATH | MATH/MATH | True |
| C34 | MATH/MATH | MATH/MATH | True |
| C35 | MATH/MATH | CODING/MATH | False |
| C36 | MATH/MATH | DAILY/MATH | False |
| C37 | MATH/MATH | GENERAL/MATH | False |
| C38 | MATH/MATH | LITERARY/LITERARY | True |
| C39 | DAILY/DAILY | DAILY/DAILY | True |
| C40 | DAILY/DAILY | CODING/DAILY | False |
| C41 | DAILY/DAILY | GENERAL/DAILY | False |
| C42 | DAILY/DAILY | LITERARY/LITERARY | True |
| C43 | DAILY/DAILY | MATH/DAILY | False |
| C44 | GENERAL/GENERAL | GENERAL/GENERAL | True |
| C45 | GENERAL/GENERAL | GENERAL/GENERAL | True |
| C46 | GENERAL/GENERAL | CODING/CODING | True |
| C47 | GENERAL/DAILY | DAILY/DAILY | True |
| C48 | GENERAL/LITERARY | LITERARY/LITERARY | True |
| C49 | GENERAL/GENERAL | MATH/MATH | True |
| C50 | CODING/CODING | CODING/CODING | True |
