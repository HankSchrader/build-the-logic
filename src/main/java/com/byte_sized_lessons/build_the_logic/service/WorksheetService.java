package com.byte_sized_lessons.build_the_logic.service;

import com.byte_sized_lessons.build_the_logic.dto.AnswerKeyDto;
import com.byte_sized_lessons.build_the_logic.dto.AnswerKeyItemDto;
import com.byte_sized_lessons.build_the_logic.dto.GenerateWorksheetRequest;
import com.byte_sized_lessons.build_the_logic.dto.QuestionDto;
import com.byte_sized_lessons.build_the_logic.dto.WorksheetDto;
import com.byte_sized_lessons.build_the_logic.dto.WorksheetMetadataDto;
import com.byte_sized_lessons.build_the_logic.exception.InvalidWorksheetRequestException;
import com.byte_sized_lessons.build_the_logic.exception.ResourceNotFoundException;
import com.byte_sized_lessons.build_the_logic.model.DifficultyLevel;
import com.byte_sized_lessons.build_the_logic.model.QuestionType;
import com.byte_sized_lessons.build_the_logic.model.Worksheet;
import com.byte_sized_lessons.build_the_logic.model.WorksheetQuestion;
import com.byte_sized_lessons.build_the_logic.repository.WorksheetRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorksheetService {

    private static final List<QuestionType> QUESTION_TYPES = List.of(
        QuestionType.FILL_IN_STEPS,
        QuestionType.REORDER_STEPS,
        QuestionType.WRITE_ALGORITHM,
        QuestionType.PREDICT_OUTPUT,
        QuestionType.DEBUG_CODE
    );

    private final WorksheetRepository worksheetRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public WorksheetDto generateWorksheet(GenerateWorksheetRequest request) {
        Worksheet worksheet = buildWorksheet(request);
        return toDto(worksheet);
    }

    @Transactional
    public WorksheetDto generateAndSaveWorksheet(GenerateWorksheetRequest request) {
        Worksheet worksheet = buildWorksheet(request);
        Worksheet savedWorksheet = worksheetRepository.save(worksheet);
        return toDto(savedWorksheet);
    }

    @Transactional(readOnly = true)
    public WorksheetDto getWorksheetById(Long worksheetId) {
        Worksheet worksheet = worksheetRepository.findById(worksheetId)
            .orElseThrow(() -> new ResourceNotFoundException("Worksheet with id %d was not found".formatted(worksheetId)));
        return toDto(worksheet);
    }

    private Worksheet buildWorksheet(GenerateWorksheetRequest request) {
        DifficultyLevel difficulty = parseDifficulty(request.difficulty());
        String normalizedTopic = normalizeTopic(request.topic());

        Worksheet worksheet = Worksheet.builder()
            .topic(normalizedTopic)
            .difficulty(difficulty)
            .title("%s Worksheet (%s)".formatted(capitalize(request.topic()), capitalize(difficulty.name())))
            .instructions(buildInstructions(request.topic(), difficulty))
            .numQuestions(request.numQuestions())
            .createdAt(Instant.now())
            .build();

        for (int index = 0; index < request.numQuestions(); index++) {
            QuestionType type = QUESTION_TYPES.get(index % QUESTION_TYPES.size());
            worksheet.addQuestion(WorksheetQuestion.builder()
                .questionOrder(index + 1)
                .type(type)
                .prompt(buildPrompt(normalizedTopic, type, index + 1))
                .contentJson(writeJson(buildQuestionContent(normalizedTopic, type, difficulty, index + 1)))
                .answerKeyJson(writeJson(buildAnswerKey(normalizedTopic, type, index + 1)))
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
        return "Complete each %s activity about %s. Show your reasoning clearly so the worksheet can be reused in print and PDF formats."
            .formatted(difficulty.name().toLowerCase(Locale.ROOT), topic.toLowerCase(Locale.ROOT));
    }

    private String normalizeTopic(String topic) {
        return topic.trim().toLowerCase(Locale.ROOT);
    }

    private String capitalize(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }

    private String buildPrompt(String topic, QuestionType type, int order) {
        return switch (type) {
            case FILL_IN_STEPS -> "Question %d: Complete the missing steps for the %s routine.".formatted(order, topic);
            case REORDER_STEPS -> "Question %d: Put the %s steps back in the correct order.".formatted(order, topic);
            case WRITE_ALGORITHM -> "Question %d: Write an algorithm that solves the %s challenge.".formatted(order, topic);
            case PREDICT_OUTPUT -> "Question %d: Predict the output of the %s program.".formatted(order, topic);
            case DEBUG_CODE -> "Question %d: Find and fix the bug in the %s example.".formatted(order, topic);
        };
    }

    private JsonNode buildQuestionContent(String topic, QuestionType type, DifficultyLevel difficulty, int order) {
        ObjectNode content = objectMapper.createObjectNode();
        content.put("topic", topic);
        content.put("difficulty", difficulty.name());
        content.put("layout", "STANDARD");

        return switch (type) {
            case FILL_IN_STEPS -> content
                .put("template", "fill_in_steps")
                .set("steps", array("Start %s task".formatted(topic), "____", "Check the result", "Finish"));
            case REORDER_STEPS -> content
                .put("template", "reorder_steps")
                .set("steps", array("Celebrate the solution", "Trace the logic", "Read the prompt", "Compare with expected output"));
            case WRITE_ALGORITHM -> content
                .put("template", "write_algorithm")
                .put("scenario", "Design a repeatable process for %s example %d".formatted(topic, order))
                .set("constraints", array("Use 4-6 steps", "Be precise", "Keep it printable"));
            case PREDICT_OUTPUT -> content
                .put("template", "predict_output")
                .put("language", "pseudocode")
                .put("snippet", "counter = 2\nrepeat 3 times\n  counter = counter + 1\nprint counter");
            case DEBUG_CODE -> content
                .put("template", "debug_code")
                .put("language", "pseudocode")
                .put("snippet", "if score = 10\n  print 'perfect'\nelse\n  print('try again')")
                .put("bugHint", "Check the conditional operator");
        };
    }

    private JsonNode buildAnswerKey(String topic, QuestionType type, int order) {
        ObjectNode answer = objectMapper.createObjectNode();
        answer.put("questionOrder", order);
        return switch (type) {
            case FILL_IN_STEPS -> answer
                .put("expectedStep", "Repeat until the %s task is complete".formatted(topic))
                .put("teacherNote", "Accept equivalent sequencing language.");
            case REORDER_STEPS -> answer
                .set("correctOrder", array("Read the prompt", "Trace the logic", "Compare with expected output", "Celebrate the solution"));
            case WRITE_ALGORITHM -> answer
                .set("rubric", array("Clear start state", "Ordered steps", "Specific actions", "Termination condition"));
            case PREDICT_OUTPUT -> answer
                .put("expectedOutput", "5")
                .put("explanation", "The loop increments the counter three times from an initial value of 2.");
            case DEBUG_CODE -> answer
                .put("fixedSnippet", "if score == 10\n  print 'perfect'\nelse\n  print 'try again'")
                .put("explanation", "Use a comparison operator when checking equality.");
        };
    }

    private ArrayNode array(String... values) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        Arrays.stream(values).forEach(arrayNode::add);
        return arrayNode;
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize worksheet question payload", exception);
        }
    }

    private JsonNode readJson(String value) {
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize worksheet question payload", exception);
        }
    }

    private WorksheetDto toDto(Worksheet worksheet) {
        List<QuestionDto> questions = worksheet.getQuestions().stream()
            .map(question -> QuestionDto.builder()
                .order(question.getQuestionOrder())
                .type(question.getType())
                .prompt(question.getPrompt())
                .content(readJson(question.getContentJson()))
                .answerKey(AnswerKeyItemDto.builder()
                    .questionOrder(question.getQuestionOrder())
                    .solution(readJson(question.getAnswerKeyJson()))
                    .build())
                .build())
            .toList();

        List<AnswerKeyItemDto> answerEntries = questions.stream()
            .map(QuestionDto::answerKey)
            .toList();

        return WorksheetDto.builder()
            .id(worksheet.getId())
            .metadata(WorksheetMetadataDto.builder()
                .topic(worksheet.getTopic())
                .difficulty(worksheet.getDifficulty())
                .title(worksheet.getTitle())
                .instructions(worksheet.getInstructions())
                .questionCount(worksheet.getNumQuestions())
                .createdAt(worksheet.getCreatedAt())
                .estimatedDurationMinutes(Math.max(worksheet.getNumQuestions() * 5, 10))
                .answerKeyIncluded(true)
                .build())
            .questions(questions)
            .answerKey(AnswerKeyDto.builder().entries(answerEntries).build())
            .build();
    }
}
