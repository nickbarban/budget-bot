package com.budgetbot.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    String timezone,
    String apiKey,
    String webhookForwardSecret,
    Telegram telegram,
    Monobank monobank) {
  public record Telegram(String botToken, String webhookSecret) {}
  public record Monobank(String baseUrl, String token, String accountId) {}
}
