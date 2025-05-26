package io.vf_lab.learning_kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class Consumer {
    private static final String GROUP_ID = "demo-consumer-group";

    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    public static void main(String[] args) {
        Thread mainThread = Thread.currentThread();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties())) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down consumer");
                consumer.wakeup();

                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));

            consumer.subscribe(List.of(Producer.TOPIC));
            while (!mainThread.isInterrupted()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
                records.forEach(record ->
                        log.info("Partition: {}, offset: {}, key: {}, value: {}",
                                record.partition(), record.offset(), record.key(), record.value()));
            }
        } catch (WakeupException e) {
            // wakeup from shutdown hook
        }
    }

    private static Properties properties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());

        properties.setProperty("group.id", GROUP_ID);
        properties.setProperty("auto.offset.reset", "earliest");
        properties.setProperty("partition.assignment.strategy", CooperativeStickyAssignor.class.getName());

        // static member
        //properties.setProperty("group.instance.id", "xyz");
        //properties.setProperty("session.timeout.ms", "1000");
        // if a static member re-joins before session timeout, it gets the same partition as before

        return properties;
    }
}
