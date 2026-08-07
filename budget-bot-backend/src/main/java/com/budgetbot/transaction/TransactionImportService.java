package com.budgetbot.transaction;
import com.budgetbot.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor
public class TransactionImportService {
  private final BankTransactionRepository repository;
  @Transactional
  public ImportResult importTransactions(AppUser user, List<NormalizedTransaction> transactions) {
    int imported=0, duplicates=0;
    for (var tx: transactions) {
      if (repository.existsByUserIdAndBankAndFingerprint(user.getId(), tx.bank(), tx.fingerprint())) { duplicates++; continue; }
      repository.save(new BankTransaction(user, tx)); imported++;
    }
    return new ImportResult(imported, duplicates);
  }
  public record ImportResult(int imported, int duplicates) {}
}
