package com.budgetbot.user;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class AppUserService {
  private final AppUserRepository repository;
  @Transactional
  public AppUser getOrCreate(long telegramUserId, long chatId) {
    return repository.findByTelegramUserId(telegramUserId).map(u -> {u.updateChatId(chatId); return u;})
        .orElseGet(() -> repository.save(new AppUser(telegramUserId, chatId)));
  }
}
