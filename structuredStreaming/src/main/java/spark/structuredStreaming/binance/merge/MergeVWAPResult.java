package spark.structuredStreaming.binance.merge;

import java.io.Serializable;

public class MergeVWAPResult implements Serializable{


  private String tradeType1;
  private String tradeType2;
  private String mergeTradeType;
  private Double vwap1;
  private Double vwap2;
  private Double vwapMerge;

  public String getTradeType1() {
    return tradeType1;
  }

  public void setTradeType1(String tradeType1) {
    this.tradeType1 = tradeType1;
  }

  public String getTradeType2() {
    return tradeType2;
  }

  public void setTradeType2(String tradeType2) {
    this.tradeType2 = tradeType2;
  }

  public String getMergeTradeType() {
    return mergeTradeType;
  }

  public void setMergeTradeType(String mergeTradeType) {
    this.mergeTradeType = mergeTradeType;
  }

  public Double getVwap1() {
    return vwap1;
  }

  public void setVwap1(Double vwap1) {
    this.vwap1 = vwap1;
  }

  public Double getVwap2() {
    return vwap2;
  }

  public void setVwap2(Double vwap2) {
    this.vwap2 = vwap2;
  }

  public Double getVwapMerge() {
    return vwapMerge;
  }

  public void setVwapMerge(Double vwapMerge) {
    this.vwapMerge = vwapMerge;
  }

}
