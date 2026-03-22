# Build the Logic

## Project summary
Build the Logic is a Spring Boot backend for generating unplugged coding worksheets that can be previewed as JSON, persisted for later retrieval, and exported to PDF. The worksheet payload is designed around strongly typed worksheet metadata plus flexible per-question content blocks so the system can grow to support multiple pedagogical formats such as fill-in-the-steps, reordering, algorithm writing, output prediction, and debugging exercises.

## DTO and domain design
- `WorksheetDto` exposes a clean API contract with typed metadata, a list of `QuestionDto` entries, and a top-level `AnswerKeyDto` section.
- `QuestionDto` keeps stable fields such as `order`, `type`, and `prompt` strongly typed while using a flexible JSON `content` structure for question-specific payloads.
- `WorksheetQuestion` persists the dynamic `content` and `answerKey` sections as JSON strings, which keeps the relational model simple while remaining extensible for future PDF rendering or template-specific mappers.
- `QuestionType` currently supports `FILL_IN_STEPS`, `REORDER_STEPS`, `WRITE_ALGORITHM`, `PREDICT_OUTPUT`, and `DEBUG_CODE`.

## Example JSON serialization
```json
{
  "id": 42,
  "metadata": {
    "topic": "loops",
    "difficulty": "BASIC",
    "title": "Loops Worksheet (Basic)",
    "instructions": "Complete each basic activity about loops. Show your reasoning clearly so the worksheet can be reused in print and PDF formats.",
    "questionCount": 2,
    "createdAt": "2026-03-22T10:15:30Z",
    "estimatedDurationMinutes": 10,
    "answerKeyIncluded": true
  },
  "questions": [
    {
      "order": 1,
      "type": "FILL_IN_STEPS",
      "prompt": "Question 1: Complete the missing steps for the loops routine.",
      "content": {
        "topic": "loops",
        "difficulty": "BASIC",
        "layout": "STANDARD",
        "template": "fill_in_steps",
        "steps": ["Start loops task", "____", "Check the result", "Finish"]
      },
      "answerKey": {
        "questionOrder": 1,
        "solution": {
          "questionOrder": 1,
          "expectedStep": "Repeat until the loops task is complete",
          "teacherNote": "Accept equivalent sequencing language."
        }
      }
    },
    {
      "order": 2,
      "type": "PREDICT_OUTPUT",
      "prompt": "Question 2: Predict the output of the loops program.",
      "content": {
        "topic": "loops",
        "difficulty": "BASIC",
        "layout": "STANDARD",
        "template": "predict_output",
        "language": "pseudocode",
        "snippet": "counter = 2\nrepeat 3 times\n  counter = counter + 1\nprint counter"
      },
      "answerKey": {
        "questionOrder": 2,
        "solution": {
          "questionOrder": 2,
          "expectedOutput": "5",
          "explanation": "The loop increments the counter three times from an initial value of 2."
        }
      }
    }
  ],
  "answerKey": {
    "entries": [
      {
        "questionOrder": 1,
        "solution": {
          "questionOrder": 1,
          "expectedStep": "Repeat until the loops task is complete",
          "teacherNote": "Accept equivalent sequencing language."
        }
      },
      {
        "questionOrder": 2,
        "solution": {
          "questionOrder": 2,
          "expectedOutput": "5",
          "explanation": "The loop increments the counter three times from an initial value of 2."
        }
      }
    ]
  }
}
```

## Run Spring Boot locally
### Prerequisites
- Java 21+
- Maven 3.9+ or use the included Maven wrapper

### Start the app
```bash
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080` by default.

## Manual testing with curl
### Generate a worksheet without saving
```bash
curl -X POST http://localhost:8080/api/worksheets/generate \
  -H 'Content-Type: application/json' \
  -d '{
    "topic": "loops",
    "difficulty": "basic",
    "numQuestions": 5
  }'
```

### Generate and save a worksheet
```bash
curl -X POST http://localhost:8080/api/worksheets \
  -H 'Content-Type: application/json' \
  -d '{
    "topic": "conditionals",
    "difficulty": "intermediate",
    "numQuestions": 4
  }'
```

### Fetch a saved worksheet
```bash
curl http://localhost:8080/api/worksheets/1
```

### Export a worksheet as PDF
```bash
curl http://localhost:8080/api/worksheets/1/export --output worksheet-1.pdf
```
