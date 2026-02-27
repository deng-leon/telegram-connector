package io.camunda.connector.telegram.inbound;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.annotation.InboundConnector;
import io.camunda.connector.api.inbound.Health;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.InboundConnectorExecutable;
import io.camunda.connector.api.inbound.webhook.MappedHttpRequest;
import io.camunda.connector.api.inbound.webhook.WebhookConnectorExecutable;
import io.camunda.connector.api.inbound.webhook.WebhookHttpResponse;
import io.camunda.connector.api.inbound.webhook.WebhookProcessingPayload;
import io.camunda.connector.api.inbound.webhook.WebhookResult;
import io.camunda.connector.api.inbound.webhook.WebhookResultContext;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InboundConnector(name = "Telegram Webhook", type = "io.camunda:telegram-inbound:1")
@ElementTemplate(
    id = "io.camunda.connector.TelegramInbound.v2",
    name = "Telegram Inbound Connector",
    version = 2,
    description = "Receives Telegram messages via Webhooks.",
    icon = "telegram.svg",
    propertyGroups = {
      @ElementTemplate.PropertyGroup(id = "authentication", label = "Authentication"),
      @ElementTemplate.PropertyGroup(id = "configuration", label = "Configuration")
    },
    inputDataClass = TelegramInboundConnectorProperties.class)
public class TelegramInboundConnectorExecutable
  implements WebhookConnectorExecutable, InboundConnectorExecutable<InboundConnectorContext> {

  private static final Logger LOG =
      LoggerFactory.getLogger(TelegramInboundConnectorExecutable.class);
  private static final HttpClient CLIENT = HttpClient.newHttpClient();
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private InboundConnectorContext context;
  private TelegramInboundConnectorProperties properties;

  @Override
  public void activate(InboundConnectorContext connectorContext) {
    this.context = Objects.requireNonNull(connectorContext, "connectorContext must not be null");
    this.properties = connectorContext.bindProperties(TelegramInboundConnectorProperties.class);

    if (properties.inboundContext() == null || properties.inboundContext().isBlank()) {
      var error = new IllegalArgumentException("Property inbound.context must not be empty");
      context.reportHealth(Health.down(error));
      throw error;
    }

    if (properties.botToken() == null || properties.botToken().isBlank()) {
      var error = new IllegalArgumentException("Property botToken must not be empty");
      context.reportHealth(Health.down(error));
      throw error;
    }

    LOG.info("Activating Telegram Webhook Connector (context: {})", properties.inboundContext());

    Map<String, String> env = System.getenv();
    if (shouldRegisterWebhook(env)) {
      try {
        registerWebhook(env);
      } catch (Exception e) {
        context.reportHealth(Health.down(e));
        throw new RuntimeException("Failed to register Telegram webhook", e);
      }
    } else {
      LOG.info("No base URL configured. Skipping Telegram setWebhook registration in connector activation.");
    }

    context.reportHealth(Health.up());
  }

  @Override
  public void deactivate() {
    LOG.info(
        "Deactivating Telegram Webhook Connector (context: {})",
        properties != null ? properties.inboundContext() : "unknown");

    if (context != null) {
      context.reportHealth(Health.down());
    }
  }

  @Override
  public WebhookResult triggerWebhook(WebhookProcessingPayload payload) throws Exception {
    LOG.debug(
        "Telegram Webhook triggered with context {} and method {}",
        properties.inboundContext(),
        payload.method());

    Map<String, Object> body =
        payload.rawBody() == null || payload.rawBody().length == 0
            ? Map.of()
            : MAPPER.readValue(payload.rawBody(), new TypeReference<>() {});

    MappedHttpRequest mappedRequest = new MappedHttpRequest(body, payload.headers(), payload.params());

    return new WebhookResult() {
      @Override
      public MappedHttpRequest request() {
        return mappedRequest;
      }

      @Override
      public Map<String, Object> connectorData() {
        return body;
      }

      @Override
      public Function<WebhookResultContext, WebhookHttpResponse> response() {
        return (ctx) -> WebhookHttpResponse.ok(Map.of("status", "received"));
      }
    };
  }

  private boolean shouldRegisterWebhook(Map<String, String> env) {
    return resolveWebhookBaseUrl(env) != null;
  }

  private void registerWebhook(Map<String, String> env) throws Exception {
    String webhookUrl = constructWebhookUrl(env);
    LOG.info("Registering Telegram webhook for context {}", properties.inboundContext());
    callTelegramApi("setWebhook", "url=" + urlEncode(webhookUrl));
  }

  private void callTelegramApi(String operation, String query) throws Exception {
    String botToken = normalizeToken(properties.botToken());
    StringBuilder endpoint =
        new StringBuilder("https://api.telegram.org/bot").append(botToken).append('/').append(operation);

    if (query != null && !query.isBlank()) {
      endpoint.append('?').append(query);
    }

    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(endpoint.toString())).POST(HttpRequest.BodyPublishers.noBody()).build();

    HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() != 200) {
      throw new IllegalStateException(
          "Telegram API call failed for operation %s with status %s. Response: %s"
              .formatted(operation, response.statusCode(), response.body()));
    }

    Map<String, Object> responsePayload = MAPPER.readValue(response.body(), new TypeReference<>() {});
    if (!Boolean.TRUE.equals(responsePayload.get("ok"))) {
      throw new IllegalStateException(
          "Telegram API returned ok=false for operation %s: %s"
              .formatted(operation, response.body()));
    }
  }

  private String constructWebhookUrl(Map<String, String> env) {
    String inboundContext = normalizePathSegment(properties.inboundContext());
    if (inboundContext == null || inboundContext.isBlank()) {
      throw new IllegalArgumentException("Property inbound.context must not be empty");
    }

    String baseUrl = resolveWebhookBaseUrl(env);
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalStateException(
          "Cannot determine webhook base URL. Set 'baseUrl' in connector config or TELEGRAM_WEBHOOK_BASE_URL env var.");
    }

    String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    if (normalizedBaseUrl.endsWith("/inbound/" + inboundContext)) {
      return normalizedBaseUrl;
    }
    return normalizedBaseUrl + "/inbound/" + inboundContext;
  }

  String resolveWebhookBaseUrl(Map<String, String> env) {
    if (properties.baseUrl() != null && !properties.baseUrl().isBlank()) {
      return properties.baseUrl();
    }

    String envBaseUrl = firstNonBlank(env.get("TELEGRAM_WEBHOOK_BASE_URL"), System.getProperty("TELEGRAM_WEBHOOK_BASE_URL"));
    if (envBaseUrl != null && !envBaseUrl.isBlank()) {
      return envBaseUrl;
    }

    return null;
  }

  private static String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }

  private static String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private static String normalizeToken(String token) {
    if (token == null) {
      return null;
    }
    String trimmed = token.trim();
    if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
      return trimmed.substring(1, trimmed.length() - 1);
    }
    return trimmed;
  }

  private static String normalizePathSegment(String segment) {
    if (segment == null) {
      return null;
    }
    String value = segment.trim();
    if (value.startsWith("/")) {
      value = value.substring(1);
    }
    return value;
  }
}
