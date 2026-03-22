package com.byte_sized_lessons.build_the_logic.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;

@Builder
public record AnswerKeyDto(
    @NotEmpty List<@Valid AnswerKeyItemDto> entries
) {
}
