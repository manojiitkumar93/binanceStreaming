This project is about analyzing trades on Binance in real time.  The program will connect to Binance’s WebSocket API and calculates
the volume-weighted average price (VWAP) of the respective trade for given n minutes.<br>

### Steps to Run..
#### Download and install kafka
Refer here (https://hevodata.com/blog/how-to-install-kafka-on-ubuntu/)<br>
1. Start the kafka server `./kafka-server-start.sh /opt/kafka/config/server.properties `
2. Start the kafka consumer `./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic <topic-name>`
#### Start the spark streaming application
1. clone the project..
2.`cd structuredStreaming`
3. `mvn clean package`
4. `java -jar target/structured-streaming.jar localhost:9092 binanceTrade 60`. localhost:9092 --> kafka host and port,
binanceTrade--> kafka topic, 1 --> collect stats for every 60 sec
#### Start the websocket Producer
1. `cd..`
2. `cd webSocketProducer`
3. `mvn clean package`
4. `java -jar target/webSocketProducer-1.0-kafkaProducer.jar localhost:9092 binanceTrade`. localhost:9092 --> kafka host and port,
binanceTrade--> kafka topic

**NOTE** : Resultant stats will be written to `/tmp/binance` folder

### References
1. For using [Akka streams](https://doc.akka.io/docs/akka-http/current/client-side/websocket-support.html?language=scala) and [java example for Akka streams](https://github.com/akka/akka-http/blob/v10.1.9/docs/src/test/java/docs/http/javadsl/WebSocketClientExampleTest.java#L40-L87)
2. Custome [merge functions](https://github.com/apache/spark/blob/master/examples/src/main/java/org/apache/spark/examples/sql/JavaUserDefinedTypedAggregation.java)
3. Connecting to [Binance Trades](https://github.com/imranshaikmuma/Websocket-Akka-Kafka-Spark)
4.Spark Structured streaming [link1](http://vishnuviswanath.com/spark_structured_streaming.html) and [link2](http://blog.madhukaraphatak.com/categories/introduction-structured-streaming/)


