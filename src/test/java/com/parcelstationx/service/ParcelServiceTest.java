package com.parcelstationx.service;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.*;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ParcelServiceTest {
  @Test
  void rejectsRepeatPickupAndBadCode() {
    Parcel parcel =
        new Parcel(
            1L,
            "TRACK-0001",
            "SF",
            1L,
            1L,
            "123456",
            ParcelStatus.IN_STOCK,
            LocalDateTime.now(),
            null,
            1L,
            "",
            LocalDateTime.now(),
            LocalDateTime.now());
    ParcelService service = new ParcelService();
    assertEquals(ParcelStatus.PICKED_UP, service.pickup(parcel, "123456"));
    assertThrows(BusinessException.class, () -> service.pickup(parcel, "wrong"));
    assertFalse(
        new ParcelStateMachine().canTransition(ParcelStatus.PICKED_UP, ParcelStatus.PICKED_UP));
  }

  @Test
  void createsUniquePickupCode() {
    String code = new PickupCodeGenerator().generate(Set.of("000000"));
    assertTrue(code.matches("\\d{6}"));
    assertNotEquals("000000", code);
  }
}
