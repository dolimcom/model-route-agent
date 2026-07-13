package com.modelroute.persistence;

import com.modelroute.domain.TaskType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "chat_message")
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", length = 20)
    private TaskType taskType;

    @Column(name = "model_id", length = 100)
    private String modelId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "route_json")
    private Map<String, Object> routeData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ConversationMessage() {
    }

    public ConversationMessage(
            Conversation conversation,
            String role,
            String content,
            TaskType taskType,
            String modelId,
            Map<String, Object> routeData) {
        this.conversation = conversation;
        this.role = role;
        this.content = content;
        this.taskType = taskType;
        this.modelId = modelId;
        this.routeData = routeData;
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public String getModelId() {
        return modelId;
    }

    public Map<String, Object> getRouteData() {
        return routeData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
