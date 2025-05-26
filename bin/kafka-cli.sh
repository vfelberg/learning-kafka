docker-compose exec kafka bash

kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic demo \
  --partitions 3 \
  --replication-factor 1

kafka-topics \
  --bootstrap-server localhost:9092 \
  --list

kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --property key.separator=, \
  --property parse.key=true \
  --topic demo

kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic demo \
  --property print.timestamp=true \
  --property print.key=true \
  --property print.value=true \
  --from-beginning

kafka-topics \
  --bootstrap-server localhost:9092 \
  --delete \
  --topic demo
