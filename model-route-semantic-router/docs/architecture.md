# 架构

## 请求流程

1. `RouteDefinitionProvider` 加载带版本号的路由语料。
2. `RouteSnapshotFactory` 校验所有路由表达并生成 Embedding。
3. `RouteSnapshotManager` 原子替换当前快照；重载失败时保留最近一次正常快照。
4. `DefaultSemanticRouter` 依次处理：
   - 强制覆盖/强提示；
   - 静态覆盖规则；
   - Embedding；
   - 路由评分；
   - 降级决策；
   - 事件发布。
5. `RoutingEventListener` 实现负责输出日志和指标。

## 评分

每条路由会生成：

- `semanticScore`：向量相似度；
- `keywordScore`：确定性的关键词重合度；
- `finalScore`：加权组合分数。

聚合模式：

- `MAX`
- `TOP_K_MEAN`
- `CENTROID`

## 确定性失败处理

策略会对以下情况作出确定性选择：

- 空输入；
- 低置信度；
- 分数接近；
- 完全平局；
- 域外输入；
- 编码器失败。

降级行为：

- `REJECT`
- `DEFAULT_ROUTE`
- `LAST_KNOWN_GOOD`

## 可观测性

`RoutingResult` 包含：

- 配置版本；
- 数据集版本；
- 编码器版本；
- 最高分候选；
- 原因码；
- 降级原因；
- 分阶段耗时。

Starter 会发布：

- 结构化 `semantic_route_decision` 日志；
- Micrometer 计数器和计时器；
- `/actuator/semanticrouter`。

## 评估

核心模块包含：

- 固定路由基线；
- 使用固定随机种子的随机基线；
- 关键词基线；
- 语义路由器评估。

指标：

- 准确率；
- Macro F1；
- 覆盖率；
- 降级 F1；
- 混淆矩阵；
- 错误导出。

---

# Architecture

> English Version

## Request Flow

1. `RouteDefinitionProvider` loads a versioned route corpus.
2. `RouteSnapshotFactory` validates and embeds all route utterances.
3. `RouteSnapshotManager` atomically swaps the current snapshot and preserves the last known good one on reload failures.
4. `DefaultSemanticRouter` handles:
   - hard override / hard hint
   - static override rules
   - embedding
   - route scoring
   - fallback decision
   - event publication
5. `RoutingEventListener` implementations emit logs and metrics.

## Scoring

Each route produces:

- `semanticScore`: vector similarity
- `keywordScore`: deterministic keyword overlap
- `finalScore`: weighted combination

Aggregation modes:

- `MAX`
- `TOP_K_MEAN`
- `CENTROID`

## Deterministic Failure Handling

The policy makes deterministic choices for:

- empty input
- low confidence
- close scores
- exact ties
- OOD
- encoder failure

Fallback behaviors:

- `REJECT`
- `DEFAULT_ROUTE`
- `LAST_KNOWN_GOOD`

## Observability

`RoutingResult` carries:

- config version
- dataset version
- encoder version
- top candidates
- reason code
- fallback reason
- timing breakdown

The starter publishes:

- structured `semantic_route_decision` logs
- Micrometer counters and timers
- `/actuator/semanticrouter`

## Evaluation

The core module includes:

- fixed baseline
- random baseline with fixed seed
- keyword baseline
- semantic router evaluation

Metrics:

- accuracy
- macro F1
- coverage
- fallback F1
- confusion matrix
- error export
