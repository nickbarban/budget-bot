package com.budgetbot.banking.monobank;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/monobank/webhook")
public class MonobankWebhookController {
  @GetMapping public ResponseEntity<Void> verify(){ return ResponseEntity.ok().build(); }
  @PostMapping public ResponseEntity<Void> event(@RequestBody JsonNode event){
    // TODO milestone 2: resolve account -> user and persist StatementItem through MonobankMapper.
    return ResponseEntity.ok().build();
  }
}
