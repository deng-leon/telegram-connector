# SubmitConnector-V2 Prefill (Telegram Connector)

Use this as a separate copy/paste source for:
https://jfk-1.tasklist.camunda.io/e0d3996c-b748-424b-9283-142ce53df946/tasklist/new/SubmitConnector-V2

## Your Information

- Company / Contributor Name: `<YOUR_NAME_OR_COMPANY>`
- Email Contact: `<YOUR_CONTACT_EMAIL>`
- Link to Your Website: `<YOUR_WEBSITE_URL>`

## Connector Details

- Marketplace Profile Title: `Telegram Connector`
- Splash Title: `Automate Telegram Messaging And Webhooks`
- Five-Word Description: `Send and receive Telegram events`
- Description:
  `Telegram Connector for Camunda supporting outbound bot operations (messaging, chat actions, media, and admin APIs) and inbound webhook-driven event triggers. Includes generated templates for start, intermediate catch, and boundary inbound events with correlation/output mapping options. This connector is developed independently and is not affiliated with or officially representing Telegram.`
- SEO Description:
  `Camunda Telegram Connector enables end-to-end bot automation with outbound API actions and inbound webhook events. Independent community connector, not affiliated with or officially representing Telegram.`
- Connector Type: `Inbound + Outbound`
- Application Category: `<SELECT_IN_FORM>`
- Industry: `<SELECT_IN_FORM>`
- Use Case: `<SELECT_IN_FORM>`

## Listing Media

- Link to App Listing Logo URL (>=512x512, no SVG): `https://raw.githubusercontent.com/deng-leon/telegram-connector/main/Logo.png`
- Link to App Profile Logo URL (>=230x230, no SVG): `https://raw.githubusercontent.com/deng-leon/telegram-connector/main/Logo.png`
- Link to Screenshots URL(s): `https://raw.githubusercontent.com/deng-leon/telegram-connector/main/Screenshots.png`
- Link to Promo Video: `<PUBLIC_VIDEO_URL>`
- Logo usage note for listing text/review comments:
  `Telegram logos are used for illustration only. This connector is not an official Telegram product and does not represent Telegram.`

## Your Listing / Feature Section

- Listing Options: `<SELECT_IN_FORM>`
- Feature Description Feature: `Comprehensive Telegram automation across outbound and inbound flows.`

- Feature 1 Title: `Outbound Bot Actions`
- Feature 1 Description: `Invoke Telegram Bot API operations including sendMessage, sendChatAction, media and chat management endpoints.`

- Feature 2 Title: `Inbound Webhook Triggering`
- Feature 2 Description: `Receive Telegram updates via webhook and correlate into BPMN start/intermediate/boundary events.`

- Feature 3 Title: `Camunda Template Coverage`
- Feature 3 Description: `Generated templates for message start, intermediate catch, boundary, and start event variants.`

- Feature 4 Title: `Correlation And Output Mapping`
- Feature 4 Description: `Supports activation conditions, correlation key expressions, message TTL, and result expression mapping.`

- Feature 5 Title: `Runtime Health Lifecycle`
- Feature 5 Description: `Reports connector health and handles webhook registration/de-registration on activate/deactivate.`

- Feature 6 Title: `Flexible Deployment`
- Feature 6 Description: `Works with explicit base URL, TELEGRAM_WEBHOOK_BASE_URL, or Camunda SaaS-derived webhook endpoint.`

## Technical Information

- Connector Runtime: `Yes - reusing Camunda runtime (protocol connector style webhook runtime)`
- Connector SDK Version: `8.8.7`
- Compatible Version: `Camunda 8.8+`

## GitHub Information

- GitHub Repository URL: `<PUBLIC_GITHUB_REPOSITORY_URL>`

- Outbound Template URL (RAW):
  `<RAW_BASE_URL>/connectors/telegram-connector.json`

- Start Inbound Template URL (RAW):
  `<RAW_BASE_URL>/element-templates/telegram-inbound-connector-message-start-event.json`

- Intermediate Inbound Template URL (RAW):
  `<RAW_BASE_URL>/element-templates/telegram-inbound-connector-intermediate-catch-event.json`

- Boundary Inbound Template URL (RAW):
  `<RAW_BASE_URL>/element-templates/telegram-inbound-connector-boundary-event.json`

## Support Links

- Documentation Link URL:
  `<PUBLIC_GITHUB_REPOSITORY_URL>/blob/main/README.md`
- Support Link URL:
  `https://forum.camunda.io/`
- Video Link URL:
  `<PUBLIC_VIDEO_URL>`
- Additional Resources URL (PDF):
  `<PUBLIC_PDF_URL>`

## Known values from current codebase

- Inbound connector type: `io.camunda:webhook:1`
- Outbound connector template id: `io.camunda.connectors.Telegram.v1`
- Inbound template base id: `io.camunda.connector.TelegramInbound.v2` (template version `3`)
- Inbound module artifact: `io.camunda.connector:telegram-inbound-connector:1.0.0-SNAPSHOT`
