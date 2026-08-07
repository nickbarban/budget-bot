package com.budgetbot.banking.csv;
import com.budgetbot.transaction.NormalizedTransaction;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
public class PrivatBankCsvParser implements BankCsvParser {
  @Override public CsvBank bank(){ return CsvBank.PRIVATBANK; }
  @Override public List<NormalizedTransaction> parse(byte[] bytes) {
    throw new UnsupportedOperationException("PrivatBank CSV format is wired into the architecture but needs one real Privat24 CSV sample to finalize column mapping.");
  }
}
