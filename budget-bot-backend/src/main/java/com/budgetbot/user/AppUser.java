package com.budgetbot.user;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
@Entity
@Table(name="app_user")
@Getter @NoArgsConstructor
public class AppUser {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="telegram_user_id", nullable=false, unique=true) private Long telegramUserId;
  @Column(name="telegram_chat_id", nullable=false) private Long telegramChatId;
  @Column(name="created_at", nullable=false) private Instant createdAt;
  public AppUser(Long telegramUserId, Long telegramChatId) {
    this.telegramUserId=telegramUserId; this.telegramChatId=telegramChatId; this.createdAt=Instant.now();
  }
  public void updateChatId(Long chatId){ this.telegramChatId=chatId; }
}
