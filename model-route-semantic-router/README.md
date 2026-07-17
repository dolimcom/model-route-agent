# 语义路由器

这是一个独立的 Java 语义路由组件，默认优先使用 Ollama 本地 Embedding，并为 `model-route-agent` 等下游应用提供 Spring Boot Starter。

## 仓库模式

当前副本内嵌在 `model-route-agent` 仓库中。

- 有效实现位于：
  - `semantic-router-core`
  - `semantic-router-spring-boot-starter`
- 父应用通过根目录 `pom.xml` 直接加入这两个源码集。
- 不要恢复本目录下旧的顶层 `src` 结构；它只是早期脚手架，已经不再使用。

## 功能范围

- 核心路由器不依赖框架，可以在普通 Java 中运行。
- Starter 负责路由加载、快照重载、Actuator、结构化日志和 Micrometer 指标。
- 路由决策不只是寻找最近向量：
  - 对低置信度、相近分数、完全平局、空输入、域外输入和编码器失败提供确定性处理；
  - 支持可配置聚合、阈值、降级链、静态覆盖规则和 SOFT/HARD 提示；
  - 提供带基线对比、`Macro-F1`、覆盖率、降级 F1、混淆矩阵和错误导出的评估运行器。

## 模块

- `semantic-router-core`
- `semantic-router-spring-boot-starter`

## 快速开始

1. 启动 Ollama，并确认 `bge-m3:latest` 已可用。
2. 运行测试：

```bash
mvn test
```

3. 让 Starter 指向路由语料 YAML：

```yaml
semantic:
  router:
    routes-location: classpath:semantic-router/routes.yml
    local-model:
      provider: OLLAMA
      base-url: http://127.0.0.1:11434
      model: bge-m3:latest
```

4. 注入 `SemanticRouter` 并调用 `route(...)`。

## 路由语料

参考格式见 [datasets/routes/sample-routes.yml](datasets/routes/sample-routes.yml)。

## 评估

评估数据集与路由语料彼此独立：

- 路由：`datasets/routes`
- 评估：`datasets/evaluation`

在 core 模块中运行 CLI：

```bash
mvn -pl semantic-router-core -DskipTests exec:java -Dexec.mainClass=com.dolimcom.semanticrouter.evaluation.SemanticRouterEvaluationCli -Dexec.args="datasets/routes/sample-routes.yml datasets/evaluation/sample-eval.jsonl"
```

报告会导出：

- 汇总指标；
- 基线对比；
- 混淆矩阵；
- `errors.jsonl`。

## 前置条件

- JDK 17+，并正确配置 `JAVA_HOME`；
- Maven 3.9+；
- 如果使用默认本地链路，Ollama 必须可通过回环地址访问。

通过父应用使用时，请在仓库根目录运行 Maven，以确保父构建包含两个内嵌模块。构建路由快照或执行语义评估时需保持 Ollama 运行。

## 文档

- [架构](docs/architecture.md)

---

# Semantic Router

> English Version

Independent Java semantic router with an Ollama-first local embedding path and a Spring Boot starter for downstream apps such as `model-route-agent`.

## Repository Mode

This copy is embedded inside the `model-route-agent` repository.

- The active implementation lives in:
  - `semantic-router-core`
  - `semantic-router-spring-boot-starter`
- The parent application build adds these source sets directly from the root `pom.xml`.
- Do not restore the old top-level `src` layout under this directory. It was an early scaffold and is no longer used.

## Scope

- Core router is framework-agnostic and can run in plain Java.
- Starter wires route loading, snapshot reload, Actuator, structured logs, and Micrometer metrics.
- Routing does more than nearest-vector match:
  - low confidence, close scores, exact ties, empty input, OOD, and encoder failures all resolve deterministically
  - configurable aggregation, thresholds, fallback chain, static overrides, and SOFT/HARD hints
  - evaluation runner with baselines, `Macro-F1`, coverage, fallback F1, confusion matrix, and error export

## Modules

- `semantic-router-core`
- `semantic-router-spring-boot-starter`

## Quickstart

1. Start Ollama and make sure `bge-m3:latest` is available.
2. Run tests:

```bash
mvn test
```

3. Point the starter at a route corpus YAML:

```yaml
semantic:
  router:
    routes-location: classpath:semantic-router/routes.yml
    local-model:
      provider: OLLAMA
      base-url: http://127.0.0.1:11434
      model: bge-m3:latest
```

4. Inject `SemanticRouter` and call `route(...)`.

## Route Corpus

See [datasets/routes/sample-routes.yml](datasets/routes/sample-routes.yml) for the reference format.

## Evaluation

The evaluation dataset is separated from the route corpus:

- routes: `datasets/routes`
- evaluation: `datasets/evaluation`

Run the CLI from the core module:

```bash
mvn -pl semantic-router-core -DskipTests exec:java -Dexec.mainClass=com.dolimcom.semanticrouter.evaluation.SemanticRouterEvaluationCli -Dexec.args="datasets/routes/sample-routes.yml datasets/evaluation/sample-eval.jsonl"
```

The report exports:

- summary metrics
- baseline comparison
- confusion matrix
- `errors.jsonl`

## Prerequisites

- JDK 17+ with `JAVA_HOME` configured
- Maven 3.9+
- Ollama reachable on loopback if you want the default local path

When used through the parent application, run Maven from the repository root so the parent build includes both embedded modules. Keep Ollama running while building route snapshots or executing semantic evaluations.

## Docs

- [Architecture](docs/architecture.md)
