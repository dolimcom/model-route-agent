package com.modelroute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WriteFileRequest(
        @NotBlank(message = "path must not be blank") String path,
        @NotNull(message = "content must not be null") String content) {
}
