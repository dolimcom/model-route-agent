package com.modelroute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modelroute.config.FileAccessProperties;
import com.modelroute.config.FileAccessProperties.AllowedRoot;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;

class FileAccessServiceTest {

    @TempDir
    Path tempDirectory;

    private FileAccessService fileAccessService;

    @BeforeEach
    void setUp() throws Exception {
        Files.writeString(tempDirectory.resolve("notes.txt"), "workspace note");
        Files.createDirectories(tempDirectory.resolve("nested"));

        FileAccessProperties properties = new FileAccessProperties();
        AllowedRoot root = new AllowedRoot();
        root.setId("test-root");
        root.setPath(tempDirectory.toString());
        properties.setAllowedRoots(List.of(root));
        fileAccessService = new FileAccessService(properties);
    }

    @Test
    void listsConfiguredRootAndEntries() {
        assertThat(fileAccessService.listRoots()).extracting("id").containsExactly("test-root");
        assertThat(fileAccessService.list("test-root", ""))
                .extracting("relativePath")
                .contains("notes.txt", "nested");
    }

    @Test
    void readsTextFileWithinAllowedRoot() {
        assertThat(fileAccessService.read("test-root", "notes.txt").content()).isEqualTo("workspace note");
    }

    @Test
    void rejectsPathTraversal() {
        assertThatThrownBy(() -> fileAccessService.read("test-root", "../outside.txt"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("outside the allowed root");
    }

    @Test
    void rejectsUnknownRoot() {
        assertThatThrownBy(() -> fileAccessService.list("missing-root", ""))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("File root not found");
    }

    @Test
    void createsUpdatesRenamesAndDeletesFile() {
        fileAccessService.createDirectory("test-root", "operations");
        fileAccessService.createFile("test-root", "operations/draft.txt", "first version");
        assertThat(fileAccessService.read("test-root", "operations/draft.txt").content())
                .isEqualTo("first version");

        fileAccessService.updateFile("test-root", "operations/draft.txt", "second version");
        fileAccessService.rename("test-root", "operations/draft.txt", "operations/final.txt");

        assertThat(fileAccessService.read("test-root", "operations/final.txt").content())
                .isEqualTo("second version");
        assertThat(Files.exists(tempDirectory.resolve("operations/draft.txt"))).isFalse();

        fileAccessService.delete("test-root", "operations/final.txt");
        fileAccessService.delete("test-root", "operations");
        assertThat(Files.exists(tempDirectory.resolve("operations"))).isFalse();
    }

    @Test
    void refusesToOverwriteExistingTarget() {
        assertThatThrownBy(() -> fileAccessService.createFile("test-root", "notes.txt", "replacement"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
        assertThat(fileAccessService.read("test-root", "notes.txt").content()).isEqualTo("workspace note");
    }

    @Test
    void rejectsWritePathTraversal() {
        assertThatThrownBy(() -> fileAccessService.createFile("test-root", "../outside.txt", "blocked"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("outside the allowed root");
    }

    @Test
    void rejectsDeletingNonEmptyDirectory() throws Exception {
        Path directory = Files.createDirectories(tempDirectory.resolve("non-empty"));
        Files.writeString(directory.resolve("child.txt"), "keep me");

        assertThatThrownBy(() -> fileAccessService.delete("test-root", "non-empty"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("recursive deletion is disabled");
    }
}
