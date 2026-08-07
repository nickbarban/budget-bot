package com.budgetbot.telegram;
import com.budgetbot.budget.BudgetService.BudgetStatus;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
@Component
public class TelegramMessageFormatter {
  private final NumberFormat money=NumberFormat.getNumberInstance(Locale.forLanguageTag("uk-UA"));
  public TelegramMessageFormatter(){ money.setMinimumFractionDigits(2); money.setMaximumFractionDigits(2); }
  public String status(BudgetStatus s){
    String emoji=s.balance().signum()<0?"🔴":s.balance().compareTo(s.dailyBudget())>=0?"🟢":"🟡";
    return emoji+" <b>Бюджет на "+s.date().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))+"</b>\n\n"+
        "Денний бюджет: <b>"+m(s.dailyBudget())+" грн</b>\n"+
        "Дозволено до сьогодні: <b>"+m(s.allowedToDate())+" грн</b>\n"+
        "Витрачено: <b>"+m(s.spent())+" грн</b>\n\n"+
        "Баланс: <b>"+sign(s.balance())+m(s.balance())+" грн</b>\n"+
        "Завтра без нових витрат: <b>"+sign(s.tomorrowBalance())+m(s.tomorrowBalance())+" грн</b>\n"+
        "Рекомендовано до кінця місяця: <b>"+m(s.recommendedDailyLimit())+" грн/день</b>";
  }
  private String m(BigDecimal v){ return money.format(v); }
  private String sign(BigDecimal v){ return v.signum()>0?"+":""; }
}
