package com.parcelstationx.api.dto;

public record InboundParcelRequest(
    String trackingNo, String courierCompany, String customerMobile, String remark) {}
