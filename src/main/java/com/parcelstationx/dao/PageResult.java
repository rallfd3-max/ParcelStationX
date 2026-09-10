package com.parcelstationx.dao;
import java.util.List;
public record PageResult<T>(List<T> items, long total, int page, int pageSize) { public PageResult { items = List.copyOf(items); } }
