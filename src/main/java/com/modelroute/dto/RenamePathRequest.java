package com.modelroute.dto;

import jakarta.validation.constraints.NotBlank;

public record RenamePathRequest(
        @NotBlank(message = "sourcePath must not be blank") String sourcePath,
        @NotBlank(message = "targetPath must not be blank") String targetPath) {
}
