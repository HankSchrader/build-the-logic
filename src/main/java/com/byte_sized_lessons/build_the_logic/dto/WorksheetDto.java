package com.byte_sized_lessons.build_the_logic.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record WorksheetDto(
    Long id,
    @NotNull @Valid WorksheetMetadataDto metadata,
    @Size(max = 20) List<@Valid QuestionDto> questions,
    @Valid AnswerKeyDto answerKey
) {
}
