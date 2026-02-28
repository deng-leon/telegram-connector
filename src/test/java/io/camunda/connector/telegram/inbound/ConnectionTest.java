package io.camunda.connector.telegram.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class ConnectionTest {

  @Test
  void shouldMapTelegramMessageEvent() {
    Map<String, Object> event =
        Map.of(
            "update_id", 12345,
            "message",
                Map.of(
                    "text", "hello",
                    "chat", Map.of("id", 789L),
                    "from", Map.of("username", "camunda-bot")));

    TelegramInboundConnectorEvent mapped = new TelegramInboundConnectorEvent(event);

    assertThat(mapped.updateId()).isEqualTo(12345L);
    assertThat(mapped.updateType()).isEqualTo("message");
    assertThat(mapped.text()).isEqualTo("hello");
    assertThat(mapped.chatId()).isEqualTo(789L);
    assertThat(mapped.senderUsername()).isEqualTo("camunda-bot");
  }

  @Test
  void shouldHandleNonMessageUpdates() {
    Map<String, Object> event = Map.of("update_id", 99, "callback_query", Map.of("id", "abc"));

    TelegramInboundConnectorEvent mapped = new TelegramInboundConnectorEvent(event);

    assertThat(mapped.updateId()).isEqualTo(99L);
    assertThat(mapped.updateType()).isEqualTo("callback_query");
    assertThat(mapped.text()).isNull();
    assertThat(mapped.chatId()).isNull();
    assertThat(mapped.senderUsername()).isNull();
  }
}
