docker-compose -f kafka/docker-compose.kafka.yaml up -d --remove-orphans
sleep 10
docker exec -i kafka ./usr/bin/kafka-topics --zookeeper zookeeper:2181 --create --topic result-value-store --partitions 3 --replication-factor 1 --if-not-exists
docker exec -i kafka ./usr/bin/kafka-topics --zookeeper zookeeper:2181 --create --topic result-body-store --partitions 3 --replication-factor 1 --if-not-exists
docker exec -i kafka ./usr/bin/kafka-topics --zookeeper zookeeper:2181 --create --topic result-request --partitions 3 --replication-factor 1 --if-not-exists
docker exec -i kafka ./usr/bin/kafka-topics --zookeeper zookeeper:2181 --create --topic result-return --partitions 3 --replication-factor 1 --if-not-exists