package com.parcelstationx.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.ExceptionType;
import com.parcelstationx.service.ExceptionService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class AiExceptionAdviceService {
  private final AiClient client;
  private final ExceptionService exceptions;
  private final ParcelDao parcels;
  private final AiResponseValidator validator;
  private final ObjectMapper json = new ObjectMapper();

  public AiExceptionAdviceService(
      AiClient client,
      ExceptionService exceptions,
      ParcelDao parcels,
      AiResponseValidator validator) {
    this.client = client;
    this.exceptions = exceptions;
    this.parcels = parcels;
    this.validator = validator;
  }

  public ExceptionAdvice advise(long exceptionId) {
    var exception = exceptions.findById(exceptionId);
    var parcel =
        parcels.findById(exception.parcelId()).orElseThrow(() -> new BusinessException("快件不存在。"));
    Map<String, Object> context =
        Map.of(
            "exceptionType", exception.exceptionType().name(),
            "exceptionStatus", exception.status(),
            "description", sanitize(exception.description()),
            "courier", sanitize(parcel.courierCompany()),
            "parcelStatus", parcel.status().name(),
            "arrivedAt", parcel.arrivedAt().toString(),
            "dwellDays",
                Math.max(0, Duration.between(parcel.arrivedAt(), LocalDateTime.now()).toDays()),
            "shelfId", parcel.shelfId() == null ? "UNASSIGNED" : parcel.shelfId(),
            "slotId", parcel.slotId() == null ? "UNASSIGNED" : parcel.slotId());
    String output =
        client.generate(
            AiPromptCatalog.STRUCTURED_OUTPUT_RULES
                + " Give human-review-only parcel exception advice. Return suggestedType, riskLevel, reason, steps, suggestedResolution. Never claim a database mutation occurred.",
            encode(context));
    return parseAdvice(output);
  }

  ExceptionAdvice parseAdvice(String output) {
    var object = validator.requireObject(output);
    String typeText = validator.requireText(object, "suggestedType", 40);
    ExceptionType type;
    try {
      type = ExceptionType.valueOf(typeText);
    } catch (IllegalArgumentException exceptionType) {
      throw new AiException(
          AiErrorCode.AI_BAD_RESPONSE, "AI returned an unsupported exception type.");
    }
    String risk = validator.requireText(object, "riskLevel", 10);
    if (!List.of("LOW", "MEDIUM", "HIGH").contains(risk)) {
      throw new AiException(AiErrorCode.AI_BAD_RESPONSE, "AI returned an unsupported risk level.");
    }
    return new ExceptionAdvice(
        type,
        risk,
        validator.requireText(object, "reason", 500),
        validator.requireTextList(object, "steps", 6, 300),
        validator.requireText(object, "suggestedResolution", 500));
  }

  static String sanitize(String value) {
    if (value == null) return "";
    return value.replaceAll("(?<!\\d)1\\d{10}(?!\\d)", "[MOBILE_REDACTED]");
  }

  private String encode(Object value) {
    try {
      return json.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Unable to encode sanitized exception context", exception);
    }
  }

  public record ExceptionAdvice(
      ExceptionType suggestedType,
      String riskLevel,
      String reason,
      List<String> steps,
      String suggestedResolution) {}
}
