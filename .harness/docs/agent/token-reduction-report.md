# Token Reduction Report

## Baseline

- `AGENTS.md` word count before bootstrap: 582 words.

## Result

- `AGENTS.md` word count after bootstrap: 582 words.
- Existing root `AGENTS.md` was preserved because it was not harness-managed.
- Detailed repo context now lives under `.harness/docs/agent/`.
- Analyzer mode: static repository scan.
- LLM summary status: skipped (disabled).
- Detected technologies: Java/Gradle.

## Agent Doc Counts

- `.harness/docs/agent/codebase-artifacts.md`: 175 words
- `.harness/docs/agent/commands.md`: 204 words
- `.harness/docs/agent/context.md`: 278 words
- `.harness/docs/agent/design-conformance-report.md`: 116 words
- `.harness/docs/agent/session-state.md`: 103 words
- `.harness/docs/agent/token-reduction-report.md`: 100 words

## Verification Commands

```bash
find . -name AGENTS.md -print | sort | xargs -r wc -w
wc -w .harness/docs/agent/*.md
rg -n -P "\p{Hangul}" AGENTS.md .harness/docs/agent || true
git diff --stat
git status --porcelain=v1 -uno
```
