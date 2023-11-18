package demo.kafka.component;

import demo.kafka.rest.api.TriggerEventsRequest;
import dev.lydtech.component.framework.client.service.ServiceClient;
import dev.lydtech.component.framework.extension.ComponentTestExtension;
import dev.lydtech.component.framework.mapper.JsonMapper;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(ComponentTestExtension.class)
public class EndToEndCT {

    /**
     * Send in a REST request to trigger sending and consuming multiple events.
     *
     * The REST call returns immediately, as it sends the events asychronously.  The test will therefore complete quickly
     * while events may still be sent and consumed by the application.
     *
     * Configure the NUMBER_OF_EVENTS.
     *
     * Configure the application-component-test.yml, for example to define whether the Producer send should be synchronous
     * or asynchronous.
     *
     * Monitor the impact of the configured compression codec on the topic size in Conduktor.
     */
    @Test
    public void testFlow() {

        Integer NUMBER_OF_EVENTS = 100000;

        TriggerEventsRequest request = TriggerEventsRequest.builder()
                .numberOfEvents(NUMBER_OF_EVENTS)
                .build();

        RestAssured.given()
                .spec(ServiceClient.getInstance().getRequestSpecification())
                .contentType("application/json")
                .body(JsonMapper.writeToJson(request))
                .post("/v1/demo/trigger")
                .then()
                .statusCode(202);
    }
}
