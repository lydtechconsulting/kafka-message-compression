# Kafka Spring Boot Metrics Demo Project

Spring Boot application demonstrating Kafka message compression.

## Overview

The application provides a REST endpoint that accepts a request to trigger sending events.  The number of events to produce can be specified.  The application consumes the events from the topic. 

## Run Spring Boot Application

### Build
```
mvn clean install
```

### Run Docker Containers

From the root dir run the `docker-compose` files to start dockerised Kafka and Zookeeper:
```
docker-compose -f docker-compose.yml up -d
```

To also start the Conduktor Platform UI in Docker, run the following instead: 
```
docker-compose -f docker-compose-conduktor.yml up -d
```

### Start Demo Spring Boot Application

To start the application use:
```
java -jar target/kafka-message-compression-1.0.0.jar
```

In order to change the application properties without recompiling the application, they can be overridden in the application.yml located in the root of the project.  Then start the application with:
```
java -jar target/kafka-message-compression-1.0.0.jar -Dspring.config.additional-location=file:./application.yml
```

### Trigger Events

To trigger sending and consuming a configurable number of events send the following request to the application using curl:

```
curl -v -d '{"numberOfEvents":10000}' -H "Content-Type: application/json" -X POST http://localhost:9001/v1/demo/trigger
```

The request should be accepted with a `202 ACCEPTED` response, as the event sending processing is asynchronous. 

The broker, producer and consumer metrics can then be viewed via the Kafka command line tools or by using the Conduktor Platform.

### Configuring The Broker And Application

A number of parameters can be configured in the `src/main/resources/application.yml`, or overridden in the `application.yml` in the root of this project.   e.g. the producer `batchSize`, `compressionType`, `lingerMs`, `acks` and `async`.

`compressionType` can be one of `none`, `gzip`, `snappy`, `lz4`, or `zstd`.

`producer` `async` configures whether the producer produces asynchronously (fire and forget) or waits synchronously for the result of each send.

## Component Tests

### Overview

The tests call the dockerised demo application over REST to trigger sending and consuming a configurable number of events.  The impact of the configured compression type on messages stored on the Kafka broker can then be viewed using the command line tools or Conduktor. 

To enable Conduktor for the component tests, ensure the following is configured in the `pom.xml`:

```
<conduktor.enabled>true</conduktor.enabled>
```

For more on the component tests see: https://github.com/lydtechconsulting/component-test-framework

### Test

The component test used to trigger sending events is `demo.kafka.component.EndToEndCT`.  Edit the request to determine the quantity of events to send.

Send a specified number of events: 
```
TriggerEventsRequest request = TriggerEventsRequest.builder()
    .numberOfEvents(NUMBER_OF_EVENTS)
    .build();
```

### Build

Build Spring Boot application jar:
```
mvn clean install
```

Build Docker container:
```
docker build -t ct/kafka-message-compression:latest .
```

### Test Execution

Run tests (by default the containers are torn down after the test run):
```
mvn test -Pcomponent
```

Run tests leaving the containers up at the end (so Conduktor metrics can be viewed):
```
mvn test -Pcomponent -Dcontainers.stayup=true
```

### Configuring The Broker And Application

A number of parameters can be configured for the component test run in the `src/test/resources/application-component-test.yml` (e.g. `producer` `compressionType`, `lingerMs`, `acks` and `async`) and the `pom.xml` `maven-surefire-plugin` `systemPropertyVariables` (e.g. `kafka.topic.partition.count`) to view their impact on the test run.  Note that for these settings to be applied it will require the containers to be restarted if they have been left up.

## Conduktor Platform

Conduktor Platform provides a UI over the Kafka cluster, providing a view of the configuration, data and information on the brokers, topics and messages.

Navigate to the Conduktor Platform once it is running, having followed either the `Run Spring Boot Application` steps or the `Component Testing` steps above:
```
http://localhost:8080
```

Login with the default credentials `admin@conduktor.io` / `admin`.

Navigate to the `Console` and view the local Kafka cluster data.

See more on Conduktor Platform at `https://www.conduktor.io/`

## Measuring Compression Impact

The topic size can be inspected either by looking at the `demo-topic` in Conduktor Console, or by using the Kafka command line tools.

To use the command line tools, first jump on to the Kafka Docker container: 
```
docker exec -it kafka /bin/sh
```

Then run the following command to see the total size in bytes of the `demo-topic`:
```
usr/bin/kafka-log-dirs \
--bootstrap-server 127.0.0.1:9092 \
--topic-list demo-topic \
--describe \
| grep -oP '(?<=size":)\d+'  \
| awk '{ sum += $1 } END { print sum }'
```

### Example Results:

Comparing impact of compression type, and whether events are sent asynchronously (resulting in large batches), or synchronously (1 event at a time). 

**Test Run 1:**

- Mode: Async
- Batch size: 64000 bytes
- Producer linger.ms: 10
- Number of events sent: 10000

|Compression| Topic Size (bytes) |
|---|-----|
|none| 5271108 |
|snappy| 3323084 |
|gzip| 2340481 |
|lz4| 3385693 |
|zstd| 2200524 |

**Test Run 2:**

- Mode: Sync
- Batch size: 64000 bytes
- Producer linger.ms: 0
- Number of events sent: 10000

|Compression| Topic Size (bytes) |
|---|-----|
|none| 5860000 |
|snappy| 5612083 |
|gzip| 4707612 |
|lz4| 5521408 |
|zstd| 4862755 |

## Docker Clean Up

Manual clean up (if left containers up):
```
docker rm -f $(docker ps -aq)
```

Further docker clean up (if network issues and to remove old networks/volumes):
```
docker network prune

docker system prune

docker volume prune
```
