package com.parcelstationx.service;

public record InboundRequest(
    String trackingNo,
    String courierCompany,
    String customerMobile,
    Long shelfId,
    Long operatorId,
    String remark) {}
