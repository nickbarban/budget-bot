package com.budgetbot.banking.monobank;
import com.budgetbot.config.AppProperties;
import com.budgetbot.transaction.TransactionImportService;
import com.budgetbot.user.AppUser;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.ArrayList;
@Service @RequiredArgsConstructor
public class MonobankSyncService {
  private final MonobankClient client;
  private final MonobankMapper mapper;
  private final TransactionImportService importer;
  private final AppProperties props;
  public TransactionImportService.ImportResult syncCurrentMonth(AppUser user) {
    ZoneId zone=ZoneId.of(props.timezone()); LocalDate today=LocalDate.now(zone);
    Instant from=today.withDayOfMonth(1).atStartOfDay(zone).toInstant();
    Instant to=Instant.now();
    JsonNode response=client.statement(props.monobank().accountId(),from,to);
    var list=new ArrayList<com.budgetbot.transaction.NormalizedTransaction>();
    if(response!=null && response.isArray()) response.forEach(n -> list.add(mapper.map(n,"API")));
    return importer.importTransactions(user,list);
  }
}
