package com.modelroute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modelroute.config.RuntimeConfigProperties;
import com.modelroute.config.FileAccessProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class AttachmentServiceTest {

    @TempDir
    Path tempDirectory;

    private AttachmentService attachmentService;
    private WorkspaceRegistry workspaceRegistry;
    private FileAccessProperties fileAccessProperties;

    @BeforeEach
    void setUp() {
        RuntimeConfigProperties properties = new RuntimeConfigProperties();
        properties.setMaxAttachmentBytes(64);
        properties.setEnabled(false);
        fileAccessProperties = new FileAccessProperties();
        workspaceRegistry = new WorkspaceRegistry(fileAccessProperties, properties);
        attachmentService = new AttachmentService(properties, workspaceRegistry);
    }

    @Test
    void acceptsUtf8TextAndReturnsStoredContent() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "Demo.java", "text/plain", "class Demo {}".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        var response = attachmentService.store(file);

        assertThat(response.fileName()).isEqualTo("Demo.java");
        assertThat(response.editable()).isFalse();
        assertThat(attachmentService.required(response.id()).content()).isEqualTo("class Demo {}");
    }

    @Test
    void selectedLocalFileIsEditableWithoutAuthorizingSiblingFiles() throws Exception {
        Path selected = tempDirectory.resolve("selected.txt");
        Path sibling = tempDirectory.resolve("private.txt");
        Files.writeString(selected, "before");
        Files.writeString(sibling, "private");

        var response = attachmentService.store(selected);
        FileAccessService fileAccessService = new FileAccessService(fileAccessProperties, workspaceRegistry);

        assertThat(response.editable()).isTrue();
        fileAccessService.updateFile(response.rootId(), response.relativePath(), "after");
        assertThat(Files.readString(selected)).isEqualTo("after");
        assertThatThrownBy(() -> fileAccessService.read(response.rootId(), sibling.getFileName().toString()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("selected file authorization");
    }

    @Test
    void rejectsUnsupportedAndOversizedFiles() {
        assertThatThrownBy(() -> attachmentService.store(new MockMultipartFile(
                "file", "image.png", "image/png", new byte[] {1, 2, 3})))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unsupported attachment type");
        assertThatThrownBy(() -> attachmentService.store(new MockMultipartFile(
                "file", "large.txt", "text/plain", new byte[65])))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("configured limit");
    }
}
