# 配置指南

## 安全默认值与本地文件

`application.yml` 默认加载已提交的 `models.yml`。该文件包含五个 Mock 槽位，不包含任何密钥。通过界面修改的运行时配置会持久化到：

- `config/models.local.yml`
- `config/workspaces.local.yml`
- `config/chooser-state.local.yml`

这三个文件都被 Git 忽略。模型与工作区配置示例位于 `config/*.example.yml`。

选择器状态会分别记录工作区和附件对话框最近访问的目录。成功选择工作区时记录所选目录本身；成功选择文件时记录文件的父目录；取消任一对话框时记录关闭前正在浏览的目录。

## 环境变量

| 变量 | 默认值 | 用途 |
| --- | --- | --- |
| `DB_URL` | 本地 `model_route_agent` JDBC URL | MySQL 连接 |
| `DB_USERNAME` | `coder` | MySQL 用户名 |
| `DB_PASSWORD` | 空 | MySQL 密码 |
| `OLLAMA_BASE_URL` | `http://127.0.0.1:11434` | Embedding 端点 |
| `EMBEDDING_MODEL` | `bge-m3:latest` | 语义编码模型 |
| `SERVER_PORT` | `8080` | HTTP 端口 |
| `SERVER_ADDRESS` | `127.0.0.1` | HTTP 监听地址；保持回环地址可避免意外暴露到局域网 |

## 模型字段

- `display-name`：可选的界面显示名称，默认采用模型名。
- `model-name`：Provider 使用的模型标识符。
- `base-url`：Provider API 根地址，不包含末尾的具体操作路径。
- `api-key`：当 Provider 和 Base URL 不变时，更新请求留空会保留已有 Key。
- `timeout-ms`：Provider 总响应或流式空闲超时时间。
- `max-tokens`：Provider 最大输出 Token 数。
- `temperature`：范围为 `0.0` 到 `2.0`。

Provider 推断规则：

- `gemini-*` -> Gemini GenerateContent。
- `claude-*` -> Anthropic Messages。
- Ollama 模型前缀或端口 `11434` -> Ollama Chat。
- OpenAI 模型配合官方 OpenAI 端点 -> Responses API。
- 其他情况 -> OpenAI-compatible Chat Completions。

兼容本机地址的端点可以不设置 Key，远程兼容端点必须提供 Key。

## 路由语料

生产路由示例和阈值位于 `src/main/resources/semantic-router/routes.yml`。业务映射、上下文和焦点标记位于 `src/main/resources/router.yml`。修改任一文件后，都应使用 `scripts/` 下的脚本重新评估，并与 `docs/` 中的报告比较。

主应用使用 `semantic.router.startup-mode=ASYNC`：Ollama 不可用或模型仍在冷启动时，应用先以规则路由工作，后台构建语义快照。`FAIL_FAST` 会在初始化失败时阻止启动，`DEGRADED` 会同步尝试一次后降级。可通过 `GET /actuator/semanticrouter` 查看 `available` 和 `lastReloadError`，通过 `POST /actuator/semanticrouter` 手动重载。连接、读取和日志预览长度分别由 `connect-timeout`、`read-timeout`、`input-preview-length` 控制。

## 文件操作兼容性

生产路径应使用 `/api/file-operations` 提案和审批接口。调试用直接写接口默认不存在；如确有本地调试需要，可设置 `model-route.files.direct-mutation-enabled=true`。升级已有数据库时需执行一次 `database/migrations/20260722_file_operation_conflict_tracking.sql`，为 `file_operation` 增加冲突检测哈希列并迁移旧状态；新数据库直接使用 `database/schema.sql`。

## 外部工作区配置

工作区列表默认为空，请通过本地界面显式添加目录。在无法打开 Swing 目录选择器的无头环境中，可参考示例创建 `config/workspaces.local.yml`。每次文件系统请求都会重新规范化并校验路径。

---

# Configuration

> English Version

## Safe defaults and local files

`application.yml` loads committed `models.yml`, which contains five mock slots and no secret. Runtime changes made in the UI are persisted to:

- `config/models.local.yml`
- `config/workspaces.local.yml`
- `config/chooser-state.local.yml`

All three are ignored by Git. Example structures for model and workspace configuration are provided as `config/*.example.yml`.

The chooser state stores separate last-visited directories for workspace and attachment dialogs. A successful workspace selection remembers the selected directory itself; a successful file selection remembers its parent. Cancelling either dialog remembers the directory that was being viewed.

## Environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | local `model_route_agent` JDBC URL | MySQL connection |
| `DB_USERNAME` | `coder` | MySQL username |
| `DB_PASSWORD` | empty | MySQL password |
| `OLLAMA_BASE_URL` | `http://127.0.0.1:11434` | embedding endpoint |
| `EMBEDDING_MODEL` | `bge-m3:latest` | semantic encoder |
| `SERVER_PORT` | `8080` | HTTP port |
| `SERVER_ADDRESS` | `127.0.0.1` | HTTP bind address; keep loopback to avoid accidental LAN exposure |

## Model fields

- `display-name`: optional UI name; defaults to model name.
- `model-name`: provider model identifier.
- `base-url`: provider API root, without a trailing operation path.
- `api-key`: blank updates preserve the existing key only when provider and Base URL are unchanged.
- `timeout-ms`: total provider response/stream inactivity timeout.
- `max-tokens`: provider output limit.
- `temperature`: `0.0` to `2.0`.

Provider inference rules:

- `gemini-*` -> Gemini GenerateContent.
- `claude-*` -> Anthropic Messages.
- Ollama model prefix or port `11434` -> Ollama chat.
- OpenAI model plus official OpenAI endpoint -> Responses API.
- Everything else -> OpenAI-compatible Chat Completions.

Localhost-compatible endpoints may omit a key. Remote compatible endpoints require one.

## Route corpus

Production route examples and thresholds are in `src/main/resources/semantic-router/routes.yml`. Business mappings and context/focus markers are in `src/main/resources/router.yml`. Changes to either should be evaluated with the scripts under `scripts/` and compared against the reports in `docs/`.

The main application uses `semantic.router.startup-mode=ASYNC`: while Ollama is unavailable or warming up, rule routing remains available and the semantic snapshot is built in the background. `FAIL_FAST` aborts startup on initialization failure; `DEGRADED` makes one synchronous attempt and continues. Inspect `available` and `lastReloadError` with `GET /actuator/semanticrouter`, and trigger a reload with `POST /actuator/semanticrouter`. `connect-timeout`, `read-timeout`, and `input-preview-length` configure connection timeout, request timeout, and logged input preview length.

## File-operation compatibility

Production changes should use the `/api/file-operations` proposal and approval endpoints. Direct mutation endpoints are absent by default; enable them only for local debugging with `model-route.files.direct-mutation-enabled=true`. Existing databases must run `database/migrations/20260722_file_operation_conflict_tracking.sql` once; new databases should use `database/schema.sql`.

## External workspace configuration

The workspace list starts empty. Add directories explicitly with the local UI. For a headless machine where Swing cannot open a folder picker, create `config/workspaces.local.yml` from the example. Every path is normalized and checked again for each filesystem request.
