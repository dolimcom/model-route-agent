package com.modelroute.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePathRequest(@NotBlank(message = "path must not be blank") String path) {
}
