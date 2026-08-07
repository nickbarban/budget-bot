package com.budgetbot.banking.csv;
import com.budgetbot.transaction.TransactionImportService;
import com.budgetbot.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class CsvImportService {
  private final BankCsvDetector detector;
  private final List<BankCsvParser> parsers;
  private final TransactionImportService importer;
  public Result importCsv(AppUser user, String filename, byte[] bytes) {
    CsvBank bank=detector.detect(bytes);
    if(bank==CsvBank.UNKNOWN) throw new IllegalArgumentException("Unknown CSV format");
    BankCsvParser parser=parsers.stream().filter(p->p.bank()==bank).findFirst().orElseThrow();
    var result=importer.importTransactions(user, parser.parse(bytes));
    return new Result(bank,result.imported(),result.duplicates());
  }
  public record Result(CsvBank bank,int imported,int duplicates){}
}
