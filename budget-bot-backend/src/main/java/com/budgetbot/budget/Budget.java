package com.budgetbot.budget;
import com.budgetbot.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
@Entity @Table(name="budget") @Getter @NoArgsConstructor
public class Budget {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id") private AppUser user;
  @Column(name="daily_amount", nullable=false) private BigDecimal dailyAmount;
  @Column(nullable=false, length=3) private String currency;
  @Column(name="valid_from", nullable=false) private LocalDate validFrom;
  @Column(name="created_at", nullable=false) private Instant createdAt;
  public Budget(AppUser user, BigDecimal amount, LocalDate validFrom) {
    this.user=user; this.dailyAmount=amount; this.currency="UAH"; this.validFrom=validFrom; this.createdAt=Instant.now();
  }
}
