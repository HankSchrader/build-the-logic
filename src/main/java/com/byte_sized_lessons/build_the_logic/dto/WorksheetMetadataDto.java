package com.byte_sized_lessons.build_the_logic.dto;

import com.byte_sized_lessons.build_the_logic.model.DifficultyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Builder;

@Builder
public record WorksheetMetadataDto(
    @NotBlank String topic,
    @NotNull DifficultyLevel difficulty,
    @NotBlank String title,
    @NotBlank String instructions,
    @Min(1) int questionCount,
    @NotNull Instant createdAt,
    @Min(1) int estimatedDurationMinutes,
    boolean answerKeyIncluded
) {
}
