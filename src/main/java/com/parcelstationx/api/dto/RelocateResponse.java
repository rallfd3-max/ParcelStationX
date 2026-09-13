package com.parcelstationx.api.dto;

import com.parcelstationx.model.ParcelRelocation;
import com.parcelstationx.service.RelocationResult;

public record RelocateResponse(ParcelDto parcel, ParcelRelocation relocation) {
  public static RelocateResponse from(RelocationResult result) {
    return new RelocateResponse(ParcelDto.from(result.parcel()), result.relocation());
  }
}
