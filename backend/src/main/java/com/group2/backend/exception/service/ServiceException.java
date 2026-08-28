package com.group2.backend.exception.service;

import org.springframework.http.HttpStatus;

import com.group2.backend.exception.base.CustomException;

public class ServiceException extends CustomException {
  public ServiceException(String message) {
    super("SERVICE_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
  public ServiceException(String message, HttpStatus status) {
    super("SERVICE_ERROR", message, status);
  }
}
