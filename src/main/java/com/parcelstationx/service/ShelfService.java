package com.parcelstationx.service;

import com.parcelstationx.dao.ShelfDao;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.Shelf;
import com.parcelstationx.model.ShelfStatus;
import java.time.LocalDateTime;
import java.util.List;

public final class ShelfService {
  private final ShelfDao shelves;

  public ShelfService(ShelfDao shelves) {
    this.shelves = shelves;
  }

  public Shelf create(String code, String zone, int capacity) {
    if (code == null || code.isBlank() || zone == null || zone.isBlank())
      throw new BusinessException("货架编号和区域不能为空。");
    if (capacity <= 0) throw new BusinessException("货架容量必须大于零。");
    return shelves.save(
        new Shelf(
            null, code.trim(), zone.trim(), capacity, 0, ShelfStatus.ACTIVE, LocalDateTime.now()));
  }

  public List<Shelf> findAll() {
    return shelves.findAll();
  }
}
