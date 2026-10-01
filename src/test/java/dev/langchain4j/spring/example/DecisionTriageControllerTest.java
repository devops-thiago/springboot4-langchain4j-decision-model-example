package dev.langchain4j.spring.example;

import dev.langchain4j.model.decision.DecisionModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DecisionTriageControllerTest {

    @Autowired
    private DecisionTriageController controller;

    @Autowired
    private DecisionModel decisionModel;

    @Test
    void testTriageEndpoint() {
        assertThat(decisionModel).isNotNull();
        assertThat(controller).isNotNull();

        TriageResponse response = controller.triageTicket(
                new TriageRequest("I need an enterprise invoice and sales agreement for 1000 users."));

        assertThat(response).isNotNull();
        assertThat(response.getModelName()).isEqualTo("devops-thiago/classone-gemma4-e2b");
        assertThat(response.getDepartment()).isNotNull();
        assertThat(response.getUrgentProbability()).isBetween(0.0, 1.0);
        assertThat(response.getFrustrationScore()).isGreaterThanOrEqualTo(0.0);
    }
}
