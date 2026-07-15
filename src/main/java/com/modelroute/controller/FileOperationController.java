package com.modelroute.controller;

import com.modelroute.domain.FileOperationStatus;
import com.modelroute.dto.FileOperationProposalRequest;
import com.modelroute.dto.FileOperationProposalResponse;
import com.modelroute.service.FileOperationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file-operations")
public class FileOperationController {

    private final FileOperationService operationService;

    public FileOperationController(FileOperationService operationService) {
        this.operationService = operationService;
    }

    @PostMapping
    public ResponseEntity<FileOperationProposalResponse> propose(
            @Valid @RequestBody FileOperationProposalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operationService.propose(request));
    }

    @GetMapping
    public List<FileOperationProposalResponse> list(
            @RequestParam(required = false) FileOperationStatus status) {
        return operationService.list(status);
    }

    @PostMapping("/{operationId}/approve")
    public FileOperationProposalResponse approve(@PathVariable String operationId) {
        return operationService.approve(operationId);
    }

    @PostMapping("/{operationId}/reject")
    public FileOperationProposalResponse reject(@PathVariable String operationId) {
        return operationService.reject(operationId);
    }

    @PostMapping("/{operationId}/rollback")
    public FileOperationProposalResponse rollback(@PathVariable String operationId) {
        return operationService.rollback(operationId);
    }

    @PostMapping("/rollback-last")
    public FileOperationProposalResponse rollbackLast() {
        return operationService.rollbackLast();
    }
}
