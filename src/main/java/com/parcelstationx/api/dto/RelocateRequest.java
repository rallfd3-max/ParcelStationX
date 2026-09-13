package com.parcelstationx.api.dto;

public record RelocateRequest(Long targetSlotId, Long expectedVersion, String reason) {}
