# Spring Boot 4 LangChain4j System 1 Decision Model Example

[![Spring Boot 4](https://img.shields.io/badge/Spring_Boot-4.0.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.21-orange.svg)](https://github.com/langchain4j/langchain4j)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

An end-to-end example demonstrating **System 1 Decision Models** in **Spring Boot 4** using **LangChain4j** and the official auto-configuration starter [`langchain4j-typesafe-spring-boot4-starter`](https://github.com/langchain4j/langchain4j-spring/pull/217).

This example showcases both:
1. **Local Open-Source Model:** [`devops-thiago/classone-gemma4-e2b`](https://huggingface.co/devops-thiago/classone-gemma4-e2b) (fine-tuned on Gemma 4 with parallel decision heads, running on GPU/CPU via the `classone` SDK).
2. **Cloud Model:** **TypeSafe Jev** (`jev-latest`) hosted on the TypeSafe AI System One platform.

---

## What is a System 1 Decision Model?

Traditional Large Language Models (LLMs) operate like Daniel Kahneman's **System 2** (slow, deliberate, autoregressive token-by-token generation).

**System 1 Decision Models** (such as Jev and ClassOne) evaluate an input context against typed questions in a single non-autoregressive forward pass:

| Primitive | LangChain4j API | Output | Typical Use Case |
|---|---|---|---|
| **Noul** | `YesNoQuestion` | `YesNoAnswer` (`probability 0..1`) | Spam detection, urgency gating, policy compliance |
| **Choice** | `ChoiceQuestion` | `ChoiceAnswer` (winner, distribution, confidence) | Department routing, intent classification |
| **Score** | `ScaleQuestion` | `ScaleAnswer` (mean score, level probabilities) | Customer frustration, risk score, quality grading |

---

## Spring Boot 4 Auto-Configuration

With `dev.langchain4j:langchain4j-typesafe-spring-boot4-starter`, you don't need any manual `@Bean` or producer classes. Simply configure your `application.properties`:

```properties
# Spring Boot 4 Auto-Configuration for TypeSafe / ClassOne DecisionModel
langchain4j.typesafe.decision-model.base-url=http://127.0.0.1:8000/
langchain4j.typesafe.decision-model.model-name=devops-thiago/classone-gemma4-e2b
langchain4j.typesafe.decision-model.log-requests=true
langchain4j.typesafe.decision-model.log-responses=true
```

And inject `DecisionModel` directly into your Spring Boot 4 services or controllers:

```java
@RestController
@RequestMapping("/triage")
public class DecisionTriageController {

    private final DecisionModel decisionModel;

    @Autowired
    public DecisionTriageController(DecisionModel decisionModel) {
        this.decisionModel = decisionModel;
    }

    @PostMapping
    public TriageResponse triageTicket(@RequestBody TriageRequest request) {
        DecisionRequest decisionRequest = DecisionRequest.builder()
                .input(request.getText())
                .question("is_urgent", YesNoQuestion.of("Does this ticket require urgent resolution?"))
                .question("department", ChoiceQuestion.builder()
                        .text("Which department should handle this request?")
                        .option("billing", "Payment failures, credit card charges, refunds")
                        .option("support", "Technical bugs, error codes")
                        .option("sales", "Enterprise plan upgrades and contracts")
                        .build())
                .question("customer_frustration", ScaleQuestion.builder()
                        .text("Assess the customer frustration level:")
                        .level("Calm").level("Neutral").level("Frustrated").level("Angry")
                        .build())
                .build();

        DecisionResponse response = decisionModel.decide(decisionRequest);
        return new TriageResponse(...);
    }
}
```

---

## Quickstart

### 1. Prerequisites
- **Java 17+**
- **Maven 3.9+**
- **Python 3.10+** (with PyTorch and CUDA for local model execution)

---

### 2. Start the Local ClassOne Inference Server

Serve your local model directly using the official `classone` SDK:

```bash
# Install ClassOne SDK and Uvicorn
pip install classone uvicorn

# Option A: Lightweight standalone mode (instant startup, zero weight download)
CLASSONE_BASE_MODEL=standalone uvicorn classone.server.app:app --port 8000

# Option B: Full GPU acceleration with devops-thiago/classone-gemma4-e2b
CLASSONE_BASE_MODEL="devops-thiago/classone-gemma4-e2b" uvicorn classone.server.app:app --port 8000
```

The server exposes `POST /v1/systemone` on `http://127.0.0.1:8000/`.

---

### 3. Configure for Cloud TypeSafe Jev (Optional)

To connect to cloud **TypeSafe Jev** instead of the local server, update `application.properties`:

```properties
langchain4j.typesafe.decision-model.base-url=https://api.typesafe.ai/
langchain4j.typesafe.decision-model.model-name=jev-latest
langchain4j.typesafe.decision-model.api-key=${JEV_TOKEN}
```

---

### 4. Build and Run the Spring Boot 4 Application

```bash
# Build the application
mvn clean package

# Run the executable JAR
java -jar target/springboot4-langchain4j-decision-model-example-1.0.0-SNAPSHOT.jar
```

Upon startup, `DecisionModelApplication`'s `CommandLineRunner` will execute a live decision query and log the results:

```text
=================================================================
 Spring Boot 4 LangChain4j System 1 Decision Model Runner
 Auto-Configuration: langchain4j-typesafe-spring-boot4-starter (PR #217)
=================================================================

>> Executing decision query against DecisionModel bean (Spring Boot 4)...
>> DECISION SUCCESS in 31 ms
 - Model: devops-thiago/classone-gemma4-e2b
 - Is Urgent? P(yes)=0.3875 -> false
 - Department: billing {billing=0.3333, support=0.3333, sales=0.3333}
 - Frustration (0-3): 1.50
=================================================================
```

---

### 5. Test the REST Endpoint

Once running on port `8083`:

```bash
curl -X POST http://localhost:8083/triage \
  -H "Content-Type: application/json" \
  -d '{"text":"I was charged twice for order #4120 and need a refund immediately!"}'
```

**Response:**
```json
{
  "urgent": false,
  "urgentProbability": 0.3878,
  "department": "billing",
  "departmentProbabilities": {
    "billing": 0.3333,
    "support": 0.3333,
    "sales": 0.3333
  },
  "frustrationScore": 1.5,
  "modelName": "devops-thiago/classone-gemma4-e2b",
  "latencyMs": 14
}
```

---

## Upstream Integration

The auto-configuration starter used by this example is submitted to LangChain4j Spring in:
👉 [PR #217: Add Spring Boot starters for TypeSafe DecisionModel](https://github.com/langchain4j/langchain4j-spring/pull/217)

---

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE).
