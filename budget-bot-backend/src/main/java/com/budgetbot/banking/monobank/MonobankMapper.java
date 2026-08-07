package com.budgetbot.banking.monobank;
import com.budgetbot.transaction.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.Instant;
@Component @RequiredArgsConstructor
public class MonobankMapper {
  private final ExpenseClassifier classifier;
  public NormalizedTransaction map(JsonNode n, String source) {
    String externalId=n.path("id").asText(null);
    Instant occurredAt=Instant.ofEpochSecond(n.path("time").asLong());
    BigDecimal amount=BigDecimal.valueOf(n.path("amount").asLong(),2);
    int mcc=n.path("mcc").asInt();
    String description=n.path("description").asText("");
    var c=classifier.classify(amount,mcc,description);
    String fp=externalId!=null && !externalId.isBlank() ? Fingerprint.sha256("mono:id:"+externalId)
        : Fingerprint.sha256("mono:"+occurredAt+":"+amount+":"+description+":"+mcc);
    return new NormalizedTransaction("MONOBANK",source,externalId,fp,occurredAt,amount,"UAH",description,null,mcc,c.expense(),c.transfer(),c.refund(),n.toString());
  }
}
