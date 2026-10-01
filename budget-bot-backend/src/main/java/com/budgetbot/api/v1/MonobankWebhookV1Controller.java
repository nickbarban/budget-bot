package com.budgetbot.api.v1;

import com.budgetbot.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/monobank")
@RequiredArgsConstructor
public class MonobankWebhookV1Controller {
  private final AppProperties props;

  @GetMapping
  public ResponseEntity<Void> verify() {
    return ResponseEntity.ok().build();
  }

  @PostMapping
  public ResponseEntity<Void> event(
      @RequestHeader(value = "X-Forwarded-Webhook-Secret", required = false) String secret,
      @RequestBody JsonNode body) {
    if (!ApiKeyFilter.matches(props.webhookForwardSecret(), secret)) {
      return ResponseEntity.status(401).build();
    }
    // Milestone 2: map account -> user and persist StatementItem via MonobankMapper.
    return ResponseEntity.ok().build();
  }
}
