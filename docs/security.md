# 安全模型

## 信任边界

本项目面向单个本地用户，并不是经过强化的多租户远程执行服务。除非额外实现身份认证、授权、CSRF 策略、速率限制和租户级存储边界，否则不要将其直接暴露到不可信网络。

应用默认绑定 `127.0.0.1`，Docker MySQL 端口也只发布到回环地址。API 会拒绝浏览器发起的跨站写请求，并返回 CSP、`nosniff`、Referrer Policy 等安全响应头。把 `SERVER_ADDRESS` 改为非回环地址并不等于完成了远程部署加固。

## 密钥

- 已提交的模型配置不包含 Key。
- 运行时 Key 只保存在被 Git 忽略的本地 YAML 中。
- 设置接口只返回 `apiKeyConfigured`，不会返回 Key 原文。
- 修改 Provider 或 Base URL 时，不会把旧端点的 Key 带入新配置。
- 请求和 Provider 日志只记录模型/Provider 标识，不记录请求头或 Key。

在共享计算机上，应使用操作系统凭据存储或密钥管理器替代本地 YAML。

## 文件访问

- 只有显式配置或选择的根目录可以访问。
- 请求使用根目录 ID 和相对路径；绝对路径和逃逸路径会被拒绝。
- 符号链接会解析为真实路径，并再次检查是否位于授权根目录中。
- 附件及文件的默认读写大小上限为 1 MiB。
- 附件必须是受支持的 UTF-8 文本；二进制或编码错误的输入会被拒绝。
- 不支持递归删除目录，也不支持启动可执行进程。
- 绕过提案流程的直接创建、更新、重命名和删除 API 默认关闭；仅可通过 `model-route.files.direct-mutation-enabled=true` 显式开启，且只建议用于本地开发调试。

选中文件和附件内容会作为不可信模型上下文包装。这可以降低 Prompt Injection 风险，但不能保证模型一定忽略恶意内容。

## 操作与回滚

手动模式创建 `PENDING` 提案，批准前不会修改文件系统。全权模式立即执行，但仍会创建审计记录。提案会记录源文件指纹；审批执行前若文件已被外部修改，操作进入 `EXECUTION_FAILED`，不会覆盖新内容。执行后也会记录结果指纹；回滚前发现外部修改时进入 `ROLLBACK_FAILED`，避免覆盖后续编辑。

## SSE 失败行为

Provider 错误会转换为 `error` SSE 事件。前端会丢弃所有已经显示的部分文本。服务端只在非空流完整结束后保存本轮对话，防止不完整的助手消息进入历史记录。

---

# Security Model

> English Version

## Trust boundary

This project targets one local user. It is not a hardened multi-tenant remote execution service. Do not expose it directly to an untrusted network without adding authentication, authorization, CSRF policy, rate limits and tenant-specific storage boundaries.

The application binds to `127.0.0.1` by default, and Docker publishes MySQL on loopback only. The API rejects cross-site browser mutations and sends CSP, `nosniff`, Referrer Policy and related security headers. Changing `SERVER_ADDRESS` to a non-loopback address does not constitute remote-deployment hardening.

## Secrets

- The committed model configuration contains no key.
- Runtime keys are stored only in ignored local YAML.
- Settings responses expose `apiKeyConfigured`, never the key value.
- Changing provider or Base URL never carries the previous endpoint's key into the new configuration.
- Request and provider logs record model/provider identifiers, not request headers or keys.

For a shared machine, replace local YAML with an OS credential store or secrets manager.

## File access

- Only explicitly configured or selected roots are addressable.
- Requests use root IDs and relative paths; absolute and escaping paths are rejected.
- Symlinks are resolved and checked against the real root.
- Attachment and file read/write sizes default to 1 MiB.
- Attachments must be supported UTF-8 text; binary and malformed input is rejected.
- Recursive directory deletion and executable process launching are not implemented.
- Direct create/update/rename/delete APIs that bypass proposals are disabled by default. Enable them only for local development with `model-route.files.direct-mutation-enabled=true`.

Selected file and attachment content is wrapped as untrusted model context. This reduces prompt-injection risk but cannot guarantee that a model will ignore malicious content.

## Operations and rollback

Manual mode creates a `PENDING` proposal and does not touch the filesystem before approval. Full-access mode executes immediately but still creates an audit row. Source fingerprints prevent approval from overwriting changes made after proposal creation (`EXECUTION_FAILED`). Result fingerprints prevent rollback from overwriting newer edits (`ROLLBACK_FAILED`).

## SSE failure behavior

Provider errors become an `error` SSE event. The frontend discards all partial visible text. The server persists the exchange only after a non-empty stream completes, preventing incomplete assistant messages from entering conversation history.
