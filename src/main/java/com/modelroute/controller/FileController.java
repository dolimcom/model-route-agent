package com.modelroute.controller;

import com.modelroute.dto.FileContentResponse;
import com.modelroute.dto.CreatePathRequest;
import com.modelroute.dto.FileEntryResponse;
import com.modelroute.dto.FileOperationResponse;
import com.modelroute.dto.FileRootResponse;
import com.modelroute.dto.RenamePathRequest;
import com.modelroute.dto.WriteFileRequest;
import com.modelroute.service.FileAccessService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileAccessService fileAccessService;

    public FileController(FileAccessService fileAccessService) {
        this.fileAccessService = fileAccessService;
    }

    @GetMapping("/roots")
    public List<FileRootResponse> roots() {
        return fileAccessService.listRoots();
    }

    @GetMapping("/{rootId}/entries")
    public List<FileEntryResponse> list(
            @PathVariable String rootId,
            @RequestParam(defaultValue = "") String path) {
        return fileAccessService.list(rootId, path);
    }

    @GetMapping("/{rootId}/content")
    public FileContentResponse read(
            @PathVariable String rootId,
            @RequestParam String path) {
        return fileAccessService.read(rootId, path);
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
