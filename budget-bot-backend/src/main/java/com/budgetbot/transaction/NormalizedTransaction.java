package com.budgetbot.transaction;
import java.math.BigDecimal;
import java.time.Instant;
public record NormalizedTransaction(String bank,String source,String externalId,String fingerprint,Instant occurredAt,BigDecimal amount,String currency,String description,String merchantName,Integer mcc,boolean expense,boolean transfer,boolean refund,String rawData) {}
