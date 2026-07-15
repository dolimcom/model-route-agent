const state = {
    rootId: null,
    currentDirectory: "nested",
    selectedPath: null,
};

const elements = {
    statusText: document.querySelector("#statusText"),
    rootSelect: document.querySelector("#rootSelect"),
    directoryPath: document.querySelector("#directoryPath"),
    entryList: document.querySelector("#entryList"),
    filePath: document.querySelector("#filePath"),
    fileContent: document.querySelector("#fileContent"),
    byteCount: document.querySelector("#byteCount"),
    approvalMode: document.querySelector("#approvalMode"),
    approvalHint: document.querySelector("#approvalHint"),
    pendingCount: document.querySelector("#pendingCount"),
    pendingOperations: document.querySelector("#pendingOperations"),
    recentOperations: document.querySelector("#recentOperations"),
    lastOperation: document.querySelector("#lastOperation"),
    lastOperationPath: document.querySelector("#lastOperationPath"),
    toast: document.querySelector("#toast"),
};

async function request(url, options = {}) {
    const response = await fetch(url, {
        headers: {"Content-Type": "application/json", ...(options.headers || {})},
        ...options,
    });
    const text = await response.text();
    const body = text ? JSON.parse(text) : null;
    if (!response.ok) {
        throw new Error(body?.message || body?.error || `请求失败：${response.status}`);
    }
    return body;
}

function encoded(path) {
    return encodeURIComponent(path || "");
}

function joinPath(directory, name) {
    return [directory, name].filter(Boolean).join("/").replaceAll("//", "/");
}

function parentPath(path) {
    const parts = path.split("/").filter(Boolean);
    parts.pop();
    return parts.join("/");
}

function operationPath(operation) {
    if (operation.sourcePath && operation.targetPath) {
        return `${operation.sourcePath} → ${operation.targetPath}`;
    }
    return operation.targetPath || operation.sourcePath || "—";
}

function showToast(message, error = false) {
    elements.toast.textContent = message;
    elements.toast.classList.toggle("error", error);
    elements.toast.classList.add("visible");
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => elements.toast.classList.remove("visible"), 3000);
}

function recordOperation(result) {
    elements.lastOperation.textContent = `${result.operationType.replaceAll("_", " ")} · ${result.status}`;
    elements.lastOperationPath.textContent = operationPath(result);
}

function updateByteCount() {
    const bytes = new TextEncoder().encode(elements.fileContent.value).length;
    elements.byteCount.textContent = `${bytes.toLocaleString()} B`;
}

async function loadRoots() {
    const roots = await request("/api/files/roots");
    elements.rootSelect.replaceChildren();
    roots.filter(root => root.enabled).forEach(root => {
        const option = document.createElement("option");
        option.value = root.id;
        option.textContent = root.id;
        elements.rootSelect.append(option);
    });
    state.rootId = elements.rootSelect.value;
    elements.statusText.textContent = state.rootId ? `已连接 · ${state.rootId}` : "没有可用根目录";
}

async function loadEntries() {
    if (!state.rootId) return;
    state.currentDirectory = elements.directoryPath.value.trim();
    const entries = await request(`/api/files/${state.rootId}/entries?path=${encoded(state.currentDirectory)}`);
    elements.entryList.replaceChildren();
    if (!entries.length) {
        renderEmpty(elements.entryList, "此目录为空");
        return;
    }

    entries.forEach(entry => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "entry-button";
        button.dataset.path = entry.relativePath;

        const icon = document.createElement("span");
        icon.textContent = entry.directory ? "▣" : "◇";
        const name = document.createElement("span");
        name.textContent = entry.name;
        const meta = document.createElement("span");
        meta.className = "entry-meta";
        meta.textContent = entry.directory ? "DIR" : `${entry.size} B`;
        button.append(icon, name, meta);

        button.addEventListener("click", () => entry.directory
            ? guarded(() => openDirectory(entry.relativePath))()
            : guarded(() => openFile(entry.relativePath))());
        elements.entryList.append(button);
    });
}

async function openDirectory(path) {
    elements.directoryPath.value = path;
    await loadEntries();
}

async function openFile(path) {
    const result = await request(`/api/files/${state.rootId}/content?path=${encoded(path)}`);
    state.selectedPath = result.relativePath;
    elements.filePath.value = result.relativePath;
    elements.fileContent.value = result.content;
    document.querySelectorAll(".entry-button").forEach(button =>
        button.classList.toggle("active", button.dataset.path === result.relativePath));
    updateByteCount();
}

async function submitOperation(operation) {
    const result = await request("/api/file-operations", {
        method: "POST",
        body: JSON.stringify({
            rootId: state.rootId,
            approvalMode: elements.approvalMode.value,
            ...operation,
        }),
    });
    recordOperation(result);
    await loadOperations();
    if (result.status === "EXECUTED") {
        await loadEntries();
        showToast(`操作已执行：${operationPath(result)}`);
    } else if (result.status === "PENDING") {
        showToast(`操作等待审批：${operationPath(result)}`);
    } else {
        showToast(result.errorMessage || `操作状态：${result.status}`, result.status === "FAILED");
    }
    return result;
}

async function createDirectory() {
    const suggested = joinPath(state.currentDirectory, "new-folder");
    const path = window.prompt("输入新目录的相对路径", suggested);
    if (!path) return;
    await submitOperation({operationType: "CREATE_DIRECTORY", targetPath: path});
}

async function createFile() {
    const path = elements.filePath.value.trim();
    if (!path) throw new Error("请先填写文件相对路径");
    const result = await submitOperation({
        operationType: "CREATE_FILE",
        targetPath: path,
        content: elements.fileContent.value,
    });
    if (result.status === "EXECUTED") state.selectedPath = result.targetPath;
}

async function saveFile() {
    const path = state.selectedPath || elements.filePath.value.trim();
    if (!path) throw new Error("请先选择文件");
    await submitOperation({
        operationType: "UPDATE_FILE",
        sourcePath: path,
        content: elements.fileContent.value,
    });
}

async function renamePath() {
    const sourcePath = state.selectedPath || elements.filePath.value.trim();
    if (!sourcePath) throw new Error("请先选择文件");
    const currentName = sourcePath.split("/").pop();
    const nextName = window.prompt("输入新的文件名", currentName);
    if (!nextName || nextName === currentName || nextName.includes("/") || nextName.includes("\\")) return;
    const targetPath = joinPath(parentPath(sourcePath), nextName.trim());
    const result = await submitOperation({operationType: "RENAME", sourcePath, targetPath});
    if (result.status === "EXECUTED") {
        state.selectedPath = targetPath;
        elements.filePath.value = targetPath;
    }
}

async function deletePath() {
    const path = state.selectedPath || elements.filePath.value.trim();
    if (!path) throw new Error("请先选择文件或填写目录路径");
    if (!window.confirm(`确认提交删除操作：${path}？`)) return;
    const result = await submitOperation({operationType: "DELETE", sourcePath: path});
    if (result.status === "EXECUTED") clearEditor();
}

function operationCard(operation, pending) {
    const card = document.createElement("article");
    card.className = "operation-card";

    const header = document.createElement("div");
    header.className = "operation-card-header";
    const type = document.createElement("strong");
    type.textContent = operation.operationType.replaceAll("_", " ");
    const status = document.createElement("span");
    status.className = `status-badge status-${operation.status.toLowerCase()}`;
    status.textContent = operation.status;
    header.append(type, status);

    const path = document.createElement("code");
    path.textContent = operationPath(operation);
    card.append(header, path);

    if (operation.proposedContent !== null && operation.proposedContent !== undefined) {
        const preview = document.createElement("pre");
        preview.textContent = operation.proposedContent.length > 180
            ? `${operation.proposedContent.slice(0, 180)}…`
            : operation.proposedContent;
        card.append(preview);
    }
    if (operation.errorMessage) {
        const error = document.createElement("p");
        error.className = "operation-error";
        error.textContent = operation.errorMessage;
        card.append(error);
    }

    if (pending) {
        const actions = document.createElement("div");
        actions.className = "operation-actions";
        actions.append(
            actionButton("批准执行", "approve", () => decideOperation(operation.operationId, "approve")),
            actionButton("拒绝", "reject", () => decideOperation(operation.operationId, "reject")),
        );
        card.append(actions);
    }
    return card;
}

function actionButton(label, kind, action) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `queue-button ${kind}`;
    button.textContent = label;
    button.addEventListener("click", guarded(action));
    return button;
}

function renderEmpty(container, message) {
    const empty = document.createElement("p");
    empty.className = "entry-meta empty-state";
    empty.textContent = message;
    container.append(empty);
}

async function loadOperations() {
    const operations = await request("/api/file-operations");
    const pending = operations.filter(operation => operation.status === "PENDING");
    elements.pendingCount.textContent = pending.length;
    elements.pendingOperations.replaceChildren();
    elements.recentOperations.replaceChildren();

    if (!pending.length) renderEmpty(elements.pendingOperations, "没有等待审批的操作");
    pending.forEach(operation => elements.pendingOperations.append(operationCard(operation, true)));

    const recent = operations.filter(operation => operation.status !== "PENDING").slice(0, 8);
    if (!recent.length) renderEmpty(elements.recentOperations, "暂无执行记录");
    recent.forEach(operation => elements.recentOperations.append(operationCard(operation, false)));
}

async function decideOperation(operationId, decision) {
    const result = await request(`/api/file-operations/${operationId}/${decision}`, {method: "POST"});
    recordOperation(result);
    await loadOperations();
    if (result.status === "EXECUTED") {
        await loadEntries();
        showToast(`已批准并执行：${operationPath(result)}`);
    } else {
        showToast(result.status === "REJECTED" ? "操作已拒绝" : (result.errorMessage || result.status),
            result.status === "FAILED");
    }
}

async function rollbackLast() {
    if (!window.confirm("确认回滚最近一次仍可回滚的文件操作？")) return;
    const result = await request("/api/file-operations/rollback-last", {method: "POST"});
    recordOperation(result);
    clearEditor();
    await Promise.all([loadEntries(), loadOperations()]);
    showToast(`已回滚：${operationPath(result)}`);
}

function clearEditor() {
    elements.filePath.value = "";
    elements.fileContent.value = "";
    state.selectedPath = null;
    updateByteCount();
}

function updateApprovalHint() {
    const manual = elements.approvalMode.value === "MANUAL";
    elements.approvalHint.textContent = manual
        ? "操作会先进入待审批队列，批准后才会修改文件。"
        : "操作提案将自动批准并立即执行，仍会保留审计记录和回滚入口。";
}

function guarded(action) {
    return async () => {
        try {
            await action();
        } catch (error) {
            showToast(error.message, true);
        }
    };
}

document.querySelector("#refreshButton").addEventListener("click", guarded(loadEntries));
document.querySelector("#refreshOperationsButton").addEventListener("click", guarded(loadOperations));
document.querySelector("#openDirectoryButton").addEventListener("click", guarded(loadEntries));
document.querySelector("#upButton").addEventListener("click", guarded(() => openDirectory(parentPath(state.currentDirectory))));
document.querySelector("#createDirectoryButton").addEventListener("click", guarded(createDirectory));
document.querySelector("#createFileButton").addEventListener("click", guarded(createFile));
document.querySelector("#saveFileButton").addEventListener("click", guarded(saveFile));
document.querySelector("#renameButton").addEventListener("click", guarded(renamePath));
document.querySelector("#deleteButton").addEventListener("click", guarded(deletePath));
document.querySelector("#rollbackLastButton").addEventListener("click", guarded(rollbackLast));
elements.fileContent.addEventListener("input", updateByteCount);
elements.approvalMode.addEventListener("change", updateApprovalHint);
elements.rootSelect.addEventListener("change", guarded(async () => {
    state.rootId = elements.rootSelect.value;
    await loadEntries();
}));

guarded(async () => {
    await loadRoots();
    await Promise.all([loadEntries(), loadOperations()]);
})();
