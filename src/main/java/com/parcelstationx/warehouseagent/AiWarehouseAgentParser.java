package com.parcelstationx.warehouseagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.parcelstationx.ai.AiClient;
import com.parcelstationx.ai.AiException;
import com.parcelstationx.ai.AiResponseValidator;
import com.parcelstationx.service.ShelfLayoutMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Uses the shared V2.2 MaiMaiYa client only for a typed plan; it never receives execution power. */
public final class AiWarehouseAgentParser implements WarehouseAgentParser {
  private static final Pattern CREATE =
      Pattern.compile(
          "(?:在)?([A-Za-z0-9]+)\\s*区?.*?(?:增加|新增|添加|加)\\s*(\\d+)\\s*个?\\s*货架(?:.*?(\\d+)\\s*层\\s*(\\d+)\\s*列)?");
  private static final Pattern DANGEROUS =
      Pattern.compile(
          "(?i)(drop|delete\\s+all|execute\\s+sql|\\bsql\\b|insert |update |rm\\s+-rf|https?://|密码|password|删除所有|忽略|访问任意|直接.*确认)");
  private static final String PROMPT =
      "You are a warehouse plan parser. Return JSON only: "
          + "{intent,parameters:{zone,count,levels,columns,layoutMode},summary}. "
          + "Allowed intent enum: CREATE_SHELF, CREATE_SHELVES, UPDATE_SHELF, RESIZE_SHELF, "
          + "MOVE_SHELF, DISABLE_SHELF, QUERY_SHELF, SUGGEST_LAYOUT, UNSUPPORTED. "
          + "Never output SQL, DAO calls, files, shell commands, HTTP requests, confirmation, or coordinates. "
          + "For unsafe or unsupported input return UNSUPPORTED.";

  private final AiClient client;
  private final AiResponseValidator validator;

  public AiWarehouseAgentParser(AiClient client, AiResponseValidator validator) {
    this.client = client;
    this.validator = validator;
  }

  @Override
  public WarehouseAgentAction parse(String command) {
    String text = command == null ? "" : command.trim();
    if (text.isBlank() || text.length() > 500 || DANGEROUS.matcher(text).find()) return unsupported();
    try {
      return validate(validator.requireObject(client.generate(PROMPT, text)));
    } catch (AiException unavailable) {
      return deterministicCreate(text);
    }
  }

  WarehouseAgentAction validate(JsonNode object) {
    try {
      WarehouseAgentIntent intent =
          WarehouseAgentIntent.valueOf(validator.requireText(object, "intent", 40));
      if (intent == WarehouseAgentIntent.UNSUPPORTED) return unsupported();
      JsonNode parameters = object.path("parameters");
      if (!parameters.isObject()) return unsupported();
      String zone = text(parameters, "zone", 20);
      Integer count = number(parameters, "count", 1, 50);
      Integer levels = number(parameters, "levels", 1, 20);
      Integer columns = number(parameters, "columns", 1, 30);
      ShelfLayoutMode mode = layout(parameters.path("layoutMode").asText("GRID"));
      if ((intent == WarehouseAgentIntent.CREATE_SHELF || intent == WarehouseAgentIntent.CREATE_SHELVES)
          && (zone == null || count == null || levels == null || columns == null)) return unsupported();
      return new WarehouseAgentAction(
          intent,
          new WarehouseAgentParameters(zone, count, levels, columns, null, null, null, mode),
          text(object, "summary", 160) == null ? "仓库计划" : text(object, "summary", 160));
    } catch (IllegalArgumentException | AiException exception) {
      return unsupported();
    }
  }

  private WarehouseAgentAction deterministicCreate(String text) {
    Matcher matcher = CREATE.matcher(text);
    if (!matcher.find()) return unsupported();
    int count = Integer.parseInt(matcher.group(2));
    return new WarehouseAgentAction(
        count == 1 ? WarehouseAgentIntent.CREATE_SHELF : WarehouseAgentIntent.CREATE_SHELVES,
        new WarehouseAgentParameters(
            matcher.group(1),
            count,
            matcher.group(3) == null ? 5 : Integer.parseInt(matcher.group(3)),
            matcher.group(4) == null ? 6 : Integer.parseInt(matcher.group(4)),
            null,
            null,
            null,
            text.contains("主通道") ? ShelfLayoutMode.WIDE_MAIN_AISLE : ShelfLayoutMode.GRID),
        "新增货架计划");
  }

  private static String text(JsonNode object, String field, int max) {
    JsonNode node = object.path(field);
    if (!node.isTextual()) return null;
    String value = node.asText().trim();
    return value.isBlank() || value.length() > max ? null : value;
  }

  private static Integer number(JsonNode object, String field, int min, int max) {
    JsonNode node = object.path(field);
    return node.canConvertToInt() && node.asInt() >= min && node.asInt() <= max ? node.asInt() : null;
  }

  private static ShelfLayoutMode layout(String value) {
    try {
      return ShelfLayoutMode.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      return ShelfLayoutMode.GRID;
    }
  }

  private static WarehouseAgentAction unsupported() {
    return new WarehouseAgentAction(WarehouseAgentIntent.UNSUPPORTED, null, "该请求不在仓库 Agent 白名单内。");
  }
}
