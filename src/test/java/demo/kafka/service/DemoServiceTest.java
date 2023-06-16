package demo.kafka.service;

import java.util.concurrent.Future;

import demo.kafka.event.DemoEvent;
import demo.kafka.producer.KafkaProducer;
import demo.kafka.properties.KafkaDemoProperties;
import demo.kafka.rest.api.TriggerEventsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DemoServiceTest {

    private KafkaProducer mockKafkaClient;
    private KafkaDemoProperties mockKafkaDemoProperties;
    private KafkaDemoProperties.Producer mockProducerProperties;
    private DemoService service;

    @BeforeEach
    public void setUp() {
        mockKafkaClient = mock(KafkaProducer.class);
        mockKafkaDemoProperties = mock(KafkaDemoProperties.class);
        mockProducerProperties = mock(KafkaDemoProperties.Producer.class);
        when(mockKafkaDemoProperties.getProducer()).thenReturn(mockProducerProperties);
        service = new DemoService(mockKafkaClient, mockKafkaDemoProperties);
    }

    /**
     * Ensure the Kafka client is called to emit the expected number of events asynchronously.
     */
    @Test
    public void testProcess_NumberOfEvents() {
        when(mockProducerProperties.isAsync()).thenReturn(true);

        TriggerEventsRequest testEvent = TriggerEventsRequest.builder()
                .numberOfEvents(10)
                .build();
        service.process(testEvent);
        verify(mockKafkaClient, times(testEvent.getNumberOfEvents().intValue())).sendMessageAsync(anyString(), any(DemoEvent.class));
    }

    @Test
    public void testProcess_SynchronousSend() {
        when(mockProducerProperties.isAsync()).thenReturn(false);
        when(mockKafkaClient.sendMessageAsync(anyString(), any(DemoEvent.class))).thenReturn(mock(Future.class));

        TriggerEventsRequest testEvent = TriggerEventsRequest.builder()
                .numberOfEvents(10)
                .build();
        service.process(testEvent);
        verify(mockKafkaClient, times(testEvent.getNumberOfEvents().intValue())).sendMessageAsync(anyString(), any(DemoEvent.class));
    }
}
