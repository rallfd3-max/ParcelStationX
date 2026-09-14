package com.parcelstationx.service;

import com.parcelstationx.exception.BusinessException;

public final class ShelfMutationValidator {
  public ShelfCreationRequest validate(ShelfCreationRequest request) {
    if (request == null) throw new BusinessException("货架参数必填。");
    if (request.count() < 1 || request.count() > 50)
      throw new BusinessException("货架数量必须在 1 到 50 之间。");
    if (request.levels() < 1 || request.levels() > 20)
      throw new BusinessException("层数必须在 1 到 20 之间。");
    if (request.columns() < 1 || request.columns() > 30)
      throw new BusinessException("列数必须在 1 到 30 之间。");
    validateDimension(request.width(), "宽度");
    validateDimension(request.height(), "高度");
    validateDimension(request.depth(), "深度");
    if (request.count() > 1 && request.shelfCode() != null && !request.shelfCode().isBlank())
      throw new BusinessException("批量新增不允许指定单个货架编号。");
    return request;
  }

  private void validateDimension(double value, String label) {
    if (!Double.isFinite(value) || value <= 0 || value > 100)
      throw new BusinessException(label + "必须大于 0 且不超过 100 米。");
  }
}
