# Telegram Inbound Connector

Inbound Camunda connector that receives Telegram updates through webhooks and correlates them into process instances.

## Features

- Registers Telegram webhook on activation (`setWebhook`)
- Removes Telegram webhook on deactivation (`deleteWebhook`)
- Exposes incoming payload as `connectorData`
- Supports Camunda SaaS auto URL detection and explicit base URL override

## Connector configuration

### Required

- **Bot Token** (`botToken`)
- **Webhook ID** (`inbound.context`)

### Optional

- **Webhook Base URL** (`baseUrl`)
  - Required for self-managed or local testing
  - Optional for Camunda SaaS if region/cluster env vars are available

## Webhook URL resolution order

The connector resolves the public webhook URL in this order:

1. `baseUrl` from connector properties
2. `TELEGRAM_WEBHOOK_BASE_URL` environment variable
3. Camunda SaaS derived URL using `CAMUNDA_CLIENT_CLOUD_REGION` + `CAMUNDA_CLIENT_CLOUD_CLUSTERID`

Final path format:

`{base}/inbound/{inbound.context}`

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
2. Ensure the webhook URL is publicly reachable (for example, via a reverse proxy/tunnel).
3. Configure `botToken` and `inbound.context` in your BPMN element template usage.
4. Deploy your process and activate the connector.

## Security notes

- Do not commit real credentials to source control.
- Prefer secrets via Camunda secret providers or runtime environment configuration.
- Limit token scope to a dedicated Telegram bot for this integration.
