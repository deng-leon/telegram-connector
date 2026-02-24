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

        try {
            deregisterWebhook();
        } catch (Exception e) {
            LOG.warn("Failed to deregister Telegram webhook", e);
        } finally {
            if (context != null) {
                context.reportHealth(Health.down());
            }
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

    private void deregisterWebhook() throws Exception {
        if (properties == null || properties.botToken() == null || properties.botToken().isBlank()) {
            return;
        }
        callTelegramApi("deleteWebhook", "drop_pending_updates=true");
    }

    private void callTelegramApi(String operation, String query) throws Exception {
        StringBuilder endpoint = new StringBuilder("https://api.telegram.org/bot")
            .append(properties.botToken())
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
                "Telegram API call failed for operation %s with status %s"
                    .formatted(operation, response.statusCode()));
        }

        Map<String, Object> responsePayload = MAPPER.readValue(response.body(), new TypeReference<>() {});
        if (!Boolean.TRUE.equals(responsePayload.get("ok"))) {
            throw new IllegalStateException(
                "Telegram API returned ok=false for operation %s: %s"
                    .formatted(operation, response.body()));
        }
    }

    private String constructWebhookUrl() {
        if (properties.inboundContext() == null || properties.inboundContext().isBlank()) {
            throw new IllegalArgumentException("Property inbound.context must not be empty");
        }

        if (properties.baseUrl() != null && !properties.baseUrl().isBlank()) {
            String url = properties.baseUrl().endsWith("/")
                ? properties.baseUrl().substring(0, properties.baseUrl().length() - 1)
                : properties.baseUrl();
            return url + "/inbound/" + properties.inboundContext();
        }

        String envBaseUrl = System.getenv("TELEGRAM_WEBHOOK_BASE_URL");
        if (envBaseUrl != null && !envBaseUrl.isBlank()) {
            String url = envBaseUrl.endsWith("/")
                ? envBaseUrl.substring(0, envBaseUrl.length() - 1)
                : envBaseUrl;
            return url + "/inbound/" + properties.inboundContext();
        }

        String region = System.getenv("CAMUNDA_CLIENT_CLOUD_REGION");
        String clusterId = System.getenv("CAMUNDA_CLIENT_CLOUD_CLUSTERID");

        if (region == null) region = System.getenv("ZEEBE_CLIENT_CLOUD_REGION");
        if (clusterId == null) clusterId = System.getenv("ZEEBE_CLIENT_CLOUD_CLUSTER_ID");

        if (region != null && !region.isBlank() && clusterId != null && !clusterId.isBlank()) {
            LOG.info("Detected Camunda 8 SaaS environment (Region: {}, Cluster: {})", region, clusterId);
            return String.format("https://%s.connectors.camunda.io/%s/inbound/%s", 
                region, clusterId, properties.inboundContext());
        }

        throw new IllegalStateException(
            "Cannot determine webhook base URL. Set 'baseUrl' in the connector config or TELEGRAM_WEBHOOK_BASE_URL env var.");
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
