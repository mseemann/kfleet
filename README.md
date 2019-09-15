# kfleet
A technology preview how to manage a fleet of autonomous vehicles with Kafka


get insights from kafka:

docker run -it --net=host --rm confluentinc/cp-ksql-cli:5.3.0 http://localhost:8088

show topics;

print `topic`;

exit ksql cli: CTRL+d


start kafka and ksql server

docker-compose up

docker exec -it kfleet_broker_1 bash 
kafka-topics --zookeeper zookeeper:2181 --list
kafka-topics --zookeeper zookeeper:2181 --describe --topic cars
kafka-console-consumer --bootstrap-server broker:29092 --topic cars --from-beginning --property print.key=true

```
docker run --tty \
           --network kfleet_default \
           edenhill/kafkacat:1.5.0 \ 
           kafkacat -b broker:29092 \
                  -t test_output_stream -C \
                  -f '\nKey (%K bytes): %k
                  Value (%S bytes): %s
                  Timestamp: %T
                  Partition: %p
                  Offset: %o
                  Headers: %h\n'
```

{"id":"1","state":"FREE","geoPosition":{"lat":59.83977184696787,"lng":10.70939965449577},"stateOfCharge":49.76350057919342}

###Sources
[1] [Should You Put Several Event Types in the Same Kafka Topic?](https://www.confluent.io/blog/put-several-event-types-kafka-topic/)
