package io.camunda.connector.telegram.inbound;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.generator.java.annotation.TemplateProperty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramInboundConnectorProperties {

    @TemplateProperty(
        id = "inbound.context",
        label = "Webhook ID",
        group = "configuration",
        description = "The unique path of your webhook URL.",
        binding = @TemplateProperty.PropertyBinding(name = "inbound.context")
    )
    @NotNull
    private Map<String, String> inbound;

    @TemplateProperty(
        id = "inbound.subtype",
        label = "Webhook subtype",
        description = "Internal subtype for SaaS HTTP Webhook connector runtime.",
        type = TemplateProperty.PropertyType.Hidden,
        defaultValue = "ConfigurableInboundWebhook",
        binding = @TemplateProperty.PropertyBinding(name = "inbound.subtype"),
        optional = false
    )
    private String inboundSubtype;

    @TemplateProperty(
        id = "inbound.method",
        label = "Webhook method",
        description = "HTTP method restriction for webhook requests.",
        type = TemplateProperty.PropertyType.Hidden,
        defaultValue = "post",
        binding = @TemplateProperty.PropertyBinding(name = "inbound.method"),
        optional = false
    )
    private String inboundMethod;

    // Default constructor for Jackson
    public TelegramInboundConnectorProperties() {}

    public String inboundContext() {
        if (inbound != null && inbound.containsKey("context")) {
            return inbound.get("context");
        }
        return null;
    }

    @Override
    public String toString() {
        return "TelegramInboundConnectorProperties{" +
                "inboundContext='" + inboundContext() + '\'' +
                ", inboundSubtype='" + inboundSubtype + '\'' +
                ", inboundMethod='" + inboundMethod + '\'' +
                '}';
    }
}
