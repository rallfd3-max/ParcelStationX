package com.parcelstationx.dao;

import com.parcelstationx.model.ParcelEvent;
import java.util.List;

public interface ParcelEventDao extends BaseDao<ParcelEvent, Long> {
  List<ParcelEvent> findByParcelId(long parcelId);
}
