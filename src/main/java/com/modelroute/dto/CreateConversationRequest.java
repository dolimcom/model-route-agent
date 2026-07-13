package com.modelroute.dto;

import jakarta.validation.constraints.Size;

public record CreateConversationRequest(@Size(max = 120, message = "title must be at most 120 characters") String title) {
}
