package spark.structuredStreaming.binance.merge;

import java.sql.Timestamp;

public class VWAPResult {

  private String tradeType;
  private Double highestPrice;
  private Double lowestPrice;
  private Double closingPrice;
  private Double totalVolume;
  private Timestamp timeStamp;
  private Double VWAP;

  public VWAPResult(String tradeType, Double highestPrice, Double lowestPrice, Double closingPrice,
      Double totalVolume, Timestamp closingTime) {
    this.tradeType = tradeType;
    this.highestPrice = highestPrice;
    this.lowestPrice = lowestPrice;
    this.closingPrice = closingPrice;
    this.totalVolume = totalVolume;
    this.timeStamp = closingTime;
  }

  public Timestamp getClosingTime() {
    return timeStamp;
  }

  public void setClosingTime(Timestamp closingTime) {
    this.timeStamp = closingTime;
  }


  public Double getClosingPrice() {
    return closingPrice;
  }

  public void setClosingPrice(Double closingPrice) {
    this.closingPrice = closingPrice;
  }

  public String getTradeType() {
    return tradeType;
  }

  public void setTradeType(String tradeType) {
    this.tradeType = tradeType;
  }

  public Double getHighestPrice() {
    return highestPrice;
  }

  public void setHighestPrice(Double highestPrice) {
    this.highestPrice = highestPrice;
  }

  public Double getLowestPrice() {
    return lowestPrice;
  }

  public void setLowestPrice(Double lowestPrice) {
    this.lowestPrice = lowestPrice;
  }

  public Double getTotalVolume() {
    return totalVolume;
  }

  public void setTotalVolume(Double totalVolume) {
    this.totalVolume = totalVolume;
  }

  public Double getVWAP() {
    return VWAP;
  }

  public void setVWAP(Double vWAP) {
    VWAP = vWAP;
  }

}
