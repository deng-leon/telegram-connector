package io.camunda.connector.telegram.inbound;

import io.camunda.connector.api.annotation.InboundConnector;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.Health;
import io.camunda.connector.api.inbound.webhook.WebhookConnectorExecutable;
import io.camunda.connector.api.inbound.webhook.WebhookProcessingPayload;
import io.camunda.connector.api.inbound.webhook.WebhookResult;
import io.camunda.connector.api.inbound.webhook.MappedHttpRequest;
import io.camunda.connector.api.inbound.webhook.WebhookHttpResponse;
import io.camunda.connector.api.inbound.webhook.WebhookResultContext;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@InboundConnector(name = "Telegram Webhook", type = "io.camunda:telegram-inbound:1")
@ElementTemplate(
    id = "io.camunda.connector.TelegramInbound.v1",
    name = "Telegram Inbound Connector",
    version = 1,
    description = "Receives Telegram messages via Webhooks.",
    icon = "telegram.svg",
    propertyGroups = {
        @ElementTemplate.PropertyGroup(id = "authentication", label = "Authentication"),
        @ElementTemplate.PropertyGroup(id = "configuration", label = "Configuration")
    },
    inputDataClass = TelegramInboundConnectorProperties.class
)
public class TelegramInboundConnectorExecutable implements WebhookConnectorExecutable {

    private static final Logger LOG = LoggerFactory.getLogger(TelegramInboundConnectorExecutable.class);
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

        LOG.info("Activating Telegram Webhook Connector (context: {})", properties.inboundContext());

        try {
            registerWebhook();
            context.reportHealth(Health.up());
        } catch (Exception e) {
            context.reportHealth(Health.down(e));
            throw new RuntimeException("Failed to register Telegram webhook", e);
        }
    }

    @Override
    public void deactivate() {
        LOG.info("Deactivating Telegram Webhook Connector (context: {})",
            properties != null ? properties.inboundContext() : "unknown");

        if (context != null) {
            context.reportHealth(Health.down());
        }
    }

    @Override
    public WebhookResult triggerWebhook(WebhookProcessingPayload payload) throws Exception {
        LOG.debug("Telegram Webhook triggered with context {} and method {}", properties.inboundContext(), payload.method());

        Map<String, Object> body = payload.rawBody() == null || payload.rawBody().length == 0
            ? Map.of()
            : MAPPER.readValue(payload.rawBody(), new TypeReference<>() {});

        MappedHttpRequest mappedRequest = new MappedHttpRequest(
            body,
            payload.headers(),
            payload.params()
        );

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

    private void registerWebhook() throws Exception {
        String webhookUrl = constructWebhookUrl();
        LOG.info("Registering Telegram webhook for context {}", properties.inboundContext());
        callTelegramApi("setWebhook", "url=" + urlEncode(webhookUrl));
    }

    private void callTelegramApi(String operation, String query) throws Exception {
        String botToken = normalizeToken(properties.botToken());
        StringBuilder endpoint = new StringBuilder("https://api.telegram.org/bot")
            .append(botToken)
            .append('/')
            .append(operation);

        if (query != null && !query.isBlank()) {
            endpoint.append('?').append(query);
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint.toString()))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

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

    private String constructWebhookUrl() {
        String inboundContext = normalizePathSegment(properties.inboundContext());
        if (inboundContext == null || inboundContext.isBlank()) {
            throw new IllegalArgumentException("Property inbound.context must not be empty");
        }

        String baseUrl = resolveBaseUrl(System.getenv(), System.getProperties());
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                "Cannot determine webhook base URL. Set 'baseUrl' in the connector config or TELEGRAM_WEBHOOK_BASE_URL env var.");
        }

        String normalizedBaseUrl = baseUrl.endsWith("/")
            ? baseUrl.substring(0, baseUrl.length() - 1)
            : baseUrl;

        if (normalizedBaseUrl.endsWith("/inbound/" + inboundContext)) {
            return normalizedBaseUrl;
        }
        return normalizedBaseUrl + "/inbound/" + inboundContext;
    }

    String resolveBaseUrl(Map<String, String> env, java.util.Properties systemProperties) {
        if (properties.baseUrl() != null && !properties.baseUrl().isBlank()) {
            return properties.baseUrl();
        }

        String envBaseUrl = env.get("TELEGRAM_WEBHOOK_BASE_URL");
        if (envBaseUrl != null && !envBaseUrl.isBlank()) {
            return envBaseUrl;
        }

        String region = firstNonBlank(
            env,
            List.of(
                "CAMUNDA_CLIENT_CLOUD_REGION",
                "ZEEBE_CLIENT_CLOUD_REGION"));

        String clusterId = firstNonBlank(
            env,
            List.of(
                "CAMUNDA_CLIENT_CLOUD_CLUSTER_ID",
                "CAMUNDA_CLIENT_CLOUD_CLUSTERID",
                "ZEEBE_CLIENT_CLOUD_CLUSTER_ID"));

        if ((region == null || region.isBlank()) && systemProperties != null) {
            region = systemProperties.getProperty("camunda.client.cloud.region");
        }
        if ((clusterId == null || clusterId.isBlank()) && systemProperties != null) {
            String dashClusterId = systemProperties.getProperty("camunda.client.cloud.cluster-id");
            String camelClusterId = systemProperties.getProperty("camunda.client.cloud.clusterId");
            clusterId = (dashClusterId != null && !dashClusterId.isBlank()) ? dashClusterId : camelClusterId;
        }

        if (region != null && !region.isBlank() && clusterId != null && !clusterId.isBlank()) {
            LOG.info("Detected Camunda 8 SaaS environment (region={}, clusterId={})", region, clusterId);
            return String.format("https://%s.connectors.camunda.io/%s",
                region, clusterId);
        }

        LOG.warn("Unable to detect Camunda SaaS base URL from environment/system properties");
        return null;
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String firstNonBlank(Map<String, String> values, List<String> keys) {
        for (String key : keys) {
            String value = values.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
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
