package com.parcelstationx.service;

import com.parcelstationx.model.Shelf;
import com.parcelstationx.model.ShelfLayout;

public record ShelfCreationItem(Shelf shelf, ShelfLayout layout, int slotCount) {}
