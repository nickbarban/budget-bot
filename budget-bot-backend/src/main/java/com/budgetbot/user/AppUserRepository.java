package com.budgetbot.user;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AppUserRepository extends JpaRepository<AppUser,Long> {
  Optional<AppUser> findByTelegramUserId(Long telegramUserId);
}
