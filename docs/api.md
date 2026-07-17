# API 接口

基础地址：`http://localhost:8080`

## Agent

- `POST /api/agent/stream`：主要的 POST SSE 对话/文件 Agent 端点。
- `POST /api/agent/chat`：同步兼容端点。
- `POST /api/agent/route`：只执行路由，不调用对话模型。
- `POST /api/agent/route/semantic`：返回原始语义路由结果。
- `POST /api/agent/file-operations`：旧版同步文件规划端点。
- `GET /api/agent/health`：应用健康状态摘要。

流式请求：

```json
{
  "question": "分析当前文件",
  "conversationId": null,
  "rootId": "agent-workspace",
  "selectedPath": "nested/Demo.java",
  "attachmentId": null,
  "approvalMode": "MANUAL"
}
```

SSE 事件顺序：

1. `meta`：对话 ID、路由决策、模型显示名称和文件操作标记。
2. `delta`：一个 Provider 文本分块，可能出现零次或多次。
3. `operation`：可选的文件操作提案。
4. `done`：成功完成。
5. `error`：终止性错误，之后不会再发送 `done`。

## 对话

- `POST /api/conversations`
- `GET /api/conversations`
- `GET /api/conversations/{conversationId}/messages`

## 运行时设置

- `GET /api/settings/models`
- `GET /api/settings/models/catalog`
- `GET /api/settings/models/status`
- `PUT /api/settings/models/{GENERAL|DAILY|LITERARY|CODING|MATH}`

Key 值只允许写入，不会通过接口返回。

## 工作区与附件

- `GET /api/workspaces`
- `POST /api/workspaces/pick`
- `DELETE /api/workspaces/{rootId}`
- `POST /api/attachments/pick`：通过本机原生选择器选择文件，返回仅针对该文件的可编辑授权。
- `POST /api/attachments`：使用 multipart 字段 `file` 上传附件。
- `GET /api/files/{rootId}/entries?path=...`
- `GET /api/files/{rootId}/content?path=...`

由于浏览器不会暴露原始绝对路径，multipart 上传得到的是只读副本。通过原生选择器选中的文件只授权该文件本身，并可通过常规审批和回滚流程执行 `UPDATE_FILE`。用于 API 调试的直接创建、更新、重命名和删除端点仍然保留，但主界面会把模型生成的修改统一转换为操作提案。

## 文件操作生命周期

- `POST /api/file-operations`
- `GET /api/file-operations?status=PENDING`
- `POST /api/file-operations/{operationId}/approve`
- `POST /api/file-operations/{operationId}/reject`
- `POST /api/file-operations/{operationId}/rollback`
- `POST /api/file-operations/rollback-last`

可执行的 IntelliJ HTTP Client 示例见 `agent-api.http`。

---

# API

> English Version

Base URL: `http://localhost:8080`

## Agent

- `POST /api/agent/stream`: primary POST SSE chat/file-agent endpoint.
- `POST /api/agent/chat`: synchronous compatibility endpoint.
- `POST /api/agent/route`: route only, without calling a chat model.
- `POST /api/agent/route/semantic`: raw semantic router result.
- `POST /api/agent/file-operations`: legacy synchronous planner endpoint.
- `GET /api/agent/health`: application health summary.

Stream request:

```json
{
  "question": "分析当前文件",
  "conversationId": null,
  "rootId": "agent-workspace",
  "selectedPath": "nested/Demo.java",
  "attachmentId": null,
  "approvalMode": "MANUAL"
}
```

SSE event order:

1. `meta`: conversation ID, route decision, display name and file-operation flag.
2. `delta`: one provider text chunk; zero or more.
3. `operation`: optional file proposal.
4. `done`: successful completion.
5. `error`: terminal failure; no `done` follows.

## Conversations

- `POST /api/conversations`
- `GET /api/conversations`
- `GET /api/conversations/{conversationId}/messages`

## Runtime settings

- `GET /api/settings/models`
- `GET /api/settings/models/catalog`
- `GET /api/settings/models/status`
- `PUT /api/settings/models/{GENERAL|DAILY|LITERARY|CODING|MATH}`

Key values are write-only.

## Workspaces and attachments

- `GET /api/workspaces`
- `POST /api/workspaces/pick`
- `DELETE /api/workspaces/{rootId}`
- `POST /api/attachments/pick`: native local-file selection; returns an editable single-file authorization.
- `POST /api/attachments` with multipart field `file`
- `GET /api/files/{rootId}/entries?path=...`
- `GET /api/files/{rootId}/content?path=...`

Browser multipart uploads are read-only copies because browsers do not expose the original absolute path. Native selected files authorize only that exact file and support `UPDATE_FILE` through the normal approval and rollback lifecycle. Direct create/update/rename/delete file endpoints remain available for API debugging, but the main UI routes model-generated changes through proposals.

## File operation lifecycle

- `POST /api/file-operations`
- `GET /api/file-operations?status=PENDING`
- `POST /api/file-operations/{operationId}/approve`
- `POST /api/file-operations/{operationId}/reject`
- `POST /api/file-operations/{operationId}/rollback`
- `POST /api/file-operations/rollback-last`

See `agent-api.http` for executable IntelliJ HTTP Client examples.
