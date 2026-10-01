package com.budgetbot.api.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.budgetbot.api")
public class ApiExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
    boolean csv = e.getMessage() != null && e.getMessage().toLowerCase().contains("csv");
    return ResponseEntity.status(csv ? 422 : 400).body(Map.of("error", String.valueOf(e.getMessage())));
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  ResponseEntity<Map<String, String>> unsupported(UnsupportedOperationException e) {
    return ResponseEntity.status(422).body(Map.of("error", String.valueOf(e.getMessage())));
  }

  @ExceptionHandler(IllegalStateException.class)
  ResponseEntity<Map<String, String>> missing(IllegalStateException e) {
    return ResponseEntity.status(404).body(Map.of("error", String.valueOf(e.getMessage())));
  }
}
