package com.modelroute.dto;

import com.modelroute.domain.TaskType;
import java.util.List;

public record ModelConfigurationStatusResponse(
        boolean runtimeFileExists,
        int configuredSlots,
        int totalSlots,
        boolean fullyConfigured,
        List<TaskType> mockTasks) {
}
