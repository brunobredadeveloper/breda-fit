# Breda Fit engineering

- Kotlin Multiplatform core belongs in shared/commonMain. Keep Java/Android, Context, platform clocks and UI imports out of it.
- Android UI uses Compose + lifecycle-aware StateFlow observation. No disk operations in composables. Inject FitRepository through the factory/composition root.
- Maintain immutable state transforms and transactional persistence. Read failures must preserve data; never implement a silent reset/migration fallback.
- Session time uses monotonic elapsed milliseconds and excludes paused/preparation/rest time. Background must pause; never mark an interrupted workout as completed.
- Do not fabricate calories, body fat, muscle mass, sensor readings, medical diagnoses or promised weight-loss dates. Update health documentation alongside indicator changes.
- Use original/licensed exercise assets. The schematic figures still require professional movement-content review before commercial publication.
- Check official Kotlin/AGP/Compose compatibility before dependency upgrades; use stable compatible versions and the committed Gradle wrapper.
- Verify with `./gradlew :shared:jvmTest :androidApp:lintDebug :androidApp:assembleDebug`. Instrumented tests additionally require an Android emulator/device.
- Android is the implemented app. Declared iOS core targets are not an implemented iOS UI or proof of an iOS build.
- Never commit personal measurements, production signing keys, account credentials or local.properties.


Signing update: GitHub builds generate an ephemeral debug key. No keystore is committed. Different build runs may require uninstalling the previous evaluation APK before installing; export test measurements first. Production signing remains separate.
