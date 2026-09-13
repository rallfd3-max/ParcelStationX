package com.parcelstationx.service;

import com.parcelstationx.dao.CustomerDao;
import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.dao.ParcelEventDao;
import com.parcelstationx.dao.ParcelRelocationDao;
import com.parcelstationx.dao.ShelfDao;
import com.parcelstationx.dao.ShelfSlotDao;
import com.parcelstationx.dao.UserDao;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.*;
import java.util.List;

public final class ParcelQueryService {
  private final ParcelDao parcels;
  private final CustomerDao customers;
  private final UserDao users;
  private final ShelfDao shelves;
  private final ShelfSlotDao slots;
  private final ParcelEventDao events;
  private final ParcelRelocationDao relocations;

  public ParcelQueryService(
      ParcelDao parcels,
      CustomerDao customers,
      UserDao users,
      ShelfDao shelves,
      ShelfSlotDao slots,
      ParcelEventDao events,
      ParcelRelocationDao relocations) {
    this.parcels = parcels;
    this.customers = customers;
    this.users = users;
    this.shelves = shelves;
    this.slots = slots;
    this.events = events;
    this.relocations = relocations;
  }

  public ParcelDetails details(long id) {
    Parcel parcel = parcels.findById(id).orElseThrow(() -> new BusinessException("快件不存在。"));
    Customer customer = customers.findById(parcel.customerId()).orElse(null);
    User operator = users.findById(parcel.operatorId()).orElse(null);
    Shelf shelf = parcel.shelfId() == null ? null : shelves.findById(parcel.shelfId()).orElse(null);
    ShelfSlot slot = parcel.slotId() == null ? null : slots.findById(parcel.slotId()).orElse(null);
    return new ParcelDetails(
        parcel,
        customer,
        operator,
        shelf,
        slot,
        events.findByParcelId(id),
        relocations.findByParcelId(id));
  }

  public List<ParcelDetails> findAllDetails() {
    return parcels.findAll().stream().map(parcel -> details(parcel.id())).toList();
  }

  public record ParcelDetails(
      Parcel parcel,
      Customer customer,
      User operator,
      Shelf shelf,
      ShelfSlot slot,
      List<ParcelEvent> events,
      List<ParcelRelocation> relocations) {}
}
