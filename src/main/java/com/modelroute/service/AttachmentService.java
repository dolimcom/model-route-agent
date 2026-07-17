package com.modelroute.service;

import com.modelroute.config.RuntimeConfigProperties;
import com.modelroute.dto.AttachmentResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.InvalidPathException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);
    private static final Duration RETENTION = Duration.ofHours(1);
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "txt", "md", "java", "kt", "kts", "groovy", "xml", "json", "yaml", "yml",
            "properties", "ini", "cfg", "conf", "sql", "py", "js", "jsx", "ts", "tsx",
            "html", "css", "scss", "csv", "log", "sh", "ps1", "bat", "c", "h", "cpp",
            "hpp", "go", "rs", "rb", "php", "vue", "svelte");

    private final long maxBytes;
    private final WorkspaceRegistry workspaceRegistry;
    private final Map<String, StoredAttachment> attachments = new ConcurrentHashMap<>();

    public AttachmentService(RuntimeConfigProperties properties, WorkspaceRegistry workspaceRegistry) {
        this.maxBytes = properties.getMaxAttachmentBytes();
        this.workspaceRegistry = workspaceRegistry;
        if (maxBytes < 1) {
            throw new IllegalStateException("model-route.runtime.max-attachment-bytes must be positive");
        }
    }

    public AttachmentResponse store(MultipartFile file) {
        cleanupExpired();
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attachment must not be empty");
        }
        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Attachment exceeds the configured limit of " + maxBytes + " bytes");
        }
        String fileName = safeFileName(file.getOriginalFilename());
        validateExtension(fileName);

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read attachment", exception);
        }
        String mediaType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : "text/plain";
        return store(fileName, mediaType, bytes, null);
    }

    public AttachmentResponse store(Path selectedFile) {
        cleanupExpired();
        if (selectedFile == null || !Files.isRegularFile(selectedFile)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected attachment is not a regular file");
        }
        String fileName = safeFileName(selectedFile.getFileName().toString());
        validateExtension(fileName);
        byte[] bytes;
        try {
            long size = Files.size(selectedFile);
            if (size > maxBytes) {
                throw new ResponseStatusException(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "Attachment exceeds the configured limit of " + maxBytes + " bytes");
            }
            bytes = Files.readAllBytes(selectedFile);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read selected attachment", exception);
        }

        WorkspaceRegistry.SelectedFileRoot selectedRoot = workspaceRegistry.registerSelectedFile(selectedFile);
        try {
            String detectedType = Files.probeContentType(selectedFile);
            return store(fileName, StringUtils.hasText(detectedType) ? detectedType : "text/plain", bytes, selectedRoot);
        } catch (IOException exception) {
            workspaceRegistry.removeSelectedFile(selectedRoot.rootId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to inspect selected attachment", exception);
        } catch (RuntimeException exception) {
            workspaceRegistry.removeSelectedFile(selectedRoot.rootId());
            throw exception;
        }
    }

    private AttachmentResponse store(
            String fileName,
            String mediaType,
            byte[] bytes,
            WorkspaceRegistry.SelectedFileRoot selectedRoot) {
        String content = decodeUtf8(bytes);
        if (content.indexOf('\0') >= 0) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Binary attachments are not supported");
        }

        String id = UUID.randomUUID().toString();
        boolean editable = selectedRoot != null;
        String rootId = editable ? selectedRoot.rootId() : null;
        String relativePath = editable ? selectedRoot.relativePath() : null;
        StoredAttachment stored = new StoredAttachment(
                new AttachedText(
                        id, fileName, mediaType, content, bytes.length,
                        editable, rootId, relativePath),
                Instant.now());
        attachments.put(id, stored);
        log.info("Text attachment accepted: id={}, name={}, size={}, editable={}",
                id, fileName, bytes.length, editable);
        return new AttachmentResponse(
                id, fileName, bytes.length, mediaType, preview(content), editable, rootId, relativePath);
    }

    public AttachedText required(String attachmentId) {
        cleanupExpired();
        StoredAttachment stored = attachments.get(attachmentId);
        if (stored == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found or expired");
        }
        return stored.attachment();
    }

    public void remove(String attachmentId) {
        if (attachmentId != null) {
            StoredAttachment removed = attachments.remove(attachmentId);
            removeAuthorization(removed);
        }
    }

    private void validateExtension(String fileName) {
        String extension = extension(fileName);
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported attachment type: ." + extension);
        }
    }

    private String decodeUtf8(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Attachment must be valid UTF-8 text",
                    exception);
        }
    }

    private String safeFileName(String originalName) {
        String value = StringUtils.hasText(originalName) ? originalName : "attachment.txt";
        try {
            return Path.of(value).getFileName().toString();
        } catch (InvalidPathException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid attachment file name", exception);
        }
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 || dot == fileName.length() - 1
                ? ""
                : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String preview(String content) {
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 160 ? normalized : normalized.substring(0, 160) + "...";
    }

    private void cleanupExpired() {
        Instant cutoff = Instant.now().minus(RETENTION);
        attachments.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().createdAt().isBefore(cutoff);
            if (expired) {
                removeAuthorization(entry.getValue());
            }
            return expired;
        });
    }

    private void removeAuthorization(StoredAttachment stored) {
        if (stored != null && stored.attachment().editable()) {
            workspaceRegistry.removeSelectedFile(stored.attachment().rootId());
        }
    }

    private record StoredAttachment(AttachedText attachment, Instant createdAt) {
    }
}
