package io.vf_lab.learning_kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

public class WordCountApp {

    private static final Serde<String> stringSerde = Serdes.String();
    private static final Serde<Long> longSerde = Serdes.Long();

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass());

        StreamsBuilder builder = new StreamsBuilder();

        KStream<Void, String> textLines = builder.stream("text-lines");

        Pattern pattern = Pattern.compile("\\W+");
        KTable<String, Long> wordCounts = textLines
                .flatMapValues(textLine -> Arrays.asList(pattern.split(textLine.toLowerCase())))
                .groupBy((_, word) -> word)
                .count();
        wordCounts.toStream().to("word-counts", Produced.with(stringSerde, longSerde));

        Topology topology = builder.build();

        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown, "streams-shutdown-hook"));

        try (KafkaStreams streams = new KafkaStreams(topology, props)) {
            streams.start();
            latch.await();
        }
    }
}
