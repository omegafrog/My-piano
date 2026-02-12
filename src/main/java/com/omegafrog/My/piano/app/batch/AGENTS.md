# AGENTS: app/batch/

## OVERVIEW
Spring Batch jobs + scheduler for persisting counters and periodic processing.

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Scheduler | `src/main/java/com/omegafrog/My/piano/app/batch/ViewCountPersistentJobScheduler.java` | `@Scheduled` every 6 hours; disabled for `test` profile |
| Job config | `src/main/java/com/omegafrog/My/piano/app/batch/ViewCountPersistentJobConfig.java` | Defines job/steps |
| Readers/writers | `src/main/java/com/omegafrog/My/piano/app/batch/` | Paging readers + item writers |

## CONVENTIONS
- Scheduler runs under `@Profile("!test")`.
- Batch emits timing/step metrics to logs.

## ANTI-PATTERNS
- Avoid tight polling loops; if you add new jobs, prefer Spring Batch listeners/metrics instead of manual `while(isRunning())` patterns.
