package com.modelroute.service;

import com.modelroute.domain.FileApprovalMode;
import com.modelroute.domain.FileOperationStatus;
import com.modelroute.domain.FileOperationType;
import com.modelroute.dto.FileOperationProposalRequest;
import com.modelroute.dto.FileOperationProposalResponse;
import com.modelroute.persistence.FileOperation;
import com.modelroute.persistence.FileOperationRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileOperationService {

    private static final Logger log = LoggerFactory.getLogger(FileOperationService.class);

    private final FileOperationRepository operationRepository;
    private final FileAccessService fileAccessService;

    public FileOperationService(
            FileOperationRepository operationRepository,
            FileAccessService fileAccessService) {
        this.operationRepository = operationRepository;
        this.fileAccessService = fileAccessService;
    }

    @Transactional
    public FileOperationProposalResponse propose(FileOperationProposalRequest request) {
        validate(request);
        FileOperation operation = operationRepository.save(new FileOperation(
                request.conversationId(),
                request.rootId(),
                request.operationType(),
                normalized(request.sourcePath()),
                normalized(request.targetPath()),
                request.content(),
                request.approvalMode()));

        if (request.approvalMode() == FileApprovalMode.FULL_ACCESS) {
            operation.approve();
            execute(operation);
        }
        log.info("File operation proposed: id={}, type={}, root={}, source={}, target={}, mode={}, status={}",
                operation.getOperationId(), operation.getOperationType(), operation.getRootId(),
                operation.getSourcePath(), operation.getTargetPath(), operation.getApprovalMode(),
                operation.getStatus());
        return toResponse(operation);
    }

    @Transactional(readOnly = true)
    public List<FileOperationProposalResponse> list(FileOperationStatus status) {
        List<FileOperation> operations = status == null
                ? operationRepository.findTop50ByOrderByCreatedAtDesc()
                : operationRepository.findTop50ByStatusOrderByCreatedAtDesc(status);
        return operations.stream().map(this::toResponse).toList();
    }

    @Transactional
    public FileOperationProposalResponse approve(String operationId) {
        FileOperation operation = requiredForUpdate(operationId);
        requireStatus(operation, FileOperationStatus.PENDING, "Only pending operations can be approved");
        operation.approve();
        execute(operation);
        log.info("File operation approved: id={}, status={}", operationId, operation.getStatus());
        return toResponse(operation);
    }

    @Transactional
    public FileOperationProposalResponse reject(String operationId) {
        FileOperation operation = requiredForUpdate(operationId);
        requireStatus(operation, FileOperationStatus.PENDING, "Only pending operations can be rejected");
        operation.reject();
        log.info("File operation rejected: id={}", operationId);
        return toResponse(operation);
    }

    @Transactional
    public FileOperationProposalResponse rollback(String operationId) {
        FileOperation operation = requiredForUpdate(operationId);
        return rollback(operation);
    }

    @Transactional
    public FileOperationProposalResponse rollbackLast() {
        FileOperation operation = operationRepository
                .findFirstByStatusOrderByExecutedAtDesc(FileOperationStatus.EXECUTED)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No executed file operation is available for rollback"));
        return rollback(requiredForUpdate(operation.getOperationId()));
    }

    private FileOperationProposalResponse rollback(FileOperation operation) {
        requireStatus(operation, FileOperationStatus.EXECUTED, "Only executed operations can be rolled back");
        rollbackExecutedOperation(operation);
        return toResponse(operation);
    }

    private void execute(FileOperation operation) {
        String beforeContent = null;
        try {
            beforeContent = captureBeforeContent(operation);
            switch (operation.getOperationType()) {
                case CREATE_DIRECTORY -> fileAccessService.createDirectory(
                        operation.getRootId(), operation.getTargetPath());
                case CREATE_FILE -> fileAccessService.createFile(
                        operation.getRootId(), operation.getTargetPath(), operation.getProposedContent());
                case UPDATE_FILE -> fileAccessService.updateFile(
                        operation.getRootId(), operation.getSourcePath(), operation.getProposedContent());
                case RENAME -> fileAccessService.rename(
                        operation.getRootId(), operation.getSourcePath(), operation.getTargetPath());
                case DELETE -> fileAccessService.delete(operation.getRootId(), operation.getSourcePath());
            }
            operation.executed(beforeContent);
            log.info("File operation executed: id={}, type={}, root={}",
                    operation.getOperationId(), operation.getOperationType(), operation.getRootId());
        } catch (RuntimeException exception) {
            operation.failed(errorMessage(exception));
            log.warn("File operation failed: id={}, type={}, error={}",
                    operation.getOperationId(), operation.getOperationType(), operation.getErrorMessage());
        }
    }

    private String captureBeforeContent(FileOperation operation) {
        return switch (operation.getOperationType()) {
            case UPDATE_FILE -> fileAccessService.snapshot(
                    operation.getRootId(), operation.getSourcePath()).content();
            case DELETE -> fileAccessService.snapshot(
                    operation.getRootId(), operation.getSourcePath()).content();
            case CREATE_DIRECTORY, CREATE_FILE, RENAME -> null;
        };
    }

    private void rollbackExecutedOperation(FileOperation operation) {
        try {
            switch (operation.getOperationType()) {
                case CREATE_DIRECTORY, CREATE_FILE -> fileAccessService.delete(
                        operation.getRootId(), operation.getTargetPath());
                case UPDATE_FILE -> fileAccessService.updateFile(
                        operation.getRootId(), operation.getSourcePath(), operation.getBeforeContent());
                case RENAME -> fileAccessService.rename(
                        operation.getRootId(), operation.getTargetPath(), operation.getSourcePath());
                case DELETE -> restoreDeletedPath(operation);
            }
            operation.rolledBack();
            log.info("File operation rolled back: id={}, type={}",
                    operation.getOperationId(), operation.getOperationType());
        } catch (RuntimeException exception) {
            operation.failed("Rollback failed: " + errorMessage(exception));
            log.warn("File operation rollback failed: id={}, error={}",
                    operation.getOperationId(), operation.getErrorMessage());
        }
    }

    private void restoreDeletedPath(FileOperation operation) {
        if (operation.getBeforeContent() == null) {
            fileAccessService.createDirectory(operation.getRootId(), operation.getSourcePath());
        } else {
            fileAccessService.createFile(
                    operation.getRootId(), operation.getSourcePath(), operation.getBeforeContent());
        }
    }

    private FileOperation requiredForUpdate(String operationId) {
        return operationRepository.findByOperationIdForUpdate(operationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "File operation not found: " + operationId));
    }

    private void requireStatus(
            FileOperation operation,
            FileOperationStatus expected,
            String message) {
        if (operation.getStatus() != expected) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }
    }

    private void validate(FileOperationProposalRequest request) {
        switch (request.operationType()) {
            case CREATE_DIRECTORY -> requirePath(request.targetPath(), "targetPath");
            case CREATE_FILE -> {
                requirePath(request.targetPath(), "targetPath");
                requireContent(request.content());
            }
            case UPDATE_FILE -> {
                requirePath(request.sourcePath(), "sourcePath");
                requireContent(request.content());
                requireExistingSource(request.rootId(), request.sourcePath());
            }
            case RENAME -> {
                requirePath(request.sourcePath(), "sourcePath");
                requirePath(request.targetPath(), "targetPath");
                requireExistingSource(request.rootId(), request.sourcePath());
            }
            case DELETE -> {
                requirePath(request.sourcePath(), "sourcePath");
                requireExistingSource(request.rootId(), request.sourcePath());
            }
        }
    }

    private void requirePath(String path, String fieldName) {
        if (!StringUtils.hasText(path)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
        }
    }

    private void requireContent(String content) {
        if (content == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content must not be null");
        }
    }

    private void requireExistingSource(String rootId, String sourcePath) {
        fileAccessService.snapshot(rootId, normalized(sourcePath));
    }

    private String normalized(String value) {
        return StringUtils.hasText(value) ? value.trim().replace('\\', '/') : null;
    }

    private String errorMessage(RuntimeException exception) {
        if (exception instanceof ResponseStatusException statusException
                && StringUtils.hasText(statusException.getReason())) {
            return statusException.getReason();
        }
        return StringUtils.hasText(exception.getMessage()) ? exception.getMessage() : exception.getClass().getSimpleName();
    }

    private FileOperationProposalResponse toResponse(FileOperation operation) {
        return new FileOperationProposalResponse(
                operation.getOperationId(),
                operation.getConversationId(),
                operation.getRootId(),
                operation.getOperationType(),
                operation.getSourcePath(),
                operation.getTargetPath(),
                operation.getProposedContent(),
                operation.getBeforeContent(),
                operation.getApprovalMode(),
                operation.getStatus(),
                operation.getErrorMessage(),
                operation.getCreatedAt(),
                operation.getApprovedAt(),
                operation.getExecutedAt(),
                operation.getRolledBackAt());
    }
}
