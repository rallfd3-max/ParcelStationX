package com.parcelstationx.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcelstationx.service.DashboardAnalyticsService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class AiOperationsService {
  private static final AiGenerationOptions SHORT_INSIGHT_LIMITS =
      new AiGenerationOptions(320, Duration.ofSeconds(20));
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
    var context = compact(contextSupplier.get());
    try {
      String output =
          client.generate(
              AiPromptCatalog.STRUCTURED_OUTPUT_RULES
                  + " Analyze only the supplied aggregate facts. Return exactly {summary:string,risks:string[],recommendations:string[]}. "
                  + "Summary <= 180 Chinese characters; each array has at most 3 items; each item <= 90 Chinese characters. "
                  + "Do not repeat metrics or add explanations outside JSON.",
              encode(context),
              SHORT_INSIGHT_LIMITS);
      var object = validator.requireObject(output);
      return new OperationsInsight(
          validator.requireText(object, "summary", 240),
          validator.requireTextList(object, "risks", 3, 120),
          validator.requireTextList(object, "recommendations", 3, 120));
    } catch (AiException exception) {
      if (exception.code() == AiErrorCode.AI_TIMEOUT
          || exception.code() == AiErrorCode.AI_UPSTREAM_ERROR
          || exception.code() == AiErrorCode.AI_RATE_LIMITED) return fallback(context);
      throw exception;
    }
  }

  private Map<String, Object> compact(Object raw) {
    var source = json.valueToTree(raw);
    Map<String, Object> metrics = Map.of(
        "todayInbound", source.path("todayInbound").asLong(),
        "todayOutbound", source.path("todayOutbound").asLong(),
        "inventory", source.path("inventory").asLong(),
        "exceptions", source.path("exceptions").asLong(),
        "overdue", source.path("overdue").asLong(),
        "slotUtilization", source.path("slotUtilization").asDouble());
    return Map.of(
        "metrics", metrics,
        "topZoneUtilization", top(source.path("distributions").path("zoneUtilization"), 3),
        "topShelfUtilization", top(source.path("distributions").path("shelfUtilization"), 3),
        "courierDistribution", copyNumbers(source.path("distributions").path("courier")),
        "dwellDistribution", copyNumbers(source.path("distributions").path("dwell")));
  }

  private Map<String, Double> top(com.fasterxml.jackson.databind.JsonNode node, int limit) {
    var entries = new ArrayList<Map.Entry<String, Double>>();
    node.fields().forEachRemaining(entry -> entries.add(Map.entry(entry.getKey(), entry.getValue().asDouble())));
    entries.sort(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()));
    var result = new java.util.LinkedHashMap<String, Double>();
    entries.stream().limit(limit).forEach(entry -> result.put(entry.getKey(), entry.getValue()));
    return result;
  }

  private Map<String, Long> copyNumbers(com.fasterxml.jackson.databind.JsonNode node) {
    var result = new java.util.LinkedHashMap<String, Long>();
    node.fields().forEachRemaining(entry -> result.put(entry.getKey(), entry.getValue().asLong()));
    return result;
  }

  @SuppressWarnings("unchecked")
  private OperationsInsight fallback(Map<String, Object> context) {
    var metrics = (Map<String, Object>) context.get("metrics");
    long inventory = ((Number) metrics.get("inventory")).longValue();
    long overdue = ((Number) metrics.get("overdue")).longValue();
    long exceptions = ((Number) metrics.get("exceptions")).longValue();
    double utilization = ((Number) metrics.get("slotUtilization")).doubleValue();
    List<String> risks = new ArrayList<>(), recommendations = new ArrayList<>();
    if (overdue > 0) { risks.add("存在 " + overdue + " 件超过 7 天未取的快件"); recommendations.add("优先联系滞留快件客户并跟进取件"); }
    if (exceptions > 0) { risks.add("当前有 " + exceptions + " 件异常件需要处理"); recommendations.add("按优先级处理未解决异常件并补全记录"); }
    if (utilization >= 85) { risks.add("已启用仓位利用率为 " + utilization + "%"); recommendations.add("优先使用低利用率分区或安排整理"); }
    if (risks.isEmpty()) risks.add("暂无严重聚合运营风险");
    if (recommendations.isEmpty()) recommendations.add("继续监控库存、异常与滞留快件变化");
    return new OperationsInsight("本地聚合显示当前库存 " + inventory + " 件，仓位利用率 " + utilization + "%。", risks, recommendations);
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
