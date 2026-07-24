package com.modelroute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.modelroute.config.FileAccessProperties;
import com.modelroute.config.FileAccessProperties.AllowedRoot;
import com.modelroute.config.RuntimeConfigProperties;
import com.modelroute.domain.FileApprovalMode;
import com.modelroute.domain.FileOperationStatus;
import com.modelroute.domain.FileOperationType;
import com.modelroute.dto.FileOperationProposalRequest;
import com.modelroute.dto.FileOperationProposalResponse;
import com.modelroute.persistence.FileOperation;
import com.modelroute.persistence.FileOperationRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

class FileOperationServiceTest {

    @TempDir
    Path tempDirectory;

    private final AtomicReference<FileOperation> savedOperation = new AtomicReference<>();
    private FileOperationRepository operationRepository;
    private FileOperationService operationService;

    @BeforeEach
    void setUp() {
        FileAccessProperties properties = new FileAccessProperties();
        AllowedRoot root = new AllowedRoot();
        root.setId("test-root");
        root.setPath(tempDirectory.toString());
        properties.setAllowedRoots(List.of(root));

        operationRepository = mock(FileOperationRepository.class);
        when(operationRepository.save(any(FileOperation.class))).thenAnswer(invocation -> {
            FileOperation operation = invocation.getArgument(0);
            savedOperation.set(operation);
            return operation;
        });
        when(operationRepository.findByOperationIdForUpdate(anyString())).thenAnswer(invocation -> {
            FileOperation operation = savedOperation.get();
            return operation != null && operation.getOperationId().equals(invocation.getArgument(0))
                    ? Optional.of(operation)
                    : Optional.empty();
        });
        RuntimeConfigProperties runtime = new RuntimeConfigProperties();
        runtime.setEnabled(false);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        operationService = new FileOperationService(operationRepository,
                new FileAccessService(properties, new WorkspaceRegistry(properties, runtime)),
                transactionManager);
    }

    @Test
    void manualOperationWaitsForApprovalAndCanBeRolledBack() {
        FileOperationProposalResponse proposal = operationService.propose(request(
                FileOperationType.CREATE_FILE,
                null,
                "manual.txt",
                "approved content",
                FileApprovalMode.MANUAL));

        assertThat(proposal.status()).isEqualTo(FileOperationStatus.PENDING);
        assertThat(Files.exists(tempDirectory.resolve("manual.txt"))).isFalse();

        FileOperationProposalResponse approved = operationService.approve(proposal.operationId());
        assertThat(approved.status()).isEqualTo(FileOperationStatus.EXECUTED);
        assertThat(tempDirectory.resolve("manual.txt")).hasContent("approved content");

        FileOperationProposalResponse rolledBack = operationService.rollback(proposal.operationId());
        assertThat(rolledBack.status()).isEqualTo(FileOperationStatus.ROLLED_BACK);
        assertThat(Files.exists(tempDirectory.resolve("manual.txt"))).isFalse();
    }

    @Test
    void fullAccessExecutesImmediatelyAndUpdateRollbackRestoresContent() throws Exception {
        Files.writeString(tempDirectory.resolve("notes.txt"), "before");

        FileOperationProposalResponse proposal = operationService.propose(request(
                FileOperationType.UPDATE_FILE,
                "notes.txt",
                null,
                "after",
                FileApprovalMode.FULL_ACCESS));

        assertThat(proposal.status()).isEqualTo(FileOperationStatus.EXECUTED);
        assertThat(proposal.beforeContent()).isEqualTo("before");
        assertThat(tempDirectory.resolve("notes.txt")).hasContent("after");

        operationService.rollback(proposal.operationId());
        assertThat(tempDirectory.resolve("notes.txt")).hasContent("before");
    }

    @Test
    void rejectedOperationNeverTouchesTheFileSystem() {
        FileOperationProposalResponse proposal = operationService.propose(request(
                FileOperationType.CREATE_DIRECTORY,
                null,
                "rejected-directory",
                null,
                FileApprovalMode.MANUAL));

        FileOperationProposalResponse rejected = operationService.reject(proposal.operationId());

        assertThat(rejected.status()).isEqualTo(FileOperationStatus.REJECTED);
        assertThat(Files.exists(tempDirectory.resolve("rejected-directory"))).isFalse();
    }

    @Test
    void rejectsMissingSourceBeforeCreatingPendingProposal() {
        assertThatThrownBy(() -> operationService.propose(request(
                FileOperationType.UPDATE_FILE,
                "missing.txt",
                null,
                "new content",
                FileApprovalMode.MANUAL)))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("File or directory not found");
        assertThat(savedOperation.get()).isNull();
    }

    @Test
    void refusesApprovalWhenSourceChangedAfterProposal() throws Exception {
        Path source = tempDirectory.resolve("changing.txt");
        Files.writeString(source, "proposal version");
        FileOperationProposalResponse proposal = operationService.propose(request(
                FileOperationType.UPDATE_FILE,
                "changing.txt",
                null,
                "agent version",
                FileApprovalMode.MANUAL));

        Files.writeString(source, "external version");
        FileOperationProposalResponse result = operationService.approve(proposal.operationId());

        assertThat(result.status()).isEqualTo(FileOperationStatus.EXECUTION_FAILED);
        assertThat(result.errorMessage()).contains("changed after the operation was proposed");
        assertThat(source).hasContent("external version");
    }

    @Test
    void refusesRollbackWhenFileChangedAfterExecution() throws Exception {
        Path source = tempDirectory.resolve("rollback-conflict.txt");
        Files.writeString(source, "before");
        FileOperationProposalResponse executed = operationService.propose(request(
                FileOperationType.UPDATE_FILE,
                "rollback-conflict.txt",
                null,
                "agent version",
                FileApprovalMode.FULL_ACCESS));

        Files.writeString(source, "external version");
        FileOperationProposalResponse result = operationService.rollback(executed.operationId());

        assertThat(result.status()).isEqualTo(FileOperationStatus.ROLLBACK_FAILED);
        assertThat(result.errorMessage()).contains("rollback would overwrite newer content");
        assertThat(source).hasContent("external version");
    }

    private FileOperationProposalRequest request(
            FileOperationType type,
            String sourcePath,
            String targetPath,
            String content,
            FileApprovalMode approvalMode) {
        return new FileOperationProposalRequest(
                null, "test-root", type, sourcePath, targetPath, content, approvalMode);
    }

}
