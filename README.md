# Telegram Inbound Connector

Telegram-focused inbound element templates that run on Camunda SaaS HTTP Webhook connector runtime.

## Trademark and affiliation notice

This is an independent community connector and is not affiliated with, endorsed by, sponsored by, or officially representing Telegram.

Telegram logos may be used for illustrative purposes (for example in documentation, diagrams, and "forward to Telegram" style references), but all materials should clearly indicate this project is not an official Telegram product.

## Features

- Uses SaaS-native webhook connector type (`io.camunda:webhook:1`)
- Generates Telegram-specific inbound element templates from Java annotations
- Avoids custom inbound connector runtime execution code

## Included templates

- Inbound templates are generated in [element-templates](element-templates).
- Outbound template is included in [connectors/telegram-connector.json](connectors/telegram-connector.json).

## Connector configuration

### Required

- **Webhook ID** (`inbound.context`)

### Hardwired internal fields

- **Webhook subtype** (`inbound.subtype`)
	- Hidden fixed template field set to `ConfigurableInboundWebhook`
- **Webhook method** (`inbound.method`)
	- Hidden fixed template field set to `post`

## Webhook registration

This project does not perform webhook registration in Java runtime lifecycle hooks.

- Configure and deploy the generated template in SaaS.
- Use the webhook URL shown by Camunda to register Telegram manually.

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

## Build

```bash
mvn clean package
```

## Test

```bash
mvn clean test
```

## Local run

1. Import generated templates from `element-templates/` into Web Modeler.
2. Apply Telegram inbound template on a Start Event or Intermediate Catch Event.
3. Deploy to SaaS cluster.
4. Copy webhook URL from Modeler and register it in Telegram Bot API.

## Security notes

- Do not commit real credentials to source control.
- Prefer secrets via Camunda secret providers or runtime environment configuration.
- Limit token scope to a dedicated Telegram bot for this integration.
