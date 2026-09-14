# Android App Factory Instructions

These instructions apply permanently to this repository and every file in it.

## Required delivery workflow

After every feature or fix:

1. Build and test the Android application locally when the required SDK and dependencies are available.
2. Commit all intended changes with a clear, descriptive commit message.
3. Push the commit directly to the `main` branch of `haik-labs/android-app-template`.
4. Wait for the GitHub Actions workflow triggered by that push to finish; do not report completion while it is queued or running.
5. Verify in the completed workflow that the APK build step succeeded and that an APK artifact was produced.
6. Verify that the workflow's Slack upload step succeeded and delivered the APK to Slack channel `#apps`.
7. If the workflow or Slack delivery fails, inspect the logs, correct the issue when possible, commit and push the fix to `main`, and repeat the verification steps.

Never claim that a build or delivery succeeded without checking its final status. Never expose GitHub or Slack secrets in source files, logs, commits, or responses.

## Critical: protect the template repository

**CRITICAL:** `haik-labs/android-app-template` is a reusable factory template, not an
application repository. Never implement, commit, or push product-specific features,
branding, package names, assets, or business logic here. Before starting app-specific
work, create a separate repository and verify its `main` branch preserves the current
template contents. Make all subsequent product changes only in that separate repository.
Do not delete this repository, rewrite its history, force-push, or use `git reset` to
clean it. If app-specific changes reach `main`, preserve them in the intended app
repository first, then restore this repository using ordinary revert commits.
