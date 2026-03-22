package com.byte_sized_lessons.build_the_logic.service;

import com.byte_sized_lessons.build_the_logic.dto.GenerateWorksheetRequest;
import com.byte_sized_lessons.build_the_logic.dto.WorksheetQuestionResponse;
import com.byte_sized_lessons.build_the_logic.dto.WorksheetResponse;
import com.byte_sized_lessons.build_the_logic.exception.InvalidWorksheetRequestException;
import com.byte_sized_lessons.build_the_logic.exception.ResourceNotFoundException;
import com.byte_sized_lessons.build_the_logic.model.DifficultyLevel;
import com.byte_sized_lessons.build_the_logic.model.Worksheet;
import com.byte_sized_lessons.build_the_logic.model.WorksheetQuestion;
import com.byte_sized_lessons.build_the_logic.repository.WorksheetRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorksheetService {

    private static final Map<String, List<String>> QUESTION_BANK = Map.of(
        "loops", List.of(
            "Trace the loop and write the output after each repetition.",
            "Circle the repeated instructions in the classroom routine and explain the loop.",
            "Write an unplugged algorithm that uses a repeat 4 times instruction.",
            "Spot the bug: the loop runs one extra time. Explain how to fix it.",
            "Create a pattern using arrows that repeats until you reach the finish square.",
            "Compare a loop with repeated single-step instructions. Which is shorter and why?",
            "Design a treasure hunt path that uses a loop and one turn command."
        ),
        "conditionals", List.of(
            "Read each scenario and choose the correct if/else action.",
            "Match each classroom rule to an if statement.",
            "Find the missing condition in the unplugged algorithm.",
            "Sort actions into true branch and false branch columns.",
            "Write an if/else rule for deciding whether a robot should turn left or right."
        ),
        "algorithms", List.of(
            "Put the algorithm cards in the correct order.",
            "Identify the step that should come first in the sequence.",
            "Rewrite the instructions so another student can follow them exactly.",
            "Find the ambiguous instruction and improve it.",
            "Create a five-step algorithm for packing a school bag."
        )
    );

    @Transactional(readOnly = true)
    public WorksheetResponse generateWorksheet(GenerateWorksheetRequest request) {
        Worksheet worksheet = buildWorksheet(request);
        return toResponse(worksheet);
    }

    @Transactional
    public WorksheetResponse generateAndSaveWorksheet(GenerateWorksheetRequest request) {
        Worksheet worksheet = buildWorksheet(request);
        Worksheet savedWorksheet = worksheetRepository.save(worksheet);
        return toResponse(savedWorksheet);
    }

    @Transactional(readOnly = true)
    public WorksheetResponse getWorksheetById(Long worksheetId) {
        Worksheet worksheet = worksheetRepository.findById(worksheetId)
            .orElseThrow(() -> new ResourceNotFoundException("Worksheet with id %d was not found".formatted(worksheetId)));
        return toResponse(worksheet);
    }

    private final WorksheetRepository worksheetRepository;

    private Worksheet buildWorksheet(GenerateWorksheetRequest request) {
        DifficultyLevel difficulty = parseDifficulty(request.difficulty());
        List<String> templates = QUESTION_BANK.get(normalizeTopic(request.topic()));

        if (templates == null || templates.isEmpty()) {
            throw new InvalidWorksheetRequestException("No worksheet template is available for topic '%s'".formatted(request.topic()));
        }

        Worksheet worksheet = Worksheet.builder()
            .topic(normalizeTopic(request.topic()))
            .difficulty(difficulty)
            .title("%s Worksheet (%s)".formatted(capitalize(request.topic()), capitalize(difficulty.name())))
            .instructions(buildInstructions(request.topic(), difficulty))
            .numQuestions(request.numQuestions())
            .createdAt(Instant.now())
            .build();

        for (int index = 0; index < request.numQuestions(); index++) {
            String prompt = templates.get(index % templates.size());
            worksheet.addQuestion(WorksheetQuestion.builder()
                .questionOrder(index + 1)
                .prompt(prompt)
                .build());
        }

        return worksheet;
    }

    private DifficultyLevel parseDifficulty(String difficultyValue) {
        try {
            return DifficultyLevel.valueOf(difficultyValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidWorksheetRequestException(
                "Difficulty must be one of: BASIC, INTERMEDIATE, ADVANCED"
            );
        }
    }

    private String buildInstructions(String topic, DifficultyLevel difficulty) {
        return "Complete each %s activity about %s. Show your thinking, discuss patterns aloud, and explain your final answer."
            .formatted(difficulty.name().toLowerCase(Locale.ROOT), topic.toLowerCase(Locale.ROOT));
    }

    private String normalizeTopic(String topic) {
        return topic.trim().toLowerCase(Locale.ROOT);
    }

    private String capitalize(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }

    private WorksheetResponse toResponse(Worksheet worksheet) {
        List<WorksheetQuestionResponse> questions = worksheet.getQuestions().stream()
            .map(question -> new WorksheetQuestionResponse(question.getQuestionOrder(), question.getPrompt()))
            .toList();

        return new WorksheetResponse(
            worksheet.getId(),
            worksheet.getTopic(),
            worksheet.getDifficulty().name(),
            worksheet.getTitle(),
            worksheet.getInstructions(),
            worksheet.getNumQuestions(),
            worksheet.getCreatedAt(),
            questions
        );
    }
}
