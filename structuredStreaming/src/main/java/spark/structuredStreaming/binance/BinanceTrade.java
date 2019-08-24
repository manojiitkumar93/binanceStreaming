package spark.structuredStreaming.binance;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.sql.Timestamp;

public class BinanceTrade implements Serializable{

  @JsonProperty("e")
  private String eventType;

  @JsonProperty("E")
  private long eventTime;

  @JsonProperty("s")
  private String symbol;

  @JsonProperty("t")
  private long tradeId;

  @JsonProperty("p")
  private Double price;

  @JsonProperty("q")
  private Double quantity;

  @JsonProperty("b")
  private long buyerId;

  @JsonProperty("a")
  private long sellerId;

  @JsonProperty("T")
  private Timestamp tradeTime;

  @JsonProperty("m")
  private boolean isBuyerMarketMaker;

  @JsonProperty("M")
  private boolean unUsed;
  
  public BinanceTrade() {
  }
  
  public BinanceTrade(String symbol, Double price, Double quantity, Timestamp tradeTime) {
    this.symbol = symbol;
    this.price = price;
    this.tradeTime = tradeTime;
    this.quantity = quantity;
  }
  

  public long getBuyerId() {
    return buyerId;
  }

  public void setBuyerId(long buyerId) {
    this.buyerId = buyerId;
  }

  public long getSellerId() {
    return sellerId;
  }

  public void setSellerId(long sellerId) {
    this.sellerId = sellerId;
  }

  public boolean isUnUsed() {
    return unUsed;
  }

  public void setUnUsed(boolean unUsed) {
    this.unUsed = unUsed;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public long getEventTime() {
    return eventTime;
  }

  public void setEventTime(long eventTime) {
    this.eventTime = eventTime;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public long getTradeId() {
    return tradeId;
  }


  public void setTradeId(long tradeId) {
    this.tradeId = tradeId;
  }

  public Double getPrice() {
    return price;
  }


  public void setPrice(Double price) {
    this.price = price;
  }

  public Double getQuantity() {
    return quantity;
  }

  public void setQuantity(Double quantity) {
    this.quantity = quantity;
  }

  public Timestamp getTradeTime() {
    return tradeTime;
  }

  public void setTradeTime(Timestamp tradeTime) {
    this.tradeTime = tradeTime;
  }

  public boolean isBuyerMarketMaker() {
    return isBuyerMarketMaker;
  }

  public void setBuyerMarketMaker(boolean isBuyerMarketMaker) {
    this.isBuyerMarketMaker = isBuyerMarketMaker;
  }

}
