package io.camunda.connector.telegram.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
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
  void shouldRegisterWebhookWhenBaseUrlIsAvailable() throws Exception {
    TelegramInboundConnectorExecutable executable = new TelegramInboundConnectorExecutable();
    TelegramInboundConnectorProperties props = new TelegramInboundConnectorProperties();
    setField(props, "inbound", Map.of("context", "telegram"));
    props.setBaseUrl("https://example.com");
    setField(executable, "properties", props);

    boolean shouldRegister =
        (boolean)
            invokeSingleArg(
                executable,
                "shouldRegisterWebhook",
                Map.of("CAMUNDA_CLIENT_MODE", "self-managed"));

    assertThat(shouldRegister).isTrue();
  }

  @Test
  void shouldRegisterWebhookInSaasWhenBaseUrlIsAvailable() throws Exception {
    TelegramInboundConnectorExecutable executable = new TelegramInboundConnectorExecutable();
    TelegramInboundConnectorProperties props = new TelegramInboundConnectorProperties();
    setField(props, "inbound", Map.of("context", "telegram"));
    props.setBaseUrl("https://example.com");
    setField(executable, "properties", props);

    boolean shouldRegister =
        (boolean)
            invokeSingleArg(
                executable,
                "shouldRegisterWebhook",
                Map.of("CAMUNDA_CLIENT_MODE", "saas"));

    assertThat(shouldRegister).isTrue();
  }

  @Test
  void shouldResolveBaseUrlFromEnvWhenPropertyMissing() throws Exception {
    TelegramInboundConnectorExecutable executable = new TelegramInboundConnectorExecutable();
    TelegramInboundConnectorProperties props = new TelegramInboundConnectorProperties();
    setField(props, "inbound", Map.of("context", "telegram"));
    setField(executable, "properties", props);

    String resolved =
        (String)
            invokeSingleArg(
                executable,
                "resolveWebhookBaseUrl",
                Map.of("TELEGRAM_WEBHOOK_BASE_URL", "https://env.example.com"));

    assertThat(resolved).isEqualTo("https://env.example.com");
  }

  @Test
  void shouldFailWebhookUrlConstructionWhenBaseUrlMissing() throws Exception {
    TelegramInboundConnectorExecutable executable = new TelegramInboundConnectorExecutable();
    TelegramInboundConnectorProperties props = new TelegramInboundConnectorProperties();
    setField(props, "inbound", Map.of("context", "telegram"));
    setField(executable, "properties", props);

    Exception exception =
        Assertions.assertThrows(
            Exception.class,
            () -> invokeSingleArg(executable, "constructWebhookUrl", Map.of()));

    assertThat(exception.getCause()).isInstanceOf(IllegalStateException.class);
    assertThat(exception.getCause().getMessage())
        .contains("Cannot determine webhook base URL");
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static Object invokeSingleArg(Object target, String methodName, Object arg)
      throws Exception {
    var method = target.getClass().getDeclaredMethod(methodName, Map.class);
    method.setAccessible(true);
    return method.invoke(target, arg);
  }
}
