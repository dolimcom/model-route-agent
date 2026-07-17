package com.modelroute.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.modelroute.config.RuntimeConfigProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DesktopPickerStateService {

    private static final Logger log = LoggerFactory.getLogger(DesktopPickerStateService.class);

    private final RuntimeConfigProperties properties;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private Path workspaceDirectory;
    private Path attachmentDirectory;

    public DesktopPickerStateService(RuntimeConfigProperties properties) {
        this.properties = properties;
        load();
    }

    public synchronized Optional<Path> initialDirectory(PickerType type) {
        Path directory = type == PickerType.WORKSPACE ? workspaceDirectory : attachmentDirectory;
        return directory != null && Files.isDirectory(directory)
                ? Optional.of(directory)
                : Optional.empty();
    }

    public synchronized void remember(PickerType type, Path directory) {
        Path resolved = resolveDirectory(directory);
        if (resolved == null) {
            return;
        }
        if (type == PickerType.WORKSPACE) {
            workspaceDirectory = resolved;
        } else {
            attachmentDirectory = resolved;
        }
        persist();
        log.debug("Desktop picker location saved: type={}, directory={}", type, resolved);
    }

    private void load() {
        if (!properties.isEnabled()) {
            return;
        }
        Path path = statePath();
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            PickerStateFile state = yamlMapper.readValue(path.toFile(), PickerStateFile.class);
            workspaceDirectory = resolveDirectory(path(state.workspaceDirectory()));
            attachmentDirectory = resolveDirectory(path(state.attachmentDirectory()));
        } catch (IOException | RuntimeException exception) {
            log.warn("Unable to load desktop picker state from {}: {}", path, exception.getMessage());
        }
    }

    private void persist() {
        if (!properties.isEnabled()) {
            return;
        }
        Path path = statePath();
        Path temporary = null;
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            temporary = Files.createTempFile(parent, "chooser-state-", ".tmp");
            yamlMapper.writeValue(temporary.toFile(), new PickerStateFile(
                    value(workspaceDirectory),
                    value(attachmentDirectory)));
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            log.warn("Unable to persist desktop picker state to {}: {}", path, exception.getMessage());
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Best-effort cleanup for optional desktop state.
                }
            }
        }
    }

    private Path resolveDirectory(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            return null;
        }
        try {
            return directory.toRealPath();
        } catch (IOException exception) {
            return null;
        }
    }

    private Path path(String value) {
        return StringUtils.hasText(value) ? Path.of(value) : null;
    }

    private String value(Path value) {
        return value == null ? null : value.toString();
    }

    private Path statePath() {
        return Path.of(properties.getChooserStateFile()).toAbsolutePath().normalize();
    }

    public enum PickerType {
        WORKSPACE,
        ATTACHMENT
    }

    private record PickerStateFile(String workspaceDirectory, String attachmentDirectory) {
    }
}
