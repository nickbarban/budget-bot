package com.budgetbot.banking.csv;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
@Component
public class BankCsvDetector {
  public CsvBank detect(byte[] bytes) {
    String first = new String(bytes, StandardCharsets.UTF_8).lines().findFirst().orElse("").replace("\uFEFF", "");
    if (first.contains("Дата i час операції") && first.contains("Сума в валюті картки")) return CsvBank.MONOBANK;
    String lower=first.toLowerCase();
    if (lower.contains("приват") || lower.contains("картка") && (lower.contains("сума") || lower.contains("дата"))) return CsvBank.PRIVATBANK;
    return CsvBank.UNKNOWN;
  }
}
