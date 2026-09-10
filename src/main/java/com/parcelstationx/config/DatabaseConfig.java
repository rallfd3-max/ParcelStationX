package com.parcelstationx.config;

/** Database connection values resolved from environment variables before local properties. */
public record DatabaseConfig(String url, String username, String password) {}
