package dev.langchain4j.spring.example;

import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.request.ChoiceQuestion;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.request.ScaleQuestion;
import dev.langchain4j.model.decision.request.YesNoQuestion;
import dev.langchain4j.model.decision.response.ChoiceAnswer;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.decision.response.ScaleAnswer;
import dev.langchain4j.model.decision.response.YesNoAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        String input = request != null && request.getText() != null
                ? request.getText()
                : "Customer inquiry without details";

        DecisionRequest decisionRequest = DecisionRequest.builder()
                .input(input)
                .question("is_urgent", YesNoQuestion.of("Does this ticket require urgent resolution?"))
                .question("department", ChoiceQuestion.builder()
                        .text("Which department should handle this request?")
                        .option("billing", "Payment failures, credit card charges, refunds")
                        .option("support", "Technical bugs, error codes")
                        .option("sales", "Enterprise plan upgrades and contracts")
                        .build())
                .question("customer_frustration", ScaleQuestion.builder()
                        .text("Assess the customer frustration level:")
                        .level("Calm")
                        .level("Neutral")
                        .level("Frustrated")
                        .level("Angry")
                        .build())
                .build();

        long start = System.currentTimeMillis();
        DecisionResponse decisionResponse = decisionModel.decide(decisionRequest);
        long latency = System.currentTimeMillis() - start;

        YesNoAnswer urgent = decisionResponse.yesNo("is_urgent");
        ChoiceAnswer dept = decisionResponse.choice("department");
        ScaleAnswer frustration = decisionResponse.scale("customer_frustration");

        return new TriageResponse(
                urgent.probability() > 0.5,
                urgent.probability(),
                dept.value(),
                dept.probabilities(),
                frustration.mean(),
                decisionResponse.modelName(),
                latency
        );
    }
}
