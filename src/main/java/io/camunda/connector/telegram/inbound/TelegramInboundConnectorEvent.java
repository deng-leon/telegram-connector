package io.camunda.connector.telegram.inbound;

import java.util.Map;

/**
 * Data provided to the process instance as "event" variable.
 */
public record TelegramInboundConnectorEvent(
    Long updateId,
    String updateType,
    Map<String, Object> update,
    Map<String, Object> message,
    String text,
    Long chatId,
    String senderUsername
) {
    public TelegramInboundConnectorEvent(Map<String, Object> update) {
        this(
            ((Number) update.get("update_id")).longValue(),
            getUpdateType(update),
            update,
            (Map<String, Object>) update.getOrDefault("message", update.getOrDefault("edited_message", null)),
            getText(update),
            getChatId(update),
            getSender(update)
        );
    }

    private static String getUpdateType(Map<String, Object> update) {
        if (update.containsKey("message")) return "message";
        if (update.containsKey("edited_message")) return "edited_message";
        if (update.containsKey("channel_post")) return "channel_post";
        if (update.containsKey("callback_query")) return "callback_query";
        return "other";
    }

    private static String getText(Map<String, Object> update) {
        Map<String, Object> msg = (Map<String, Object>) update.getOrDefault("message", update.getOrDefault("edited_message", null));
        if (msg != null && msg.containsKey("text")) return (String) msg.get("text");
        return null;
    }

    private static Long getChatId(Map<String, Object> update) {
        Map<String, Object> msg = (Map<String, Object>) update.getOrDefault("message", update.getOrDefault("edited_message", null));
        if (msg != null && msg.containsKey("chat")) {
            Map<String, Object> chat = (Map<String, Object>) msg.get("chat");
            return ((Number) chat.get("id")).longValue();
        }
        return null;
    }

    private static String getSender(Map<String, Object> update) {
        Map<String, Object> msg = (Map<String, Object>) update.getOrDefault("message", update.getOrDefault("edited_message", null));
        if (msg != null && msg.containsKey("from")) {
            Map<String, Object> from = (Map<String, Object>) msg.get("from");
            return (String) from.get("username");
        }
        return null;
    }
}
