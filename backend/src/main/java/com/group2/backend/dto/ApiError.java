package com.group2.backend.dto;

public record ApiError(
  String code,
  String message
) {}
