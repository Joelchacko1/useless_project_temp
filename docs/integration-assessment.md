# ScrollJourney Integration Assessment — Agent 4

**Date:** 2026-09-10
**Branch:** `claude-integration` (base: `fd81e2b`)
**Assessor:** Agent 4 (Claude / Integration + QA)

---

## 1. Current Project Structure (base commit `fd81e2b`)

The base project is a stock Android Studio Compose template:

```
app/
  build.gradle.kts          — Compose + Material3, minSdk 24, targetSdk 37
  src/main/
    AndroidManifest.xml      — Single MainActivity, no services
    java/com/example/scrolljourney/
      MainActivity.kt        — Stock "Hello Android" Compose activity
      ui/theme/              — Color.kt, Theme.kt, Type.kt (template defaults)
    res/                     — Standard resources, no xml/accessibility_service_config.xml
  src/test/                  — ExampleUnitTest.kt
  src/androidTest/           — ExampleInstrumentedTest.kt
build.gradle.kts             — Root (AGP 9.4.0, Kotlin 2.2.10)
settings.gradle.kts          — Single :app module
gradle/libs.versions.toml   — Compose BOM 2026.02.01, no Room, no KSP, no lifecycle-viewmodel
```

Key observations:
- **No Kotlin plugin** beyond `kotlin-compose` — `kotlin-android` is not explicitly listed (AGP 9.4 may bundle it).
- **No Room**, no KSP, no DataStore dependencies.
- **No lifecycle-viewmodel-compose** dependency — Agent 2's ViewModel needs this.
- **No accessibility service config XML** exists yet.
- **No `kotlinOptions` block** — the `jvmTarget` may need to be set if compilation targets differ.

---

## 2. Agent 2 (Copilot / UI) — Branch `copilot-ui` @ `508b94b`

### Files created/modified (15 files, +3563 / −47 lines)

| Path | Type |
|------|------|
| `ui/screens/DashboardScreen.kt` | New — primary dashboard |
| `ui/screens/TrackingScreen.kt` | New — tracking ON/OFF + permission UI |
| `ui/screens/StatsScreen.kt` | New — Today/Week/Month stats |
| `ui/screens/AchievementsScreen.kt` | New — achievement grid |
| `ui/screens/CalibrationScreen.kt` | New — calibration + **PrivacyScreen** (bundled) |
| `ui/navigation/Navigation.kt` | New — state-based screen switching |
| `ui/state/ScrollJourneyViewModel.kt` | New — main ViewModel |
| `ui/previews/PreviewFixtures.kt` | New — fixture data for previews |
| `ui/theme/Color.kt` | Modified — gamified palette |
| `ui/theme/Theme.kt` | Modified — updated color scheme |
| `domain/ui/UiState.kt` | New — immutable UI state classes |
| `domain/repository/Repositories.kt` | New — **interface definitions** |
| `data/mock/MockRepositories.kt` | New — mock impls for testing |
| `MainActivity.kt` | **Modified** — wires mock repos into Compose |
| `docs/handoff-agent2.md` | New — handoff documentation |

### Contracts published by Agent 2

Agent 2 defined these interfaces in `domain/repository/Repositories.kt`:

1. **`ScrollStatsRepository`** — `observeToday()`, `observeWeek()`, `observeMonth()`, `observeAppBreakdown(dateKey)`
   - Returns `Flow<AggregatedStats>` and `Flow<List<AppMetric>>`
   - **Uses Agent 2's own `AggregatedStats`** from `domain.ui.UiState` (not Agent 3's)

2. **`TrackingController`** — `isTrackingEnabled()`, `setTrackingEnabled()`, `openAccessibilitySettings()`, `isServiceConnected()`, `isAccessibilityPermissionGranted()`
   - Returns `Flow<Boolean>` (not `StateFlow`)

3. **`GamificationRepository`** — `observeCurrentXpAndLevel()`, `observeCurrentStreak()`, `observeLongestStreak()`, `observeAchievements()`, `observeXpToNextLevel()`

4. **`CalibrationRepository`** — `startCalibration()`, `cancelCalibration()`, `observeCalibrationProgress()`

5. **`AchievementData`** — data class co-located in `Repositories.kt`

### Agent 2's model types (in `domain/ui/UiState.kt`)

- `AggregatedStats` with fields: `totalScrolls`, `totalDistanceMeters`, `activeTrackingMs`, `dateKey`, `topApps: List<AppMetric>`
- `AppMetric` with fields: `packageName`, `appName`, `distanceMeters`, `scrollCount`, `iconUrl`

### Agent 2 boundary violations (minor, expected)

- **Modified `MainActivity.kt`** — spec says this is Agent 4 owned, but Agent 2 needed to wire mock repos. Handoff explicitly marks it as "Agent 4 must wire repositories."
- Defined `AggregatedStats` in `domain.ui` — duplicates the master spec contract name.

---

## 3. Agent 3 (Antigravity / Data) — Branch `antigravity-data` @ `ea683d4`

### Files created (10 files, +455 lines)

| Path | Type |
|------|------|
| `domain/data/SharedModels.kt` | New — master-spec domain models |
| `domain/data/ScrollEventSink.kt` | New — sink interface |
| `domain/data/ScrollStatsRepository.kt` | New — stats repository interface |
| `data/repository/InMemoryScrollRepository.kt` | New — in-memory impl |
| `data/repository/DateUtils.kt` | New — date key helper |
| `gamification/GamificationEngine.kt` | New — XP/level/achievement/streak logic |
| `gamification/GamificationModels.kt` | New — GamificationState, Achievement |
| `test/.../InMemoryScrollRepositoryTest.kt` | New — unit tests |
| `test/.../GamificationEngineTest.kt` | New — unit tests |
| `docs/handoff-agent3.md` | New — handoff documentation |

### Contracts published by Agent 3

1. **`ScrollEventSink`** (`domain.data`) — `suspend fun recordScroll(event: ProcessedScroll)`
2. **`ScrollStatsRepository`** (`domain.data`) — same 4 methods as master spec
   - Returns `Flow<AggregatedStats>` and `Flow<List<AppDistanceStat>>`
   - **Uses Agent 3's own `AggregatedStats`** from `domain.data.SharedModels`
3. **Domain models** (`domain.data.SharedModels`):
   - `ProcessedScroll`, `ScrollDirection`, `EstimationMethod`, `CalibrationProfile`, `AppDistanceStat`, `AggregatedStats`
4. **`GamificationEngine`** (object) — `calculateXp()`, `calculateLevel()`, `evaluateAchievements()`, `updateStreak()`
5. **`GamificationState`**, **`Achievement`** (`gamification/GamificationModels.kt`)

### Agent 3 design decisions
- **In-memory repository** by design — Room dependencies cannot be added by Agent 3 per spec.
- Gamification state is held in-memory in `InMemoryScrollRepository.gamificationState`.
- Handoff provides a complete Room migration blueprint for Agent 4.

---

## 4. Agent 1 (Codex / Tracking) — Branch `codex-tracking`

### Status: NO NEW CODE

The `codex-tracking` branch is identical to `fd81e2b` (initial commit). **Agent 1 has not delivered any tracking/accessibility service implementation.**

### What Agent 1 was expected to provide
Per the master spec, Agent 1 owns:
- `tracking/**` — AccessibilityService implementation
- `domain/tracking/**` — RawScrollEvent model, ScrollEventProcessor
- `domain/distance/**` — Hybrid distance calculator, calibration engine
- Duplicate/noise filtering
- Package/app attribution
- `ProcessedScroll` creation from raw events
- Tracking session start/stop state via an interface

### Impact of Agent 1's absence
This is the most significant gap. Without Agent 1:
- There is no `AccessibilityService` subclass
- There is no scroll event processor or distance calculator
- There is no `TrackingController` implementation (Agent 2's UI calls methods on this interface)
- The app cannot detect scrolls in other apps

**Agent 4 must implement a minimal tracking layer** to produce a buildable, demonstrable app. This includes:
1. An `AccessibilityService` that captures `TYPE_VIEW_SCROLLED` events
2. A basic scroll event processor with duplicate filtering
3. A fallback distance estimator
4. A `TrackingController` implementation (also Agent 4's responsibility per spec)

---

## 5. Files Claude (Agent 4) Owns

Per the master spec and agent prompt:

| File | Status |
|------|--------|
| `MainActivity.kt` | Exists (modified by Agent 2 with mock wiring — must be replaced) |
| `AndroidManifest.xml` | Exists (stock — needs service declaration) |
| `res/xml/accessibility_service_config.xml` | Does not exist — must create |
| `integration/**` | Does not exist — must create |
| `core/appstate/**` | Does not exist — must create |
| `app/build.gradle.kts` | Exists — must add Room/KSP/lifecycle deps |
| `settings.gradle.kts` | Exists — may need KSP plugin |
| `gradle/libs.versions.toml` | Exists — must add Room/KSP versions |
| `app/src/androidTest/**` | Exists (template only) |
| `docs/**` | Does not exist — must create |

---

## 6. Duplicate / Conflicting Contracts Between Agent 2 and Agent 3

### CRITICAL: Duplicate `ScrollStatsRepository` interface

| Aspect | Agent 2 (`domain.repository`) | Agent 3 (`domain.data`) |
|--------|-------------------------------|------------------------|
| Package | `com.example.scrolljourney.domain.repository` | `com.example.scrolljourney.domain.data` |
| `observeAppBreakdown()` return | `Flow<List<AppMetric>>` | `Flow<List<AppDistanceStat>>` |
| `AggregatedStats.topApps` type | `List<AppMetric>` | `List<AppDistanceStat>` |

**`AppMetric`** (Agent 2) has: `packageName, appName, distanceMeters, scrollCount, iconUrl`
**`AppDistanceStat`** (Agent 3) has: `packageName, distanceMeters`

These are incompatible. The ViewModel imports Agent 2's interface. The InMemoryScrollRepository implements Agent 3's interface.

### CRITICAL: Duplicate `AggregatedStats` data class

| Aspect | Agent 2 (`domain.ui`) | Agent 3 (`domain.data`) |
|--------|----------------------|------------------------|
| `topApps` type | `List<AppMetric>` | `List<AppDistanceStat>` |
| Has `appName`, `scrollCount`, `iconUrl` | Yes (in AppMetric) | No |

### Duplicate model names that don't conflict (different packages, used separately)
- `AchievementData` (Agent 2, in `Repositories.kt`) vs `Achievement` (Agent 3, in `GamificationModels.kt`) — different classes, same concept

### Resolution strategy
1. **Keep Agent 3's `domain.data` models as the canonical domain layer** — they match the master spec exactly.
2. **Keep Agent 2's `domain.ui` models as the UI presentation layer** — they carry UI-specific fields (`appName`, `scrollCount`, `iconUrl`).
3. **Keep Agent 2's `domain.repository` interfaces** as the contract the ViewModel consumes.
4. **Agent 4 writes adapter/bridge implementations** that:
   - Implement Agent 2's `ScrollStatsRepository` interface
   - Delegate to Agent 3's `InMemoryScrollRepository` internally
   - Map `AppDistanceStat` → `AppMetric` (with `appName` derived from package name)
5. **Agent 2's mock repos become test-only** and are not used in production wiring.

---

## 7. Required Gradle / Room Dependencies

### `gradle/libs.versions.toml` additions

```toml
[versions]
room = "2.6.1"
ksp = "2.0.0-1.0.24"  # Must match Kotlin 2.2.10 — VERIFY compatibility
lifecycleViewmodel = "2.6.1"
datastorePreferences = "1.0.0"

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodel" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### `app/build.gradle.kts` additions

```kotlin
plugins {
    alias(libs.plugins.ksp)
}
dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // DataStore for gamification state persistence (optional for MVP)
    // implementation(libs.androidx.datastore.preferences)
}
```

### Compatibility concern
- **Kotlin 2.2.10 + KSP**: KSP version must match the Kotlin version. `2.0.0-1.0.24` is for Kotlin 2.0.x. For Kotlin 2.2.10, we need KSP `2.2.10-1.0.x` or compatible. This must be verified at build time. If KSP is incompatible, we may defer Room to in-memory only for the MVP demo.
- **Room 2.6.1** should work with recent AGP/Kotlin, but may need Room 2.7.x for Kotlin 2.2.

### MVP decision: Room may be deferred
Agent 3's `InMemoryScrollRepository` is fully functional for a demo. Room persistence is a nice-to-have for the hackathon. If KSP/Room version conflicts block the build, we ship with in-memory storage and document the limitation. Data survives as long as the process lives.

---

## 8. Required AndroidManifest / Accessibility Service Configuration

### `AndroidManifest.xml` additions

```xml
<!-- AccessibilityService declaration -->
<service
    android:name=".tracking.ScrollTrackingService"
    android:exported="false"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

### `res/xml/accessibility_service_config.xml` (new file)

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeViewScrolled"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100"
    android:canRetrieveWindowContent="false"
    android:description="@string/accessibility_service_description" />
```

Key: `canRetrieveWindowContent="false"` — per the privacy spec, we do NOT read screen content.

### String resource needed
```xml
<string name="accessibility_service_description">
    ScrollJourney measures estimated scroll distance across apps.
    It does not read screen content, messages, or passwords.
</string>
```

---

## 9. MainActivity Integration Requirements

Agent 2's `MainActivity` currently wires mock repositories. The final `MainActivity` must:

1. **Instantiate Agent 3's `InMemoryScrollRepository`** (or Room-backed variant) as both `ScrollEventSink` and data source.
2. **Create a bridge `ScrollStatsRepository`** that adapts Agent 3's domain types to Agent 2's UI types.
3. **Create a bridge `GamificationRepository`** that exposes Agent 3's `GamificationEngine` through Agent 2's interface.
4. **Implement `TrackingController`** — the real one that:
   - Checks `AccessibilityManager` for service enabled status
   - Persists tracking preference
   - Opens Android Settings via `Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)`
   - Exposes `Flow<Boolean>` for enabled/connected/permission states
5. **Create `CalibrationRepository`** implementation (or stub for MVP).
6. **Pass all real implementations to `ScrollJourneyViewModel`**.
7. **Handle lifecycle** — re-check accessibility permission on `onResume`.
8. **Bind to `ScrollTrackingService`** to get event flow and feed into `ScrollEventSink`.

### ViewModel construction concern
Agent 2's `ScrollJourneyViewModel` takes constructor parameters directly (not using `ViewModelProvider.Factory`). Since it's created via `remember { ... }`, this works but doesn't survive configuration changes properly. For the MVP demo this is acceptable — the ViewModel is re-created but the underlying data source persists.

---

## 10. Expected Merge Order

Since all three content branches diverge from the same base (`fd81e2b`) with no overlapping files (except `MainActivity.kt` which Agent 2 modified), the merge order is:

### Recommended sequence:

1. **Merge `antigravity-data` (Agent 3) first**
   - Adds `domain/data/`, `data/repository/`, `gamification/`, tests, docs
   - Zero conflict with base — all new files
   - Establishes the canonical domain models

2. **Merge `copilot-ui` (Agent 2) second**
   - Adds `ui/`, `domain/ui/`, `domain/repository/`, `data/mock/`, docs
   - **One conflict**: `MainActivity.kt` (Agent 2 modified it; base has the template)
   - **Resolution**: Take Agent 2's version (it will be replaced by Agent 4 anyway)
   - Color.kt and Theme.kt are modified but only from the template — no conflict with Agent 3

3. **Skip `codex-tracking` (Agent 1)**
   - No new content to merge — identical to base

4. **Agent 4 implements** on top of the merged result:
   - Tracking service (filling Agent 1's gap)
   - TrackingController
   - Bridge repositories
   - Manifest + accessibility config
   - Gradle dependencies
   - Real MainActivity wiring
   - Integration tests
   - Diagnostics

---

## 11. Potential Compile Conflicts

### After merging Agent 3 + Agent 2:

| Issue | Severity | Resolution |
|-------|----------|------------|
| Two `ScrollStatsRepository` interfaces in different packages | **HIGH** — both compile but ViewModel uses Agent 2's; Agent 3's impl implements Agent 3's | Agent 4 writes bridge adapter |
| Two `AggregatedStats` classes in different packages | **MEDIUM** — both compile independently but are incompatible | Agent 4 maps between them |
| Agent 2's `AppMetric` vs Agent 3's `AppDistanceStat` | **MEDIUM** — incompatible return types | Agent 4 maps `AppDistanceStat` → `AppMetric` |
| `MockScrollStatsRepository` imports `domain.ui.AggregatedStats` | **LOW** — compiles fine, used only for mocks | Leave as-is; mocks are testing-only |
| Agent 2's ViewModel imports `domain.repository.ScrollStatsRepository` | **LOW** — correct, this is Agent 2's own interface | No change needed |
| Missing `lifecycle-viewmodel-compose` dependency | **BUILD BREAK** — `ViewModel` class may not resolve | Agent 4 adds dependency |
| Agent 3 uses `java.time.LocalDate` (API 26+) with `minSdk 24` | **MEDIUM** — crashes on API 24-25 devices | Add `coreLibraryDesugaring` or raise minSdk to 26 (Redmi Note 13 on Android 16 is fine) |
| No `kotlin-android` plugin explicit in `build.gradle.kts` | **LOW** — AGP 9.4 likely handles this | Verify at build time |

### Post Agent 4 implementation:

| Issue | Severity | Resolution |
|-------|----------|------------|
| KSP version vs Kotlin 2.2.10 compatibility | **HIGH** if Room is used | Find compatible KSP version or defer Room |
| `compileSdk { version = release(37) }` — non-standard syntax | **POSSIBLE** — may be AGP 9.4 API | Verify at build time |

---

## 12. Test Strategy for Final Integrated Application

### Unit tests (existing)
- Agent 3: `InMemoryScrollRepositoryTest` — scroll recording, stats aggregation
- Agent 3: `GamificationEngineTest` — XP calculation, level, achievements, streaks
- Both should pass after merge with no changes

### Unit tests (Agent 4 to add)
- `ScrollTrackingService` event processing (using fake `AccessibilityEvent`)
- Distance estimator — deterministic output for known inputs
- `TrackingController` state transitions
- Bridge repository mapping (`AppDistanceStat` → `AppMetric`)
- Duplicate scroll filter logic

### Integration tests (Agent 4 to add)
- Fresh install → tracking OFF
- Toggle request → accessibility settings intent fired
- Service status → UI state mapping via fakes
- `ScrollEventSink.recordScroll()` → `ScrollStatsRepository.observeToday()` updates
- Achievement state updates after processing scroll events
- ViewModel receives correct state from bridge repos

### Manual test plan (Redmi Note 13)
1. Clean install
2. Verify tracking OFF on first launch
3. Tap tracking ON → guided to Accessibility Settings
4. Enable ScrollJourney service → return → tracking ACTIVE
5. Open Chrome → scroll → return → verify distance > 0
6. Open YouTube → scroll → return → verify app breakdown
7. Kill and reopen app → verify data persists (in-memory: data lost; Room: data persists)
8. Turn tracking OFF → verify service stops
9. Test diagnostics screen values
10. Verify no crash on rapid toggle

### What CANNOT be tested in CI
- Real `AccessibilityService` event delivery (requires device with service enabled)
- Real scroll detection in third-party apps
- Battery/background behavior under HyperOS

---

## Summary: Integration Plan

| Step | Action | Risk |
|------|--------|------|
| 1 | Merge `antigravity-data` into `claude-integration` | None |
| 2 | Merge `copilot-ui` into `claude-integration` | Minor conflict on `MainActivity.kt` |
| 3 | Add Gradle dependencies (Room/KSP or defer; lifecycle-viewmodel) | KSP version compat |
| 4 | Create `AccessibilityService` + config + manifest (filling Agent 1 gap) | Core — requires careful impl |
| 5 | Create `TrackingController` implementation | Medium — lifecycle-aware |
| 6 | Create bridge repositories (Agent 3 → Agent 2 interfaces) | Straightforward mapping |
| 7 | Wire real `MainActivity` | Straightforward |
| 8 | Create diagnostics model/screen | Low risk |
| 9 | Add string resources | Trivial |
| 10 | Run unit tests | Should pass |
| 11 | Run build | Verify compilation |
| 12 | Device testing | Requires physical Redmi Note 13 |
| 13 | Write final integration report | Documentation |

**Estimated complexity: MEDIUM-HIGH** — primarily due to Agent 1's absence requiring Agent 4 to build the tracking layer.
