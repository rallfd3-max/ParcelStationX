package com.parcelstationx.api.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcelstationx.api.error.BadRequestException;

public final class JsonCodec {
  private final ObjectMapper mapper =
      new ObjectMapper()
          .findAndRegisterModules()
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  public <T> T read(String json, Class<T> type) {
    try {
      return mapper.readValue(json, type);
    } catch (JsonProcessingException exception) {
      throw new BadRequestException("请求 JSON 格式错误。", "MALFORMED_JSON");
    }
  }

  public String write(Object value) {
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Unable to encode API response", exception);
    }
  }
}
