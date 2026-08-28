package com.group2.backend.exception.base;

import org.springframework.http.HttpStatus;

public abstract class CustomException extends RuntimeException {

  private final String code;
  private final HttpStatus status;

  protected CustomException(String code, String message, HttpStatus status) {
    super(message);
    this.code = code;
    this.status = status;
  }

  public String getCode() { return code; }
  public HttpStatus getStatus() { return status; }
}
