# Fix Compose UI Test JUnit4 Resolution Issue

The build error `Could not find androidx.compose.ui:ui-test-junit4:.` indicates that Gradle is unable to resolve the version for the `ui-test-junit4` dependency. This typically happens when using a Bill of Materials (BOM) alongside explicit version declarations in the Version Catalog (`libs.versions.toml`) that might be conflicting or improperly resolved.

## Proposed Changes

### [gradle](file:///D:/Backend/Collabbit/finder-mobile/gradle)

#### [MODIFY] [libs.versions.toml](file:///D:/Backend/Collabbit/finder-mobile/gradle/libs.versions.toml)

Remove explicit `version.ref` from Compose libraries that should be managed by the Compose BOM. This ensures that the versions are synchronized and resolved correctly via the BOM defined in `app/build.gradle.kts`.

Specifically, I will:
- Remove `version.ref = "compose"` from `androidx-ui`, `androidx-ui-graphics`, `androidx-ui-tooling`, `androidx-ui-tooling-preview`, `androidx-ui-test-manifest`, `androidx-ui-test-junit4`, and `androidx-material-icons-extended`.
- Remove `version.ref = "foundation"` from `androidx-foundation`.
- Remove `version.ref = "runtime"` from `androidx-compose-runtime`.

### [app](file:///D:/Backend/Collabbit/finder-mobile/app)

#### [MODIFY] [build.gradle.kts](file:///D:/Backend/Collabbit/finder-mobile/app/build.gradle.kts)

Clean up hardcoded dependencies to use the Version Catalog where appropriate, ensuring consistency.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify the build succeeds.
- Run `./gradlew :app:connectedDebugAndroidTest` (or just sync the project) to ensure dependencies are resolved.

### Manual Verification
- Perform a Gradle Sync in Android Studio to confirm the error is gone.
