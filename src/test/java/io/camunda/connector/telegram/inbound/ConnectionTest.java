package io.camunda.connector.telegram.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
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

  @Test
  void shouldResolveSaasBaseUrlWithoutInboundSuffix() throws Exception {
    TelegramInboundConnectorExecutable executable = new TelegramInboundConnectorExecutable();
    TelegramInboundConnectorProperties props = new TelegramInboundConnectorProperties();
    setField(props, "inbound", Map.of("context", "telegram"));
    setField(executable, "properties", props);

    String baseUrl =
        executable.resolveBaseUrl(
            Map.of(
                "CAMUNDA_CLIENT_CLOUD_REGION", "bru-2",
                "CAMUNDA_CLIENT_CLOUD_CLUSTER_ID", "cluster-id-123"),
            new Properties());

    assertThat(baseUrl).isEqualTo("https://bru-2.connectors.camunda.io/cluster-id-123");
  }

  @Test
  void shouldConstructWebhookUrlWithoutDoubleInbound() throws Exception {
    TelegramInboundConnectorExecutable executable = new TelegramInboundConnectorExecutable();
    TelegramInboundConnectorProperties props = new TelegramInboundConnectorProperties();
    props.setBaseUrl("https://bru-2.connectors.camunda.io/cluster-id-123/inbound/telegram");
    setField(props, "inbound", Map.of("context", "telegram"));
    setField(executable, "properties", props);

    String webhookUrl = (String) invokeNoArg(executable, "constructWebhookUrl");

    assertThat(webhookUrl)
        .isEqualTo("https://bru-2.connectors.camunda.io/cluster-id-123/inbound/telegram");
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static Object invokeNoArg(Object target, String methodName) throws Exception {
    var method = target.getClass().getDeclaredMethod(methodName);
    method.setAccessible(true);
    return method.invoke(target);
  }
}
