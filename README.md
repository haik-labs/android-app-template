# VisaReady

VisaReady is a Jetpack Compose MVP that guides travelers through a six-step
Schengen visa-preparation questionnaire, calculates a local readiness assessment,
and unlocks the detailed report through a one-time Google Play product.

## Features

- Six editable assessment sections matching the supplied mobile designs
- Review, animated analysis, paywall, and personalized result screens
- Transparent on-device scoring with strengths, risks, and non-guarantee guidance
- One-time Google Play Billing purchase and purchase restoration
- No account, API token, or off-device personal-data transfer required

## Google Play setup

Create a one-time in-app product with ID `visa_readiness_report` in Play Console
and distribute a signed build through an internal test track. The app queries the
product from Google Play, acknowledges completed purchases, and restores previous
purchases when billing reconnects.

## Build

1. Install Android SDK 35 and set `ANDROID_HOME`.
2. Run `./gradlew testDebugUnitTest assembleDebug`.
3. Install `app/build/outputs/apk/debug/app-debug.apk`.

The readiness score measures preparation against common application factors. It
is not legal advice, an official decision, or a guarantee of visa approval.
