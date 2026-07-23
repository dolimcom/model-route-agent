package com.modelroute.service;

import com.modelroute.domain.FileApprovalMode;
import com.modelroute.domain.FileOperationStatus;
import com.modelroute.domain.FileOperationType;
import com.modelroute.dto.FileOperationProposalRequest;
import com.modelroute.dto.FileOperationProposalResponse;
import com.modelroute.persistence.FileOperation;
import com.modelroute.persistence.FileOperationRepository;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileOperationService {

    private static final Logger log = LoggerFactory.getLogger(FileOperationService.class);

    private final FileOperationRepository operationRepository;
    private final FileAccessService fileAccessService;
    private final TransactionTemplate transactions;

    public FileOperationService(
            FileOperationRepository operationRepository,
            FileAccessService fileAccessService,
            PlatformTransactionManager transactionManager) {
        this.operationRepository = operationRepository;
        this.fileAccessService = fileAccessService;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    public FileOperationProposalResponse propose(FileOperationProposalRequest request) {
        String expectedBeforeHash = validateAndFingerprint(request);
        FileOperation operation = inTransaction(() -> operationRepository.save(new FileOperation(
                request.conversationId(),
                request.rootId(),
                request.operationType(),
                normalized(request.sourcePath()),
                normalized(request.targetPath()),
                request.content(),
                expectedBeforeHash,
                request.approvalMode())));

        log.info("File operation proposed: id={}, type={}, root={}, source={}, target={}, mode={}, status={}",
                operation.getOperationId(), operation.getOperationType(), operation.getRootId(),
                operation.getSourcePath(), operation.getTargetPath(), operation.getApprovalMode(),
                operation.getStatus());
        if (request.approvalMode() == FileApprovalMode.FULL_ACCESS) {
            return approve(operation.getOperationId());
        }
        return toResponse(operation);
    }

    public List<FileOperationProposalResponse> list(FileOperationStatus status) {
        List<FileOperation> operations = status == null
                ? operationRepository.findTop50ByOrderByCreatedAtDesc()
                : operationRepository.findTop50ByStatusOrderByCreatedAtDesc(status);
        return operations.stream().map(this::toResponse).toList();
    }

    public FileOperationProposalResponse approve(String operationId) {
        FileOperation operation = inTransaction(() -> {
            FileOperation current = requiredForUpdate(operationId);
            requireStatus(current, FileOperationStatus.PENDING, "Only pending operations can be approved");
            current.startExecution();
            return current;
        });

        ExecutionSnapshot snapshot;
        try {
            snapshot = executeFileSystem(operation);
        } catch (RuntimeException exception) {
            return inTransaction(() -> {
                FileOperation current = requiredForUpdate(operationId);
                requireStatus(current, FileOperationStatus.EXECUTING,
                        "Operation is no longer awaiting execution completion");
                current.executionFailed(errorMessage(exception));
                log.warn("File operation failed: id={}, type={}, error={}",
                        current.getOperationId(), current.getOperationType(), current.getErrorMessage());
                return toResponse(current);
            });
        }

        FileOperation completed = inTransaction(() -> {
            FileOperation current = requiredForUpdate(operationId);
            requireStatus(current, FileOperationStatus.EXECUTING,
                    "Operation is no longer awaiting execution completion");
            current.executed(snapshot.beforeContent(), snapshot.afterHash());
            return current;
        });
        log.info("File operation executed: id={}, type={}, root={}",
                completed.getOperationId(), completed.getOperationType(), completed.getRootId());
        return toResponse(completed);
    }

    public FileOperationProposalResponse reject(String operationId) {
        return inTransaction(() -> {
            FileOperation operation = requiredForUpdate(operationId);
            requireStatus(operation, FileOperationStatus.PENDING, "Only pending operations can be rejected");
            operation.reject();
            log.info("File operation rejected: id={}", operationId);
            return toResponse(operation);
        });
    }

    public FileOperationProposalResponse rollback(String operationId) {
        FileOperation operation = inTransaction(() -> {
            FileOperation current = requiredForUpdate(operationId);
            if (current.getStatus() != FileOperationStatus.EXECUTED
                    && current.getStatus() != FileOperationStatus.ROLLBACK_FAILED) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Only executed operations can be rolled back");
            }
            current.startRollback();
            return current;
        });

        try {
            rollbackFileSystem(operation);
        } catch (RuntimeException exception) {
            return inTransaction(() -> {
                FileOperation current = requiredForUpdate(operationId);
                requireStatus(current, FileOperationStatus.ROLLING_BACK,
                        "Operation is no longer awaiting rollback completion");
                current.rollbackFailed(errorMessage(exception));
                log.warn("File operation rollback failed: id={}, error={}",
                        current.getOperationId(), current.getErrorMessage());
                return toResponse(current);
            });
        }

        FileOperation completed = inTransaction(() -> {
            FileOperation current = requiredForUpdate(operationId);
            requireStatus(current, FileOperationStatus.ROLLING_BACK,
                    "Operation is no longer awaiting rollback completion");
            current.rolledBack();
            return current;
        });
        log.info("File operation rolled back: id={}, type={}",
                completed.getOperationId(), completed.getOperationType());
        return toResponse(completed);
    }

    public FileOperationProposalResponse rollbackLast() {
        FileOperation operation = operationRepository
                .findFirstByStatusOrderByExecutedAtDesc(FileOperationStatus.EXECUTED)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No executed file operation is available for rollback"));
        return rollback(operation.getOperationId());
    }

    private ExecutionSnapshot executeFileSystem(FileOperation operation) {
        FileSnapshot before = sourceSnapshot(operation);
        verifyExpectedFingerprint(operation, before);
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
        return new ExecutionSnapshot(
                before == null ? null : before.content(),
                fingerprintAfterExecution(operation));
    }

    private void rollbackFileSystem(FileOperation operation) {
        verifyRollbackFingerprint(operation);
        switch (operation.getOperationType()) {
            case CREATE_DIRECTORY, CREATE_FILE -> fileAccessService.delete(
                    operation.getRootId(), operation.getTargetPath());
            case UPDATE_FILE -> fileAccessService.updateFile(
                    operation.getRootId(), operation.getSourcePath(), operation.getBeforeContent());
            case RENAME -> fileAccessService.rename(
                    operation.getRootId(), operation.getTargetPath(), operation.getSourcePath());
            case DELETE -> restoreDeletedPath(operation);
        }
    }

    private FileSnapshot sourceSnapshot(FileOperation operation) {
        return switch (operation.getOperationType()) {
            case UPDATE_FILE, RENAME, DELETE -> fileAccessService.snapshot(
                    operation.getRootId(), operation.getSourcePath());
            case CREATE_DIRECTORY, CREATE_FILE -> null;
        };
    }

    private void verifyExpectedFingerprint(FileOperation operation, FileSnapshot before) {
        String current = before == null ? null : before.fingerprint();
        if (!Objects.equals(operation.getExpectedBeforeHash(), current)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Source changed after the operation was proposed; create a new proposal");
        }
    }

    private String fingerprintAfterExecution(FileOperation operation) {
        return switch (operation.getOperationType()) {
            case CREATE_DIRECTORY, CREATE_FILE, RENAME -> fileAccessService.snapshot(
                    operation.getRootId(), operation.getTargetPath()).fingerprint();
            case UPDATE_FILE -> fileAccessService.snapshot(
                    operation.getRootId(), operation.getSourcePath()).fingerprint();
            case DELETE -> null;
        };
    }

    private void verifyRollbackFingerprint(FileOperation operation) {
        String current = switch (operation.getOperationType()) {
            case CREATE_DIRECTORY, CREATE_FILE, RENAME -> currentFingerprint(
                    operation.getRootId(), operation.getTargetPath());
            case UPDATE_FILE -> currentFingerprint(operation.getRootId(), operation.getSourcePath());
            case DELETE -> currentFingerprint(operation.getRootId(), operation.getSourcePath());
        };
        if (operation.getOperationType() == FileOperationType.DELETE) {
            if (current != null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Deleted path was recreated after execution; rollback would overwrite it");
            }
            return;
        }
        if (!Objects.equals(operation.getAfterHash(), current)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "File changed after execution; rollback would overwrite newer content");
        }
    }

    private String currentFingerprint(String rootId, String path) {
        try {
            return fileAccessService.snapshot(rootId, path).fingerprint();
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }
            throw exception;
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

    private String validateAndFingerprint(FileOperationProposalRequest request) {
        if (request.operationType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "operationType must not be null");
        }
        if (request.approvalMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "approvalMode must not be null");
        }
        return switch (request.operationType()) {
            case CREATE_DIRECTORY -> {
                requirePath(request.targetPath(), "targetPath");
                yield null;
            }
            case CREATE_FILE -> {
                requirePath(request.targetPath(), "targetPath");
                requireContent(request.content());
                yield null;
            }
            case UPDATE_FILE -> {
                requirePath(request.sourcePath(), "sourcePath");
                requireContent(request.content());
                yield fileAccessService.snapshot(request.rootId(), normalized(request.sourcePath())).fingerprint();
            }
            case RENAME, DELETE -> {
                requirePath(request.sourcePath(), "sourcePath");
                if (request.operationType() == FileOperationType.RENAME) {
                    requirePath(request.targetPath(), "targetPath");
                }
                yield fileAccessService.snapshot(request.rootId(), normalized(request.sourcePath())).fingerprint();
            }
        };
    }

    private FileOperation requiredForUpdate(String operationId) {
        return operationRepository.findByOperationIdForUpdate(operationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "File operation not found: " + operationId));
    }

    private void requireStatus(FileOperation operation, FileOperationStatus expected, String message) {
        if (operation.getStatus() != expected) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
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

    private String normalized(String value) {
        return StringUtils.hasText(value) ? value.trim().replace('\\', '/') : null;
    }

    private String errorMessage(RuntimeException exception) {
        if (exception instanceof ResponseStatusException statusException
                && StringUtils.hasText(statusException.getReason())) {
            return statusException.getReason();
        }
        return StringUtils.hasText(exception.getMessage())
                ? exception.getMessage()
                : exception.getClass().getSimpleName();
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

    private <T> T inTransaction(Supplier<T> action) {
        return transactions.execute(status -> action.get());
    }

    private record ExecutionSnapshot(String beforeContent, String afterHash) {
    }
}
