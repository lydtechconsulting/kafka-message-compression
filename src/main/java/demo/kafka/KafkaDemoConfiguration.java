package demo.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import demo.kafka.event.DemoEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Slf4j
@ComponentScan(basePackages = {"demo.kafka"})
@Configuration
public class KafkaDemoConfiguration {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(final ConsumerFactory<String, Object> consumerFactory) {
        final ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(final ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(@Value("${kafka.bootstrap-servers}") final String bootstrapServers,
                                                           @Value("${kafka.consumer.maxPollIntervalMs}") final String maxPollIntervalMs,
                                                           @Value("${kafka.consumer.maxPollRecords}") final String maxPollRecords) {
        final Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "demo-consumer-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DemoEvent.class.getCanonicalName());
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID());
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(@Value("${kafka.bootstrap-servers}") final String bootstrapServers,
                                                           @Value("${kafka.producer.compressionType}") final String compressionType,
                                                           @Value("${kafka.producer.batchSize}") final String batchSize,
                                                           @Value("${kafka.producer.lingerMs}") final String lingerMs,
                                                           @Value("${kafka.producer.acks}") final String acks) {
        log.info("Compression type is: " + compressionType);
        final Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        config.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        config.put(ProducerConfig.ACKS_CONFIG, acks);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        return new DefaultKafkaProducerFactory<>(config);
    }
}
