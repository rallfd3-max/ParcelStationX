package com.parcelstationx.service;

import com.parcelstationx.model.Parcel;
import com.parcelstationx.model.ParcelRelocation;

public record RelocationResult(Parcel parcel, ParcelRelocation relocation) {}
