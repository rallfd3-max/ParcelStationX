package com.parcelstationx.config;

import java.sql.Connection;

@FunctionalInterface
public interface ConnectionProvider {
  Connection getConnection();
}
