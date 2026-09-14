package com.parcelstationx.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiFeatureServicesTest {
  private final AiResponseValidator validator = new AiResponseValidator();

  @Test
  void operationsInsightUsesServerContextAndValidatesShape() {
    FakeAiClient client =
        new FakeAiClient(
            "{\"summary\":\"稳定\",\"risks\":[\"A区较满\"],\"recommendations\":[\"优先C区\"]}");
    AiOperationsService service =
        new AiOperationsService(
            client,
            () -> Map.of("todayInbound", 3, "zoneUtilization", Map.of("A", 92.0)),
            validator);
    var result = service.analyze();
    assertEquals("稳定", result.summary());
    assertTrue(client.lastUserPrompt().contains("todayInbound"));
    assertFalse(client.lastUserPrompt().contains("pickupCode"));
    assertFalse(client.lastUserPrompt().contains("mobile"));
  }

  @Test
  void operationsInsightRejectsInvalidModelOutput() {
    AiOperationsService service =
        new AiOperationsService(new FakeAiClient("not-json"), Map::of, validator);
    assertEquals(
        AiErrorCode.AI_BAD_RESPONSE, assertThrows(AiException.class, service::analyze).code());
  }

  @Test
  void exceptionAdviceRejectsUnknownEnumsAndSanitizesMobile() {
    AiExceptionAdviceService service = new AiExceptionAdviceService(null, null, null, validator);
    String valid =
        "{\"suggestedType\":\"DAMAGED\",\"riskLevel\":\"HIGH\",\"reason\":\"包装破损\",\"steps\":[\"拍照\"],\"suggestedResolution\":\"人工核验\"}";
    assertEquals(List.of("拍照"), service.parseAdvice(valid).steps());
    assertEquals("联系[MOBILE_REDACTED]", AiExceptionAdviceService.sanitize("联系13900000001"));
    String invalid = valid.replace("DAMAGED", "DROP_DATABASE");
    assertEquals(
        AiErrorCode.AI_BAD_RESPONSE,
        assertThrows(AiException.class, () -> service.parseAdvice(invalid)).code());
  }
}
