package com.skillforge.backend.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record SubmitAttemptRequest(
        @NotEmpty Map<Long, String> answers
) {
}
