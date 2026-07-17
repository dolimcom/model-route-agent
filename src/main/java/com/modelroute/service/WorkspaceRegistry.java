package com.modelroute.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.modelroute.config.FileAccessProperties;
import com.modelroute.config.FileAccessProperties.AllowedRoot;
import com.modelroute.config.RuntimeConfigProperties;
import com.modelroute.dto.FileRootResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkspaceRegistry {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceRegistry.class);

    private final RuntimeConfigProperties runtimeProperties;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final Map<String, AllowedRoot> rootsById = new LinkedHashMap<>();
    private final Map<String, Path> singleFileRoots = new LinkedHashMap<>();

    public WorkspaceRegistry(
            FileAccessProperties properties,
            RuntimeConfigProperties runtimeProperties) {
        this.runtimeProperties = runtimeProperties;
        properties.getAllowedRoots().forEach(this::putCopy);
        if (runtimeProperties.isEnabled()) {
            loadPersisted().forEach(this::putCopy);
        }
    }

    public synchronized List<FileRootResponse> list() {
        return rootsById.entrySet().stream()
                .filter(entry -> !singleFileRoots.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .filter(AllowedRoot::isEnabled)
                .map(this::toResponse)
                .sorted(Comparator.comparing(FileRootResponse::displayName))
                .toList();
    }

    public synchronized AllowedRoot required(String rootId) {
        AllowedRoot root = rootsById.get(rootId);
        if (root == null || !root.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File root not found: " + rootId);
        }
        return copy(root);
    }

    public synchronized FileRootResponse register(Path selectedDirectory) {
        Path realPath;
        try {
            realPath = selectedDirectory.toRealPath();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to resolve selected directory", exception);
        }
        if (!Files.isDirectory(realPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected path is not a directory");
        }

        Optional<AllowedRoot> existing = rootsById.values().stream()
                .filter(root -> Path.of(root.getPath()).toAbsolutePath().normalize().equals(realPath))
                .findFirst();
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        AllowedRoot root = new AllowedRoot();
        root.setId(uniqueId(realPath));
        root.setPath(realPath.toString());
        root.setEnabled(true);
        rootsById.put(root.getId(), root);
        persist();
        log.info("Workspace registered: id={}, path={}", root.getId(), realPath);
        return toResponse(root);
    }

    public synchronized SelectedFileRoot registerSelectedFile(Path selectedFile) {
        Path realFile;
        try {
            realFile = selectedFile.toRealPath();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to resolve selected file", exception);
        }
        if (!Files.isRegularFile(realFile)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected path is not a regular file");
        }

        String rootId = "attachment-" + UUID.randomUUID();
        AllowedRoot root = new AllowedRoot();
        root.setId(rootId);
        root.setPath(realFile.getParent().toString());
        root.setEnabled(true);
        rootsById.put(rootId, root);
        singleFileRoots.put(rootId, realFile);
        log.info("Single file authorized temporarily: rootId={}, file={}", rootId, realFile.getFileName());
        return new SelectedFileRoot(rootId, realFile.getFileName().toString());
    }

    public synchronized boolean isSingleFileRoot(String rootId) {
        return singleFileRoots.containsKey(rootId);
    }

    public synchronized void assertPathAllowed(String rootId, Path candidate) {
        Path allowedFile = singleFileRoots.get(rootId);
        if (allowedFile != null && !candidate.toAbsolutePath().normalize().equals(allowedFile)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Path is outside the selected file authorization");
        }
    }

    public synchronized void removeSelectedFile(String rootId) {
        if (singleFileRoots.remove(rootId) != null) {
            rootsById.remove(rootId);
            log.info("Single file authorization removed: rootId={}", rootId);
        }
    }

    public synchronized void remove(String rootId) {
        if (rootsById.remove(rootId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File root not found: " + rootId);
        }
        singleFileRoots.remove(rootId);
        persist();
        log.info("Workspace removed: id={}", rootId);
    }

    private List<AllowedRoot> loadPersisted() {
        Path path = configPath();
        if (!Files.isRegularFile(path)) {
            return List.of();
        }
        try {
            RuntimeWorkspaceFile file = yamlMapper.readValue(path.toFile(), RuntimeWorkspaceFile.class);
            return file.workspaces() == null ? List.of() : file.workspaces();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read runtime workspaces: " + path, exception);
        }
    }

    private void persist() {
        if (!runtimeProperties.isEnabled()) {
            return;
        }
        Path path = configPath();
        Path temporary = null;
        try {
            Files.createDirectories(path.getParent());
            temporary = Files.createTempFile(path.getParent(), "workspaces-", ".tmp");
            yamlMapper.writeValue(temporary.toFile(), new RuntimeWorkspaceFile(
                    rootsById.entrySet().stream()
                            .filter(entry -> !singleFileRoots.containsKey(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .toList()));
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist runtime workspaces: " + path, exception);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Best-effort cleanup.
                }
            }
        }
    }

    private String uniqueId(Path path) {
        String name = path.getFileName() == null ? "workspace" : path.getFileName().toString();
        String slug = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_-]+", "-")
                .replaceAll("^-+|-+$", "");
        if (slug.isBlank()) {
            slug = "workspace";
        }
        String candidate = slug;
        int suffix = 2;
        while (rootsById.containsKey(candidate)) {
            candidate = slug + "-" + suffix++;
        }
        return candidate;
    }

    private FileRootResponse toResponse(AllowedRoot root) {
        Path path = Path.of(root.getPath()).toAbsolutePath().normalize();
        String displayName = path.getFileName() == null ? root.getId() : path.getFileName().toString();
        return new FileRootResponse(root.getId(), displayName, path.toString(), root.isEnabled());
    }

    private void putCopy(AllowedRoot root) {
        rootsById.put(root.getId(), copy(root));
    }

    private AllowedRoot copy(AllowedRoot source) {
        AllowedRoot copy = new AllowedRoot();
        copy.setId(source.getId());
        copy.setPath(source.getPath());
        copy.setEnabled(source.isEnabled());
        return copy;
    }

    private Path configPath() {
        return Path.of(runtimeProperties.getWorkspacesFile()).toAbsolutePath().normalize();
    }

    private record RuntimeWorkspaceFile(List<AllowedRoot> workspaces) {
    }

    public record SelectedFileRoot(String rootId, String relativePath) {
    }
}
