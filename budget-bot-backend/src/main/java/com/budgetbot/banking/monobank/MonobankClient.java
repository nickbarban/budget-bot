package com.budgetbot.banking.monobank;
import com.budgetbot.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.time.Instant;
@Component @RequiredArgsConstructor
public class MonobankClient {
  private final AppProperties props;
  private RestClient client(){ return RestClient.builder().baseUrl(props.monobank().baseUrl()).defaultHeader("X-Token", props.monobank().token()).build(); }
  public JsonNode clientInfo(){ return client().get().uri("/personal/client-info").retrieve().body(JsonNode.class); }
  public JsonNode statement(String account, Instant from, Instant to){
    return client().get().uri("/personal/statement/{account}/{from}/{to}", account, from.getEpochSecond(), to.getEpochSecond()).retrieve().body(JsonNode.class);
  }
  public void setWebhook(String url){ client().post().uri("/personal/webhook").body(java.util.Map.of("webHookUrl",url)).retrieve().toBodilessEntity(); }
}
