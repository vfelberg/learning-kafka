package io.vf_lab.learning_kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class Producer {
    static final String TOPIC = "demo";

    private static final Logger log = LoggerFactory.getLogger(Producer.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());

        // safe producer settings (default in Kafka >= 3.1)
        //properties.setProperty(ProducerConfig.ACKS_CONFIG, "all"); // -1
        //properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        //properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        //properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

        // safe broker/topic settings
        // replication.factor=3
        // min.insync.replicas=2

        // high-throughput producer settings
        //properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        //properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        //properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024));

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
            IntStream.rangeClosed(1, 5).forEach(batchNumber -> {
                produceBatch(producer, batchNumber);
                producer.flush();

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    private static void produceBatch(KafkaProducer<String, String> producer, int batchNumber) {
        log.info("Producing batch #{}", batchNumber);
        IntStream.rangeClosed(1, 10).forEach(messageNumber -> {
            //String key = batchNumber + "_" + messageNumber;
            String value = String.format("Message #%d:%d", batchNumber, messageNumber);

            //ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key, value);
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, value);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Error while producing", exception);
                    return;
                }
                log.info("Partition: {}, offset: {}", metadata.partition(), metadata.offset());
            });
        });
    }
}
