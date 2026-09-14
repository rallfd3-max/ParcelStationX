package com.parcelstationx.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcelstationx.service.DashboardAnalyticsService;
import java.util.List;
import java.util.function.Supplier;

public final class AiOperationsService {
  private final AiClient client;
  private final Supplier<Object> contextSupplier;
  private final AiResponseValidator validator;
  private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();

  public AiOperationsService(
      AiClient client, DashboardAnalyticsService analytics, AiResponseValidator validator) {
    this.client = client;
    this.contextSupplier = analytics::aiContext;
    this.validator = validator;
  }

  AiOperationsService(
      AiClient client, Supplier<Object> contextSupplier, AiResponseValidator validator) {
    this.client = client;
    this.contextSupplier = contextSupplier;
    this.validator = validator;
  }

  public OperationsInsight analyze() {
    String context = encode(contextSupplier.get());
    String output =
        client.generate(
            AiPromptCatalog.STRUCTURED_OUTPUT_RULES
                + " Analyze only the supplied aggregate ParcelStationX facts. Do not invent causes, counts, or rates. Return summary string, risks array, recommendations array.",
            context);
    var object = validator.requireObject(output);
    return new OperationsInsight(
        validator.requireText(object, "summary", 800),
        validator.requireTextList(object, "risks", 5, 300),
        validator.requireTextList(object, "recommendations", 5, 300));
  }

  private String encode(Object value) {
    try {
      return json.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Unable to encode sanitized dashboard context", exception);
    }
  }

  public record OperationsInsight(
      String summary, List<String> risks, List<String> recommendations) {}
}
