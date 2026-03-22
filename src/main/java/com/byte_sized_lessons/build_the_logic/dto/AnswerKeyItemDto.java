package com.byte_sized_lessons.build_the_logic.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AnswerKeyItemDto(
    @Min(1) int questionOrder,
    @NotNull JsonNode solution
) {
}
