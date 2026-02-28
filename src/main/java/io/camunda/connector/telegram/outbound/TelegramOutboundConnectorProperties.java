package io.camunda.connector.telegram.outbound;

import io.camunda.connector.generator.dsl.Property;
import io.camunda.connector.generator.java.annotation.TemplateProperty;

public class TelegramOutboundConnectorProperties {

  @TemplateProperty(
      id = "botToken",
      label = "Bot Token",
      group = "authentication",
      description = "Bot API token provided by @BotFather",
      feel = Property.FeelMode.optional,
      constraints = @TemplateProperty.PropertyConstraints(notEmpty = true),
      binding = @TemplateProperty.PropertyBinding(name = "botToken"))
  private String botToken;

  @TemplateProperty(
      id = "operationGroup",
      label = "Operation Category",
      group = "operation",
      type = TemplateProperty.PropertyType.Dropdown,
      choices = {
        @TemplateProperty.DropdownPropertyChoice(label = "Webhook Registration", value = "register"),
        @TemplateProperty.DropdownPropertyChoice(label = "Messages", value = "messages"),
        @TemplateProperty.DropdownPropertyChoice(label = "Chat Management", value = "chat"),
        @TemplateProperty.DropdownPropertyChoice(label = "Updating Messages", value = "editing"),
        @TemplateProperty.DropdownPropertyChoice(label = "Stickers / Inline-Mode", value = "stickers_inline"),
        @TemplateProperty.DropdownPropertyChoice(label = "Payments / Games / Passport", value = "advanced"),
        @TemplateProperty.DropdownPropertyChoice(label = "Bot Settings", value = "settings")
      },
      binding = @TemplateProperty.PropertyBinding(name = "operationGroup"))
  private String operationGroup;

  @TemplateProperty(
      id = "operationRegister",
      label = "Operation",
      group = "operation",
      type = TemplateProperty.PropertyType.Dropdown,
      choices = {
        @TemplateProperty.DropdownPropertyChoice(label = "Register Webhook", value = "setWebhook")
      },
      condition =
          @TemplateProperty.PropertyCondition(property = "operationGroup", equals = "register"),
      binding = @TemplateProperty.PropertyBinding(name = "operation"))
  private String operationRegister;

  @TemplateProperty(
      id = "webhookUrl",
      label = "Webhook URL",
      group = "parameters",
      description =
          "Complete Telegram webhook callback URL to register (for example: https://example.com/inbound/my-webhook).",
      feel = Property.FeelMode.optional,
      constraints = @TemplateProperty.PropertyConstraints(notEmpty = true),
      condition =
          @TemplateProperty.PropertyCondition(property = "operationGroup", equals = "register"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.url"))
  private String webhookUrl;

  @TemplateProperty(
      id = "operationMessages",
      label = "Operation",
      group = "operation",
      type = TemplateProperty.PropertyType.Dropdown,
      choices = {
        @TemplateProperty.DropdownPropertyChoice(label = "Send Text Message", value = "sendMessage"),
        @TemplateProperty.DropdownPropertyChoice(label = "Forward Message", value = "forwardMessage"),
        @TemplateProperty.DropdownPropertyChoice(label = "Copy Message", value = "copyMessage"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Photo", value = "sendPhoto"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Audio", value = "sendAudio"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Document", value = "sendDocument"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Video", value = "sendVideo"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Animation", value = "sendAnimation"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Voice", value = "sendVoice"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Video Note", value = "sendVideoNote"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Media Group", value = "sendMediaGroup"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Location", value = "sendLocation"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Venue", value = "sendVenue"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Contact", value = "sendContact"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Poll", value = "sendPoll"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Dice", value = "sendDice"),
        @TemplateProperty.DropdownPropertyChoice(label = "Send Chat Action", value = "sendChatAction")
      },
      condition =
          @TemplateProperty.PropertyCondition(property = "operationGroup", equals = "messages"),
      binding = @TemplateProperty.PropertyBinding(name = "operation"))
  private String operationMessages;

  @TemplateProperty(
      id = "operationChat",
      label = "Operation",
      group = "operation",
      type = TemplateProperty.PropertyType.Dropdown,
      choices = {
        @TemplateProperty.DropdownPropertyChoice(label = "Ban Chat Member", value = "banChatMember"),
        @TemplateProperty.DropdownPropertyChoice(label = "Unban Chat Member", value = "unbanChatMember"),
        @TemplateProperty.DropdownPropertyChoice(label = "Restrict Chat Member", value = "restrictChatMember"),
        @TemplateProperty.DropdownPropertyChoice(label = "Promote Chat Member", value = "promoteChatMember"),
        @TemplateProperty.DropdownPropertyChoice(label = "Set Chat Photo", value = "setChatPhoto"),
        @TemplateProperty.DropdownPropertyChoice(label = "Delete Chat Photo", value = "deleteChatPhoto"),
        @TemplateProperty.DropdownPropertyChoice(label = "Set Chat Title", value = "setChatTitle"),
        @TemplateProperty.DropdownPropertyChoice(label = "Set Chat Description", value = "setChatDescription"),
        @TemplateProperty.DropdownPropertyChoice(label = "Pin Chat Message", value = "pinChatMessage"),
        @TemplateProperty.DropdownPropertyChoice(label = "Unpin Chat Message", value = "unpinChatMessage"),
        @TemplateProperty.DropdownPropertyChoice(label = "Unpin All Chat Messages", value = "unpinAllChatMessages"),
        @TemplateProperty.DropdownPropertyChoice(label = "Leave Chat", value = "leaveChat"),
        @TemplateProperty.DropdownPropertyChoice(label = "Get Chat", value = "getChat")
      },
      condition = @TemplateProperty.PropertyCondition(property = "operationGroup", equals = "chat"),
      binding = @TemplateProperty.PropertyBinding(name = "operation"))
  private String operationChat;

  @TemplateProperty(
      id = "operationEditing",
      label = "Operation",
      group = "operation",
      type = TemplateProperty.PropertyType.Dropdown,
      choices = {
        @TemplateProperty.DropdownPropertyChoice(label = "Edit Message Text", value = "editMessageText"),
        @TemplateProperty.DropdownPropertyChoice(label = "Edit Message Caption", value = "editMessageCaption"),
        @TemplateProperty.DropdownPropertyChoice(label = "Edit Message Media", value = "editMessageMedia"),
        @TemplateProperty.DropdownPropertyChoice(label = "Edit Message Reply Markup", value = "editMessageReplyMarkup"),
        @TemplateProperty.DropdownPropertyChoice(label = "Stop Poll", value = "stopPoll"),
        @TemplateProperty.DropdownPropertyChoice(label = "Delete Message", value = "deleteMessage")
      },
      condition = @TemplateProperty.PropertyCondition(property = "operationGroup", equals = "editing"),
      binding = @TemplateProperty.PropertyBinding(name = "operation"))
  private String operationEditing;

  @TemplateProperty(
      id = "operationStickers",
      label = "Operation",
      group = "operation",
      type = TemplateProperty.PropertyType.Dropdown,
      choices = {
        @TemplateProperty.DropdownPropertyChoice(label = "Send Sticker", value = "sendSticker"),
        @TemplateProperty.DropdownPropertyChoice(label = "Get Sticker Set", value = "getStickerSet"),
        @TemplateProperty.DropdownPropertyChoice(label = "Create New Sticker Set", value = "createNewStickerSet"),
        @TemplateProperty.DropdownPropertyChoice(label = "Answer Inline Query", value = "answerInlineQuery"),
        @TemplateProperty.DropdownPropertyChoice(label = "Answer Web App Query", value = "answerWebAppQuery")
      },
      condition =
          @TemplateProperty.PropertyCondition(property = "operationGroup", equals = "stickers_inline"),
      binding = @TemplateProperty.PropertyBinding(name = "operation"))
  private String operationStickers;

  @TemplateProperty(
      id = "operationSettings",
      label = "Operation",
      group = "operation",
      type = TemplateProperty.PropertyType.Dropdown,
      choices = {
        @TemplateProperty.DropdownPropertyChoice(label = "Set My Commands", value = "setMyCommands"),
        @TemplateProperty.DropdownPropertyChoice(label = "Delete My Commands", value = "deleteMyCommands"),
        @TemplateProperty.DropdownPropertyChoice(label = "Get My Commands", value = "getMyCommands"),
        @TemplateProperty.DropdownPropertyChoice(label = "Set My Name", value = "setMyName"),
        @TemplateProperty.DropdownPropertyChoice(label = "Get My Name", value = "getMyName"),
        @TemplateProperty.DropdownPropertyChoice(label = "Set My Description", value = "setMyDescription"),
        @TemplateProperty.DropdownPropertyChoice(label = "Get My Description", value = "getMyDescription"),
        @TemplateProperty.DropdownPropertyChoice(label = "Set My Short Description", value = "setMyShortDescription"),
        @TemplateProperty.DropdownPropertyChoice(label = "Get My Short Description", value = "getMyShortDescription")
      },
      condition = @TemplateProperty.PropertyCondition(property = "operationGroup", equals = "settings"),
      binding = @TemplateProperty.PropertyBinding(name = "operation"))
  private String operationSettings;

  @TemplateProperty(
      id = "chat_id",
      label = "Chat ID",
      group = "parameters",
      description = "Unique identifier for the target chat or username of the target channel",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(
              property = "operationGroup",
              oneOf = {"messages", "chat", "editing", "stickers_inline", "advanced"}),
      binding = @TemplateProperty.PropertyBinding(name = "_params.chat_id"))
  private String chatId;

  @TemplateProperty(
      id = "text",
      label = "Text",
      group = "parameters",
      type = TemplateProperty.PropertyType.Text,
      description = "Content of the message",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(property = "operationMessages", equals = "sendMessage"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.text"))
  private String text;

  @TemplateProperty(
      id = "caption",
      label = "Caption",
      group = "parameters",
      description = "Media caption",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(
              property = "operationMessages",
              oneOf = {"sendPhoto", "sendAudio", "sendDocument", "sendVideo", "sendAnimation", "sendVoice"}),
      binding = @TemplateProperty.PropertyBinding(name = "_params.caption"))
  private String caption;

  @TemplateProperty(
      id = "parse_mode",
      label = "Parse Mode",
      group = "parameters",
      type = TemplateProperty.PropertyType.Dropdown,
      choices = {
        @TemplateProperty.DropdownPropertyChoice(label = "None", value = ""),
        @TemplateProperty.DropdownPropertyChoice(label = "MarkdownV2", value = "MarkdownV2"),
        @TemplateProperty.DropdownPropertyChoice(label = "HTML", value = "HTML"),
        @TemplateProperty.DropdownPropertyChoice(label = "Markdown", value = "Markdown")
      },
      condition =
          @TemplateProperty.PropertyCondition(
              property = "operationMessages",
              oneOf = {"sendMessage", "sendPhoto", "sendAudio", "sendDocument", "sendVideo", "sendAnimation", "sendVoice"}),
      binding = @TemplateProperty.PropertyBinding(name = "_params.parse_mode"))
  private String parseMode;

  @TemplateProperty(
      id = "photo",
      label = "Photo",
      group = "parameters",
      description = "File ID or URL for the photo",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(property = "operationMessages", equals = "sendPhoto"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.photo"))
  private String photo;

  @TemplateProperty(
      id = "audio",
      label = "Audio",
      group = "parameters",
      description = "File ID or URL for the audio",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(property = "operationMessages", equals = "sendAudio"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.audio"))
  private String audio;

  @TemplateProperty(
      id = "document",
      label = "Document",
      group = "parameters",
      description = "File ID or URL for the document",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(property = "operationMessages", equals = "sendDocument"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.document"))
  private String document;

  @TemplateProperty(
      id = "video",
      label = "Video",
      group = "parameters",
      description = "File ID or URL for the video",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(property = "operationMessages", equals = "sendVideo"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.video"))
  private String video;

  @TemplateProperty(
      id = "animation",
      label = "Animation",
      group = "parameters",
      description = "File ID or URL for the animation",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(property = "operationMessages", equals = "sendAnimation"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.animation"))
  private String animation;

  @TemplateProperty(
      id = "voice",
      label = "Voice",
      group = "parameters",
      description = "File ID or URL for the voice message",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(property = "operationMessages", equals = "sendVoice"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.voice"))
  private String voice;

  @TemplateProperty(
      id = "video_note",
      label = "Video Note",
      group = "parameters",
      description = "File ID or URL for the video note",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(property = "operationMessages", equals = "sendVideoNote"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.video_note"))
  private String videoNote;

  @TemplateProperty(
      id = "sticker",
      label = "Sticker",
      group = "parameters",
      description = "File ID or URL for the sticker",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(property = "operationStickers", equals = "sendSticker"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.sticker"))
  private String sticker;

  @TemplateProperty(
      id = "user_id",
      label = "User ID",
      group = "parameters",
      description = "Unique identifier of the target user",
      feel = Property.FeelMode.optional,
      condition = @TemplateProperty.PropertyCondition(property = "operationGroup", equals = "chat"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.user_id"))
  private String userId;

  @TemplateProperty(
      id = "message_id",
      label = "Message ID",
      group = "parameters",
      description = "Required for editing/deleting/pinning messages",
      feel = Property.FeelMode.optional,
      condition = @TemplateProperty.PropertyCondition(property = "operationGroup", equals = "editing"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.message_id"))
  private String messageId;

  @TemplateProperty(
      id = "question",
      label = "Poll Question",
      group = "parameters",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(property = "operationMessages", equals = "sendPoll"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.question"))
  private String question;

  @TemplateProperty(
      id = "options",
      label = "Poll Options",
      group = "parameters",
      description = "JSON array of answer options ([ \"Option 1\", \"Option 2\" ])",
      feel = Property.FeelMode.optional,
      condition =
          @TemplateProperty.PropertyCondition(property = "operationMessages", equals = "sendPoll"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.options"))
  private String options;

  @TemplateProperty(
      id = "action",
      label = "Chat Action",
      group = "parameters",
      type = TemplateProperty.PropertyType.Dropdown,
      choices = {
        @TemplateProperty.DropdownPropertyChoice(label = "Typing", value = "typing"),
        @TemplateProperty.DropdownPropertyChoice(label = "Upload Photo", value = "upload_photo"),
        @TemplateProperty.DropdownPropertyChoice(label = "Record Video", value = "record_video"),
        @TemplateProperty.DropdownPropertyChoice(label = "Upload Video", value = "upload_video"),
        @TemplateProperty.DropdownPropertyChoice(label = "Record Voice", value = "record_voice"),
        @TemplateProperty.DropdownPropertyChoice(label = "Upload Voice", value = "upload_voice"),
        @TemplateProperty.DropdownPropertyChoice(label = "Upload Document", value = "upload_document"),
        @TemplateProperty.DropdownPropertyChoice(label = "Find Location", value = "find_location"),
        @TemplateProperty.DropdownPropertyChoice(label = "Record Video Note", value = "record_video_note"),
        @TemplateProperty.DropdownPropertyChoice(label = "Upload Video Note", value = "upload_video_note")
      },
      condition =
          @TemplateProperty.PropertyCondition(property = "operationMessages", equals = "sendChatAction"),
      binding = @TemplateProperty.PropertyBinding(name = "_params.action"))
  private String action;

  @TemplateProperty(
      id = "reply_markup",
      label = "Reply Markup (JSON / FEEL)",
      group = "parameters",
      type = TemplateProperty.PropertyType.Text,
      description = "JSON object for keyboards, inline buttons, etc.",
      feel = Property.FeelMode.optional,
      binding = @TemplateProperty.PropertyBinding(name = "_reply_markup"))
  private String replyMarkup;

  @TemplateProperty(
      id = "payload",
      label = "Other Parameters (JSON / FEEL)",
      group = "parameters",
      type = TemplateProperty.PropertyType.Text,
      description =
          "Optional: Enter any other Telegram API parameters as a JSON object (this will override any fields filled above).",
      feel = Property.FeelMode.optional,
      binding = @TemplateProperty.PropertyBinding(name = "_payload"))
  private String payload;

  @TemplateProperty(
      id = "authTypeHidden",
      label = "Authentication Type",
      type = TemplateProperty.PropertyType.Hidden,
      feel = Property.FeelMode.disabled,
      defaultValue = "noAuth",
      binding = @TemplateProperty.PropertyBinding(name = "authentication.type"))
  private String authTypeHidden;

  @TemplateProperty(
      id = "urlHidden",
      label = "Request URL",
      type = TemplateProperty.PropertyType.Hidden,
      feel = Property.FeelMode.disabled,
      defaultValue =
          "= \"https://api.telegram.org/bot\" + (if is defined(botToken) then botToken else \"\") + \"/\" + (if is defined(operation) then operation else \"\")",
      binding = @TemplateProperty.PropertyBinding(name = "url"))
  private String urlHidden;

  @TemplateProperty(
      id = "bodyHidden",
      label = "Request Body",
      type = TemplateProperty.PropertyType.Hidden,
      feel = Property.FeelMode.disabled,
      defaultValue =
          "= context merge([context merge(for entry in get entries(if _params = null then {} else _params) return if entry.value = null or entry.value = \"\" then {} else context put({}, entry.key, entry.value)), if _reply_markup = null then {} else if _reply_markup = \"\" then {} else context put({}, \"reply_markup\", _reply_markup), if _payload = null then {} else if _payload = \"\" then {} else _payload])",
      binding = @TemplateProperty.PropertyBinding(name = "body"))
  private String bodyHidden;

  @TemplateProperty(
      id = "methodHidden",
      label = "Request Method",
      type = TemplateProperty.PropertyType.Hidden,
      feel = Property.FeelMode.disabled,
      defaultValue = "post",
      binding = @TemplateProperty.PropertyBinding(name = "method"))
  private String methodHidden;
}
