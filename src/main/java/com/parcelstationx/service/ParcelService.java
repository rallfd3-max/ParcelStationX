package com.parcelstationx.service;

import com.parcelstationx.dao.impl.CustomerDaoImpl;
import com.parcelstationx.dao.impl.OperationLogDaoImpl;
import com.parcelstationx.dao.impl.ParcelDaoImpl;
import com.parcelstationx.dao.impl.ParcelEventDaoImpl;
import com.parcelstationx.dao.impl.ShelfDaoImpl;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;

public final class ParcelService {
  private final TransactionRunner transactions;
  private final CustomerDaoImpl customers;
  private final ShelfDaoImpl shelves;
  private final ParcelDaoImpl parcels;
  private final ParcelEventDaoImpl events;
  private final OperationLogDaoImpl logs;
  private final PickupCodeGenerator pickupCodes = new PickupCodeGenerator();
  private final ParcelStateMachine states = new ParcelStateMachine();
  private final Clock clock = Clock.systemDefaultZone();
  private NotificationService notifications;

  public ParcelService(
      TransactionRunner transactions,
      CustomerDaoImpl customers,
      ShelfDaoImpl shelves,
      ParcelDaoImpl parcels,
      ParcelEventDaoImpl events,
      OperationLogDaoImpl logs) {
    this.transactions = transactions;
    this.customers = customers;
    this.shelves = shelves;
    this.parcels = parcels;
    this.events = events;
    this.logs = logs;
  }

  public ParcelService withNotifications(NotificationService value) {
    notifications = value;
    return this;
  }

  public Parcel inbound(InboundRequest request) {
    validateInbound(request.trackingNo(), request.customerMobile());
    Parcel result =
        transactions.run(
            connection -> {
              if (parcels.findByTrackingNo(connection, request.trackingNo()).isPresent()) {
                throw new BusinessException("运单号已入库。");
              }
              Customer customer =
                  customers.findByMobile(connection, request.customerMobile()).stream()
                      .findFirst()
                      .orElseThrow(() -> new BusinessException("未找到匹配客户。"));
              Shelf shelf =
                  request.shelfId() == null
                      ? shelves
                          .findAvailable(connection)
                          .orElseThrow(() -> new BusinessException("没有可用货架。"))
                      : shelves
                          .findById(connection, request.shelfId())
                          .orElseThrow(() -> new BusinessException("货架不存在。"));
              if (shelf.status() != ShelfStatus.ACTIVE || shelf.occupied() >= shelf.capacity()) {
                throw new BusinessException("货架已停用或已满。");
              }
              LocalDateTime now = LocalDateTime.now(clock);
              String code =
                  pickupCodes.generate(
                      new HashSet<>(
                          parcels.findAll(connection).stream()
                              .filter(p -> p.status() == ParcelStatus.IN_STOCK)
                              .map(Parcel::pickupCode)
                              .toList()));
              Parcel parcel =
                  parcels.save(
                      connection,
                      new Parcel(
                          null,
                          request.trackingNo(),
                          request.courierCompany(),
                          customer.id(),
                          shelf.id(),
                          code,
                          ParcelStatus.IN_STOCK,
                          now,
                          null,
                          request.operatorId(),
                          request.remark(),
                          now,
                          now));
              shelves.save(
                  connection,
                  new Shelf(
                      shelf.id(),
                      shelf.shelfCode(),
                      shelf.zone(),
                      shelf.capacity(),
                      shelf.occupied() + 1,
                      shelf.status(),
                      shelf.createdAt()));
              events.save(
                  connection,
                  new ParcelEvent(
                      null,
                      parcel.id(),
                      "STORED",
                      null,
                      ParcelStatus.IN_STOCK,
                      request.operatorId(),
                      "快件入库",
                      now));
              logs.save(
                  connection,
                  new OperationLog(
                      null,
                      request.operatorId(),
                      "INBOUND",
                      "PARCEL",
                      parcel.id(),
                      "快件 " + request.trackingNo() + " 入库",
                      now));
              return parcel;
            });
    if (notifications != null) notifications.notifyInbound(result);
    return result;
  }

  public Parcel outbound(String pickupCode, long operatorId) {
    if (pickupCode == null || pickupCode.isBlank()) throw new BusinessException("请输入取件码。");
    return transactions.run(
        connection -> {
          Parcel parcel =
              parcels
                  .findByPickupCode(connection, pickupCode)
                  .orElseThrow(() -> new BusinessException("取件码不存在。"));
          states.requireTransition(parcel.status(), ParcelStatus.PICKED_UP);
          Shelf shelf =
              shelves
                  .findById(connection, parcel.shelfId())
                  .orElseThrow(() -> new BusinessException("货架不存在。"));
          LocalDateTime now = LocalDateTime.now(clock);
          Parcel picked =
              parcels.save(
                  connection,
                  new Parcel(
                      parcel.id(),
                      parcel.trackingNo(),
                      parcel.courierCompany(),
                      parcel.customerId(),
                      parcel.shelfId(),
                      parcel.pickupCode(),
                      ParcelStatus.PICKED_UP,
                      parcel.arrivedAt(),
                      now,
                      parcel.operatorId(),
                      parcel.remark(),
                      parcel.createdAt(),
                      now));
          shelves.save(
              connection,
              new Shelf(
                  shelf.id(),
                  shelf.shelfCode(),
                  shelf.zone(),
                  shelf.capacity(),
                  Math.max(0, shelf.occupied() - 1),
                  shelf.status(),
                  shelf.createdAt()));
          events.save(
              connection,
              new ParcelEvent(
                  null,
                  parcel.id(),
                  "PICKED_UP",
                  ParcelStatus.IN_STOCK,
                  ParcelStatus.PICKED_UP,
                  operatorId,
                  "快件出库",
                  now));
          logs.save(
              connection,
              new OperationLog(
                  null,
                  operatorId,
                  "OUTBOUND",
                  "PARCEL",
                  parcel.id(),
                  "快件 " + parcel.trackingNo() + " 出库",
                  now));
          return picked;
        });
  }

  public void validateInbound(String trackingNo, String mobile) {
    if (trackingNo == null || !trackingNo.matches("[A-Za-z0-9-]{6,100}")) {
      throw new BusinessException("运单号格式无效。");
    }
    if (mobile == null || !mobile.matches("1\\d{10}")) {
      throw new BusinessException("手机号格式无效。");
    }
  }
}
