package com.budgetbot.telegram;
import com.budgetbot.banking.csv.CsvImportService;
import com.budgetbot.banking.monobank.MonobankSyncService;
import com.budgetbot.budget.BudgetService;
import com.budgetbot.user.AppUserService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
@Service @RequiredArgsConstructor
public class TelegramUpdateService {
  private final TelegramClient telegram;
  private final AppUserService users;
  private final BudgetService budgets;
  private final MonobankSyncService monobank;
  private final CsvImportService csv;
  private final TelegramMessageFormatter formatter;

  public void handle(JsonNode update){
    JsonNode message=update.path("message"); if(message.isMissingNode()) return;
    long userId=message.path("from").path("id").asLong(); long chatId=message.path("chat").path("id").asLong();
    var user=users.getOrCreate(userId,chatId);
    try {
      if(message.has("document")) { handleDocument(user,chatId,message.path("document")); return; }
      String text=message.path("text").asText("").trim();
      if(text.startsWith("/budget")) {
        String[] p=text.split("\\s+"); if(p.length<2) throw new IllegalArgumentException("Використання: /budget 1000");
        var b=budgets.setDailyBudget(user,new BigDecimal(p[1].replace(',','.')));
        telegram.sendMessage(chatId,"✅ Денний бюджет встановлено: <b>"+b.getDailyAmount()+" грн</b>");
      } else if(text.equals("/sync")) {
        var r=monobank.syncCurrentMonth(user); telegram.sendMessage(chatId,"✅ Monobank синхронізовано. Нових: <b>"+r.imported()+"</b>, дублів: <b>"+r.duplicates()+"</b>");
      } else if(text.equals("/status")) {
        telegram.sendMessage(chatId,formatter.status(budgets.status(user)));
      } else if(text.equals("/start") || text.equals("/help")) {
        telegram.sendMessage(chatId,"<b>Budget Bot</b>\n\n/budget 1000 — денний бюджет\n/sync — синхронізація monobank\n/status — поточний стан\n\nТакож можна надіслати CSV-виписку monobank. PrivatBank CSV adapter підготовлений і буде завершений після тестового файлу.");
      }
    } catch(Exception e){ telegram.sendMessage(chatId,"⚠️ "+escape(e.getMessage())); }
  }
  private void handleDocument(com.budgetbot.user.AppUser user,long chatId,JsonNode doc){
    String name=doc.path("file_name").asText(""); if(!name.toLowerCase().endsWith(".csv")) throw new IllegalArgumentException("Потрібен файл .csv");
    byte[] bytes=telegram.downloadFile(doc.path("file_id").asText());
    var r=csv.importCsv(user,name,bytes);
    telegram.sendMessage(chatId,"✅ <b>"+r.bank()+" CSV</b> імпортовано. Нових: <b>"+r.imported()+"</b>, дублів: <b>"+r.duplicates()+"</b>\n\n"+formatter.status(budgets.status(user)));
  }
  private String escape(String s){ return s==null?"Помилка":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }
}
