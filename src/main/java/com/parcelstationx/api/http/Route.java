package com.parcelstationx.api.http;

import com.parcelstationx.model.UserRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Route {
  private static final Pattern PARAMETER = Pattern.compile("\\{([A-Za-z][A-Za-z0-9]*)}");
  private final String method;
  private final Pattern pathPattern;
  private final List<String> parameterNames;
  private final boolean authenticated;
  private final UserRole requiredRole;
  private final ApiHandler handler;

  public Route(
      String method,
      String path,
      boolean authenticated,
      UserRole requiredRole,
      ApiHandler handler) {
    this.method = method;
    this.parameterNames = new ArrayList<>();
    Matcher matcher = PARAMETER.matcher(path);
    StringBuilder regex = new StringBuilder("^");
    int offset = 0;
    while (matcher.find()) {
      regex.append(Pattern.quote(path.substring(offset, matcher.start()))).append("([^/]+)");
      parameterNames.add(matcher.group(1));
      offset = matcher.end();
    }
    regex.append(Pattern.quote(path.substring(offset))).append('$');
    this.pathPattern = Pattern.compile(regex.toString());
    this.authenticated = authenticated;
    this.requiredRole = requiredRole;
    this.handler = handler;
  }

  public boolean matchesPath(String path) {
    return pathPattern.matcher(path).matches();
  }

  public boolean matchesMethod(String requestMethod) {
    return method.equalsIgnoreCase(requestMethod);
  }

  public Optional<Map<String, String>> parameters(String path) {
    Matcher matcher = pathPattern.matcher(path);
    if (!matcher.matches()) {
      return Optional.empty();
    }
    var values = new java.util.LinkedHashMap<String, String>();
    for (int index = 0; index < parameterNames.size(); index++) {
      values.put(parameterNames.get(index), matcher.group(index + 1));
    }
    return Optional.of(values);
  }

  public boolean authenticated() {
    return authenticated;
  }

  public UserRole requiredRole() {
    return requiredRole;
  }

  public ApiHandler handler() {
    return handler;
  }
}
