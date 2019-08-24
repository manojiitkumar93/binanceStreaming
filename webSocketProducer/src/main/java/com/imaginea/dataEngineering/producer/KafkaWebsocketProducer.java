package com.imaginea.dataEngineering.producer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import com.softwaremill.react.kafka.ProducerProperties;
import com.softwaremill.react.kafka.PropertiesBuilder;
import akka.Done;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.WebSocketRequest;
import akka.http.javadsl.model.ws.WebSocketUpgradeResponse;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import kafka.producer.ReactiveKafkaProducer;

public class KafkaWebsocketProducer {

  private static ActorSystem system;
  private static Materializer materializer;
  private static Http http;

  static {
    system = ActorSystem.create();
    materializer = ActorMaterializer.create(system);
    http = Http.get(system);
  }

  public static void main(String[] args) {
    String bootStrapServers = args[0];
    String topic = args[1];
    //createProducer("localhost:9092", "test");
    createProducer(bootStrapServers, topic);
  }

  private static void createProducer(String bootStrapServer, String topic) {
    ProducerProperties<byte[], String> producerProperties =
        new PropertiesBuilder.Producer(bootStrapServer, topic, new ByteArraySerializer(),
            new StringSerializer()).build();
    List<String> webUrls = new ArrayList<String>();
    webUrls.add("wss://stream.binance.com:9443/ws/btcusdt@trade");
    webUrls.add("wss://stream.binance.com:9443/ws/xvgbtc@trade");
    webUrls.forEach((String url) -> {
      connectWebSocketOnlyForGettingMessages(url, producerProperties, topic);
    });
  }

  private static void connectWebSocketOnlyForGettingMessages(String url,
      ProducerProperties<byte[], String> producerProperties, String topic) {
    ReactiveKafkaProducer<byte[], String> rkp =
        new ReactiveKafkaProducer<byte[], String>(producerProperties);

    final Sink<Message, CompletionStage<Done>> sink = Sink.foreach((message) -> {
      ProducerRecord<byte[], String> producerRecord =
          new ProducerRecord<>(topic, message.asTextMessage().getStrictText());
      rkp.producer().send(producerRecord);
    });

    Source<Message, CompletableFuture<Optional<Message>>> source = Source.maybe();


    Flow<Message, Message, CompletionStage<Done>> flow =
        Flow.fromSinkAndSourceMat(sink, source, Keep.left());

    Pair<CompletionStage<WebSocketUpgradeResponse>, CompletionStage<Done>> btcusdtResponse =
        http.singleWebSocketRequest(WebSocketRequest.create(url), flow, materializer);

    long startTime = System.currentTimeMillis();
    CompletionStage<Object> connected = btcusdtResponse.first().thenApply(upgrade -> {
      if (upgrade.response().status().equals(StatusCodes.SWITCHING_PROTOCOLS)) {
        return Done.getInstance();
      } else {
        throw new RuntimeException("Connection failed: " + upgrade.response().status());
      }
    });

    final CompletionStage<Done> closed = btcusdtResponse.second();
    connected.thenAccept(done -> {
      System.out.println("Connected for url " + url + " at : " + startTime);
    });
    closed.thenAccept(done -> {
      long endTime = System.currentTimeMillis();
      long diff = (endTime - startTime) / 1000;
      System.out.println("Connection closed in sec : " + diff);
      System.out.println("re-connecting for url " + url);
      connectWebSocketOnlyForGettingMessages(url, producerProperties, topic);
    });
  }
}
