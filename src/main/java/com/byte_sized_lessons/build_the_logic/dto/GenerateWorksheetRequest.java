package com.byte_sized_lessons.build_the_logic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateWorksheetRequest(
    @NotBlank(message = "Topic is required")
    String topic,
    @NotNull(message = "Difficulty is required")
    String difficulty,
    @Min(value = 1, message = "At least one question is required")
    @Max(value = 20, message = "A worksheet can contain at most 20 questions")
    int numQuestions
) {
}
