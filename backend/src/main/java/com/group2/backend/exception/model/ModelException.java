package com.group2.backend.exception.model;

import org.springframework.http.HttpStatus;

import com.group2.backend.exception.base.CustomException;

public class ModelException extends CustomException {
  public ModelException(String message) {
    super("MODEL_ERROR", message, HttpStatus.BAD_REQUEST);
  }
}
