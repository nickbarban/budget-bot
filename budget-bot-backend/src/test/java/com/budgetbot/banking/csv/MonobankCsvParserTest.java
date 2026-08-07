package com.budgetbot.banking.csv;
import com.budgetbot.transaction.ExpenseClassifier;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
class MonobankCsvParserTest {
  @Test void parsesAndClassifiesPurchaseAndTransfer(){
    String csv="Дата i час операції,Деталі операції,MCC,Сума в валюті картки (UAH),Сума в валюті операції,Валюта,Курс,Сума комісій (UAH),Сума кешбеку (UAH),Залишок після операції\n"+
      "24.07.2026 10:00:00,NOVUS,5411,-250.50,-250.50,UAH,—,—,—,10000\n"+
      "24.07.2026 11:00:00,На баловство,4829,-3.50,-3.50,UAH,—,—,—,9996.50\n";
    var parser=new MonobankCsvParser(new ExpenseClassifier());
    var tx=parser.parse(csv.getBytes(StandardCharsets.UTF_8));
    assertThat(tx).hasSize(2);
    assertThat(tx.get(0).expense()).isTrue();
    assertThat(tx.get(1).expense()).isFalse();
    assertThat(tx.get(1).transfer()).isTrue();
  }
}
