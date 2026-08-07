package com.budgetbot.transaction;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Locale;
@Component
public class ExpenseClassifier {
  public Classification classify(BigDecimal amount, Integer mcc, String description) {
    String d = description == null ? "" : description.toLowerCase(Locale.ROOT);
    boolean refund = amount.signum() > 0;
    boolean transfer = (mcc != null && mcc == 4829) || d.contains("на баловство") || d.contains("округлення балансу") || d.contains("переказ на картку");
    boolean expense = amount.signum() < 0 && !transfer;
    return new Classification(expense, transfer, refund);
  }
  public record Classification(boolean expense, boolean transfer, boolean refund) {}
}
