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


