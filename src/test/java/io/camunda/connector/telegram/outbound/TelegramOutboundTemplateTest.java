package io.camunda.connector.telegram.outbound;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.feel.FeelEngineWrapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.nio.file.Path;
import java.nio.file.Files;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class TelegramOutboundTemplateTest {

  private static final String CAMUNDA_TEMPLATE_SCHEMA_URL =
      "https://unpkg.com/@camunda/zeebe-element-templates-json-schema@0.36.0/resources/schema.json";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void shouldValidateAgainstCamundaTemplateSchema() throws Exception {
    JsonNode root = readTemplate();

    JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    JsonSchema schema = schemaFactory.getSchema(URI.create(CAMUNDA_TEMPLATE_SCHEMA_URL));
    Set<ValidationMessage> validationMessages = schema.validate(root);

    assertThat(validationMessages)
        .withFailMessage(
            "Template does not match schema %s. Errors: %s",
            CAMUNDA_TEMPLATE_SCHEMA_URL,
            validationMessages)
        .isEmpty();
  }

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
    assertThat(authType.has("feel")).isFalse();
    assertThat(authType.path("value").asText()).isEqualTo("noAuth");

    JsonNode urlHidden = findProperty(properties, "urlHidden");
    assertThat(urlHidden).isNotNull();
    assertThat(urlHidden.path("type").asText()).isEqualTo("Hidden");
    assertThat(urlHidden.path("binding").path("name").asText()).isEqualTo("url");
    assertThat(urlHidden.has("feel")).isFalse();
    assertThat(urlHidden.path("value").asText()).contains("https://api.telegram.org/bot");

    JsonNode bodyHidden = findProperty(properties, "bodyHidden");
    assertThat(bodyHidden).isNotNull();
    assertThat(bodyHidden.path("type").asText()).isEqualTo("Hidden");
    assertThat(bodyHidden.path("binding").path("name").asText()).isEqualTo("body");
    assertThat(bodyHidden.has("feel")).isFalse();
    assertThat(bodyHidden.path("value").asText()).contains("context merge");

    JsonNode methodHidden = findProperty(properties, "methodHidden");
    assertThat(methodHidden).isNotNull();
    assertThat(methodHidden.path("type").asText()).isEqualTo("Hidden");
    assertThat(methodHidden.path("binding").path("name").asText()).isEqualTo("method");
    assertThat(methodHidden.has("feel")).isFalse();
    assertThat(methodHidden.path("value").asText()).isEqualTo("post");

    JsonNode taskDefinitionType =
        findHiddenByBinding(properties, "zeebe:taskDefinition", "property", "type");
    assertThat(taskDefinitionType).isNotNull();
    assertThat(taskDefinitionType.path("value").asText()).isEqualTo("io.camunda:http-json:1");
  }

  @Test
  void shouldGenerateExpectedFeelModesForOptionalAndRequiredFields() throws Exception {
    JsonNode properties = readTemplate().path("properties");

    assertThat(findProperty(properties, "botToken").path("feel").asText()).isEqualTo("optional");
    assertThat(findProperty(properties, "webhookUrl").path("feel").asText()).isEqualTo("optional");
    assertThat(findProperty(properties, "chat_id").path("feel").asText()).isEqualTo("optional");
    assertThat(findProperty(properties, "payload").path("feel").asText()).isEqualTo("optional");
    assertThat(findProperty(properties, "reply_markup").path("feel").asText()).isEqualTo("optional");

    assertThat(findProperty(properties, "resultExpression").path("feel").asText()).isEqualTo("required");
    assertThat(findProperty(properties, "errorExpression").path("feel").asText()).isEqualTo("required");
  }

  @Test
  void shouldNotEmitFeelForDisabledHiddenTransportFields() throws Exception {
    JsonNode properties = readTemplate().path("properties");

    assertThat(findProperty(properties, "authTypeHidden").has("feel")).isFalse();
    assertThat(findProperty(properties, "urlHidden").has("feel")).isFalse();
    assertThat(findProperty(properties, "bodyHidden").has("feel")).isFalse();
    assertThat(findProperty(properties, "methodHidden").has("feel")).isFalse();

    for (JsonNode property : properties) {
      if (!property.has("feel")) {
        continue;
      }
      assertThat(property.path("feel").asText()).isIn("optional", "required", "static");
    }
  }

    @Test
    void shouldPlaceDependentHiddenMappingsAfterTheirSourceInputs() throws Exception {
    JsonNode properties = readTemplate().path("properties");

    int botTokenIndex = findPropertyIndex(properties, "botToken");
    int operationMessagesIndex = findPropertyIndex(properties, "operationMessages");
    int payloadIndex = findPropertyIndex(properties, "payload");
    int replyMarkupIndex = findPropertyIndex(properties, "reply_markup");

    int urlHiddenIndex = findPropertyIndex(properties, "urlHidden");
    int bodyHiddenIndex = findPropertyIndex(properties, "bodyHidden");

    assertThat(botTokenIndex).isGreaterThanOrEqualTo(0);
    assertThat(operationMessagesIndex).isGreaterThanOrEqualTo(0);
    assertThat(payloadIndex).isGreaterThanOrEqualTo(0);
    assertThat(replyMarkupIndex).isGreaterThanOrEqualTo(0);
    assertThat(urlHiddenIndex).isGreaterThanOrEqualTo(0);
    assertThat(bodyHiddenIndex).isGreaterThanOrEqualTo(0);

    assertThat(urlHiddenIndex)
      .withFailMessage("urlHidden must be emitted after botToken to access its mapped value at runtime")
      .isGreaterThan(botTokenIndex);
    assertThat(urlHiddenIndex)
      .withFailMessage("urlHidden must be emitted after operation mappings to access operation at runtime")
      .isGreaterThan(operationMessagesIndex);
    assertThat(bodyHiddenIndex)
      .withFailMessage("bodyHidden must be emitted after payload mapping to access _payload at runtime")
      .isGreaterThan(payloadIndex);
    assertThat(bodyHiddenIndex)
      .withFailMessage("bodyHidden must be emitted after reply_markup mapping to access _reply_markup at runtime")
      .isGreaterThan(replyMarkupIndex);
    }

  @Test
  void shouldEvaluateGeneratedUrlFeelScript() throws Exception {
    JsonNode properties = readTemplate().path("properties");
    String urlExpression = findProperty(properties, "urlHidden").path("value").asText();

    FeelEngineWrapper feelEngine = new FeelEngineWrapper();

    String resolvedUrl =
        feelEngine.evaluate(
            urlExpression,
            String.class,
        Map.of("botToken", "abc123", "operation", "sendMessage"));
    assertThat(resolvedUrl).isEqualTo("https://api.telegram.org/botabc123/sendMessage");

    String withoutOperation = feelEngine.evaluate(urlExpression, String.class, Map.of("botToken", "abc123"));
    assertThat(withoutOperation).isEqualTo("https://api.telegram.org/botabc123/");

    Map<String, Object> runtimeLikeContext = new HashMap<>();
    runtimeLikeContext.put("botToken", "{{secrets.TELEGRAM_BOT_TOKEN}}");
    runtimeLikeContext.put("operation", "sendChatAction");
    runtimeLikeContext.put("operationGroup", "messages");
    runtimeLikeContext.put("_params", null);
    runtimeLikeContext.put("_payload", null);
    runtimeLikeContext.put("_reply_markup", null);
    runtimeLikeContext.put("authentication", Map.of("type", "noAuth"));
    runtimeLikeContext.put("method", "post");
    runtimeLikeContext.put("body", Map.of());

    String withStringInputs = feelEngine.evaluate(urlExpression, String.class, runtimeLikeContext);
    assertThat(withStringInputs)
      .isEqualTo("https://api.telegram.org/bot{{secrets.TELEGRAM_BOT_TOKEN}}/sendChatAction");

    String withConcreteToken =
      feelEngine.evaluate(
        urlExpression,
        String.class,
        Map.of("botToken", "1234567890:ABCDEF-abcdef", "operation", "sendChatAction"));
    assertThat(withConcreteToken)
      .isEqualTo("https://api.telegram.org/bot1234567890:ABCDEF-abcdef/sendChatAction");
  }

  @Test
  void shouldEvaluateGeneratedBodyFeelScript() throws Exception {
    JsonNode properties = readTemplate().path("properties");
    String bodyExpression = findProperty(properties, "bodyHidden").path("value").asText();

    Map<String, Object> params = new HashMap<>();
    params.put("chat_id", "12345");
    params.put("text", "hello");
    params.put("emptyField", "");

    Map<String, Object> payload = new HashMap<>();
    payload.put("disable_notification", true);

    FeelEngineWrapper feelEngine = new FeelEngineWrapper();
    Object result =
        feelEngine.evaluate(
            bodyExpression,
            Map.of("_params", params, "_reply_markup", "{\"inline_keyboard\":[]}", "_payload", payload));

    assertThat(result).isInstanceOf(Map.class);
    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) result;

    assertThat(body)
        .containsEntry("chat_id", "12345")
        .containsEntry("text", "hello")
        .containsEntry("reply_markup", "{\"inline_keyboard\":[]}")
        .containsEntry("disable_notification", true)
        .doesNotContainKey("emptyField");
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
    Path templatePath = resolveModuleTemplatePath();
    return MAPPER.readTree(templatePath.toFile());
  }

  private static Path resolveModuleTemplatePath() {
    Path moduleLocal = Path.of("connectors", "telegram-connector.json");
    Path moduleMarker = Path.of("src", "main", "java");
    if (Files.exists(moduleLocal) && Files.exists(moduleMarker)) {
      return moduleLocal;
    }

    Path workspaceLocal = Path.of("connector-template-inbound", "connectors", "telegram-connector.json");
    Path workspaceMarker = Path.of("connector-template-inbound", "src", "main", "java");
    if (Files.exists(workspaceLocal) && Files.exists(workspaceMarker)) {
      return workspaceLocal;
    }

    return moduleLocal;
  }

  private static JsonNode findProperty(JsonNode properties, String id) {
    for (JsonNode property : properties) {
      if (id.equals(property.path("id").asText(null))) {
        return property;
      }
    }
    return null;
  }

  private static int findPropertyIndex(JsonNode properties, String id) {
    for (int index = 0; index < properties.size(); index++) {
      JsonNode property = properties.get(index);
      if (id.equals(property.path("id").asText(null))) {
        return index;
      }
    }
    return -1;
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
