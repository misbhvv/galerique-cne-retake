package com.group2.backend.exception.controller;

import org.springframework.http.HttpStatus;

import com.group2.backend.exception.base.CustomException;

public class ControllerException extends CustomException {
  public ControllerException(String message) {
    super("CONTROLLER_ERROR", message, HttpStatus.BAD_REQUEST);
  }
}
