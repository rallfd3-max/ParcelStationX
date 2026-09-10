package com.parcelstationx.dao;
import org.junit.jupiter.api.Test; import java.util.List; import static org.junit.jupiter.api.Assertions.*;
class GenericTypesTest { @Test void preservesTypedResults() { Result<String> result = Result.success("ok"); PageResult<Integer> page = new PageResult<>(List.of(1, 2), 2, 1, 10); assertEquals("ok", result.value().orElseThrow()); assertEquals(2, page.items().size()); } }
