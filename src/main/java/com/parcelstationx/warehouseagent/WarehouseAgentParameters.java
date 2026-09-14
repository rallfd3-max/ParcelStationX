package com.parcelstationx.warehouseagent;

import com.parcelstationx.service.ShelfLayoutMode;

public record WarehouseAgentParameters(
    String zone,
    Integer count,
    Integer levels,
    Integer columns,
    Long shelfId,
    Double positionX,
    Double positionZ,
    ShelfLayoutMode layoutMode) {}
