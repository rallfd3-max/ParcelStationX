package com.parcelstationx.service;

import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.Shelf;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ShelfCodeGenerator {
  private static final Pattern ZONE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]{0,9}$");

  public String normalizeZone(String value) {
    if (value == null) throw new BusinessException("区域必填。");
    String zone = value.trim().toUpperCase(Locale.ROOT);
    if (zone.endsWith("区")) zone = zone.substring(0, zone.length() - 1).trim();
    if (!ZONE_PATTERN.matcher(zone).matches()) {
      throw new BusinessException("区域必须以字母开头，且只能包含 1 到 10 位字母或数字。");
    }
    return zone;
  }

  public List<String> generate(String zone, int count, List<Shelf> existing) {
    String normalizedZone = normalizeZone(zone);
    Pattern codePattern = Pattern.compile("^" + Pattern.quote(normalizedZone) + "-(\\d+)$");
    int maximum = 0;
    Set<String> used = new HashSet<>();
    for (Shelf shelf : existing) {
      String code = shelf.shelfCode().toUpperCase(Locale.ROOT);
      used.add(code);
      Matcher matcher = codePattern.matcher(code);
      if (matcher.matches()) maximum = Math.max(maximum, Integer.parseInt(matcher.group(1)));
    }
    java.util.ArrayList<String> generated = new java.util.ArrayList<>();
    for (int suffix = maximum + 1; generated.size() < count; suffix++) {
      String code = normalizedZone + "-" + String.format(Locale.ROOT, "%02d", suffix);
      if (!used.contains(code)) generated.add(code);
    }
    return List.copyOf(generated);
  }

  public String normalizeExplicitCode(String value) {
    if (value == null || value.isBlank()) return null;
    String code = value.trim().toUpperCase(Locale.ROOT);
    if (!code.matches("^[A-Z][A-Z0-9]{0,9}-[0-9]{2,6}$")) {
      throw new BusinessException("货架编号格式无效，例如 E-01。");
    }
    return code;
  }

  public String slotCode(String shelfCode, int level, int column) {
    return String.format(Locale.ROOT, "%s-%02d-%02d", shelfCode, level, column);
  }
}
