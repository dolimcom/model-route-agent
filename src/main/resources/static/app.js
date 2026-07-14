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

function showToast(message, error = false) {
    elements.toast.textContent = message;
    elements.toast.classList.toggle("error", error);
    elements.toast.classList.add("visible");
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => elements.toast.classList.remove("visible"), 2600);
}

function recordOperation(result) {
    elements.lastOperation.textContent = result.operation.replaceAll("_", " ");
    elements.lastOperationPath.textContent = result.previousPath
        ? `${result.previousPath} → ${result.path}`
        : result.path;
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
        const empty = document.createElement("p");
        empty.className = "entry-meta";
        empty.textContent = "此目录为空";
        elements.entryList.append(empty);
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
            ? openDirectory(entry.relativePath)
            : openFile(entry.relativePath));
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

async function createDirectory() {
    const suggested = joinPath(state.currentDirectory, "new-folder");
    const path = window.prompt("输入新目录的相对路径", suggested);
    if (!path) return;
    const result = await request(`/api/files/${state.rootId}/directories`, {
        method: "POST",
        body: JSON.stringify({path}),
    });
    state.currentDirectory = result.path;
    elements.directoryPath.value = result.path;
    state.selectedPath = null;
    elements.filePath.value = joinPath(result.path, "new-file.txt");
    elements.fileContent.value = "";
    updateByteCount();
    recordOperation(result);
    showToast(`目录已创建：${result.path}`);
    await loadEntries();
}

async function createFile() {
    const path = elements.filePath.value.trim();
    if (!path) throw new Error("请先填写文件相对路径");
    const result = await request(`/api/files/${state.rootId}/files`, {
        method: "POST",
        body: JSON.stringify({path, content: elements.fileContent.value}),
    });
    state.selectedPath = result.path;
    recordOperation(result);
    showToast(`文件已创建：${result.path}`);
    await loadEntries();
}

async function saveFile() {
    const path = state.selectedPath || elements.filePath.value.trim();
    if (!path) throw new Error("请先选择文件");
    const result = await request(`/api/files/${state.rootId}/content`, {
        method: "PUT",
        body: JSON.stringify({path, content: elements.fileContent.value}),
    });
    state.selectedPath = result.path;
    recordOperation(result);
    showToast(`文件已保存：${result.path}`);
    await loadEntries();
}

async function renamePath() {
    const sourcePath = state.selectedPath || elements.filePath.value.trim();
    if (!sourcePath) throw new Error("请先选择文件");
    const currentName = sourcePath.split("/").pop();
    const nextName = window.prompt("输入新的文件名", currentName);
    if (!nextName || nextName === currentName || nextName.includes("/") || nextName.includes("\\")) return;
    const targetPath = joinPath(parentPath(sourcePath), nextName.trim());
    const result = await request(`/api/files/${state.rootId}/rename`, {
        method: "POST",
        body: JSON.stringify({sourcePath, targetPath}),
    });
    state.selectedPath = result.path;
    elements.filePath.value = result.path;
    recordOperation(result);
    showToast(`已重命名为：${result.path}`);
    await loadEntries();
}

async function deletePath() {
    const path = state.selectedPath || elements.filePath.value.trim();
    if (!path) throw new Error("请先选择文件或填写目录路径");
    if (!window.confirm(`确认删除 ${path}？Day 8 暂不支持回滚。`)) return;
    const result = await request(`/api/files/${state.rootId}?path=${encoded(path)}`, {method: "DELETE"});
    recordOperation(result);
    elements.filePath.value = "";
    elements.fileContent.value = "";
    state.selectedPath = null;
    updateByteCount();
    showToast(`已删除：${result.path}`);
    await loadEntries();
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
document.querySelector("#openDirectoryButton").addEventListener("click", guarded(loadEntries));
document.querySelector("#upButton").addEventListener("click", guarded(() => openDirectory(parentPath(state.currentDirectory))));
document.querySelector("#createDirectoryButton").addEventListener("click", guarded(createDirectory));
document.querySelector("#createFileButton").addEventListener("click", guarded(createFile));
document.querySelector("#saveFileButton").addEventListener("click", guarded(saveFile));
document.querySelector("#renameButton").addEventListener("click", guarded(renamePath));
document.querySelector("#deleteButton").addEventListener("click", guarded(deletePath));
elements.fileContent.addEventListener("input", updateByteCount);
elements.rootSelect.addEventListener("change", guarded(async () => {
    state.rootId = elements.rootSelect.value;
    await loadEntries();
}));

guarded(async () => {
    await loadRoots();
    await loadEntries();
})();
