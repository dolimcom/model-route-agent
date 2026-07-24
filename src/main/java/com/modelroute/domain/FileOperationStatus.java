package com.modelroute.domain;

public enum FileOperationStatus {
    PENDING,
    EXECUTING,
    REJECTED,
    EXECUTED,
    EXECUTION_FAILED,
    ROLLING_BACK,
    ROLLBACK_FAILED,
    ROLLED_BACK
}
