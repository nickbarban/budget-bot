package com.budgetbot.telegram;
import com.budgetbot.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;
@Component @RequiredArgsConstructor
public class TelegramClient {
  private final AppProperties props;
  private RestClient api(){ return RestClient.builder().baseUrl("https://api.telegram.org/bot"+props.telegram().botToken()).build(); }
  public void sendMessage(long chatId,String text){ api().post().uri("/sendMessage").body(Map.of("chat_id",chatId,"text",text,"parse_mode","HTML")).retrieve().toBodilessEntity(); }
  public byte[] downloadFile(String fileId){
    JsonNode result=api().get().uri("/getFile?file_id={id}",fileId).retrieve().body(JsonNode.class);
    String path=result.path("result").path("file_path").asText();
    return RestClient.create().get().uri("https://api.telegram.org/file/bot"+props.telegram().botToken()+"/"+path).retrieve().body(byte[].class);
  }
}
