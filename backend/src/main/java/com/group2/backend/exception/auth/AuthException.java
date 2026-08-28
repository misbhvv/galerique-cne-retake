package com.group2.backend.exception.auth;

import org.springframework.http.HttpStatus;

import com.group2.backend.exception.base.CustomException;

public class AuthException extends CustomException {
  public AuthException(String message) {
    super("AUTH_ERROR", message, HttpStatus.UNAUTHORIZED);
  }

  public AuthException(String message, HttpStatus status) {
    super("AUTH_ERROR", message, status);
  }
}

