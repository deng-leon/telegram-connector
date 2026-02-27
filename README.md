# Telegram Inbound Connector

Inbound Camunda connector that receives Telegram updates through webhooks and correlates them into process instances.

## Trademark and affiliation notice

This is an independent community connector and is not affiliated with, endorsed by, sponsored by, or officially representing Telegram.

Telegram logos may be used for illustrative purposes (for example in documentation, diagrams, and "forward to Telegram" style references), but all materials should clearly indicate this project is not an official Telegram product.

## Features

- Calls Telegram `setWebhook` during activation when a base URL is configured
- Exposes incoming payload as `connectorData`
- Uses runtime-managed webhook endpoint routing (`/inbound/{context}`)

## Included templates

- Inbound templates are generated in [element-templates](element-templates).
- Outbound template is included in [connectors/telegram-connector.json](connectors/telegram-connector.json).

## Connector configuration

### Required

- **Bot Token** (`botToken`)
- **Webhook ID** (`inbound.context`)

### Optional

- **Webhook Base URL** (`baseUrl`)
	- Enables automatic `setWebhook` registration when set
- **Environment variable** `TELEGRAM_WEBHOOK_BASE_URL`
	- Alternative to `baseUrl` for automatic registration

## Webhook registration

This connector calls Telegram `setWebhook` from `activate()` when a base URL is provided (`baseUrl` or `TELEGRAM_WEBHOOK_BASE_URL`).

- The Camunda runtime exposes the inbound endpoint at `/inbound/{inbound.context}`.
- If no base URL is configured, the connector skips automatic `setWebhook` registration.
- You can still register Telegram webhook manually if you prefer explicit control.

### Register webhook manually via Telegram Bot API

1. Determine your externally reachable runtime URL and connector context.
	 - Webhook URL format: `https://<your-runtime-domain>/inbound/<inbound.context>`
2. Register the webhook:

```bash
curl -X POST "https://api.telegram.org/bot<TELEGRAM_BOT_TOKEN>/setWebhook" \
	-d "url=https://<your-runtime-domain>/inbound/<inbound.context>"
```

3. Verify registration:

```bash
curl "https://api.telegram.org/bot<TELEGRAM_BOT_TOKEN>/getWebhookInfo"
```

4. (Optional) Remove webhook:

```bash
curl -X POST "https://api.telegram.org/bot<TELEGRAM_BOT_TOKEN>/deleteWebhook"
```

### Example automatic registration setup

```bash
export TELEGRAM_WEBHOOK_BASE_URL="https://<your-runtime-domain>"
```

## Build

```bash
mvn clean package
```

## Test

```bash
mvn clean test
```

## Local run

1. Start Camunda 8 runtime with inbound connectors enabled.
2. Set `baseUrl` or `TELEGRAM_WEBHOOK_BASE_URL` if you want automatic webhook registration; otherwise register Telegram webhook manually to `/inbound/{context}`.
3. Configure `botToken` and `inbound.context` in your BPMN element template usage.
4. Deploy your process and activate the connector.

## Security notes

- Do not commit real credentials to source control.
- Prefer secrets via Camunda secret providers or runtime environment configuration.
- Limit token scope to a dedicated Telegram bot for this integration.
