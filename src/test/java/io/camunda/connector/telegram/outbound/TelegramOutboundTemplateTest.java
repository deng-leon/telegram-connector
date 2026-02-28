package io.camunda.connector.telegram.outbound;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class TelegramOutboundTemplateTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void shouldExposeRegisterOperationGroupAndSetWebhookOperation() throws Exception {
    JsonNode root = readTemplate();
    JsonNode properties = root.path("properties");

    JsonNode operationGroup = findProperty(properties, "operationGroup");
    assertThat(operationGroup).isNotNull();

    List<String> operationGroups = new ArrayList<>();
    operationGroup.path("choices").forEach(choice -> operationGroups.add(choice.path("value").asText()));
    assertThat(operationGroups).contains("register");
    assertThat(operationGroups).doesNotContain("updates");

    JsonNode operationRegister = findProperty(properties, "operationRegister");
    assertThat(operationRegister).isNotNull();
    assertThat(operationRegister.path("condition").path("property").asText()).isEqualTo("operationGroup");
    assertThat(operationRegister.path("condition").path("equals").asText()).isEqualTo("register");

    List<String> registerOps = new ArrayList<>();
    operationRegister.path("choices").forEach(choice -> registerOps.add(choice.path("value").asText()));
    assertThat(registerOps).containsExactly("setWebhook");
  }

  @Test
  void shouldRequireWebhookUrlForRegisterOperation() throws Exception {
    JsonNode root = readTemplate();
    JsonNode properties = root.path("properties");

    JsonNode webhookUrl = findProperty(properties, "webhookUrl");
    assertThat(webhookUrl).isNotNull();
    assertThat(webhookUrl.path("binding").path("type").asText()).isEqualTo("zeebe:input");
    assertThat(webhookUrl.path("binding").path("name").asText()).isEqualTo("_params.url");
    assertThat(webhookUrl.path("constraints").path("notEmpty").asBoolean()).isTrue();
    assertThat(webhookUrl.path("condition").path("property").asText()).isEqualTo("operationGroup");
    assertThat(webhookUrl.path("condition").path("equals").asText()).isEqualTo("register");
  }

  @Test
  void shouldExposeExpectedTemplateMetadataAndGroups() throws Exception {
    JsonNode root = readTemplate();

    assertThat(root.path("id").asText()).isEqualTo("io.camunda.connectors.Telegram.v1");
    assertThat(root.path("version").asInt()).isEqualTo(5);
    assertThat(root.path("documentationRef").asText()).isEqualTo("https://core.telegram.org/bots/api");

    List<String> keywords = new ArrayList<>();
    root.path("metadata").path("keywords").forEach(keyword -> keywords.add(keyword.asText()));
    assertThat(keywords).contains("telegram", "bot", "messages", "chat", "stickers");

    List<String> groups = new ArrayList<>();
    root.path("groups").forEach(group -> groups.add(group.path("id").asText()));
    assertThat(groups)
        .containsExactly("operation", "authentication", "parameters", "connector", "output", "error", "retries");
  }

  @Test
  void shouldConfigureHiddenHttpTransportProperties() throws Exception {
    JsonNode properties = readTemplate().path("properties");

    JsonNode authType = findProperty(properties, "authTypeHidden");
    assertThat(authType).isNotNull();
    assertThat(authType.path("type").asText()).isEqualTo("Hidden");
    assertThat(authType.path("binding").path("type").asText()).isEqualTo("zeebe:input");
    assertThat(authType.path("binding").path("name").asText()).isEqualTo("authentication.type");
    assertThat(authType.path("value").asText()).isEqualTo("noAuth");

    JsonNode urlHidden = findProperty(properties, "urlHidden");
    assertThat(urlHidden).isNotNull();
    assertThat(urlHidden.path("binding").path("name").asText()).isEqualTo("url");
    assertThat(urlHidden.path("value").asText()).contains("https://api.telegram.org/bot");

    JsonNode bodyHidden = findProperty(properties, "bodyHidden");
    assertThat(bodyHidden).isNotNull();
    assertThat(bodyHidden.path("binding").path("name").asText()).isEqualTo("body");
    assertThat(bodyHidden.path("value").asText()).contains("context merge");

    JsonNode methodHidden = findProperty(properties, "methodHidden");
    assertThat(methodHidden).isNotNull();
    assertThat(methodHidden.path("binding").path("name").asText()).isEqualTo("method");
    assertThat(methodHidden.path("value").asText()).isEqualTo("post");

    JsonNode taskDefinitionType =
        findHiddenByBinding(properties, "zeebe:taskDefinition", "property", "type");
    assertThat(taskDefinitionType).isNotNull();
    assertThat(taskDefinitionType.path("value").asText()).isEqualTo("io.camunda:http-json:1");
  }

  @Test
  void shouldExposeOperationSelectorsWithCorrectRoutingConditions() throws Exception {
    JsonNode properties = readTemplate().path("properties");

    assertOperationSelector(properties, "operationMessages", "messages");
    assertOperationSelector(properties, "operationChat", "chat");
    assertOperationSelector(properties, "operationEditing", "editing");
    assertOperationSelector(properties, "operationStickers", "stickers_inline");
    assertOperationSelector(properties, "operationSettings", "settings");

    JsonNode operationMessages = findProperty(properties, "operationMessages");
    List<String> messageOps = collectChoiceValues(operationMessages);
    assertThat(messageOps)
        .contains("sendMessage", "sendPhoto", "sendPoll", "sendChatAction")
        .doesNotContain("setWebhook");
  }

  @Test
  void shouldPreserveParameterConditionsAndSpecialFields() throws Exception {
    JsonNode properties = readTemplate().path("properties");

    JsonNode chatId = findProperty(properties, "chat_id");
    assertThat(chatId).isNotNull();
    assertThat(chatId.path("condition").path("property").asText()).isEqualTo("operationGroup");
    Set<String> chatIdAllowed = new HashSet<>();
    chatId.path("condition").path("oneOf").forEach(value -> chatIdAllowed.add(value.asText()));
    assertThat(chatIdAllowed).contains("messages", "chat", "editing", "stickers_inline", "advanced");

    JsonNode parseMode = findProperty(properties, "parse_mode");
    assertThat(parseMode).isNotNull();
    assertThat(parseMode.path("type").asText()).isEqualTo("Dropdown");
    assertThat(collectChoiceValues(parseMode)).contains("", "MarkdownV2", "HTML", "Markdown");

    JsonNode payload = findProperty(properties, "payload");
    assertThat(payload).isNotNull();
    assertThat(payload.path("binding").path("name").asText()).isEqualTo("_payload");
    assertThat(payload.path("type").asText()).isEqualTo("Text");

    JsonNode replyMarkup = findProperty(properties, "reply_markup");
    assertThat(replyMarkup).isNotNull();
    assertThat(replyMarkup.path("binding").path("name").asText()).isEqualTo("_reply_markup");
  }

  @Test
  void shouldExposeConnectorOutputErrorAndRetryMappings() throws Exception {
    JsonNode properties = readTemplate().path("properties");

    JsonNode templateVersion = findProperty(properties, "version");
    assertThat(templateVersion).isNotNull();
    assertThat(templateVersion.path("type").asText()).isEqualTo("Hidden");
    assertThat(templateVersion.path("binding").path("type").asText()).isEqualTo("zeebe:taskHeader");
    assertThat(templateVersion.path("binding").path("key").asText()).isEqualTo("elementTemplateVersion");
    assertThat(templateVersion.path("value").asText()).isEqualTo("5");

    JsonNode resultVariable = findProperty(properties, "resultVariable");
    assertThat(resultVariable).isNotNull();
    assertThat(resultVariable.path("group").asText()).isEqualTo("output");
    assertThat(resultVariable.path("binding").path("key").asText()).isEqualTo("resultVariable");

    JsonNode resultExpression = findProperty(properties, "resultExpression");
    assertThat(resultExpression).isNotNull();
    assertThat(resultExpression.path("feel").asText()).isEqualTo("required");

    JsonNode errorExpression = findProperty(properties, "errorExpression");
    assertThat(errorExpression).isNotNull();
    assertThat(errorExpression.path("group").asText()).isEqualTo("error");
    assertThat(errorExpression.path("binding").path("key").asText()).isEqualTo("errorExpression");

    JsonNode retryCount = findProperty(properties, "retryCount");
    assertThat(retryCount).isNotNull();
    assertThat(retryCount.path("binding").path("type").asText()).isEqualTo("zeebe:taskDefinition");
    assertThat(retryCount.path("binding").path("property").asText()).isEqualTo("retries");
    assertThat(retryCount.path("value").asText()).isEqualTo("3");
  }

  private static JsonNode readTemplate() throws Exception {
    Path templatePath = Path.of("connectors", "telegram-connector.json");
    return MAPPER.readTree(templatePath.toFile());
  }

  private static JsonNode findProperty(JsonNode properties, String id) {
    for (JsonNode property : properties) {
      if (id.equals(property.path("id").asText(null))) {
        return property;
      }
    }
    return null;
  }

  private static JsonNode findHiddenByBinding(
      JsonNode properties, String bindingType, String bindingField, String bindingValue) {
    for (JsonNode property : properties) {
      JsonNode binding = property.path("binding");
      if (!bindingType.equals(binding.path("type").asText())) {
        continue;
      }
      if (bindingValue.equals(binding.path(bindingField).asText())) {
        return property;
      }
    }
    return null;
  }

  private static List<String> collectChoiceValues(JsonNode property) {
    List<String> values = new ArrayList<>();
    property.path("choices").forEach(choice -> values.add(choice.path("value").asText()));
    return values;
  }

  private static void assertOperationSelector(JsonNode properties, String propertyId, String operationGroupValue) {
    JsonNode selector = findProperty(properties, propertyId);
    assertThat(selector).isNotNull();
    assertThat(selector.path("binding").path("name").asText()).isEqualTo("operation");
    assertThat(selector.path("condition").path("property").asText()).isEqualTo("operationGroup");
    assertThat(selector.path("condition").path("equals").asText()).isEqualTo(operationGroupValue);
    assertThat(selector.path("type").asText()).isEqualTo("Dropdown");
  }
}
