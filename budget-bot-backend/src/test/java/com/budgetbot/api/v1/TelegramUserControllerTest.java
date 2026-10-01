package com.budgetbot.api.v1;

import com.budgetbot.budget.BudgetService;
import com.budgetbot.config.AppProperties;
import com.budgetbot.user.AppUser;
import com.budgetbot.user.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TelegramUserControllerTest {
  private MockMvc mvc;
  private AppUserService users;

  @BeforeEach void setup() {
    users = mock(AppUserService.class);
    var budgets = mock(BudgetService.class);
    var props = new AppProperties("Europe/Kyiv", "test-key", "fwd-secret",
        new AppProperties.Telegram("", "tg"),
        new AppProperties.Monobank("https://api.monobank.ua", "", "0"));
    var controller = new TelegramUserController(users, budgets, null, null, props);
    mvc = MockMvcBuilders.standaloneSetup(controller)
        .addFilters(new ApiKeyFilter(props))
        .setControllerAdvice(new ApiExceptionHandler())
        .build();
  }

  @Test void bootstrapRequiresApiKey() throws Exception {
    mvc.perform(post("/api/v1/users/telegram/42/bootstrap")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isUnauthorized());
  }

  @Test void bootstrapCreatesUser() throws Exception {
    var user = new AppUser(42L, 99L);
    when(users.bootstrap(eq(42L), eq(99L))).thenReturn(new AppUserService.BootstrapResult(user, true));
    mvc.perform(post("/api/v1/users/telegram/42/bootstrap")
            .header("X-API-Key", "test-key")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"chatId\":99,\"username\":\"nick\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.telegramUserId").value(42))
        .andExpect(jsonPath("$.created").value(true));
  }
}
