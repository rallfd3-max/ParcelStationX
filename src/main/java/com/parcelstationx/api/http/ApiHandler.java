package com.parcelstationx.api.http;

@FunctionalInterface
public interface ApiHandler {
  Object handle(RequestContext context) throws Exception;
}
