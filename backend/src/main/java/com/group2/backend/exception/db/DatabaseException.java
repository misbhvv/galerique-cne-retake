package com.group2.backend.exception.db;

import org.springframework.http.HttpStatus;

import com.group2.backend.exception.base.CustomException;

public class DatabaseException extends CustomException {
  public DatabaseException(String message) {
    super("DB_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
