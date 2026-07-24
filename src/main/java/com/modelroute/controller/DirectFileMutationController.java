package com.modelroute.controller;

import com.modelroute.dto.CreatePathRequest;
import com.modelroute.dto.FileOperationResponse;
import com.modelroute.dto.RenamePathRequest;
import com.modelroute.dto.WriteFileRequest;
import com.modelroute.service.FileAccessService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Development-only file mutation API. Agent-driven changes must use the approval workflow.
 */
@RestController
@RequestMapping("/api/files")
@ConditionalOnProperty(prefix = "model-route.files", name = "direct-mutation-enabled", havingValue = "true")
public class DirectFileMutationController {

    private final FileAccessService fileAccessService;

    public DirectFileMutationController(FileAccessService fileAccessService) {
        this.fileAccessService = fileAccessService;
    }

    @PostMapping("/{rootId}/directories")
    public ResponseEntity<FileOperationResponse> createDirectory(
            @PathVariable String rootId,
            @Valid @RequestBody CreatePathRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileAccessService.createDirectory(rootId, request.path()));
    }

    @PostMapping("/{rootId}/files")
    public ResponseEntity<FileOperationResponse> createFile(
            @PathVariable String rootId,
            @Valid @RequestBody WriteFileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileAccessService.createFile(rootId, request.path(), request.content()));
    }

    @PutMapping("/{rootId}/content")
    public FileOperationResponse updateFile(
            @PathVariable String rootId,
            @Valid @RequestBody WriteFileRequest request) {
        return fileAccessService.updateFile(rootId, request.path(), request.content());
    }

    @PostMapping("/{rootId}/rename")
    public FileOperationResponse rename(
            @PathVariable String rootId,
            @Valid @RequestBody RenamePathRequest request) {
        return fileAccessService.rename(rootId, request.sourcePath(), request.targetPath());
    }

    @DeleteMapping("/{rootId}")
    public FileOperationResponse delete(
            @PathVariable String rootId,
            @RequestParam String path) {
        return fileAccessService.delete(rootId, path);
    }
}
