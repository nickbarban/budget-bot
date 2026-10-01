package com.budgetbot.api.v1;

import com.budgetbot.budget.BudgetService;
import com.budgetbot.budget.BudgetService.BudgetStatus;
import com.budgetbot.user.AppUser;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class ApiDtos {
  private ApiDtos() {}

  public record TelegramProfile(Long chatId, String username, String firstName, String lastName, String languageCode) {}

  public record UserResponse(String id, long telegramUserId, boolean created) {
    static UserResponse from(AppUser user, boolean created) {
      String id = user.getId() == null ? null : String.valueOf(user.getId());
      return new UserResponse(id, user.getTelegramUserId(), created);
    }
  }

  public record DailyBudgetRequest(
      @NotNull @DecimalMin("0.01") BigDecimal amount,
      @NotBlank String currency) {}

  public record BudgetStatusResponse(
      LocalDate date,
      String currency,
      BigDecimal dailyBudget,
      BigDecimal allowedToDate,
      BigDecimal spentToDate,
      BigDecimal balance,
      BigDecimal spentToday,
      BigDecimal projectedTomorrowBalance,
      BigDecimal remainingMonthBudget,
      BigDecimal recommendedDailyLimit,
      Integer countedTransactions,
      Instant lastSyncedAt) {
    static BudgetStatusResponse from(BudgetStatus s) {
      BigDecimal remaining = s.dailyBudget()
          .multiply(BigDecimal.valueOf(s.date().lengthOfMonth()))
          .subtract(s.spent());
      return new BudgetStatusResponse(
          s.date(),
          "UAH",
          s.dailyBudget(),
          s.allowedToDate(),
          s.spent(),
          s.balance(),
          BigDecimal.ZERO,
          s.tomorrowBalance(),
          remaining,
          s.recommendedDailyLimit(),
          null,
          null);
    }
  }

  public record SyncResponse(int imported, int duplicates, Instant from, Instant to, Instant syncedAt) {}

  public record CsvImportResponse(String bank, int imported, int duplicates, BudgetStatusResponse status) {}
}
