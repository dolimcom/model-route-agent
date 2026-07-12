package com.modelroute.router;

import com.modelroute.domain.TaskType;

/**
 * A single interpretable routing signal extracted from the user input.
 */
public record RuleSignal(TaskType taskType, String type, String detail, boolean strong) {
}
