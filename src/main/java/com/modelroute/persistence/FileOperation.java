package com.modelroute.persistence;

import com.modelroute.domain.FileApprovalMode;
import com.modelroute.domain.FileOperationStatus;
import com.modelroute.domain.FileOperationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "file_operation")
public class FileOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", nullable = false, unique = true, length = 36)
    private String operationId;

    @Column(name = "conversation_id", length = 48)
    private String conversationId;

    @Column(name = "root_id", nullable = false, length = 100)
    private String rootId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 32)
    private FileOperationType operationType;

    @Column(name = "source_path", length = 1024)
    private String sourcePath;

    @Column(name = "target_path", length = 1024)
    private String targetPath;

    @Column(name = "proposed_content", columnDefinition = "MEDIUMTEXT")
    private String proposedContent;

    @Column(name = "before_content", columnDefinition = "MEDIUMTEXT")
    private String beforeContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_mode", nullable = false, length = 20)
    private FileApprovalMode approvalMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private FileOperationStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "rolled_back_at")
    private LocalDateTime rolledBackAt;

    protected FileOperation() {
    }

    public FileOperation(
            String conversationId,
            String rootId,
            FileOperationType operationType,
            String sourcePath,
            String targetPath,
            String proposedContent,
            FileApprovalMode approvalMode) {
        this.operationId = UUID.randomUUID().toString();
        this.conversationId = conversationId;
        this.rootId = rootId;
        this.operationType = operationType;
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.proposedContent = proposedContent;
        this.approvalMode = approvalMode;
        this.status = FileOperationStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getRootId() {
        return rootId;
    }

    public FileOperationType getOperationType() {
        return operationType;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getProposedContent() {
        return proposedContent;
    }

    public String getBeforeContent() {
        return beforeContent;
    }

    public FileApprovalMode getApprovalMode() {
        return approvalMode;
    }

    public FileOperationStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public LocalDateTime getRolledBackAt() {
        return rolledBackAt;
    }

    public void approve() {
        status = FileOperationStatus.APPROVED;
        approvedAt = LocalDateTime.now();
        errorMessage = null;
    }

    public void reject() {
        status = FileOperationStatus.REJECTED;
        errorMessage = null;
    }

    public void executed(String beforeContent) {
        this.beforeContent = beforeContent;
        status = FileOperationStatus.EXECUTED;
        executedAt = LocalDateTime.now();
        errorMessage = null;
    }

    public void failed(String errorMessage) {
        status = FileOperationStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void rolledBack() {
        status = FileOperationStatus.ROLLED_BACK;
        rolledBackAt = LocalDateTime.now();
        errorMessage = null;
    }
}
