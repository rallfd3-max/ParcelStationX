package com.parcelstationx.warehouseagent;

import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.service.ShelfCreationResult;
import com.parcelstationx.service.ShelfManagementService;
import java.util.UUID;

public final class WarehouseAgentService {
  private final WarehouseAgentParser parser;
  private final WarehouseAgentValidator validator;
  private final WarehousePlanStore store;
  private final ShelfManagementService shelves;

  public WarehouseAgentService(
      WarehouseAgentParser parser,
      WarehouseAgentValidator validator,
      WarehousePlanStore store,
      ShelfManagementService shelves) {
    this.parser = parser;
    this.validator = validator;
    this.store = store;
    this.shelves = shelves;
  }

  public WarehousePlan plan(long userId, String command) {
    WarehouseAgentAction action = parser.parse(command);
    if (action.intent() == WarehouseAgentIntent.UNSUPPORTED)
      throw new BusinessException("不支持该仓库指令。");
    ShelfCreationResult preview = shelves.preview(validator.creation(action));
    return store.create(userId, action, preview);
  }

  public ShelfCreationResult confirm(UUID id, long userId) {
    WarehousePlan plan = store.begin(id, userId);
    try {
      ShelfCreationResult result = shelves.create(validator.creation(plan.action()), userId);
      store.complete(id, userId);
      return result;
    } catch (RuntimeException exception) {
      store.releaseAfterFailure(id, userId);
      throw exception;
    }
  }

  public void cancel(UUID id, long userId) {
    store.cancel(id, userId);
  }
}
