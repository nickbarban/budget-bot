package com.budgetbot.transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.Instant;
public interface BankTransactionRepository extends JpaRepository<BankTransaction,Long> {
  boolean existsByUserIdAndBankAndFingerprint(Long userId, String bank, String fingerprint);
  @Query("select coalesce(sum(-t.amount),0) from BankTransaction t where t.user.id=:userId and t.expense=true and t.amount<0 and t.occurredAt>=:from and t.occurredAt<:to")
  BigDecimal sumExpenses(@Param("userId") Long userId,@Param("from") Instant from,@Param("to") Instant to);
}
