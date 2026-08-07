package com.budgetbot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class BudgetBotApplication {
  public static void main(String[] args) { SpringApplication.run(BudgetBotApplication.class, args); }
}
