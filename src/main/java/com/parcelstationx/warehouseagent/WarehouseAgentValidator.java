package com.parcelstationx.warehouseagent;

import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.service.ShelfCreationRequest;
import com.parcelstationx.service.ShelfLayoutMode;

public final class WarehouseAgentValidator {
  public ShelfCreationRequest creation(WarehouseAgentAction action) {
    if (action == null
        || !(action.intent() == WarehouseAgentIntent.CREATE_SHELF
            || action.intent() == WarehouseAgentIntent.CREATE_SHELVES))
      throw new BusinessException("该计划不能创建货架。");
    WarehouseAgentParameters p = action.parameters();
    if (p == null || p.count() == null || p.levels() == null || p.columns() == null)
      throw new BusinessException("AI 计划参数不完整。");
    return new ShelfCreationRequest(
        null,
        p.zone(),
        p.count(),
        p.levels(),
        p.columns(),
        3.6,
        2.6,
        .8,
        p.layoutMode() == null ? ShelfLayoutMode.GRID : p.layoutMode(),
        4,
        .6,
        2.5);
  }
}
