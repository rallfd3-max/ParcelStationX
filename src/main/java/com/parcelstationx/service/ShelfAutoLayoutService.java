package com.parcelstationx.service;

import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.ShelfLayout;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ShelfAutoLayoutService {
  public List<ShelfLayout> plan(
      List<ShelfLayout> existing, ShelfAutoLayoutRequest request, LocalDateTime now) {
    validate(request);
    double maxZ =
        existing.stream()
            .mapToDouble(layout -> layout.positionZ() + layout.depth() / 2)
            .max()
            .orElse(-request.depth() / 2);
    double startZ = maxZ + request.aisleGap() + request.depth() / 2;
    List<ShelfLayout> planned = new ArrayList<>();
    for (int index = 0; index < request.count(); index++) {
      int row = index / request.maxShelvesPerRow();
      int column = index % request.maxShelvesPerRow();
      double x = column * (request.width() + request.shelfGap());
      double z = startZ + rowOffset(row, request);
      double rotation =
          request.mode() == ShelfLayoutMode.TWO_SIDED_AISLE && row % 2 == 1 ? Math.PI : 0;
      ShelfLayout candidate =
          new ShelfLayout(
              null,
              x,
              0,
              z,
              rotation,
              request.width(),
              request.height(),
              request.depth(),
              1,
              1,
              now);
      if (collides(candidate, existing) || collides(candidate, planned)) {
        throw new BusinessException("自动布局与现有货架重叠。");
      }
      planned.add(candidate);
    }
    return List.copyOf(planned);
  }

  public boolean collides(ShelfLayout candidate, List<ShelfLayout> layouts) {
    return layouts.stream().anyMatch(other -> overlaps(candidate, other));
  }

  private double rowOffset(int row, ShelfAutoLayoutRequest request) {
    if (row == 0) return 0;
    double standard = request.depth() + request.shelfGap();
    return switch (request.mode()) {
      case GRID -> row * standard;
      case WIDE_MAIN_AISLE -> request.depth() + request.aisleGap() + (row - 1) * standard;
      case TWO_SIDED_AISLE -> {
        int pair = row / 2;
        boolean secondSide = row % 2 == 1;
        yield pair * (2 * request.depth() + request.aisleGap())
            + (secondSide ? request.depth() + request.aisleGap() : 0);
      }
    };
  }

  private boolean overlaps(ShelfLayout left, ShelfLayout right) {
    double epsilon = 0.000001;
    return Math.abs(left.positionX() - right.positionX())
            < (left.width() + right.width()) / 2 - epsilon
        && Math.abs(left.positionZ() - right.positionZ())
            < (left.depth() + right.depth()) / 2 - epsilon;
  }

  private void validate(ShelfAutoLayoutRequest request) {
    if (request == null || request.mode() == null) throw new BusinessException("布局模式必填。");
    if (request.count() < 1 || request.count() > 50) throw new BusinessException("货架数量无效。");
    if (request.maxShelvesPerRow() < 1 || request.maxShelvesPerRow() > 20)
      throw new BusinessException("每排货架数必须为 1 到 20。");
    if (request.shelfGap() < .2 || request.shelfGap() > 20)
      throw new BusinessException("货架间距必须为 0.2 到 20 米。");
    if (request.aisleGap() < 1 || request.aisleGap() > 50)
      throw new BusinessException("通道间距必须为 1 到 50 米。");
    if (request.width() <= 0 || request.depth() <= 0 || request.height() <= 0)
      throw new BusinessException("货架尺寸必须为正数。");
  }
}
