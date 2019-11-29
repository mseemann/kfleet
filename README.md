# kfleet
A technology preview how to manage a fleet of autonomous vehicles with Kafka

[![CircleCI](https://circleci.com/gh/mseemann/kfleet/tree/master.svg?style=shield)](https://circleci.com/gh/mseemann/kfleet/tree/master)
[![codecov](https://codecov.io/gh/mseemann/kfleet/branch/master/graph/badge.svg)](https://codecov.io/gh/mseemann/kfleet)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fmseemann%2Fkfleet.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fmseemann%2Fkfleet?ref=badge_shield)

get insights from kafka:

``` bash
docker run -it --net=host --rm confluentinc/cp-ksql-cli:5.3.0 http://localhost:8088

show topics;

print `topic`;
```

exit ksql cli: CTRL+d

start kafka and ksql server

``` bash
docker-compose up
```

``` bash
docker exec -it kfleet_broker1_1 bash `

kafka-topics --zookeeper zookeeper:2181 --list

kafka-topics --zookeeper zookeeper:2181 --describe --topic cars

kafka-console-consumer --bootstrap-server broker1:29092 --topic cars --from-beginning --property print.key=true
```

``` bash
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

``` bash
docker run --tty \
           --network kfleet_default \
           edenhill/kafkacat:1.5.0 \
           kafkacat -b broker:29092 \
                    -t cars -C -s value=avro -r http://registry:8081\
  -f '\nKey (%K bytes): %k
  Value (%S bytes): %s
  Timestamp: %T
  Partition: %p
  Offset: %o
  Headers: %h\n'
```

```json
{"id":"1","state":"FREE","geoPosition":{"lat":59.83977184696787,"lng":10.70939965449577},"stateOfCharge":49.76350057919342}
```

### Thoughts and findings
- *In domain-driven design (DDD), an aggregate defines a consistency boundary" [3]
- "Both the sender and the receiver of a command should be in the same bounded context. You should not send a command to another bounded context because you would be instructing that other bounded context, which has separate responsibilities in another consistency boundary, to perform some work for you." [3]
- "Commands should be processed once, by a single recipient." [3]
- "When source code is executed sequentially, the code is easier to understand and debug. When concurrency is used, code may get executed in parallel or in some irregular order." [5]
- "Tell don't ask!"

### Using the interactive query service to provide a REST-Interface to the state store
Kafka Streams provide a _ReadOnlyKeyValueStore_ that is a thread safe way to access a state store. 
The store is available through Spring Cloud Streams _InteractiveQueryService_. We can use this
service to find a specific record by key, all records in the store or a range of records - if we are quering
for a range of keys (lexicographically).
This sounds great. But we should remind our self that a state store is bound to the current streaming 
application instance and if we want to scale our application we need to start more instances of our 
application (up to the number of partitions our topic has).
If we provide the host and port of our application (`application.server`-property) we can query all
hosts that are part of our streaming app cluster or we can determine the host a given key is stored.

To conclude: if we have only one streaming application instance we can query a record by key in the 
easiest possible way:
``` kotlin
override fun findByIdLocal(id: String): Mono<Car> {
    return carsStore().get(id)?.toMono() ?: Mono.error(Exception("car with id: $id not found"))
}

private fun carsStore(): ReadOnlyKeyValueStore<String, Car> = interactiveQueryService
        .getQueryableStore(CarStateCountProcessorBinding.CAR_STORE, QueryableStoreTypes.keyValueStore<String, Car>())

```
If we have more than one instance of our streaming application running we need to decide whether the
record is stored locally or can only be queries from another application instance. If the key is stored
locally we query the local store. If not, we need a remote call to query the reocord:

``` kotlin
override fun findById(id: String): Mono<Car> {
    val hostInfo = interactiveQueryService.getHostInfo(CarStateCountProcessorBinding.CAR_STORE, id, StringSerializer())

    if (hostInfo == interactiveQueryService.currentHostInfo) {
        log.debug { "find car by id local: $id" }
        return findByIdLocal(id)
    }

    log.debug { "find car by id remote: $id" }
    val webClient = WebClient.create("http://${hostInfo.host()}:${hostInfo.port()}")
    return webClient.get().uri("/$CARS_RPC/$id")
            .retrieve()
            .bodyToMono(Car::class.java)
}
```

The _WebClient_ uses a special rest endpoint that queries the local store directly. The provided solution is the
fastest one because it runs a local key lookup in the local store if the key is processed by the same application
instance. Only if the key is processed by another application instance a remote call is necessary.
The disadvantage is that the provided code is more complicated and not very concise.

How to overcome this disadvantage? 

We could provide a REST-Proxy that can determine the streaming application for
a given key. To achive this the REST-Proxy must be very smart because he needs to know how to extract the key for 
a REST-Call and query the host of the streaming application that processes the given key. This is possible but
also not a simple solution.

Another way would be to run the remote call in any case. E.g. a http call is necessary for each key lookup. The advantage 
is a more readable code:

``` kotlin
override fun findById(id: String): Mono<Car> {
    val hostInfo = interactiveQueryService.getHostInfo(CarStateCountProcessorBinding.CAR_STORE, id, StringSerializer())
    val webClient = WebClient.create("http://${hostInfo.host()}:${hostInfo.port()}")
    return webClient.get().uri("/$CARS_RPC/$id")
            .retrieve()
            .bodyToMono(Car::class.java)
}
```

### realtime queries - e.g. live queries with a push based update 

- maintain state
- register query for a state
- push state changes to the query initializer (client)
- this can be used to throttle the changes - e.g the client can set a throttle by himself
- but how to distribute the state maintenance on multiple nodes
  and combine it to answer queries on one node


### Resources and readings

[1] [Should You Put Several Event Types in the Same Kafka Topic?](https://www.confluent.io/blog/put-several-event-types-kafka-topic/)

[2] [1 Year of Event Sourcing and CQRS](https://medium.com/hackernoon/1-year-of-event-sourcing-and-cqrs-fb9033ccd1c6)

[3] [A CQRS and ES deep dive](https://docs.microsoft.com/en-us/previous-versions/msp-n-p/jj591577(v=pandp.10)?redirectedfrom=MSDN)

[4] [Every Company is Becoming ~~a~~ Software ~~Company~~](https://www.confluent.io/blog/every-company-is-becoming-software)

[5] [How To Write Less Code and Get More Done.](https://medium.com/@rsrajan1/how-to-write-less-code-and-get-more-done-40006282817d)

[6] [https://github.com/Yolean/kubernetes-kafka](https://github.com/Yolean/kubernetes-kafka)

[7] [https://www.matsim.org/](https://www.matsim.org/)

[8] [Sinusoidal](https://github.com/geotools/geotools/blob/master/modules/library/referencing/src/main/java/org/geotools/referencing/operation/projection/Sinusoidal.java)


## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fmseemann%2Fkfleet.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fmseemann%2Fkfleet?ref=badge_large)