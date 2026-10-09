# Molecule Studio
Native Kotlin / Jetpack Compose educational Android app. Open this directory in Android Studio, install Android SDK 35, use JDK 17 or 21, sync Gradle, and run `app` on Android 8.0+.

## Build and test
```
./gradlew assembleDebug testDebugUnitTest lintDebug --max-workers=2
python3 -m unittest discover -s backend -v
```
The Gradle wrapper pins Gradle 8.9; AGP 8.7.3 and Kotlin 2.0.21 are pinned. No provider credentials belong in Android resources, BuildConfig, source, or APKs.

## Chemistry scope
Supported: ClF3, IF5, NO2-, H2O, NH3, CH4, CO2, SF6. Input is case sensitive and accepts common Unicode subscripts and minus signs. Unsupported formulas are rejected. This is a curated model library, not a general molecular structure solver. Electron totals, formal charge totals, terminal octets/duets, and second-row central octets are checked before display. Central expanded-shell Lewis models are used for ClF3, IF5 and SF6. Both nitrite resonance contributors are shown; actual bonds are equivalent. Angles and Canvas projections are approximate educational illustrations, not measured coordinates. Solid wedges point toward the viewer; hashed wedges point away. Color is not the only indicator.

## AI backend
The app works offline for structures and deterministic explanations. Optional AI text calls an HTTPS backend supplied in the UI. Run the development backend with `python3 backend/server.py` (loopback port 8080). `/health` returns readiness; POST `/explain` with `{"formula":"ClF3"}` uses server-owned curated facts and rejects other formulas. Set `AI_API_KEY` securely on the backend host and optionally `AI_MODEL`; no key is sent to Android. Without a key the endpoint returns 503. Generated text is labeled and never controls structures.

For deployment, place this loopback development service behind an HTTPS reverse proxy and authenticated, rate-limited gateway. Add bounded concurrency, user quotas, monitoring, consent/privacy disclosures, and provider timeout handling appropriate to your hosting platform. The stdlib development server is not a public production server. Client identity should use short-lived user sessions or app attestation, never a shared secret embedded in the APK. AI output is explanatory and can still contain textual errors; the app does not claim to chemically prove free-form generated prose.

## Before Google Play
Confirm your permanent application ID (`com.popfan999552.moleculestudio`) before first publication, finalize launcher/store artwork, perform device and accessibility testing, review chemistry diagrams with a chemistry educator, deploy the secured backend, write a privacy policy and complete Data Safety declarations. Configure release signing outside source control, enable Play App Signing, run `./gradlew bundleRelease`, and test the signed bundle. Recheck the current Play target API requirement at submission time. No release credentials or production deployment are included.
