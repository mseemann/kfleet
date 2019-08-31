# kfleet
A technology preview how to manage a fleet of autonomous vehicles with Kafka


get insights from kafka:

docker run -it --net=host --rm confluentinc/cp-ksql-cli:5.3.0 http://localhost:8088

show topics;

print `topic`;

exit ksql cli: CTRL+d


start kafka and ksql server

docker-compose up
