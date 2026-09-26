package com.example.csvteamstats.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.SerializationUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaRecoveryTest {

    @Test
    void routesOriginalBytesFromDeserializationFailureToDlt() {
        byte[] malformedAvro = new byte[] {1, 2, 3, 4};
        RecordHeaders headers = new RecordHeaders();

        ErrorHandlingDeserializer<byte[]> deserializer =
                new ErrorHandlingDeserializer<>(new AlwaysFailingDeserializer());

        byte[] value = deserializer.deserialize("team-stats.parsed", headers, malformedAvro);

        assertThat(value).isNull();
        assertThat(headers.lastHeader(SerializationUtils.VALUE_DESERIALIZER_EXCEPTION_HEADER)).isNotNull();

        @SuppressWarnings("unchecked")
        KafkaTemplate<Object, Object> template = mock(KafkaTemplate.class);
        when(template.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));

        DeadLetterPublishingRecoverer recoverer =
                new KafkaErrorHandlingConfig().deadLetterPublishingRecoverer(
                        template, "team-stats.parsed.DLT");

        ConsumerRecord<Object, Object> failedRecord =
                new ConsumerRecord<>("team-stats.parsed", 3, 15L, null, value);
        headers.forEach(failedRecord.headers()::add);

        recoverer.accept(failedRecord, new IllegalStateException("deserialization failed"));

        ArgumentCaptor<ProducerRecord<Object, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(template).send(captor.capture());

        ProducerRecord<Object, Object> dltRecord = captor.getValue();
        assertThat(dltRecord.topic()).isEqualTo("team-stats.parsed.DLT");
        assertThat(dltRecord.partition()).isNull();
        assertThat(dltRecord.value()).isEqualTo(malformedAvro);
    }

    @Test
    void propagatesDltPublishingFailureInsteadOfSilentlyRecovering() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<Object, Object> template = mock(KafkaTemplate.class);
        CompletableFuture failed = new CompletableFuture();
        failed.completeExceptionally(new KafkaException("dlt broker unavailable"));
        when(template.send(any(ProducerRecord.class))).thenReturn(failed);

        DeadLetterPublishingRecoverer recoverer =
                new KafkaErrorHandlingConfig().deadLetterPublishingRecoverer(
                        template, "team-stats.parsed.DLT");

        ConsumerRecord<Object, Object> failedRecord =
                new ConsumerRecord<>("team-stats.parsed", 0, 1L, null, "payload");

        assertThatThrownBy(() ->
                recoverer.accept(failedRecord, new IllegalArgumentException("invalid data")))
                .isInstanceOf(KafkaException.class);
    }

    static class AlwaysFailingDeserializer implements Deserializer<byte[]> {
        @Override
        public byte[] deserialize(String topic, byte[] data) {
            throw new IllegalArgumentException("malformed avro");
        }

        @Override
        public byte[] deserialize(String topic, org.apache.kafka.common.header.Headers headers, byte[] data) {
            throw new IllegalArgumentException("malformed avro");
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // No configuration required.
        }
    }
}
