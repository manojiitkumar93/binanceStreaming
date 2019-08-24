package spark.structuredStreaming.binance.merge;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.expressions.Aggregator;
import spark.structuredStreaming.binance.BinanceTrade;
import spark.structuredStreaming.binance.Trade;

public class MergeVWAP extends Aggregator<BinanceTrade, BinanceTrade, MergeVWAPResult> implements Serializable{

  private Trade trade1;
  private Trade trade2;
  private Trade mergedTrade;
  private Calendar cal = Calendar.getInstance();
  
  private static VWAPResult vwapResult1;
  private static VWAPResult vwapResult2;
  

  public MergeVWAP(Trade trade1, Trade trade2, Trade mergedTrade) {
    this.trade1 = trade1;
    this.trade2 = trade2;
    this.mergedTrade = mergedTrade;
    init();
  }
  
  private void init() {
    cal.add(Calendar.DATE, -1);
    Timestamp timeStamp = new Timestamp(cal.getTimeInMillis());
    vwapResult1 =
        new VWAPResult(trade1.getDescription(), 0.0, 0.0, 0.0, 0.0, timeStamp);
    vwapResult2 =
        new VWAPResult(trade2.getDescription(), 0.0, 0.0, 0.0, 0.0, timeStamp);
  }

  @Override
  public Encoder<BinanceTrade> bufferEncoder() {
    return Encoders.bean(BinanceTrade.class);
  }

  @Override
  public MergeVWAPResult finish(BinanceTrade arg0) {
    executeMergeOrReduce(arg0);
    //return mergeTrades(btcUsdtTradeResult, xvgBtcTradeResult);
    return mergeTrades(vwapResult1, vwapResult2);
  }

  @Override
  public BinanceTrade merge(BinanceTrade arg0, BinanceTrade arg1) {
    BinanceTrade binanceTrade = (arg0.getTradeTime().after(arg1.getTradeTime())) ? arg0 : arg1;
    executeMergeOrReduce(binanceTrade);
    return binanceTrade;
  }

  @Override
  public Encoder<MergeVWAPResult> outputEncoder() {
    return Encoders.bean(MergeVWAPResult.class);
  }

  @Override
  public BinanceTrade reduce(BinanceTrade arg0, BinanceTrade arg1) {
    BinanceTrade binanceTrade = (arg0.getTradeTime().after(arg1.getTradeTime())) ? arg0 : arg1;
    executeMergeOrReduce(binanceTrade);
    return binanceTrade;
  }

  @Override
  public BinanceTrade zero() {
    BinanceTrade binanceRawTrade = new BinanceTrade(trade1.getDescription(), 0.0, 0.0,
        new Timestamp(cal.getTimeInMillis()));
    return binanceRawTrade;
  }

  private void executeMergeOrReduce(BinanceTrade binanceRawTrade) {
    String tradeType = binanceRawTrade.getSymbol();
    Trade trade = Trade.getTradeByDescription(tradeType);
    if (Objects.isNull(trade)) {
      throw new RuntimeException("Unknown tradeType exception : " + tradeType);
    }

    if (StringUtils.equals(tradeType, trade1.getDescription())) {
      setHeighestPrice(binanceRawTrade, vwapResult1);
      setLowestPrice(binanceRawTrade, vwapResult1);
      setLatestPrice(binanceRawTrade, vwapResult1);
      setTotalVolume(binanceRawTrade, vwapResult1);
    }

    else if (StringUtils.equals(tradeType, trade2.getDescription())) {
      setHeighestPrice(binanceRawTrade, vwapResult2);
      setLowestPrice(binanceRawTrade, vwapResult2);
      setLatestPrice(binanceRawTrade, vwapResult2);
      setTotalVolume(binanceRawTrade, vwapResult2);
    }
  }

  private void setHeighestPrice(BinanceTrade binanceRawTrade, VWAPResult vwapResultForTrade) {
    if (vwapResultForTrade.getHighestPrice() < binanceRawTrade.getPrice()) {
      vwapResultForTrade.setHighestPrice(binanceRawTrade.getPrice());
    }
  }

  private void setLowestPrice(BinanceTrade binanceRawTrade, VWAPResult vwapResultForTrade) {

    // this is to handle zero()
    if (vwapResultForTrade.getLowestPrice() == 0.0) {
      vwapResultForTrade.setLowestPrice(binanceRawTrade.getPrice());
      return;
    }

    if (vwapResultForTrade.getLowestPrice() > binanceRawTrade.getPrice()) {
      vwapResultForTrade.setLowestPrice(binanceRawTrade.getPrice());
    }
  }

  private void setLatestPrice(BinanceTrade binanceRawTrade, VWAPResult vwapResultForTrade) {
    if (binanceRawTrade.getTradeTime().after(vwapResultForTrade.getClosingTime())) {
      vwapResultForTrade.setClosingTime(binanceRawTrade.getTradeTime());
      vwapResultForTrade.setClosingPrice(binanceRawTrade.getPrice());
    }
  }

  private void setTotalVolume(BinanceTrade binanceRawTrade, VWAPResult vwapResultForTrade) {
    vwapResultForTrade
        .setTotalVolume(vwapResultForTrade.getTotalVolume() + binanceRawTrade.getQuantity());
  }

  private MergeVWAPResult mergeTrades(VWAPResult tradeResult1, VWAPResult tradeResult2) {
    MergeVWAPResult mergeResult = new MergeVWAPResult();
    mergeResult.setMergeTradeType(mergedTrade.getDescription());
    mergeResult.setTradeType1(tradeResult1.getTradeType());
    mergeResult.setTradeType2(tradeResult2.getTradeType());
    
    // calculate only if we get atleast on trade event
    if (tradeResult1.getTotalVolume() != 0.0) {
      mergeResult.setVwap1(
          calculateVWAP(tradeResult1.getHighestPrice(), tradeResult1.getLowestPrice(),
              tradeResult1.getClosingPrice(), tradeResult1.getTotalVolume()));
    }

    // calculate only if we get atleast on trade event
    if (tradeResult2.getTotalVolume() != 0.0) {
      mergeResult.setVwap2(
          calculateVWAP(tradeResult2.getHighestPrice(), tradeResult2.getLowestPrice(),
              tradeResult2.getClosingPrice(), tradeResult2.getTotalVolume()));
    }

    // If both trade events exists then calculate merge
    if (tradeResult2.getTotalVolume() != 0.0 && tradeResult1.getTotalVolume() != 0.0) {
      // calculate the lowest price, closing price, heighest price for xvg/usdt
      Double xvgUsdtClosingPrice = calculateMergePrice(tradeResult2.getClosingPrice(),
          tradeResult1.getClosingPrice());
      Double xvgUsdtHeighestPrice = calculateMergePrice(tradeResult2.getHighestPrice(),
          tradeResult1.getHighestPrice());
      Double xvgUsdtLowestPrice = calculateMergePrice(tradeResult2.getLowestPrice(),
          tradeResult1.getLowestPrice());

      // TODO : How to merge the volume
      // As of now taking the heighest volume
      Double xvgUsdtVolume =
          (tradeResult2.getTotalVolume() > tradeResult1.getTotalVolume())
              ? tradeResult2.getTotalVolume()
              : tradeResult1.getTotalVolume();
      mergeResult.setVwapMerge(calculateVWAP(xvgUsdtHeighestPrice, xvgUsdtLowestPrice,
          xvgUsdtClosingPrice, xvgUsdtVolume));

    }

    return mergeResult;
  }

  private Double calculateVWAP(Double highestPrice, Double lowestPrice, Double closingPrice,
      Double volume) {
    // https://education.howthemarketworks.com/advanced/technical-analysis/volume-weighted-average-price/
    Double price = (highestPrice + lowestPrice + closingPrice) / 3;
    return (price * volume) / volume;
  }

  private Double calculateMergePrice(Double xvgBtcPrice, Double btcUsdtPrice) {
    return 1.0 / (xvgBtcPrice * btcUsdtPrice);
  }

}
