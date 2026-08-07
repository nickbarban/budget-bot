package com.budgetbot.transaction;
import com.budgetbot.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="bank_transaction") @Getter @NoArgsConstructor
public class BankTransaction {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id") private AppUser user;
  @Column(nullable=false) private String bank;
  @Column(nullable=false) private String source;
  @Column(name="external_id") private String externalId;
  @Column(nullable=false, length=64) private String fingerprint;
  @Column(name="occurred_at", nullable=false) private Instant occurredAt;
  @Column(nullable=false) private BigDecimal amount;
  @Column(nullable=false, length=3) private String currency;
  @Column(columnDefinition="text") private String description;
  @Column(name="merchant_name", columnDefinition="text") private String merchantName;
  private Integer mcc;
  @Column(name="is_expense", nullable=false) private boolean expense;
  @Column(name="is_transfer", nullable=false) private boolean transfer;
  @Column(name="is_refund", nullable=false) private boolean refund;
  @JdbcTypeCode(SqlTypes.JSON) @Column(name="raw_data", columnDefinition="jsonb") private String rawData;
  @Column(name="created_at", nullable=false) private Instant createdAt;
  public BankTransaction(AppUser user, NormalizedTransaction tx) {
    this.user=user; this.bank=tx.bank(); this.source=tx.source(); this.externalId=tx.externalId(); this.fingerprint=tx.fingerprint();
    this.occurredAt=tx.occurredAt(); this.amount=tx.amount(); this.currency=tx.currency(); this.description=tx.description();
    this.merchantName=tx.merchantName(); this.mcc=tx.mcc(); this.expense=tx.expense(); this.transfer=tx.transfer(); this.refund=tx.refund();
    this.rawData=tx.rawData(); this.createdAt=Instant.now();
  }
}
