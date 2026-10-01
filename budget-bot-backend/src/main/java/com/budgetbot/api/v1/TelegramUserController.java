package com.budgetbot.api.v1;

import com.budgetbot.banking.csv.CsvImportService;
import com.budgetbot.banking.monobank.MonobankSyncService;
import com.budgetbot.budget.BudgetService;
import com.budgetbot.config.AppProperties;
import com.budgetbot.user.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/v1/users/telegram/{telegramUserId}")
@RequiredArgsConstructor
public class TelegramUserController {
  private final AppUserService users;
  private final BudgetService budgets;
  private final MonobankSyncService monobank;
  private final CsvImportService csv;
  private final AppProperties props;

  @PostMapping("/bootstrap")
  public ApiDtos.UserResponse bootstrap(
      @PathVariable long telegramUserId,
      @RequestBody(required = false) ApiDtos.TelegramProfile profile) {
    long chatId = profile != null && profile.chatId() != null ? profile.chatId() : telegramUserId;
    var result = users.bootstrap(telegramUserId, chatId);
    return ApiDtos.UserResponse.from(result.user(), result.created());
  }

  @GetMapping("/budget/status")
  public ApiDtos.BudgetStatusResponse status(@PathVariable long telegramUserId) {
    return ApiDtos.BudgetStatusResponse.from(budgets.status(users.require(telegramUserId)));
  }

  @PutMapping("/budget/daily")
  public ApiDtos.BudgetStatusResponse setDaily(
      @PathVariable long telegramUserId,
      @Valid @RequestBody ApiDtos.DailyBudgetRequest body) {
    if (!"UAH".equalsIgnoreCase(body.currency())) {
      throw new IllegalArgumentException("Only UAH is supported");
    }
    var user = users.require(telegramUserId);
    budgets.setDailyBudget(user, body.amount());
    return ApiDtos.BudgetStatusResponse.from(budgets.status(user));
  }

  @PostMapping("/banks/monobank/sync")
  public ApiDtos.SyncResponse sync(@PathVariable long telegramUserId) {
    var user = users.require(telegramUserId);
    ZoneId zone = ZoneId.of(props.timezone() == null ? "Europe/Kyiv" : props.timezone());
    LocalDate today = LocalDate.now(zone);
    Instant from = today.withDayOfMonth(1).atStartOfDay(zone).toInstant();
    Instant to = Instant.now();
    var result = monobank.syncCurrentMonth(user);
    return new ApiDtos.SyncResponse(result.imported(), result.duplicates(), from, to, Instant.now());
  }

  @PostMapping(value = "/imports/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiDtos.CsvImportResponse importCsv(
      @PathVariable long telegramUserId,
      @RequestParam("file") MultipartFile file) throws Exception {
    var user = users.require(telegramUserId);
    var name = file.getOriginalFilename() == null ? "upload.csv" : file.getOriginalFilename();
    var result = csv.importCsv(user, name, file.getBytes());
    ApiDtos.BudgetStatusResponse status = null;
    try {
      status = ApiDtos.BudgetStatusResponse.from(budgets.status(user));
    } catch (IllegalStateException ignored) {
      // budget may be unset; import result still returned
    }
    return new ApiDtos.CsvImportResponse(result.bank().name(), result.imported(), result.duplicates(), status);
  }
}
