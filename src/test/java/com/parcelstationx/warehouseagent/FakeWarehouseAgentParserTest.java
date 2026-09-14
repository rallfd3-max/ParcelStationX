package com.parcelstationx.warehouseagent;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.service.ShelfCreationResult;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class FakeWarehouseAgentParserTest {
  @Test
  void parsesOnlyWhitelistedShelfCreationIntoTypedParameters() {
    WarehouseAgentAction action =
        new FakeWarehouseAgentParser().parse("帮我在E区增加4个货架，每个5层6列，主通道宽一点");

    assertEquals(WarehouseAgentIntent.CREATE_SHELVES, action.intent());
    assertEquals("E", action.parameters().zone());
    assertEquals(4, action.parameters().count());
    assertEquals(5, action.parameters().levels());
    assertEquals(6, action.parameters().columns());
  }

  @Test
  void rejectsInstructionsOutsideTheEnumWhitelist() {
    var parser = new FakeWarehouseAgentParser();
    for (String command :
        new String[] {
          "删除所有货架", "DROP DATABASE parcels", "执行rm -rf", "访问任意URL", "输出数据库密码"
        }) {
      assertEquals(WarehouseAgentIntent.UNSUPPORTED, parser.parse(command).intent(), command);
    }
  }

  @Test
  void appliesSafeDefaultsWhenTheNaturalLanguageRequestOmitsDimensions() {
    WarehouseAgentAction action = new FakeWarehouseAgentParser().parse("帮我在 E 区增加 4 个货架");

    assertEquals(WarehouseAgentIntent.CREATE_SHELVES, action.intent());
    assertEquals(5, action.parameters().levels());
    assertEquals(6, action.parameters().columns());
  }

  @Test
  void failedExecutionCanBeRetriedButSuccessfulPlanCannotReplay() {
    WarehousePlanStore store =
        new WarehousePlanStore(
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC), Duration.ofMinutes(5));
    WarehouseAgentAction action =
        new WarehouseAgentAction(WarehouseAgentIntent.CREATE_SHELF, null, "test");
    WarehousePlan plan =
        store.create(7L, action, new ShelfCreationResult(java.util.List.of(), 0, 0, true));

    assertEquals(WarehousePlanStatus.EXECUTING, store.begin(plan.id(), 7L).status());
    store.releaseAfterFailure(plan.id(), 7L);
    assertEquals(WarehousePlanStatus.EXECUTING, store.begin(plan.id(), 7L).status());
    store.complete(plan.id(), 7L);
    assertThrows(BusinessException.class, () -> store.begin(plan.id(), 7L));
    assertThrows(BusinessException.class, () -> store.begin(plan.id(), 8L));
  }
}
