package com.budgetbot.budget;
import com.budgetbot.config.AppProperties;
import com.budgetbot.transaction.BankTransactionRepository;
import com.budgetbot.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
@Service @RequiredArgsConstructor
public class BudgetService {
  private final BudgetRepository budgetRepository;
  private final BankTransactionRepository transactionRepository;
  private final AppProperties properties;

  @Transactional
  public Budget setDailyBudget(AppUser user, BigDecimal amount) {
    if (amount.signum() <= 0) throw new IllegalArgumentException("Budget must be positive");
    return budgetRepository.save(new Budget(user, amount.setScale(2, RoundingMode.HALF_UP), LocalDate.now(zone())));
  }

  @Transactional(readOnly=true)
  public BudgetStatus status(AppUser user) {
    Budget budget = budgetRepository.findFirstByUserIdOrderByValidFromDesc(user.getId())
        .orElseThrow(() -> new IllegalStateException("Budget is not configured. Use /budget 1000"));
    ZoneId zone=zone();
    LocalDate today=LocalDate.now(zone);
    LocalDate monthStart=today.withDayOfMonth(1);
    Instant from=monthStart.atStartOfDay(zone).toInstant();
    Instant tomorrow=today.plusDays(1).atStartOfDay(zone).toInstant();
    BigDecimal spent=transactionRepository.sumExpenses(user.getId(), from, tomorrow);
    BigDecimal allowed=budget.getDailyAmount().multiply(BigDecimal.valueOf(today.getDayOfMonth()));
    BigDecimal balance=allowed.subtract(spent);
    int daysInMonth=today.lengthOfMonth();
    int daysAfterToday=daysInMonth-today.getDayOfMonth();
    BigDecimal monthBudget=budget.getDailyAmount().multiply(BigDecimal.valueOf(daysInMonth));
    BigDecimal remaining=monthBudget.subtract(spent);
    BigDecimal recommended=daysAfterToday == 0 ? remaining.max(BigDecimal.ZERO)
        : remaining.max(BigDecimal.ZERO).divide(BigDecimal.valueOf(daysAfterToday),2,RoundingMode.HALF_UP);
    BigDecimal tomorrowBalance=balance.add(budget.getDailyAmount());
    return new BudgetStatus(today,budget.getDailyAmount(),allowed,spent,balance,tomorrowBalance,recommended);
  }

  private ZoneId zone(){ return ZoneId.of(properties.timezone()==null ? "Europe/Kyiv" : properties.timezone()); }
  public record BudgetStatus(LocalDate date, BigDecimal dailyBudget, BigDecimal allowedToDate, BigDecimal spent, BigDecimal balance, BigDecimal tomorrowBalance, BigDecimal recommendedDailyLimit) {}
}
