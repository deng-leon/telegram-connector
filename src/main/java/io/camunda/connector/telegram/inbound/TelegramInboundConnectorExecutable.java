package io.camunda.connector.telegram.inbound;

import io.camunda.connector.api.annotation.InboundConnector;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.InboundConnectorExecutable;
import io.camunda.connector.generator.java.annotation.ElementTemplate;

@InboundConnector(name = "Telegram Webhook", type = "io.camunda:webhook:1")
@ElementTemplate(
    id = "io.camunda.connector.TelegramInbound.v2",
    name = "Telegram Inbound Connector",
    version = 3,
    description = "Receives Telegram messages via Webhooks.",
    icon = "telegram.svg",
    propertyGroups = {
      @ElementTemplate.PropertyGroup(id = "configuration", label = "Configuration")
    },
    inputDataClass = TelegramInboundConnectorProperties.class)
  public class TelegramInboundConnectorExecutable
    implements InboundConnectorExecutable<InboundConnectorContext> {

  @Override
  public void activate(InboundConnectorContext context) {}

  @Override
  public void deactivate() {}
}
