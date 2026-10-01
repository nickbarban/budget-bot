package com.budgetbot.user;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class AppUserService {
  private final AppUserRepository repository;
  @Transactional
  public AppUser getOrCreate(long telegramUserId, long chatId) {
    return bootstrap(telegramUserId, chatId).user();
  }

  @Transactional
  public BootstrapResult bootstrap(long telegramUserId, long chatId) {
    return repository.findByTelegramUserId(telegramUserId)
        .map(u -> { u.updateChatId(chatId); return new BootstrapResult(u, false); })
        .orElseGet(() -> new BootstrapResult(repository.save(new AppUser(telegramUserId, chatId)), true));
  }

  @Transactional(readOnly = true)
  public AppUser require(long telegramUserId) {
    return repository.findByTelegramUserId(telegramUserId)
        .orElseThrow(() -> new IllegalStateException("User is not bootstrapped"));
  }

  public record BootstrapResult(AppUser user, boolean created) {}
}
