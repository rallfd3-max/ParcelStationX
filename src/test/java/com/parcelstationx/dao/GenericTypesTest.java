package com.parcelstationx.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class GenericTypesTest {
  @Test
  void preservesTypedResults() {
    Result<String> result = Result.success("ok");
    PageResult<Integer> page = new PageResult<>(List.of(1, 2), 2, 1, 10);
    assertEquals("ok", result.value().orElseThrow());
    assertEquals(2, page.items().size());
  }
}
