const $ = selector => document.querySelector(selector);

const state = {
    conversationId: null,
    conversations: [],
    workspaces: [],
    rootId: null,
    directory: "",
    selectedPath: null,
    attachment: null,
    operations: [],
    modelConfigs: [],
    modelCatalog: [],
    settingsTask: "GENERAL",
    sending: false,
};

const elements = {
    conversationList: $("#conversationList"),
    activeConversationTitle: $("#activeConversationTitle"),
    routeStatus: $("#routeStatus"),
    routeChip: $("#routeChip"),
    workspaceSelect: $("#workspaceSelect"),
    workspacePath: $("#workspacePath"),
    folderBreadcrumb: $("#folderBreadcrumb"),
    fileTree: $("#fileTree"),
    messageList: $("#messageList"),
    emptyState: $("#emptyState"),
    chatScroll: $("#chatScroll"),
    approvalDock: $("#approvalDock"),
    pendingCount: $("#pendingCount"),
    pendingOperations: $("#pendingOperations"),
    operationList: $("#operationList"),
    questionInput: $("#questionInput"),
    sendButton: $("#sendButton"),
    approvalMode: $("#approvalMode"),
    attachmentInput: $("#attachmentInput"),
    contextChips: $("#contextChips"),
    characterCount: $("#characterCount"),
    selectedFileName: $("#selectedFileName"),
    selectedFilePath: $("#selectedFilePath"),
    previewSelectedButton: $("#previewSelectedButton"),
    toast: $("#toast"),
    settingsModal: $("#settingsModal"),
    settingsForm: $("#settingsForm"),
    taskTabs: $("#taskTabs"),
    modelDisplayName: $("#modelDisplayName"),
    modelNameSelect: $("#modelNameSelect"),
    customModelField: $("#customModelField"),
    customModelName: $("#customModelName"),
    modelBaseUrl: $("#modelBaseUrl"),
    modelApiKey: $("#modelApiKey"),
    apiKeyStatus: $("#apiKeyStatus"),
    modelProvider: $("#modelProvider"),
    modelTimeout: $("#modelTimeout"),
    modelMaxTokens: $("#modelMaxTokens"),
    modelTemperature: $("#modelTemperature"),
    temperatureValue: $("#temperatureValue"),
    previewModal: $("#previewModal"),
    previewTitle: $("#previewTitle"),
    previewContent: $("#previewContent"),
    panelScrim: $("#panelScrim"),
    configurationNotice: $("#configurationNotice"),
    configurationNoticeTitle: $("#configurationNoticeTitle"),
    configurationNoticeText: $("#configurationNoticeText"),
};

const taskLabels = {
    GENERAL: "通用",
    DAILY: "日常",
    LITERARY: "文学",
    CODING: "编程",
    MATH: "数学",
};

async function api(path, options = {}) {
    const headers = new Headers(options.headers || {});
    if (options.body && !(options.body instanceof FormData)) headers.set("Content-Type", "application/json");
    const response = await fetch(path, {...options, headers});
    if (response.status === 204) return null;
    const text = await response.text();
    let body = null;
    try { body = text ? JSON.parse(text) : null; } catch { body = text; }
    if (!response.ok) {
        const error = new Error(body?.message || body?.error || body || `请求失败 (${response.status})`);
        error.status = response.status;
        throw error;
    }
    return body;
}

function showToast(message, error = false) {
    elements.toast.textContent = message;
    elements.toast.classList.toggle("error", error);
    elements.toast.classList.add("visible");
    clearTimeout(showToast.timer);
    showToast.timer = setTimeout(() => elements.toast.classList.remove("visible"), 3600);
}

function pathParent(path) {
    const parts = (path || "").split("/").filter(Boolean);
    parts.pop();
    return parts.join("/");
}

function fileName(path) {
    return (path || "").split("/").filter(Boolean).pop() || path;
}

function formatTime(value) {
    if (!value) return "";
    return new Intl.DateTimeFormat("zh-CN", {hour: "2-digit", minute: "2-digit"}).format(new Date(value));
}

function renderEmpty(container, text) {
    const element = document.createElement("p");
    element.className = "list-empty";
    element.textContent = text;
    container.replaceChildren(element);
}

function renderMarkdown(target, source) {
    target.classList.add("markdown-rendered");
    target.replaceChildren();
    const lines = (source || "").replace(/\r\n?/g, "\n").split("\n");
    let index = 0;

    while (index < lines.length) {
        const line = lines[index];
        if (!line.trim()) {
            index++;
            continue;
        }

        const fence = markdownFence(line);
        if (fence) {
            const codeLines = [];
            index++;
            while (index < lines.length && !markdownFenceEnd(lines[index])) {
                codeLines.push(lines[index++]);
            }
            if (index < lines.length) index++;
            target.append(markdownCodeBlock(codeLines.join("\n"), fence.language));
            continue;
        }

        const heading = line.match(/^\s{0,3}(#{1,6})\s+(.+?)\s*#*\s*$/);
        if (heading) {
            const element = document.createElement(`h${heading[1].length}`);
            appendInlineMarkdown(element, heading[2]);
            target.append(element);
            index++;
            continue;
        }

        if (isMarkdownTable(lines, index)) {
            const table = document.createElement("table");
            const head = document.createElement("thead");
            const headRow = document.createElement("tr");
            markdownTableCells(lines[index]).forEach(value => {
                const cell = document.createElement("th");
                appendInlineMarkdown(cell, value);
                headRow.append(cell);
            });
            head.append(headRow);
            table.append(head);
            index += 2;
            const body = document.createElement("tbody");
            while (index < lines.length && lines[index].includes("|") && lines[index].trim()) {
                const row = document.createElement("tr");
                markdownTableCells(lines[index]).forEach(value => {
                    const cell = document.createElement("td");
                    appendInlineMarkdown(cell, value);
                    row.append(cell);
                });
                body.append(row);
                index++;
            }
            table.append(body);
            const wrapper = document.createElement("div");
            wrapper.className = "markdown-table-wrap";
            wrapper.append(table);
            target.append(wrapper);
            continue;
        }

        const unordered = line.match(/^\s*[-+*]\s+(.+)$/);
        const ordered = line.match(/^\s*\d+[.)]\s+(.+)$/);
        if (unordered || ordered) {
            const list = document.createElement(ordered ? "ol" : "ul");
            const itemPattern = ordered ? /^\s*\d+[.)]\s+(.+)$/ : /^\s*[-+*]\s+(.+)$/;
            while (index < lines.length) {
                const item = lines[index].match(itemPattern);
                if (!item) break;
                const listItem = document.createElement("li");
                appendInlineMarkdown(listItem, item[1]);
                list.append(listItem);
                index++;
            }
            target.append(list);
            continue;
        }

        if (/^\s*>/.test(line)) {
            const quote = document.createElement("blockquote");
            const quoteLines = [];
            while (index < lines.length && /^\s*>/.test(lines[index])) {
                quoteLines.push(lines[index++].replace(/^\s*>\s?/, ""));
            }
            appendInlineMarkdown(quote, quoteLines.join("\n"));
            target.append(quote);
            continue;
        }

        if (/^\s{0,3}([-*_])(?:\s*\1){2,}\s*$/.test(line)) {
            target.append(document.createElement("hr"));
            index++;
            continue;
        }

        const paragraphLines = [line];
        index++;
        while (index < lines.length && lines[index].trim() && !isMarkdownBlockStart(lines, index)) {
            paragraphLines.push(lines[index++]);
        }
        const paragraph = document.createElement("p");
        paragraphLines.forEach((value, lineIndex) => {
            if (lineIndex > 0) paragraph.append(document.createElement("br"));
            appendInlineMarkdown(paragraph, value);
        });
        target.append(paragraph);
    }
}

function markdownFence(line) {
    const match = line.match(/^\s*[“”"]?\s*```\s*([a-zA-Z0-9_+#.-]*)\s*[“”"]?\s*$/);
    return match ? {language: match[1] || "text"} : null;
}

function markdownFenceEnd(line) {
    return /^\s*```\s*[“”"]?\s*$/.test(line);
}

function markdownCodeBlock(content, language) {
    const wrapper = document.createElement("div");
    wrapper.className = "markdown-code-block";
    const header = document.createElement("div");
    header.className = "markdown-code-header";
    const label = document.createElement("span");
    label.textContent = language.toUpperCase();
    const copy = document.createElement("button");
    copy.type = "button";
    copy.textContent = "复制";
    copy.addEventListener("click", async () => {
        await navigator.clipboard.writeText(content);
        copy.textContent = "已复制";
        setTimeout(() => { copy.textContent = "复制"; }, 1200);
    });
    header.append(label, copy);
    const pre = document.createElement("pre");
    const code = document.createElement("code");
    code.textContent = content;
    pre.append(code);
    wrapper.append(header, pre);
    return wrapper;
}

function appendInlineMarkdown(parent, value) {
    const pattern = /(`[^`\n]+`|\*\*[^*\n]+\*\*|~~[^~\n]+~~|\*[^*\n]+\*|\[[^\]\n]+\]\(https?:\/\/[^)\s]+\))/g;
    let cursor = 0;
    for (const match of value.matchAll(pattern)) {
        if (match.index > cursor) parent.append(document.createTextNode(value.slice(cursor, match.index)));
        const token = match[0];
        let element;
        if (token.startsWith("`")) {
            element = document.createElement("code");
            element.textContent = token.slice(1, -1);
        } else if (token.startsWith("**")) {
            element = document.createElement("strong");
            element.textContent = token.slice(2, -2);
        } else if (token.startsWith("~~")) {
            element = document.createElement("del");
            element.textContent = token.slice(2, -2);
        } else if (token.startsWith("*")) {
            element = document.createElement("em");
            element.textContent = token.slice(1, -1);
        } else {
            const link = token.match(/^\[([^\]]+)\]\((https?:\/\/[^)]+)\)$/);
            element = document.createElement("a");
            element.textContent = link[1];
            element.href = link[2];
            element.target = "_blank";
            element.rel = "noopener noreferrer";
        }
        parent.append(element);
        cursor = match.index + token.length;
    }
    if (cursor < value.length) parent.append(document.createTextNode(value.slice(cursor)));
}

function isMarkdownTable(lines, index) {
    return index + 1 < lines.length
        && lines[index].includes("|")
        && /^\s*\|?\s*:?-{3,}:?\s*(?:\|\s*:?-{3,}:?\s*)+\|?\s*$/.test(lines[index + 1]);
}

function markdownTableCells(line) {
    return line.trim().replace(/^\||\|$/g, "").split("|").map(value => value.trim());
}

function isMarkdownBlockStart(lines, index) {
    const line = lines[index];
    return Boolean(markdownFence(line))
        || /^\s{0,3}#{1,6}\s+/.test(line)
        || /^\s*[-+*]\s+/.test(line)
        || /^\s*\d+[.)]\s+/.test(line)
        || /^\s*>/.test(line)
        || /^\s{0,3}([-*_])(?:\s*\1){2,}\s*$/.test(line)
        || isMarkdownTable(lines, index);
}

async function loadConversations() {
    state.conversations = await api("/api/conversations");
    elements.conversationList.replaceChildren();
    if (!state.conversations.length) {
        renderEmpty(elements.conversationList, "开始你的第一段对话");
        return;
    }
    state.conversations.forEach(conversation => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "conversation-item";
        button.classList.toggle("active", conversation.id === state.conversationId);
        const title = document.createElement("strong");
        title.textContent = conversation.title;
        const meta = document.createElement("span");
        meta.textContent = `${formatTime(conversation.updatedAt)} · ${conversation.id.slice(0, 8)}`;
        button.append(title, meta);
        button.addEventListener("click", () => guarded(() => openConversation(conversation))());
        elements.conversationList.append(button);
    });
}

async function createConversation() {
    state.conversationId = null;
    elements.activeConversationTitle.textContent = "新对话";
    elements.messageList.replaceChildren();
    elements.emptyState.hidden = false;
    resetRouteStatus();
    await loadConversations();
    closeMobilePanels();
}

async function openConversation(conversation) {
    state.conversationId = conversation.id;
    elements.activeConversationTitle.textContent = conversation.title;
    const messages = await api(`/api/conversations/${encodeURIComponent(conversation.id)}/messages`);
    elements.messageList.replaceChildren();
    messages.forEach(message => appendMessage(message.role, message.content, {
        taskType: message.taskType,
        modelId: message.modelId,
    }));
    elements.emptyState.hidden = messages.length > 0;
    const lastAssistant = [...messages].reverse().find(message => message.role === "assistant");
    if (lastAssistant) updateRouteStatus(lastAssistant.taskType, lastAssistant.modelId);
    await loadConversations();
    scrollChat();
    closeMobilePanels();
}

function resetRouteStatus() {
    elements.routeStatus.textContent = "等待任务";
    elements.routeChip.textContent = "AUTO ROUTE";
    elements.routeChip.removeAttribute("data-task");
}

function updateRouteStatus(taskType, modelName) {
    if (!taskType) return;
    elements.routeStatus.textContent = `${taskLabels[taskType] || taskType} · ${modelName || "模型已选择"}`;
    elements.routeChip.textContent = taskType;
    elements.routeChip.dataset.task = taskType;
}

function appendMessage(role, content = "", route = null) {
    elements.emptyState.hidden = true;
    const article = document.createElement("article");
    article.className = `message ${role}`;
    const avatar = document.createElement("div");
    avatar.className = "message-avatar";
    avatar.textContent = role === "user" ? "YOU" : "MR";
    const body = document.createElement("div");
    body.className = "message-body";
    const heading = document.createElement("div");
    heading.className = "message-heading";
    const name = document.createElement("strong");
    name.textContent = role === "user" ? "你" : "ModelRoute Agent";
    const meta = document.createElement("span");
    meta.textContent = route?.taskType
        ? `${taskLabels[route.taskType] || route.taskType} · ${route.modelId || ""}`
        : (role === "assistant" ? "正在选择模型" : "刚刚");
    heading.append(name, meta);
    const text = document.createElement("div");
    text.className = "message-content";
    if (role === "assistant" && content) {
        renderMarkdown(text, content);
    } else {
        text.textContent = content;
    }
    body.append(heading, text);
    article.append(avatar, body);
    elements.messageList.append(article);
    scrollChat();
    return {article, text, meta};
}

function createTypewriter(target) {
    const queue = [];
    let source = "";
    let timer = null;
    let failed = false;
    let drainResolver = null;

    const tick = () => {
        if (failed) return;
        const next = queue.shift();
        if (next !== undefined) {
            source += next;
            target.textContent = source;
            scrollChat(false);
            timer = setTimeout(tick, 6);
            return;
        }
        timer = null;
        if (drainResolver) {
            drainResolver();
            drainResolver = null;
        }
    };
    return {
        push(value) {
            queue.push(...Array.from(value || ""));
            if (!timer) tick();
        },
        drain() {
            if (!timer && queue.length === 0) return Promise.resolve();
            return new Promise(resolve => { drainResolver = resolve; });
        },
        finish() {
            renderMarkdown(target, source);
        },
        fail(message) {
            failed = true;
            clearTimeout(timer);
            queue.length = 0;
            target.textContent = `生成失败：${message}`;
            target.closest(".message").classList.add("message-error");
            if (drainResolver) drainResolver();
        },
    };
}

async function sendMessage(event) {
    event.preventDefault();
    const question = elements.questionInput.value.trim();
    if (!question || state.sending) return;

    state.sending = true;
    elements.sendButton.disabled = true;
    appendMessage("user", question);
    const assistant = appendMessage("assistant");
    assistant.article.classList.add("streaming");
    const typewriter = createTypewriter(assistant.text);
    elements.questionInput.value = "";
    resizeComposer();
    updateCharacterCount();

    let streamError = null;
    try {
        const response = await fetch("/api/agent/stream", {
            method: "POST",
            headers: {"Content-Type": "application/json", "Accept": "text/event-stream"},
            body: JSON.stringify({
                question,
                conversationId: state.conversationId,
                rootId: state.rootId,
                selectedPath: state.selectedPath,
                attachmentId: state.attachment?.id || null,
                approvalMode: elements.approvalMode.value,
            }),
        });
        if (!response.ok || !response.body) {
            const body = await response.text();
            try { streamError = JSON.parse(body).message; } catch { streamError = body; }
            throw new Error(streamError || `流式请求失败 (${response.status})`);
        }
        await consumeSse(response.body, (name, data) => {
            if (name === "meta") {
                state.conversationId = data.conversationId;
                assistant.meta.textContent = `${taskLabels[data.route.taskType] || data.route.taskType} · ${data.modelDisplayName}`;
                updateRouteStatus(data.route.taskType, data.modelDisplayName);
                if (data.fileOperation) assistant.article.classList.add("file-operation-message");
            } else if (name === "delta") {
                typewriter.push(data.text);
            } else if (name === "operation") {
                state.operations.unshift(data);
                renderOperations();
            } else if (name === "error") {
                streamError = data.message || "未知模型错误";
            }
        });
        if (streamError) throw new Error(streamError);
        await typewriter.drain();
        typewriter.finish();
        assistant.article.classList.remove("streaming");
        clearAttachment();
        await Promise.all([loadConversations(), loadOperations()]);
        const active = state.conversations.find(item => item.id === state.conversationId);
        if (active) elements.activeConversationTitle.textContent = active.title;
    } catch (error) {
        typewriter.fail(error.message || "请求中断");
        clearAttachment();
        showToast(error.message || "请求中断", true);
    } finally {
        state.sending = false;
        elements.sendButton.disabled = false;
        elements.questionInput.focus();
        scrollChat();
    }
}

async function consumeSse(body, onEvent) {
    const reader = body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";
    while (true) {
        const {value, done} = await reader.read();
        buffer += decoder.decode(value || new Uint8Array(), {stream: !done});
        const blocks = buffer.split(/\r?\n\r?\n/);
        buffer = blocks.pop() || "";
        blocks.forEach(block => parseSseBlock(block, onEvent));
        if (done) break;
    }
    if (buffer.trim()) parseSseBlock(buffer, onEvent);
}

function parseSseBlock(block, onEvent) {
    let eventName = "message";
    const dataLines = [];
    block.split(/\r?\n/).forEach(line => {
        if (line.startsWith("event:")) eventName = line.slice(6).trim();
        if (line.startsWith("data:")) dataLines.push(line.slice(5).trimStart());
    });
    if (!dataLines.length) return;
    const raw = dataLines.join("\n");
    let data = raw;
    try { data = JSON.parse(raw); } catch { /* Text SSE data is valid. */ }
    onEvent(eventName, data);
}

async function loadWorkspaces(preferredRootId = null) {
    state.workspaces = await api("/api/workspaces");
    elements.workspaceSelect.replaceChildren();
    state.workspaces.forEach(workspace => {
        const option = document.createElement("option");
        option.value = workspace.id;
        option.textContent = workspace.displayName;
        elements.workspaceSelect.append(option);
    });
    state.rootId = preferredRootId && state.workspaces.some(item => item.id === preferredRootId)
        ? preferredRootId
        : (state.workspaces.some(item => item.id === state.rootId) ? state.rootId : state.workspaces[0]?.id || null);
    elements.workspaceSelect.value = state.rootId || "";
    updateWorkspaceMeta();
    state.directory = "";
    await loadFiles();
}

function updateWorkspaceMeta() {
    const workspace = state.workspaces.find(item => item.id === state.rootId);
    elements.workspacePath.textContent = workspace?.path || "尚未选择目录";
}

async function pickWorkspace() {
    showToast("正在打开系统目录选择器…");
    const workspace = await api("/api/workspaces/pick", {method: "POST"});
    if (!workspace) {
        showToast("已取消目录选择");
        return;
    }
    await loadWorkspaces(workspace.id);
    showToast(`已授权工作区：${workspace.displayName}`);
}

async function loadFiles() {
    elements.folderBreadcrumb.textContent = state.directory ? `/${state.directory}` : "/";
    elements.fileTree.replaceChildren();
    if (!state.rootId) {
        renderEmpty(elements.fileTree, "点击 + 授权一个本机目录");
        return;
    }
    const entries = await api(`/api/files/${encodeURIComponent(state.rootId)}/entries?path=${encodeURIComponent(state.directory)}`);
    if (!entries.length) {
        renderEmpty(elements.fileTree, "此目录为空");
        return;
    }
    entries.forEach(entry => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "file-entry";
        button.classList.toggle("active", entry.relativePath === state.selectedPath);
        const icon = document.createElement("span");
        icon.className = "file-icon";
        icon.textContent = entry.directory ? "▱" : "·";
        const name = document.createElement("strong");
        name.textContent = entry.name;
        const meta = document.createElement("small");
        meta.textContent = entry.directory ? "DIR" : sizeLabel(entry.size);
        button.append(icon, name, meta);
        button.addEventListener("click", () => guarded(async () => {
            if (entry.directory) {
                state.directory = entry.relativePath;
                await loadFiles();
            } else {
                selectFile(entry.relativePath);
            }
        })());
        elements.fileTree.append(button);
    });
}

function selectFile(path) {
    state.selectedPath = path;
    elements.selectedFileName.textContent = fileName(path);
    elements.selectedFilePath.textContent = path;
    elements.previewSelectedButton.disabled = false;
    renderContextChips();
    loadFiles().catch(error => showToast(error.message, true));
}

function clearSelectedFile() {
    state.selectedPath = null;
    elements.selectedFileName.textContent = "未选择文件";
    elements.selectedFilePath.textContent = "从左侧工作区选择文件";
    elements.previewSelectedButton.disabled = true;
    renderContextChips();
    loadFiles().catch(() => {});
}

async function uploadAttachment() {
    const file = elements.attachmentInput.files?.[0];
    if (!file) return;
    const formData = new FormData();
    formData.append("file", file);
    try {
        state.attachment = await api("/api/attachments", {method: "POST", body: formData});
        renderContextChips();
        showToast(`已上传只读副本：${state.attachment.fileName}`);
    } catch (error) {
        elements.attachmentInput.value = "";
        showToast(error.message, true);
    }
}

async function pickAttachment() {
    showToast("正在打开系统文件选择器…");
    try {
        const attachment = await api("/api/attachments/pick", {method: "POST"});
        if (!attachment) {
            showToast("已取消文件选择");
            return;
        }
        state.attachment = attachment;
        renderContextChips();
        showToast(`已授权可编辑文件：${attachment.fileName}`);
    } catch (error) {
        if (error.status === 501) {
            showToast("原生选择器不可用，将上传只读副本");
            elements.attachmentInput.click();
            return;
        }
        throw error;
    }
}

function clearAttachment() {
    state.attachment = null;
    elements.attachmentInput.value = "";
    renderContextChips();
}

function renderContextChips() {
    elements.contextChips.replaceChildren();
    if (state.selectedPath) {
        elements.contextChips.append(contextChip("工作区文件", state.selectedPath, clearSelectedFile));
    }
    if (state.attachment) {
        const kind = state.attachment.editable ? "可编辑文件" : "附件（只读副本）";
        elements.contextChips.append(contextChip(kind, state.attachment.fileName, clearAttachment));
    }
}

function contextChip(kind, value, remove) {
    const chip = document.createElement("span");
    chip.className = "context-chip";
    const text = document.createElement("span");
    text.textContent = `${kind} · ${value}`;
    const close = document.createElement("button");
    close.type = "button";
    close.textContent = "×";
    close.addEventListener("click", remove);
    chip.append(text, close);
    return chip;
}

async function loadOperations() {
    state.operations = await api("/api/file-operations");
    renderOperations();
}

function renderOperations() {
    const pending = state.operations.filter(operation => operation.status === "PENDING");
    elements.pendingCount.textContent = pending.length;
    elements.approvalDock.hidden = pending.length === 0;
    elements.pendingOperations.replaceChildren(...pending.map(operation => operationCard(operation, true)));

    elements.operationList.replaceChildren();
    if (!state.operations.length) {
        renderEmpty(elements.operationList, "Agent 的文件操作会显示在这里");
        return;
    }
    state.operations.slice(0, 30).forEach(operation =>
        elements.operationList.append(operationCard(operation, false)));
}

function operationCard(operation, pendingDock) {
    const article = document.createElement("article");
    article.className = `operation-card status-${operation.status.toLowerCase()}`;
    const head = document.createElement("div");
    head.className = "operation-head";
    const type = document.createElement("strong");
    type.textContent = operation.operationType.replaceAll("_", " ");
    const status = document.createElement("span");
    status.textContent = operation.status;
    head.append(type, status);
    const path = document.createElement("button");
    path.type = "button";
    path.className = "operation-path";
    path.textContent = operationPath(operation);
    path.addEventListener("click", () => guarded(() => previewOperation(operation))());
    article.append(head, path);
    if (operation.proposedContent != null && pendingDock) {
        const preview = document.createElement("pre");
        preview.textContent = operation.proposedContent.length > 260
            ? `${operation.proposedContent.slice(0, 260)}…`
            : operation.proposedContent;
        article.append(preview);
    }
    if (operation.errorMessage) {
        const error = document.createElement("p");
        error.textContent = operation.errorMessage;
        article.append(error);
    }
    const actions = document.createElement("div");
    actions.className = "operation-actions";
    if (operation.status === "PENDING") {
        actions.append(
            operationButton("批准", "approve", () => decideOperation(operation.operationId, "approve")),
            operationButton("拒绝", "reject", () => decideOperation(operation.operationId, "reject")),
        );
    } else if (operation.status === "EXECUTED") {
        actions.append(operationButton("撤销操作", "undo", () => rollbackOperation(operation.operationId)));
    }
    if (actions.childElementCount) article.append(actions);
    return article;
}

function operationButton(label, className, handler) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = className;
    button.textContent = label;
    button.addEventListener("click", guarded(handler));
    return button;
}

function operationPath(operation) {
    if (operation.sourcePath && operation.targetPath) return `${operation.sourcePath} → ${operation.targetPath}`;
    return operation.targetPath || operation.sourcePath || "无路径";
}

async function decideOperation(operationId, decision) {
    const result = await api(`/api/file-operations/${encodeURIComponent(operationId)}/${decision}`, {method: "POST"});
    await Promise.all([loadOperations(), loadFiles()]);
    if (result.status === "EXECUTED") {
        showToast("操作已批准并执行");
    } else if (result.status === "REJECTED") {
        showToast("操作已拒绝");
    } else if (result.status === "FAILED") {
        showToast(`执行失败：${result.errorMessage || "未知文件操作错误"}`, true);
    } else {
        showToast(`操作状态：${result.status}`);
    }
}

async function rollbackOperation(operationId) {
    if (!confirm("确认撤销这次文件操作？")) return;
    const result = await api(`/api/file-operations/${encodeURIComponent(operationId)}/rollback`, {method: "POST"});
    await Promise.all([loadOperations(), loadFiles()]);
    showToast(result.status === "ROLLED_BACK" ? "操作已撤销" : (result.errorMessage || result.status),
        result.status === "FAILED");
}

async function previewOperation(operation) {
    if (operation.status === "PENDING" && operation.proposedContent != null) {
        elements.previewTitle.textContent = operationPath(operation);
        elements.previewContent.textContent = operation.proposedContent;
        elements.previewModal.showModal();
        return;
    }
    if (operation.operationType === "DELETE" && operation.beforeContent != null
            && operation.status !== "ROLLED_BACK") {
        elements.previewTitle.textContent = `${operation.sourcePath}（删除前）`;
        elements.previewContent.textContent = operation.beforeContent;
        elements.previewModal.showModal();
        return;
    }
    const path = operation.status === "ROLLED_BACK"
        ? operation.sourcePath
        : (operation.targetPath || operation.sourcePath);
    if (!path) throw new Error("该操作没有可预览的文件");
    await previewFile(operation.rootId, path);
}

async function previewFile(rootId, path) {
    const result = await api(`/api/files/${encodeURIComponent(rootId)}/content?path=${encodeURIComponent(path)}`);
    elements.previewTitle.textContent = result.relativePath;
    elements.previewContent.textContent = result.content;
    elements.previewModal.showModal();
}

async function openSettings() {
    [state.modelConfigs, state.modelCatalog] = await Promise.all([
        api("/api/settings/models"),
        api("/api/settings/models/catalog"),
    ]);
    renderTaskTabs();
    renderSettingsTask();
    elements.settingsModal.showModal();
}

async function loadConfigurationStatus() {
    const status = await api("/api/settings/models/status");
    if (status.fullyConfigured) {
        elements.configurationNotice.hidden = true;
        return;
    }
    const missingNames = status.mockTasks.map(task => taskLabels[task] || task).join("、");
    elements.configurationNoticeTitle.textContent = status.runtimeFileExists
        ? "模型配置尚未完成"
        : "尚未保存用户模型配置";
    elements.configurationNoticeText.textContent = status.runtimeFileExists
        ? `仍在使用 Mock 的任务：${missingNames}。`
        : `当前使用默认 Mock 配置，请配置这些任务：${missingNames}。`;
    elements.configurationNotice.hidden = false;
}

function renderTaskTabs() {
    elements.taskTabs.replaceChildren();
    Object.entries(taskLabels).forEach(([task, label]) => {
        const button = document.createElement("button");
        button.type = "button";
        button.classList.toggle("active", task === state.settingsTask);
        button.textContent = label;
        button.addEventListener("click", () => {
            state.settingsTask = task;
            renderTaskTabs();
            renderSettingsTask();
        });
        elements.taskTabs.append(button);
    });
}

function renderSettingsTask() {
    const config = state.modelConfigs.find(item => item.taskType === state.settingsTask);
    if (!config) return;
    elements.modelDisplayName.value = config.displayName || "";
    elements.modelBaseUrl.value = config.baseUrl || "";
    elements.modelProvider.value = config.provider || "";
    elements.modelTimeout.value = config.timeoutMs;
    elements.modelMaxTokens.value = config.maxTokens;
    elements.modelTemperature.value = config.temperature;
    elements.temperatureValue.value = Number(config.temperature).toFixed(2);
    elements.modelApiKey.value = "";
    elements.apiKeyStatus.textContent = config.apiKeyConfigured ? "已配置 Key；留空将保留" : "尚未配置 Key";

    elements.modelNameSelect.replaceChildren();
    const grouped = state.modelCatalog.reduce((groups, item) => {
        if (!groups.has(item.family)) groups.set(item.family, []);
        groups.get(item.family).push(item);
        return groups;
    }, new Map());
    grouped.forEach((models, family) => {
        const group = document.createElement("optgroup");
        group.label = family;
        models.forEach(model => {
            const option = document.createElement("option");
            option.value = model.modelName;
            option.textContent = model.label;
            group.append(option);
        });
        elements.modelNameSelect.append(group);
    });
    const customOption = document.createElement("option");
    customOption.value = "__custom__";
    customOption.textContent = "自定义模型…";
    elements.modelNameSelect.append(customOption);
    const known = state.modelCatalog.some(item => item.modelName === config.modelName);
    elements.modelNameSelect.value = known ? config.modelName : "__custom__";
    elements.customModelName.value = known ? "" : config.modelName;
    elements.customModelField.hidden = known;
}

function selectedModelName() {
    return elements.modelNameSelect.value === "__custom__"
        ? elements.customModelName.value.trim()
        : elements.modelNameSelect.value;
}

function inferProvider(modelName, baseUrl) {
    const model = modelName.toLowerCase();
    const url = baseUrl.toLowerCase();
    if (model.startsWith("gemini")) return "gemini";
    if (model.startsWith("claude")) return "anthropic";
    if (model.startsWith("ollama/") || url.includes(":11434")) return "ollama";
    if ((model.startsWith("gpt-") || /^o[134]-/.test(model)) && url.includes("api.openai.com")) {
        return "openai-responses";
    }
    return "openai-compatible";
}

function modelSelectionChanged() {
    const custom = elements.modelNameSelect.value === "__custom__";
    elements.customModelField.hidden = !custom;
    if (!custom) {
        const catalog = state.modelCatalog.find(item => item.modelName === elements.modelNameSelect.value);
        if (catalog) elements.modelBaseUrl.value = catalog.defaultBaseUrl;
    }
    updateInferredProvider();
}

function updateInferredProvider() {
    elements.modelProvider.value = inferProvider(selectedModelName(), elements.modelBaseUrl.value);
}

async function saveSettings(event) {
    event.preventDefault();
    const modelName = selectedModelName();
    if (!modelName) throw new Error("请输入模型名");
    const payload = {
        displayName: elements.modelDisplayName.value.trim(),
        modelName,
        baseUrl: elements.modelBaseUrl.value.trim(),
        apiKey: elements.modelApiKey.value.trim(),
        timeoutMs: Number(elements.modelTimeout.value),
        maxTokens: Number(elements.modelMaxTokens.value),
        temperature: Number(elements.modelTemperature.value),
    };
    const updated = await api(`/api/settings/models/${state.settingsTask}`, {
        method: "PUT",
        body: JSON.stringify(payload),
    });
    state.modelConfigs = state.modelConfigs.map(item => item.taskType === updated.taskType ? updated : item);
    renderSettingsTask();
    await loadConfigurationStatus();
    showToast(`${taskLabels[state.settingsTask]}模型配置已保存`);
}

function resizeComposer() {
    elements.questionInput.style.height = "auto";
    elements.questionInput.style.height = `${Math.min(elements.questionInput.scrollHeight, 180)}px`;
}

function updateCharacterCount() {
    elements.characterCount.textContent = `${elements.questionInput.value.length} / 20000`;
}

function scrollChat(smooth = true) {
    requestAnimationFrame(() => elements.chatScroll.scrollTo({
        top: elements.chatScroll.scrollHeight,
        behavior: smooth ? "smooth" : "auto",
    }));
}

function sizeLabel(size) {
    if (size < 1024) return `${size} B`;
    return `${(size / 1024).toFixed(1)} KB`;
}

function openMobilePanel(id) {
    document.getElementById(id)?.classList.add("mobile-open");
    elements.panelScrim.classList.add("visible");
}

function closeMobilePanels() {
    $("#sidebar").classList.remove("mobile-open");
    $("#activityPanel").classList.remove("mobile-open");
    elements.panelScrim.classList.remove("visible");
}

function guarded(action) {
    return async (...args) => {
        try { await action(...args); } catch (error) { showToast(error.message || "操作失败", true); }
    };
}

$("#composerForm").addEventListener("submit", sendMessage);
$("#newConversationButton").addEventListener("click", guarded(createConversation));
$("#refreshConversationsButton").addEventListener("click", guarded(loadConversations));
$("#addWorkspaceButton").addEventListener("click", guarded(pickWorkspace));
$("#folderBackButton").addEventListener("click", guarded(async () => {
    state.directory = pathParent(state.directory);
    await loadFiles();
}));
elements.workspaceSelect.addEventListener("change", guarded(async () => {
    state.rootId = elements.workspaceSelect.value;
    state.directory = "";
    clearSelectedFile();
    updateWorkspaceMeta();
    await loadFiles();
}));
$("#attachButton").addEventListener("click", guarded(pickAttachment));
elements.attachmentInput.addEventListener("change", uploadAttachment);
elements.previewSelectedButton.addEventListener("click", guarded(() =>
    previewFile(state.rootId, state.selectedPath)));
$("#settingsButton").addEventListener("click", guarded(openSettings));
$("#configureModelsButton").addEventListener("click", guarded(openSettings));
$("#dismissConfigurationNotice").addEventListener("click", () => {
    elements.configurationNotice.hidden = true;
});
$("#closeSettingsButton").addEventListener("click", () => elements.settingsModal.close());
elements.settingsForm.addEventListener("submit", guarded(saveSettings));
elements.modelNameSelect.addEventListener("change", modelSelectionChanged);
elements.customModelName.addEventListener("input", updateInferredProvider);
elements.modelBaseUrl.addEventListener("input", updateInferredProvider);
elements.modelTemperature.addEventListener("input", () => {
    elements.temperatureValue.value = Number(elements.modelTemperature.value).toFixed(2);
});
$("#closePreviewButton").addEventListener("click", () => elements.previewModal.close());
elements.questionInput.addEventListener("input", () => {
    resizeComposer();
    updateCharacterCount();
});
elements.questionInput.addEventListener("keydown", event => {
    if (event.key === "Enter" && !event.shiftKey && !event.isComposing) {
        event.preventDefault();
        $("#composerForm").requestSubmit();
    }
});
document.querySelectorAll("[data-prompt]").forEach(button => button.addEventListener("click", () => {
    elements.questionInput.value = button.dataset.prompt;
    resizeComposer();
    updateCharacterCount();
    elements.questionInput.focus();
}));
document.querySelectorAll("[data-open-panel]").forEach(button =>
    button.addEventListener("click", () => openMobilePanel(button.dataset.openPanel)));
document.querySelectorAll("[data-close-panel]").forEach(button =>
    button.addEventListener("click", closeMobilePanels));
elements.panelScrim.addEventListener("click", closeMobilePanels);

guarded(async () => {
    await Promise.all([loadConversations(), loadWorkspaces(), loadOperations(), loadConfigurationStatus()]);
    resizeComposer();
    updateCharacterCount();
})();
