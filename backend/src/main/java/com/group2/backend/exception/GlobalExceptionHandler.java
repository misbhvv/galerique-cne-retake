package com.group2.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.group2.backend.dto.ApiError;
import com.group2.backend.exception.base.CustomException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiError> handleCustom(CustomException ex) {
    ApiError err = new ApiError(
      ex.getCode(),
      ex.getMessage()
    );
    return ResponseEntity.status(ex.getStatus()).body(err);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnknown(Exception ex) {
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(500)
      .body(new ApiError("INTERNAL_ERROR", "Internal server error"));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiError> handleMaxUpload(MaxUploadSizeExceededException ex) {
    return ResponseEntity.status(413)
      .body(new ApiError("UPLOAD_TOO_LARGE", "Upload exceeds allowed size limits. Max 5MB per file and 20MB per request."));
  }
}
