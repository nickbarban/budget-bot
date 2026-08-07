package com.budgetbot.banking.csv;
import com.budgetbot.transaction.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Component;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
@Component @RequiredArgsConstructor
public class MonobankCsvParser implements BankCsvParser {
  private static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
  private final ExpenseClassifier classifier;
  @Override public CsvBank bank(){ return CsvBank.MONOBANK; }
  @Override public List<NormalizedTransaction> parse(byte[] bytes) {
    try (Reader r=new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
         CSVParser parser=CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(r)) {
      List<NormalizedTransaction> out=new ArrayList<>();
      for(var row:parser){
        String dateRaw=row.get("Дата i час операції").replace("\uFEFF","");
        String description=row.get("Деталі операції");
        Integer mcc=parseInt(row.get("MCC"));
        BigDecimal amount=parseDecimal(row.get("Сума в валюті картки (UAH)"));
        Instant at=LocalDateTime.parse(dateRaw,DATE).atZone(ZoneId.of("Europe/Kyiv")).toInstant();
        var c=classifier.classify(amount,mcc,description);
        String fp=Fingerprint.sha256("mono-csv:"+at+":"+amount+":"+description+":"+mcc+":"+row.get("Залишок після операції"));
        out.add(new NormalizedTransaction("MONOBANK","CSV",null,fp,at,amount,"UAH",description,null,mcc,c.expense(),c.transfer(),c.refund(),null));
      }
      return out;
    } catch(IOException e){ throw new IllegalArgumentException("Cannot parse Monobank CSV",e); }
  }
  private static Integer parseInt(String s){ try{return Integer.valueOf(s.trim());}catch(Exception e){return null;} }
  private static BigDecimal parseDecimal(String s){ return new BigDecimal(s.trim().replace(" ","").replace(',','.')); }
}
