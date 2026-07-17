package com.modelroute.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.modelroute.config.RuntimeConfigProperties;
import com.modelroute.service.DesktopPickerStateService.PickerType;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DesktopPickerStateServiceTest {

    @TempDir
    Path tempDirectory;

    @Test
    void persistsWorkspaceAndAttachmentDirectoriesIndependently() throws Exception {
        Path workspaceDirectory = Files.createDirectory(tempDirectory.resolve("workspace"));
        Path nextWorkspaceDirectory = Files.createDirectory(tempDirectory.resolve("next-workspace"));
        Path attachmentDirectory = Files.createDirectory(tempDirectory.resolve("attachments"));
        RuntimeConfigProperties properties = properties();

        DesktopPickerStateService state = new DesktopPickerStateService(properties);
        state.remember(PickerType.WORKSPACE, workspaceDirectory);
        state.remember(PickerType.ATTACHMENT, attachmentDirectory);

        DesktopPickerStateService reloaded = new DesktopPickerStateService(properties);
        assertThat(reloaded.initialDirectory(PickerType.WORKSPACE)).contains(workspaceDirectory.toRealPath());
        assertThat(reloaded.initialDirectory(PickerType.ATTACHMENT)).contains(attachmentDirectory.toRealPath());

        reloaded.remember(PickerType.WORKSPACE, nextWorkspaceDirectory);
        DesktopPickerStateService updated = new DesktopPickerStateService(properties);
        assertThat(updated.initialDirectory(PickerType.WORKSPACE)).contains(nextWorkspaceDirectory.toRealPath());
        assertThat(updated.initialDirectory(PickerType.ATTACHMENT)).contains(attachmentDirectory.toRealPath());
    }

    private RuntimeConfigProperties properties() {
        RuntimeConfigProperties properties = new RuntimeConfigProperties();
        properties.setChooserStateFile(tempDirectory.resolve("chooser-state.local.yml").toString());
        return properties;
    }
}
