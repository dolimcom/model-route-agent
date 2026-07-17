# ModelRoute 语义路由器 V7 最终评估

## 摘要

V7 使用三套相互独立的测试集进行评估，避免普通准确率、话语鲁棒性和对话上下文能力被隐藏在单一聚合指标中。

| 测试集 | 规模 | 通过数 | 准确率 | 请求错误数 |
|---|---:|---:|---:|---:|
| 人工整理的单轮回归集 | 680 | 655 | 96.32% | 0 |
| 分层话语压力集 | 1000 | 938 | 93.80% | 0 |
| 上下文追问场景 | 50 | 48 | 96.00% | 重试后为 0 |

上下文测试中的 C43 遇到一次 DeepSeek 瞬时 `502`。单独重试成功，最终上下文 CSV 和报告已记录重试结果。

## 回归集准确率

| 预期任务 | 样本数 | 通过数 | 准确率 |
|---|---:|---:|---:|
| CODING | 136 | 129 | 94.85% |
| DAILY | 136 | 133 | 97.79% |
| GENERAL | 136 | 127 | 93.38% |
| LITERARY | 136 | 132 | 97.06% |
| MATH | 136 | 134 | 98.53% |

与 V6 相比，总准确率从 93.24% 提升到 96.32%。V7 修复了 28 个 V6 错误，同时引入 7 个回归，净增加 21 个正确样本。

最终 150 个困难样本从 V6 的 86.00% 提升到 V7 的 95.33%：

| 预期任务 | 样本数 | 通过数 | 准确率 |
|---|---:|---:|---:|
| CODING | 30 | 29 | 96.67% |
| DAILY | 30 | 29 | 96.67% |
| GENERAL | 30 | 29 | 96.67% |
| LITERARY | 30 | 28 | 93.33% |
| MATH | 30 | 28 | 93.33% |

## 压力集准确率

压力集包含 125 个留出基础意图，每个意图生成八种话语变体；五类任务各有 200 个样本，类别保持平衡。

| 预期任务 | 样本数 | 通过数 | 准确率 |
|---|---:|---:|---:|
| CODING | 200 | 175 | 87.50% |
| DAILY | 200 | 200 | 100.00% |
| GENERAL | 200 | 193 | 96.50% |
| LITERARY | 200 | 188 | 94.00% |
| MATH | 200 | 182 | 91.00% |

按话语变体统计准确率：

| 变体 | 准确率 |
|---:|---:|
| 直接请求 | 95.20% |
| 末尾请求指令 | 92.00% |
| 跨领域引用上下文 | 94.40% |
| 否定式干扰项 | 95.20% |
| 显式任务切换 | 95.20% |
| 输出约束加否定 | 93.60% |
| 英文干扰前缀 | 89.60% |
| 显式焦点标记 | 95.20% |

压力集中主要剩余混淆为 `CODING -> MATH` 和 `LITERARY -> GENERAL`。英文话语前缀的效果也弱于已配置的中文焦点标记。

## 上下文准确率

种子问题路由准确率为 46/50（92.00%），追问路由准确率为 48/50（96.00%）。

| 追问任务 | 场景数 | 通过数 | 准确率 |
|---|---:|---:|---:|
| CODING | 11 | 11 | 100.00% |
| DAILY | 9 | 8 | 88.89% |
| GENERAL | 9 | 8 | 88.89% |
| LITERARY | 11 | 11 | 100.00% |
| MATH | 10 | 10 | 100.00% |

仍有两个追问失败：

- C18 的通用总结请求继承了错误的 LITERARY 种子路由。
- C36 将基于先前计算安排阅读时间理解为 MATH，而不是 DAILY。

## 工程结论

- 路由 ID、业务任务类型和模型 ID 已通过声明式配置映射，不再要求名称相同。
- 中文焦点标记和上下文切换短语由配置管理，不再硬编码到 Java 路由逻辑中。
- 当显式专业候选与继承上下文的分差位于配置的 `0.02` 范围内时，V7 允许该候选替换继承上下文。
- 模拟结果表明，提高全局最小分差会使当前确定性降级降低端到端准确率，因此未采用该方案。
- 1680 个单轮路由请求全部完成，没有传输错误。一个真实 Provider 上下文种子请求出现瞬时失败，重试后成功。

当前准确率已经足以冻结 MVP 的路由行为。后续修改应使用新的、未参与调参的评估集，而不是继续针对这些报告调优。

---

# ModelRoute Semantic Router V7 Final Evaluation

> English Version

## Summary

V7 was evaluated with three separate suites so that ordinary accuracy, discourse robustness, and conversation context are not hidden behind one aggregate number.

| Suite | Size | Passed | Accuracy | Request errors |
|---|---:|---:|---:|---:|
| Manually curated single-turn regression | 680 | 655 | 96.32% | 0 |
| Stratified discourse stress suite | 1000 | 938 | 93.80% | 0 |
| Context follow-up scenarios | 50 | 48 | 96.00% | 0 after retry |

The context run encountered one transient DeepSeek `502` in C43. The isolated retry succeeded and is recorded in the final context CSV and report.

## Regression Accuracy

| Expected task | Samples | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 136 | 129 | 94.85% |
| DAILY | 136 | 133 | 97.79% |
| GENERAL | 136 | 127 | 93.38% |
| LITERARY | 136 | 132 | 97.06% |
| MATH | 136 | 134 | 98.53% |

Compared with V6, overall accuracy increased from 93.24% to 96.32%. V7 fixed 28 V6 failures and introduced 7 regressions, producing a net gain of 21 correct samples.

The difficult final 150 samples improved from 86.00% in V6 to 95.33% in V7:

| Expected task | Samples | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 30 | 29 | 96.67% |
| DAILY | 30 | 29 | 96.67% |
| GENERAL | 30 | 29 | 96.67% |
| LITERARY | 30 | 28 | 93.33% |
| MATH | 30 | 28 | 93.33% |

## Stress Accuracy

The stress suite contains 125 held-out base intents and eight discourse variants per intent, balanced at 200 samples per task.

| Expected task | Samples | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 200 | 175 | 87.50% |
| DAILY | 200 | 200 | 100.00% |
| GENERAL | 200 | 193 | 96.50% |
| LITERARY | 200 | 188 | 94.00% |
| MATH | 200 | 182 | 91.00% |

Accuracy by discourse variant:

| Variant | Accuracy |
|---:|---:|
| Direct request | 95.20% |
| Last-request instruction | 92.00% |
| Cross-domain quoted context | 94.40% |
| Negated distractor | 95.20% |
| Explicit task switch | 95.20% |
| Output constraint plus negation | 93.60% |
| English distractor prefix | 89.60% |
| Explicit focus marker | 95.20% |

The main remaining stress confusions are `CODING -> MATH` and `LITERARY -> GENERAL`. English discourse prefixes are also weaker than configured Chinese focus markers.

## Context Accuracy

Seed routing accuracy was 46/50 (92.00%). Follow-up routing accuracy was 48/50 (96.00%).

| Follow-up task | Scenarios | Passed | Accuracy |
|---|---:|---:|---:|
| CODING | 11 | 11 | 100.00% |
| DAILY | 9 | 8 | 88.89% |
| GENERAL | 9 | 8 | 88.89% |
| LITERARY | 11 | 11 | 100.00% |
| MATH | 10 | 10 | 100.00% |

Two follow-up failures remain:

- C18 inherits an incorrect LITERARY seed route for a generic summary request.
- C36 interprets arranging reading time from a prior calculation as MATH rather than DAILY.

## Engineering Conclusions

- Route IDs, business task types, and model IDs are mapped declaratively and no longer require identical names.
- Chinese focus markers and context-switch phrases are configuration-owned rather than embedded in Java routing logic.
- V7 allows a close, explicit specialist candidate to replace inherited context when its score is within the configured `0.02` gap.
- Raising the global minimum margin was rejected after simulation because the current deterministic fallback reduced end-to-end accuracy.
- The 1680 single-turn routing requests completed without transport errors. One real-provider context seed failed transiently and succeeded on retry.

The current accuracy is sufficient to freeze routing behavior for the MVP. Further changes should use a new untouched evaluation set instead of tuning against these reports.
