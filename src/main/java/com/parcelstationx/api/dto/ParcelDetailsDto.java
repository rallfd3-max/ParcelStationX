package com.parcelstationx.api.dto;

import com.parcelstationx.model.Customer;
import com.parcelstationx.model.ParcelEvent;
import com.parcelstationx.model.ParcelRelocation;
import com.parcelstationx.service.ParcelQueryService.ParcelDetails;
import java.util.List;

public record ParcelDetailsDto(
    ParcelDto parcel,
    CustomerDto customer,
    String operator,
    String shelfCode,
    String slotCode,
    List<ParcelEvent> events,
    List<ParcelRelocation> relocations,
    String pickupCode) {
  public static ParcelDetailsDto from(ParcelDetails details) {
    return new ParcelDetailsDto(
        ParcelDto.from(details.parcel()),
        CustomerDto.from(details.customer()),
        details.operator() == null ? null : details.operator().displayName(),
        details.shelf() == null ? null : details.shelf().shelfCode(),
        details.slot() == null ? null : details.slot().slotCode(),
        details.events(),
        details.relocations(),
        details.parcel().pickupCode());
  }

  public record CustomerDto(Long id, String name, String maskedMobile) {
    static CustomerDto from(Customer customer) {
      if (customer == null) return null;
      String mobile = customer.mobile();
      String masked =
          mobile == null || mobile.length() < 7
              ? "***"
              : mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
      return new CustomerDto(customer.id(), customer.name(), masked);
    }
  }
}
