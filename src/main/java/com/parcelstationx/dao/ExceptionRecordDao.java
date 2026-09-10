package com.parcelstationx.dao;

import com.parcelstationx.model.ExceptionRecord;
import java.util.List;

public interface ExceptionRecordDao extends BaseDao<ExceptionRecord, Long> {
  List<ExceptionRecord> findOpen();
}
