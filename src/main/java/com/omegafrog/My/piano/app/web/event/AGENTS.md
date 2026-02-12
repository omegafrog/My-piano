# AGENTS: app/web/event/

## OVERVIEW
Kafka event payloads, producers, and consumers; includes compensation/saga-style events for Elasticsearch failures.

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Producer | `src/main/java/com/omegafrog/My/piano/app/web/event/EventPublisher.java` | Centralizes topic sends |
| Post consumers | `src/main/java/com/omegafrog/My/piano/app/web/event/PostEventConsumer.java` | Indexing side effects to Elasticsearch |
| Sheet post consumers | `src/main/java/com/omegafrog/My/piano/app/web/event/SheetPostCreatedEventConsumer.java` | Indexing + retries |
| File upload consumers | `src/main/java/com/omegafrog/My/piano/app/web/event/FileUploadEventConsumer.java` | Post-process upload results |
| Compensation | `src/main/java/com/omegafrog/My/piano/app/web/event/CompensationConsumer.java` | Handles failed index events |

## CONVENTIONS
- Topics are hard-coded in `EventPublisher` and `@KafkaListener` annotations.
- Typical topics: `post-created-topic`, `post-updated-topic`, `post-deleted-topic`, `elasticsearch-failed-topic`, `compensation-topic`, `file-upload-completed-topic`, `file-upload-failed-topic`.

## ANTI-PATTERNS
- Avoid introducing new topic names in multiple files; add send helpers to `EventPublisher` and keep consumers consistent.
