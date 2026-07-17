# 贡献指南

## 开发流程

1. Fork 仓库并创建职责单一的分支。
2. 将 Provider 特有的请求与响应载荷限制在 `provider` 适配器中。
3. 将业务路由定义保存在 YAML 中，而不是写成 Java 条件分支。
4. 为成功、失败、歧义和降级路径补充测试。
5. 运行 `mvn clean test` 和 `node --check src/main/resources/static/app.js`。

不要提交 API Key、`models_local.yml`、`config/*.local.yml`、数据库文件、日志、IDE 状态或生成的 `target` 目录。

## Pull Request

请说明行为变化、安全影响、已执行的验证，以及是否存在配置迁移。修改路由阈值时，应提供修改前后的评估结果，而不是只给出个别案例。

---

# Contributing

> English Version

## Development workflow

1. Fork the repository and create a focused branch.
2. Keep provider-specific payloads inside `provider` adapters.
3. Keep business route definitions in YAML rather than Java conditionals.
4. Add tests for success, failure, ambiguity, and fallback paths.
5. Run `mvn clean test` and `node --check src/main/resources/static/app.js`.

Do not commit API keys, `models_local.yml`, `config/*.local.yml`, database files, logs, IDE state, or generated `target` directories.

## Pull requests

Describe the behavior change, security impact, verification performed, and any configuration migration. Changes to routing thresholds should include before/after evaluation results rather than only anecdotal examples.
