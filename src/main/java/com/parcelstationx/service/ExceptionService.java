package com.parcelstationx.service;

import com.parcelstationx.dao.impl.*;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.*;
import java.time.LocalDateTime;
import java.util.List;

public final class ExceptionService {
  private final TransactionRunner tx;
  private final ExceptionRecordDaoImpl exceptions;
  private final ParcelDaoImpl parcels;
  private final ParcelEventDaoImpl events;
  private final OperationLogDaoImpl logs;
  private final ParcelStateMachine states = new ParcelStateMachine();

  public ExceptionService(
      TransactionRunner tx,
      ExceptionRecordDaoImpl exceptions,
      ParcelDaoImpl parcels,
      ParcelEventDaoImpl events,
      OperationLogDaoImpl logs) {
    this.tx = tx;
    this.exceptions = exceptions;
    this.parcels = parcels;
    this.events = events;
    this.logs = logs;
  }

  public ExceptionRecord create(
      long parcelId, ExceptionType type, String description, long userId) {
    return tx.run(
        c -> {
          Parcel p =
              parcels.findById(c, parcelId).orElseThrow(() -> new BusinessException("快件不存在。"));
          states.requireTransition(p.status(), ParcelStatus.EXCEPTION);
          LocalDateTime now = LocalDateTime.now();
          parcels.save(
              c,
              new Parcel(
                  p.id(),
                  p.trackingNo(),
                  p.courierCompany(),
                  p.customerId(),
                  p.shelfId(),
                  p.pickupCode(),
                  ParcelStatus.EXCEPTION,
                  p.arrivedAt(),
                  p.pickedUpAt(),
                  p.operatorId(),
                  p.remark(),
                  p.createdAt(),
                  now));
          ExceptionRecord r =
              exceptions.save(
                  c,
                  new ExceptionRecord(
                      null, p.id(), type, description, "OPEN", userId, null, now, null, null));
          events.save(
              c,
              new ParcelEvent(
                  null,
                  p.id(),
                  "MARKED_EXCEPTION",
                  p.status(),
                  ParcelStatus.EXCEPTION,
                  userId,
                  description,
                  now));
          logs.save(
              c,
              new OperationLog(
                  null, userId, "EXCEPTION_CREATE", "PARCEL", p.id(), description, now));
          return r;
        });
  }

  public ExceptionRecord resolve(long id, ParcelStatus target, String resolution, long userId) {
    if (target != ParcelStatus.IN_STOCK && target != ParcelStatus.RETURNED)
      throw new BusinessException("异常件只能恢复库存或退回。");
    return tx.run(
        c -> {
          ExceptionRecord r = exceptions.findById(c, id).orElseThrow();
          Parcel p = parcels.findById(c, r.parcelId()).orElseThrow();
          states.requireTransition(p.status(), target);
          LocalDateTime now = LocalDateTime.now();
          parcels.save(
              c,
              new Parcel(
                  p.id(),
                  p.trackingNo(),
                  p.courierCompany(),
                  p.customerId(),
                  p.shelfId(),
                  p.pickupCode(),
                  target,
                  p.arrivedAt(),
                  p.pickedUpAt(),
                  p.operatorId(),
                  p.remark(),
                  p.createdAt(),
                  now));
          ExceptionRecord done =
              new ExceptionRecord(
                  r.id(),
                  r.parcelId(),
                  r.exceptionType(),
                  r.description(),
                  "RESOLVED",
                  r.createdBy(),
                  userId,
                  r.createdAt(),
                  now,
                  resolution);
          exceptions.save(c, done);
          events.save(
              c,
              new ParcelEvent(
                  null,
                  p.id(),
                  "EXCEPTION_RESOLVED",
                  ParcelStatus.EXCEPTION,
                  target,
                  userId,
                  resolution,
                  now));
          logs.save(
              c,
              new OperationLog(
                  null, userId, "EXCEPTION_RESOLVE", "PARCEL", p.id(), resolution, now));
          return done;
        });
  }

  public List<ExceptionRecord> findOpen() {
    return exceptions.findOpen();
  }
}
