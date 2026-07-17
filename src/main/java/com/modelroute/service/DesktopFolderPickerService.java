package com.modelroute.service;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DesktopFolderPickerService {

    private final DesktopPickerStateService pickerStateService;

    public DesktopFolderPickerService(DesktopPickerStateService pickerStateService) {
        this.pickerStateService = pickerStateService;
    }

    public Optional<File> chooseDirectory() {
        return choose(
                "选择 ModelRoute Agent 工作区",
                JFileChooser.DIRECTORIES_ONLY,
                false,
                DesktopPickerStateService.PickerType.WORKSPACE);
    }

    public Optional<File> chooseFile() {
        return choose(
                "选择要交给 ModelRoute Agent 的文件",
                JFileChooser.FILES_ONLY,
                true,
                DesktopPickerStateService.PickerType.ATTACHMENT);
    }

    private Optional<File> choose(
            String title,
            int selectionMode,
            boolean acceptAllFiles,
            DesktopPickerStateService.PickerType pickerType) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_IMPLEMENTED,
                    "Native file selection is unavailable in a headless environment");
        }

        AtomicReference<File> selection = new AtomicReference<>();
        AtomicReference<Path> visitedDirectory = new AtomicReference<>();
        Runnable chooserTask = () -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to Swing's default look and feel.
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(title);
            chooser.setFileSelectionMode(selectionMode);
            chooser.setAcceptAllFileFilterUsed(acceptAllFiles);
            pickerStateService.initialDirectory(pickerType)
                    .map(Path::toFile)
                    .ifPresent(chooser::setCurrentDirectory);
            int result = chooser.showOpenDialog(null);
            File selected = result == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
            if (selected != null) {
                selection.set(selected);
            }
            if (selected != null && pickerType == DesktopPickerStateService.PickerType.WORKSPACE) {
                visitedDirectory.set(selected.toPath());
            } else if (selected != null && selected.getParentFile() != null) {
                visitedDirectory.set(selected.getParentFile().toPath());
            } else if (chooser.getCurrentDirectory() != null) {
                visitedDirectory.set(chooser.getCurrentDirectory().toPath());
            }
        };

        try {
            if (SwingUtilities.isEventDispatchThread()) {
                chooserTask.run();
            } else {
                SwingUtilities.invokeAndWait(chooserTask);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File selection was interrupted");
        } catch (InvocationTargetException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to open the native folder selector",
                    exception.getCause());
        }
        pickerStateService.remember(pickerType, visitedDirectory.get());
        return Optional.ofNullable(selection.get());
    }
}
