package com.parcelstationx.dao;

import com.parcelstationx.model.ParcelRelocation;
import java.util.List;

public interface ParcelRelocationDao extends BaseDao<ParcelRelocation, Long> {
  List<ParcelRelocation> findByParcelId(long parcelId);
}
