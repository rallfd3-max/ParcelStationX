package com.parcelstationx.warehouseagent;

public record WarehouseAgentAction(
    WarehouseAgentIntent intent, WarehouseAgentParameters parameters, String summary) {}
