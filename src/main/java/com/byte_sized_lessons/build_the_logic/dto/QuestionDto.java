package com.byte_sized_lessons.build_the_logic.dto;

import com.byte_sized_lessons.build_the_logic.model.QuestionType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record QuestionDto(
    @Min(1) int order,
    @NotNull QuestionType type,
    @NotBlank String prompt,
    @NotNull JsonNode content,
    @Valid AnswerKeyItemDto answerKey
) {
}
