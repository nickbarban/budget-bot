package com.budgetbot.banking.csv;
import com.budgetbot.transaction.NormalizedTransaction;
import java.util.List;
public interface BankCsvParser {
  CsvBank bank();
  List<NormalizedTransaction> parse(byte[] bytes);
}
