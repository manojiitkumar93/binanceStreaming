package spark.structuredStreaming.binance;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class BinanceTradeMapper {

  private BinanceTradeMapper() {}

  private static ObjectMapper mapper = new ObjectMapper();

  public static BinanceTrade mapToPojo(String tradeString)
      throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
    return mapper.readValue(tradeString, BinanceTrade.class);
  }
  



}
