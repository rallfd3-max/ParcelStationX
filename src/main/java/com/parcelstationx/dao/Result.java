package com.parcelstationx.dao;
import java.util.Optional;
public record Result<T>(boolean success, String message, T data) { public static <T> Result<T> success(T data) { return new Result<>(true, "", data); } public static <T> Result<T> failure(String message) { return new Result<>(false, message, null); } public Optional<T> value() { return Optional.ofNullable(data); } }
