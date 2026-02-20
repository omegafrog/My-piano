# AGENTS: app/external/

## OVERVIEW
External service clients/config: Elasticsearch (client + repository support) and Toss Payments integration.

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Elasticsearch client config | `src/main/java/com/omegafrog/My/piano/app/external/elasticsearch/ElasticSearchConfig.java` | Adds ApiKey auth header; enables ES repositories |
| Elasticsearch operations | `src/main/java/com/omegafrog/My/piano/app/external/elasticsearch/ElasticSearchInstance.java` | Search/index helpers; async indexing |
| Toss Payments client | `src/main/java/com/omegafrog/My/piano/app/external/tossPayment/TossPaymentInstance.java` | `confirm`, cancel flows via HTTP |

## CONVENTIONS
- Config values are injected via `@Value` (avoid hard-coding credentials/URLs).
- Elasticsearch keys: properties under `elasticsearch.*`.
- Toss keys: properties under `payment.toss.*`.

## ANTI-PATTERNS
- Do not embed API keys in code; keep secrets in external config (deploy pulls from S3).
