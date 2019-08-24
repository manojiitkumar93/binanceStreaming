package spark.structuredStreaming.binance;

import java.util.HashMap;
import java.util.Map;

public enum Trade {
  BTCUSDT("BTCUSDT"), XVGBTC("XVGBTC"), XVGUSDT("XVGUSDT");

  private final String description;
  private static final Map<String, Trade> BY_DESCRIPTION = new HashMap<>();

  static {
    for (Trade priority : values()) {
      BY_DESCRIPTION.put(priority.getDescription(), priority);
    }
  }

  Trade(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }


  public static Trade getTradeByDescription(String description) {
    return BY_DESCRIPTION.get(description);
  }
}
