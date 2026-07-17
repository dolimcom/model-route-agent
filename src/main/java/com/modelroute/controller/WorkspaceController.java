package com.modelroute.controller;

import com.modelroute.dto.FileRootResponse;
import com.modelroute.service.DesktopFolderPickerService;
import com.modelroute.service.WorkspaceRegistry;
import java.io.File;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceRegistry workspaceRegistry;
    private final DesktopFolderPickerService folderPickerService;

    public WorkspaceController(
            WorkspaceRegistry workspaceRegistry,
            DesktopFolderPickerService folderPickerService) {
        this.workspaceRegistry = workspaceRegistry;
        this.folderPickerService = folderPickerService;
    }

    @GetMapping
    public List<FileRootResponse> list() {
        return workspaceRegistry.list();
    }

    @PostMapping("/pick")
    public ResponseEntity<FileRootResponse> pick() {
        return folderPickerService.chooseDirectory()
                .map(File::toPath)
                .map(workspaceRegistry::register)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping("/{rootId}")
    public ResponseEntity<Void> remove(@PathVariable String rootId) {
        workspaceRegistry.remove(rootId);
        return ResponseEntity.noContent().build();
    }
}
