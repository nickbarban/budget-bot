package com.budgetbot.telegram;
import com.budgetbot.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/telegram") @RequiredArgsConstructor
public class TelegramWebhookController {
  private final TelegramUpdateService service;
  private final AppProperties props;
  @PostMapping("/webhook")
  public ResponseEntity<Void> webhook(@RequestHeader(value="X-Telegram-Bot-Api-Secret-Token",required=false) String secret,@RequestBody JsonNode update){
    if(!props.telegram().webhookSecret().equals(secret)) return ResponseEntity.status(403).build();
    service.handle(update); return ResponseEntity.ok().build();
  }
}
