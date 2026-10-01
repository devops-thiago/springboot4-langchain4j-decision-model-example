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
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DecisionModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(DecisionModelApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(DecisionModel decisionModel) {
        return args -> {
            System.out.println("\n=================================================================");
            System.out.println(" Spring Boot 4 LangChain4j System 1 Decision Model Runner");
            System.out.println(" Auto-Configuration: langchain4j-typesafe-spring-boot4-starter (PR #217)");
            System.out.println("=================================================================");

            String ticket = "Customer #5512: I was billed twice for my renewal order and received an internal server error!";

            DecisionRequest request = DecisionRequest.builder()
                    .input(ticket)
                    .question("is_urgent", YesNoQuestion.of("Does this ticket require immediate attention?"))
                    .question("department", ChoiceQuestion.builder()
                            .text("Route ticket to:")
                            .option("billing", "Billing and charges")
                            .option("support", "Technical bugs")
                            .option("sales", "Upgrades and contracts")
                            .build())
                    .question("frustration", ScaleQuestion.builder()
                            .text("Customer frustration:")
                            .level("Calm")
                            .level("Neutral")
                            .level("Frustrated")
                            .level("Angry")
                            .build())
                    .build();

            System.out.println("\n>> Executing decision query against DecisionModel bean (Spring Boot 4)...");
            try {
                long start = System.currentTimeMillis();
                DecisionResponse response = decisionModel.decide(request);
                long latency = System.currentTimeMillis() - start;

                YesNoAnswer urgent = response.yesNo("is_urgent");
                ChoiceAnswer dept = response.choice("department");
                ScaleAnswer frustration = response.scale("frustration");

                System.out.println(">> DECISION SUCCESS in " + latency + " ms");
                System.out.println(" - Model: " + response.modelName());
                System.out.println(" - Is Urgent? P(yes)=" + String.format("%.4f", urgent.probability()) + " -> " + (urgent.probability() > 0.5));
                System.out.println(" - Department: " + dept.value() + " " + dept.probabilities());
                System.out.println(" - Frustration (0-3): " + String.format("%.2f", frustration.mean()));
            } catch (Exception e) {
                System.err.println(">> DECISION QUERY FAILED: " + e.getMessage());
            }
            System.out.println("=================================================================\n");
        };
    }
}
