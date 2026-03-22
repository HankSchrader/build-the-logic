package com.byte_sized_lessons.build_the_logic.dto;

import java.time.Instant;
import java.util.List;

public record WorksheetResponse(
    Long id,
    String topic,
    String difficulty,
    String title,
    String instructions,
    int numQuestions,
    Instant createdAt,
    List<WorksheetQuestionResponse> questions
) {
}
