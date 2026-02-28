package io.camunda.connector.telegram.outbound;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import java.util.Map;

@OutboundConnector(
    name = "Telegram Outbound Connector",
    inputVariables = {
      "botToken",
      "operationGroup",
      "operation",
      "_params",
      "_payload",
      "_reply_markup"
    },
    type = "io.camunda:http-json:1")
@ElementTemplate(
    id = "io.camunda.connectors.Telegram.v1",
    name = "Telegram Outbound Connector",
    version = 5,
    description =
        "Comprehensive Telegram Connector to manage messages, chats, stickers, and bot settings using the Telegram Bot API.",
    documentationRef = "https://core.telegram.org/bots/api",
    metadata =
      @ElementTemplate.Metadata(
        keywords = {"telegram", "bot", "messages", "chat", "stickers"}),
    icon = "telegram.svg",
    propertyGroups = {
      @ElementTemplate.PropertyGroup(id = "operation", label = "Operation"),
      @ElementTemplate.PropertyGroup(id = "authentication", label = "Bot Authentication"),
      @ElementTemplate.PropertyGroup(id = "parameters", label = "Operation Parameters")
    },
    inputDataClass = TelegramOutboundConnectorProperties.class)
public class TelegramOutboundConnectorFunction implements OutboundConnectorFunction {

  @Override
  public Object execute(OutboundConnectorContext context) {
    return Map.of();
  }
}
