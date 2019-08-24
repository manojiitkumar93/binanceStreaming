package spark.structuredStreaming;

import java.util.concurrent.TimeUnit;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.TypedColumn;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import spark.structuredStreaming.binance.BinanceTrade;
import spark.structuredStreaming.binance.BinanceTradeMapper;
import spark.structuredStreaming.binance.Trade;
import spark.structuredStreaming.binance.merge.MergeVWAP;
import spark.structuredStreaming.binance.merge.MergeVWAPResult;


public class App {
  public static void main(String[] args) throws StreamingQueryException {
    String bootStrapServers = args[0];
    String topic = args[1];
    String timeIntervalString = args[2];

    SparkConf sparkConf = new SparkConf().setAppName("BinanceTadeStreaming");
    if (!sparkConf.contains("spark.master")) {
      sparkConf.setMaster("local[4]");
    }

    SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
    spark.sparkContext().setLogLevel("ERROR");
    Dataset<Row> trades =
        spark.readStream().format("kafka").option("kafka.bootstrap.servers", bootStrapServers)
            .option("subscribe", topic).option("auto.offset.reset", "earliest").load();


    Dataset<String> tradesAsString =
        trades.selectExpr("CAST(value AS STRING)").select("value").as(Encoders.STRING());

    Dataset<BinanceTrade> mappedTrades = tradesAsString
        .map(value -> BinanceTradeMapper.mapToPojo(value), Encoders.bean(BinanceTrade.class));
    mappedTrades.printSchema();
    mappedTrades.withWatermark("tradeTime", "5 seconds");

    MergeVWAP vwapCalculator = new MergeVWAP(Trade.BTCUSDT, Trade.XVGBTC, Trade.XVGUSDT);
    TypedColumn<BinanceTrade, MergeVWAPResult> vwap = vwapCalculator.toColumn();

    mappedTrades.select(vwap).writeStream()
        .foreachBatch(new VoidFunction2<Dataset<MergeVWAPResult>, Long>() {

          @Override
          public void call(Dataset<MergeVWAPResult> dataset, Long batchId) throws Exception {
            dataset.persist();
            dataset.show();
            dataset.write().format("com.databricks.spark.csv").option("header", "true")
                .mode(SaveMode.Append).save("/tmp/binance");
            dataset.unpersist();
          }
        }).trigger(Trigger.ProcessingTime(Integer.valueOf(timeIntervalString), TimeUnit.SECONDS))
        .outputMode(OutputMode.Complete()).start().awaitTermination();
  }

}
