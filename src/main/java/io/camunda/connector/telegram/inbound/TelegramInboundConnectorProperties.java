package io.camunda.connector.telegram.inbound;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.camunda.connector.generator.java.annotation.TemplateProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramInboundConnectorProperties {

    @NotEmpty 
    @TemplateProperty(
        id = "botToken",
        label = "Bot Token",
        group = "authentication",
        description = "Your Telegram Bot token"
    )
    private String botToken;

    @TemplateProperty(
        id = "inbound.context",
        label = "Webhook ID",
        group = "configuration",
        description = "The unique path of your webhook URL.",
        binding = @TemplateProperty.PropertyBinding(name = "inbound.context")
    )
    @JsonProperty("inbound")
    @NotNull
    private Map<String, String> inbound;

    @TemplateProperty(
        id = "baseUrl",
        label = "Webhook Base URL (Optional)",
        group = "configuration",
        description = "Only required for Self-Managed or Local development. Leave blank for Camunda 8 SaaS.",
        optional = true
    )
    private String baseUrl;

    // Default constructor for Jackson
    public TelegramInboundConnectorProperties() {}

    public String botToken() { return botToken; }
    public void setBotToken(String botToken) { this.botToken = botToken; }

    public String inboundContext() {
        if (inbound != null && inbound.containsKey("context")) {
            return inbound.get("context");
        }
        return null;
    }

    public String baseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    @Override
    public String toString() {
        return "TelegramInboundConnectorProperties{" +
                "botToken='***', " +
                "inboundContext='" + inboundContext() + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                '}';
    }
}
