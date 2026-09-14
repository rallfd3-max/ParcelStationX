package com.parcelstationx.warehouseagent;

import com.parcelstationx.service.ShelfLayoutMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FakeWarehouseAgentParser implements WarehouseAgentParser {
  private static final Pattern CREATE =
      Pattern.compile(
          "(?:在)?([A-Za-z0-9]+)\\s*区?.*?(?:增加|新增|添加|加)\\s*(\\d+)\\s*个?\\s*货架(?:.*?(\\d+)\\s*层\\s*(\\d+)\\s*列)?");

  @Override
  public WarehouseAgentAction parse(String command) {
    String text = command == null ? "" : command.trim();
    String lower = text.toLowerCase(Locale.ROOT);
    if (text.isEmpty()
        || lower.matches(
            ".*(drop|delete all|execute sql|\\bsql\\b|insert |update |rm -rf|http://|https://|密码|password|删除全部|忽略).*")
        || text.matches(".*(DROP|删除所有|执行rm|访问任意).*$")) return unsupported();
    Matcher matcher = CREATE.matcher(text);
    if (matcher.find()) {
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
              lower.contains("主通道") ? ShelfLayoutMode.WIDE_MAIN_AISLE : ShelfLayoutMode.GRID),
          "新增货架计划");
    }
    return unsupported();
  }

  private WarehouseAgentAction unsupported() {
    return new WarehouseAgentAction(WarehouseAgentIntent.UNSUPPORTED, null, "该请求不在仓库 Agent 白名单内。");
  }
}
