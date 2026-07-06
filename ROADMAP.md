# Vedra — Hands‑on Roadmap (interview‑oriented)

You'll build the rest of the app yourself. Every phase below names the
**concepts you should be ready to explain in an Android interview**, the
**hands‑on tasks** to implement them in Vedra, and a **"Done when…"** checklist.

Treat each phase as a self‑contained study session (1–4 hours).

---

## ✅ Already in place (phase 0)
- KMP project with shared business logic in Kotlin; Android UI in Compose, iOS UI in SwiftUI.
- `shared` module with: XML parser, domain models, 15 file parsers,
  `LocalDhmzDataSource`, `WeatherRepository`, `FeelsLike` util.
- Unit tests for parsers + XML reader + feels‑like.

## 🟡 Phase 1 — partially done
- ✅ Theme files in place: `Color.kt` (full M3 light/dark schemes derived from
  `#2196F3 / #607D8B / #DB7900 / #73777E`), `Type.kt` (Manrope for
  display/headline/title, Inter 18pt for body/label), `Theme.kt`
  (`VedraTheme` composable with dynamic-color support on Android 12+).
- ✅ Fonts bundled in `res/font/` (14 files, ~1.3 MB).
- ⏳ Wire `VedraTheme` into `MainActivity.setContent { ... }` and render a
  first "Hello Vedra" `Scaffold + TopAppBar` body.

You can call `WeatherRepository(LocalDhmzDataSource()).sevenDayForecastFor("ZAGREB-GRIČ")`
from any Compose screen today. Everything below is the path from data → app.

---

## ✅ Phase 0.5 — Data-layer hardening (from code review) — DONE

Completed 2026-07-07. What shipped, beyond the tasks below:
- `DhmzDataSource` interface extracted; `WeatherRepository(source)` is
  explicit, **main-safe** (`Dispatchers.Default`) and **cached** (per-file
  `Mutex` + timestamp; `invalidate()` clears it — wire to pull-to-refresh).
- `sevenDayForecastFor(...)` now filters the cached full parse instead of
  re-parsing the XML.
- New parsers + repository functions: `waterTemperatures()`
  (`temp_vode.xml`, KiTS river-water timeseries; stations are numeric codes)
  and `snowDepths()` (`snijeg_n.xml`; summer fixture is title-only, so the
  per-station tags are a best guess — **re-verify with a winter file**).
  `DhmzFile.SEA_WATER_TEMPERATURE` renamed to `WATER_TEMPERATURE`, `SNOW_DEPTH` added.
- Removed: `agro7.xml`, `agro_bilten.xml`, `agro_temp.xml` (no parsers,
  recoverable from git), `Greeting.kt` + `Platform.*` template leftovers
  (iOS `ContentView.swift` updated to a plain stub).
- `WindDirection` is now an enum (8 directions + `CALM`); used by both
  `CurrentObservation` and `EuropeanCityWeather`.
- New tests: water temp + snow fixtures, stray-`&` regression, numeric entities.

**Concepts:**
- Program to interfaces, not implementations; why default constructor args hide dependencies.
- Coroutine dispatchers: `Dispatchers.Default` for CPU-bound work (parsing), main-safety of repository APIs.
- `Mutex` for suspend-friendly locking; simple in-memory caching.

**Tasks:**
1. Extract a `DhmzDataSource` interface (`suspend fun read(file: DhmzFile): String`);
   make `LocalDhmzDataSource` implement it. `WeatherRepository` takes the interface,
   **no default value** — pass it explicitly (DI comes in phase 3). This also
   unblocks `RemoteDhmzDataSource` in phase 8.
2. Make `WeatherRepository` main-safe: wrap read+parse in
   `withContext(Dispatchers.Default)` — `7d_graf_i_simboli.xml` is ~324 cities ×
   7 days of hourly slots; never parse it on the main thread.
3. Add a thin in-memory cache in the repository: per-`DhmzFile` `Mutex` +
   parsed-result + timestamp, so repeated calls don't re-read/re-parse.
4. `XmlParser.readEntity`: bound the entity length (e.g. ≤ 10 chars, otherwise
   treat the `&` as literal text) so a stray raw `&` can't swallow text until
   the next `;` or EOF. Note: `toInt().toChar()` breaks for code points above
   U+FFFF — leave a comment.
5. `MeteoAlertParser`: only collect `<geocode>` entries whose
   `valueName == "EMMA_ID"` into `regionCodes`.
6. Cleanup:
   - Remove `color: String` from `AlertSeverity` — severity→color mapping is a
     UI concern (phase 7 maps it in Compose).
   - Rename `DailyStationMeasurements.Unit` → `Kind` (shadows `kotlin.Unit`).
   - Consider a `WindDirection` enum instead of the `String` typealias.
   - Delete template leftovers `Greeting.kt` / unused `Platform` code.
   - Decide on bundled-but-unparsed files (`temp_vode.xml`, `snijeg_n.xml`,
     `agro*.xml`): add parsers or drop them from resources.
   - Document that `HourlyForecast.time` is implicit **Europe/Zagreb** local
     time — matters for "now" markers and DST handling later.

**Done when:** `./gradlew check` still passes; no UI code change needed; repository is main-safe and cached; `WeatherRepository(LocalDhmzDataSource())` is constructed explicitly.

**Interview talking points:** *"What makes a repository API main-safe?"*, *"`Dispatchers.Default` vs `Dispatchers.IO` — which for parsing and why?"*, *"Why is a default constructor argument a DI smell?"*

---

## Phase 1 — Bootstrap a Compose UI screen

**Concepts to master:**
- `setContent { ... }` vs Activity vs ComponentActivity.
- Compose’s mental model: **declarative + recomposition**.
- `remember { }`, `rememberSaveable { }`, configuration changes.
- Material 3 theming: `MaterialTheme`, color schemes, typography, dynamic color.
- Edge‑to‑edge / `WindowCompat.setDecorFitsSystemWindows`.

**Tasks:**
1. ✅ Theme files in `composeApp/src/androidMain/kotlin/hr/doda/vedra/ui/theme/`:
   `Color.kt` (full M3 light/dark schemes derived from the 4 brand seeds),
   `Type.kt` (Manrope for display/headline/title, Inter 18pt for body/label),
   `Theme.kt` (`VedraTheme` composable with dynamic-color support on
   Android 12+).
2. ⏳ In `MainActivity.onCreate`, call `setContent { VedraTheme { /* … */ } }`
   and render a `Scaffold` + `TopAppBar` + body.
3. ⏳ Drop a hard-coded "Hello Vedra" centred `Text` styled with
   `MaterialTheme.typography.displayMedium`.
4. ⏳ Enable edge-to-edge with `enableEdgeToEdge()` and verify both light
   and dark system bars look correct.
5. ⏳ **Go expressive (from code review):** you're on material3
   `1.10.0-alpha05`, so switch `VedraTheme` to `MaterialExpressiveTheme`
   with `motionScheme = MotionScheme.expressive()` — this unlocks the
   expressive components the design uses (`LargeFlexibleTopAppBar`, button
   groups, wavy progress, shape morphing).
6. ⏳ **Default `dynamicColor = false`** so the brand palette (sky/cloud/sun
   seeds) is what users actually see; expose dynamic color as a Settings
   toggle later. Most devices run Android 12+, so `true` means nobody sees
   your brand.

**Done when:** App runs on an Android emulator with consistent theming, including dark mode toggle. (iOS gets its own SwiftUI implementation in phase 17 — for now you can leave the iOS app as the template stub.)

**Interview talking points:** *"Why is recomposition skipped for stable parameters?"*, *"How does Compose differ from XML/View system?"*, *"What is the difference between `remember` and `rememberSaveable`?"*

---

## Phase 2 — ViewModel + StateFlow + Result wrapper

**Concepts:**
- `ViewModel` lifecycle on Android, `viewModelScope` cancellation.
- KMP shared ViewModel via `androidx.lifecycle.ViewModel` (already a dep).
- **Unidirectional Data Flow (UDF)**: state → UI, events → ViewModel.
- `StateFlow` vs `SharedFlow` vs `LiveData`; cold vs hot flows.
- Wrapping async ops in a `Result`/`UiState` sealed hierarchy (`Loading / Success(data) / Error(throwable)`).
- `collectAsStateWithLifecycle()` and why you should never use plain `collectAsState` on Android.

**Tasks:**
1. Create `shared/src/commonMain/kotlin/hr/doda/vedra/core/UiState.kt`:
   ```kotlin
   sealed interface UiState<out T> {
       data object Loading : UiState<Nothing>
       data class Success<T>(val data: T) : UiState<T>
       data class Error(val error: DataError) : UiState<Nothing>
   }
   ```
2. **Design the error model now, not in phase 8 (from code review):**
   parsers currently `error()` on malformed input — fine for bundled
   fixtures, a crash with real network data. Keep parsers throwing, but
   have the repository catch and wrap into a sealed `DataError`:
   ```kotlin
   sealed interface DataError {
       data class Parse(val file: DhmzFile, val cause: Throwable) : DataError
       data class Network(val cause: Throwable) : DataError   // used from phase 8
       data object NotFound : DataError                        // e.g. unknown city
   }
   ```
   Doing this now means ViewModels won't need reworking when networking
   lands in phase 8.
3. Create `composeApp/.../ui/home/HomeViewModel.kt` extending `ViewModel`, exposing `val state: StateFlow<HomeUiState>`. `HomeUiState` should hold:
   - currently selected city,
   - `currentObservation: UiState<CurrentObservation>`,
   - `today: UiState<NationalDailyForecast>`,
   - `forecast: UiState<CityForecast>`,
   - `activeAlerts: List<MeteoAlertInfo>`.
4. Load data in `init { viewModelScope.launch { ... } }`, using `MutableStateFlow.update { copy(...) }`.
5. In `App.kt`, get the ViewModel with `viewModel { HomeViewModel(WeatherRepository(LocalDhmzDataSource())) }` and collect with `collectAsStateWithLifecycle()`.

**Done when:** Home screen shows real "current temperature in Zagreb" loaded from XML.

**Interview talking points:** *"Walk me through what happens to the ViewModel during a configuration change vs. process death."*, *"Why use `update {}` instead of `value = value.copy(...)`?"*, *"What's the difference between `stateIn` and `shareIn`?"*

---

## Phase 3 — Dependency Injection

**Concepts:**
- DI principles: inversion of control, single source of truth.
- **Manual DI** vs **Hilt** (Android only) vs **Koin** (KMP).
- Scopes: app / activity / viewmodel.
- Why constructor injection > property injection > service locator.

**Tasks:**
- Pick **Koin** (works in KMP). Add `io.insert-koin:koin-core` to `commonMain` and `koin-androidx-compose` to Android.
- Create `shared/.../di/SharedModule.kt` with `single<DhmzDataSource> { LocalDhmzDataSource() }` and `single { WeatherRepository(get()) }` — binding the interface (phase 0.5) means phase 8 swaps to `RemoteDhmzDataSource` in one line.
- Create `composeApp/.../di/AppModule.kt` with `viewModel { HomeViewModel(get()) }`.
- Initialize Koin in `MainActivity.onCreate` (Android). For iOS, expose an `initKoin()` top-level function in `iosMain` that SwiftUI can call from `@main App.init` later (phase 17).
- Replace any direct `WeatherRepository()` constructions with injected instances.

**Done when:** No `new WeatherRepository()` calls exist in UI code; ViewModels are obtained via `koinViewModel()`.

**Interview talking points:** *"How does Hilt resolve dependencies at compile time vs Koin at runtime — trade‑offs?"*, *"What is a circular dependency, how would you spot it?"*

---

## Phase 4 — Navigation

**Concepts:**
- Navigation Compose: `NavHost`, `composable`, type‑safe args (Kotlin 2.x serializable routes).
- Single‑activity architecture.
- Deep linking, back stack management.
- **Saving state across destinations**: `SavedStateHandle`.

**Tasks:**
1. Add `androidx.navigation:navigation-compose` (or KMP equivalent `org.jetbrains.androidx.navigation:navigation-compose`).
2. Define a sealed `Route` hierarchy:
   - `Home`, `CityPicker`, `CityDetail(cityCode: String)`, `Alerts`, `Marine`, `Indices`, `Europe`, `Settings`.
3. Build a top‑level `NavHost` in `App.kt`.
4. Wire `Home → CityDetail(cityCode)` navigation.
5. Pass `cityCode` through `SavedStateHandle` into `CityDetailViewModel` (study why this matters for process death).

**Done when:** Tapping a city in the picker pushes a detail screen with the right forecast.

**Interview talking points:** *"What happens to ViewModel scoped to a NavBackStackEntry when you pop it?"*, *"How would you survive Don't‑keep‑activities?"*

---

## Phase 5 — City picker + search

**Concepts:**
- `LazyColumn` performance: keys, content type, item recomposition.
- `derivedStateOf` for filter performance.
- Debounce input with `Flow.debounce`.
- Saving favorites with **DataStore Preferences** (or KMP **multiplatform-settings**).

**Tasks:**
1. Add a city repository function: `WeatherRepository.allCities(): List<CityForecast>` (you have this — wraps `sevenDayForecast()`).
2. Build `CityPickerScreen` with a `TextField` and a filtered `LazyColumn`. Keys = city code. Use `derivedStateOf` for the filtered list.
3. Add favorites: a `FavoritesRepository` backed by DataStore that exposes a `Flow<Set<String>>` of city codes.
4. Sort: favorites pinned to top; the rest A‑Z.

**Done when:** You can search "zag", star/unstar Zagreb, kill the app, reopen, and see the favorite preserved.

**Interview talking points:** *"Why does `LazyColumn` need keys?"*, *"Why is `mutableStateListOf` not equivalent to `MutableState<List>`?"*, *"DataStore vs SharedPreferences."*

---

## Phase 6 — Forecast UI: hourly chart + 7-day list

**Concepts:**
- `Canvas` and `drawScope`, `Modifier.drawBehind`, custom layouts.
- Animations: `animate*AsState`, `Crossfade`, `AnimatedVisibility`, `Animatable`.
- **Stable** vs **immutable** data classes and the `@Stable`/`@Immutable` annotations; smart recomposition.
- Reading `LocalDensity` and converting `dp ↔ px`.

**Tasks:**
1. `HourlyTemperatureChart(slots: List<HourlyForecast>)` — draw a sparkline with min/max labels, 1px = 1 hour.
2. `DailyForecastRow(day: DailyForecast)` — icon + tempMin..tempMax + rain probability bar.
3. Map `symbol` (e.g. "2", "15n") to a Compose `Painter` (build a sealed `WeatherIcon` registry; bundle DHMZ symbols as drawables in `composeResources/drawable/dhmz/<n>.png`).
4. Animate temperature changes when switching cities with `animateFloatAsState`.

**Done when:** Hourly chart looks plausible vs. raw numbers, list scrolls smoothly with no recomposition jank (verify with **Layout Inspector**).

**Interview talking points:** *"What causes recomposition?"*, *"What is structural equality vs referential equality in Compose?"*

---

## Phase 7 — Alerts & warnings

**Concepts:**
- Modeling time correctly: `Instant` vs `LocalDateTime` vs `LocalDate`, time zones.
- `Modifier.semantics` for accessibility.
- Bottom sheets / `ModalBottomSheet` with state.

**Tasks:**
1. `AlertsScreen` listing today/tomorrow/day‑after CAP alerts.
2. Color the card by `AlertSeverity` (yellow/orange/red). The severity→color
   mapping lives here in Compose (e.g. `AlertSeverity.containerColor()`), not
   in the domain model — the hex string was removed from `AlertSeverity` in
   phase 0.5.
3. Filter by region — selecting a city shows only alerts whose `regionCodes` contain its EMMA region.
4. Tapping opens a bottom sheet with full HR/EN description + instructions; toggle language.
5. Add an `expect/actual` `currentInstant()` so you can show "starts in 2 h" relative times.

**Done when:** Alerts are clearly readable, severity color is visible to color‑blind users (icon + label, not just color), TalkBack reads "Yellow thunderstorm warning, starts at 8 PM".

**Interview talking points:** *"How do you correctly handle daylight saving in Croatia?"*, *"What is `kotlinx-datetime` solving over `java.time`?"*

---

## Phase 8 — Networking layer (replace local data source)

**Concepts:**
- **Ktor client**: engines per platform (`OkHttp` Android / `Darwin` iOS — both consumed by the shared module).
- Logging, timeout, retry/backoff strategies.
- Error model: `Result<Failure, Success>` or sealed `DataError`.
- Caching: HTTP cache vs **app cache** with timestamps.
- ETag / If‑Modified‑Since (DHMZ XMLs change ~hourly).

**Tasks:**
1. Add Ktor: `ktor-client-core`, `ktor-client-content-negotiation`, `ktor-client-logging`, `ktor-client-okhttp` (Android), `ktor-client-darwin` (iOS).
2. Create `expect class HttpClientFactory` (or use Ktor's expect/actual engines) — return a configured `HttpClient`.
3. Create `RemoteDhmzDataSource(client: HttpClient)` implementing the
   `DhmzDataSource` interface from phase 0.5, fetching from `https://vrijeme.hr/...`.
4. **Honor the declared XML encoding (from code review):** decode remote bytes
   per the `<?xml encoding="..."?>` declaration — `hladnival.xml` and
   `toplinskival_5.xml` declare ISO-8859-1(-2). Blind UTF-8 decoding corrupts
   Croatian diacritics (č ć š ž đ) with real remote bytes. Sniff the declaration
   from the first ~100 bytes, then decode accordingly.
5. Bind `Local` for tests, `Remote` in production (one-line Koin change thanks
   to phase 0.5).
6. Extend the phase 0.5 in-memory cache with `expirationMs` so the parsers run
   at most once per N minutes; map fetch failures into `DataError.Network`
   (model already exists since phase 2).

**Done when:** App online → fetches real DHMZ XMLs; offline → falls back to last cached. Add a "Last updated 12 min ago" footer.

**Interview talking points:** *"How does Ktor differ from Retrofit?"*, *"How do you implement exponential backoff?"*, *"Where does WorkManager fit for background syncs?"*

---

## Phase 9 — Local persistence with Room (KMP) or SQLDelight

**Concepts:**
- Schema migrations.
- DAO patterns; suspend DAOs vs `Flow`‑returning DAOs.
- Indexing & query plans.
- Why Room 2.7+ is now KMP — or use **SQLDelight** if you want full KMP control.

**Tasks:**
- Pick **SQLDelight** (cleaner KMP story).
- Define `Forecast.sq`: tables for `city`, `hourly_forecast`, `daily_forecast`, `alert`.
- Create a `ForecastLocalCache` using SQLDelight that caches results returned from the remote source.
- Repository becomes: *try cache → if stale, fetch remote → upsert into cache → emit*.
- Expose `Flow<CityForecast>` so UI reacts to background refreshes.

**Done when:** Killing process and turning airplane mode on still gives a usable forecast for previously visited cities.

**Interview talking points:** *"How does Room generate code at compile time?"*, *"Difference between Flow.distinctUntilChanged and combine."*

---

## Phase 10 — Background updates with WorkManager

**Concepts:**
- `WorkManager`: `OneTimeWorkRequest`, `PeriodicWorkRequest`, constraints, backoff.
- Why not AlarmManager / JobScheduler directly; Doze mode.
- Foreground service vs expedited work.

**Tasks (Android only):**
1. Create `WeatherSyncWorker(appContext, params, repo)` — refreshes current observations + favorite cities’ 7-day forecasts.
2. Schedule periodic 30-minute work with `NetworkType.CONNECTED` constraint, `BackoffPolicy.EXPONENTIAL`.
3. Show last-sync timestamp on the home screen.
4. Add a manual "Refresh" pull-to-refresh (`PullToRefreshBox`) that triggers an expedited one-time work.

**Done when:** App refreshes itself in the background; screen shows new data on reopening.

**Interview talking points:** *"When does WorkManager guarantee work runs?"*, *"How do you observe a worker’s progress in UI?"*

---

## Phase 11 — Notifications for severe alerts

**Concepts:**
- Notification channels (Android 8+), `POST_NOTIFICATIONS` runtime permission (Android 13+).
- `NotificationCompat.Builder`, importance levels, group keys.
- PendingIntent flags & immutability.

**Tasks:**
1. After each background sync, look up active CAP alerts whose region contains the user's favorite cities.
2. Show one notification per alert; tapping opens `AlertsScreen` with that alert pre-selected (pass via deep link).
3. Avoid duplicates: store seen `<identifier>` in DataStore.

**Done when:** Triggering a fake "red wind" alert (manually editing the bundled XML for a dev build) produces a tappable notification that opens the right screen.

**Interview talking points:** *"Difference between MUTABLE and IMMUTABLE PendingIntents and why it matters."*, *"What changes did Android 13 introduce for notifications?"*

---

## Phase 12 — Maps & location

**Concepts:**
- Runtime permissions: precise vs coarse location, "while in use" vs background.
- `FusedLocationProviderClient` API.
- Embedding a Google Map / OSM in Compose with `AndroidView`.

**Tasks:**
1. On home screen "use my location" button → request permission, fetch lat/lon, find the nearest city in `WeatherRepository.allCities()` (Haversine distance utility — write tests for it!).
2. Add a `MapScreen` that plots active alerts as colored polygons by region, and current observations as pins.

**Done when:** Permission flow handles all states (granted, denied, "Don’t ask again"). Map updates as you pan.

**Interview talking points:** *"How do you handle 'denied forever'?"*, *"What's the difference between manifest permissions and runtime permissions?"*, *"Battery cost of location."*

---

## Phase 13 — Localization & accessibility

**Concepts:**
- Compose Resources string tables (`Res.string.xxx`) + multiple language folders.
- RTL support, `LayoutDirection`.
- `Modifier.semantics`, `contentDescription`, dynamic text scaling, **TalkBack** testing.
- Color contrast (WCAG AA: 4.5:1 minimum).

**Tasks:**
- Add `values/strings.xml` for HR + `values-en/strings.xml` for English.
- Replace every hardcoded string with `stringResource(...)`.
- Audit your screens with TalkBack on Android (Settings → Accessibility).
- Run **Accessibility Scanner**.

**Done when:** A TalkBack user can navigate the home screen and understand current weather + alerts.

**Interview talking points:** *"How are resources resolved per locale?"*, *"What is `LocalConfiguration` for?"*

---

## Phase 14 — Testing strategy

**Concepts:**
- Unit tests (JUnit 4/5), parameterized tests.
- **Turbine** for testing `Flow`s.
- **MockK** for Kotlin mocking.
- `runTest`, virtual time, `TestDispatcher`, `Dispatchers.setMain`.
- Compose UI tests: `createComposeRule()`, semantics matchers, `waitUntil`.
- Screenshot / regression testing (Paparazzi or Roborazzi).
- Test pyramid: unit > integration > UI > E2E.

**Tasks:**
1. Add **Turbine** + **MockK**. Test `HomeViewModel.state` emits `Loading → Success` on init, and `Error` on repository failure (use a fake repo).
2. Add `composeApp/src/androidUnitTest/.../HomeScreenTest.kt` — Compose UI tests for the home screen using semantics tags.
3. Add a Paparazzi screenshot test for `DailyForecastRow` covering low/normal/high temps, day vs night symbols.
4. Wire `./gradlew check` so CI runs everything.

**Done when:** `./gradlew check` runs unit + UI + screenshot tests in <2 min.

**Interview talking points:** *"What is the difference between fakes, stubs, and mocks?"*, *"How do you avoid flakiness in Compose UI tests?"*, *"What is a `MainCoroutineRule`?"*

---

## Phase 15 — Performance & polish

**Concepts:**
- **Baseline Profiles** & startup tracing (Macrobenchmark).
- R8 / Proguard rules.
- App Startup time: cold vs warm vs hot.
- Recomposition counts (Layout Inspector).
- Memory leaks: **LeakCanary**.
- Strict mode, ANRs, `onTrimMemory`.

**Tasks:**
1. Add **LeakCanary** in debug builds.
2. Add a `:macrobenchmark` module; create a `StartupBenchmark` and a `ScrollForecastBenchmark`.
3. Generate a Baseline Profile and bundle it.
4. Enable R8 minification + shrinkResources for release; verify with `./gradlew assembleRelease`.
5. Run Layout Inspector → look for items recomposing unnecessarily and add `key`/`@Stable` accordingly.

**Done when:** Cold start < 800 ms on a Pixel 6, no leaks reported, release APK < 5 MB.

**Interview talking points:** *"How does R8 differ from Proguard?"*, *"What is a baseline profile and how does it speed up startup?"*, *"Common causes of ANRs."*

---

## Phase 16 — Modularization & architecture polish

**Concepts:**
- Multi‑module Gradle: `:core`, `:data`, `:feature:home`, `:feature:alerts`.
- Convention plugins (`buildSrc` or `build-logic`).
- Public API surface; `internal` keyword; `api` vs `implementation`.
- **Clean Architecture** layers and use‑cases.

**Tasks:**
1. Split `composeApp` into per‑feature modules: each feature owns its own ViewModel, screens, navigation graph fragment.
2. Move shared UI (`theme`, `WeatherIcon`, common components) into `:core:ui`.
3. Add a `:domain` module of pure use‑cases: `GetHomeWeatherUseCase`, `ObserveAlertsUseCase`. ViewModels depend on use‑cases, not repositories directly.
4. Build a `build-logic` convention plugin that applies your standard plugins + lint config.

**Done when:** Each feature module compiles independently; cyclic deps lint passes; `:feature:home` can be tested in isolation.

**Interview talking points:** *"Why modularize?"*, *"Where do data classes live in clean architecture?"*, *"What is the dependency rule?"*

---

## Phase 17 — Stretch goals (Android side)

- **Widget** showing current temperature + symbol on the Android home screen (Glance API).
- **Wear OS** companion module showing alerts.
- **CI/CD** with GitHub Actions: lint, test, screenshot, assemble, upload mapping files.
- **Crashlytics** + Firebase Performance.
- **A/B test** different forecast layouts via Remote Config.
- Add **astronomical** computations (sunrise/sunset/golden hour) from lat/lon.

---

## Phase 18 — Ship the iOS app with SwiftUI

This is where the KMP investment pays off: the entire `shared` module
(parsers, repository, domain models, networking, caching, use‑cases) is
already callable from Swift — you only need to write the UI layer in
SwiftUI plus a thin Swift presentation layer.

**Concepts to master:**
- How Kotlin types are exported to Swift: classes, sealed classes,
  enums, suspend functions, `Flow`, generics. Read about
  [Kotlin/Native interop](https://kotlinlang.org/docs/native-objc-interop.html)
  and the **`Shared` framework**.
- **SwiftUI fundamentals**: `View`, `@State`, `@Binding`, `@StateObject`,
  `@ObservedObject`, `@EnvironmentObject`, `@Observable` (iOS 17+).
- **`async`/`await`** in Swift, `Task { }`, structured concurrency,
  `@MainActor`.
- Bridging Kotlin `Flow` → Swift `AsyncSequence` (use **KMP-NativeCoroutines**
  or roll a small wrapper).
- **MVVM in SwiftUI**: an `ObservableObject` per screen that wraps a
  Kotlin use‑case and republishes its state on the main actor.
- iOS lifecycle: `App`, `Scene`, `@main`, `WindowGroup`.
- **Navigation**: `NavigationStack` + `NavigationPath` (iOS 16+),
  type‑safe destinations.
- iOS theming: `ColorScheme`, dynamic type, SF Symbols, `.tint()`,
  light/dark assets.
- Push notifications on iOS: `UNUserNotificationCenter`, APNs, the
  `UIApplicationDelegate` shim from a SwiftUI `App`.
- Localization: `String(localized:)`, `.strings` / `.xcstrings` catalog,
  language fallback rules.
- Accessibility: `.accessibilityLabel`, VoiceOver, Dynamic Type.

**Tasks:**
1. **Verify the framework export.** In `shared/build.gradle.kts`, the
   iOS targets already produce a `Shared.framework`. Run
   `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` and confirm
   the output appears under `shared/build/bin/...`. Open `iosApp` in
   Xcode and make sure the framework is linked (it is in the template).
2. **Choose a Flow bridge.** Add the
   [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines)
   plugin to the shared module, annotate public `suspend`/`Flow`
   APIs with `@NativeCoroutines`, and consume them from Swift via
   `AsyncSequence`.
3. **Skeleton the SwiftUI app.** In `iosApp`, create:
   ```
   iosApp/
   ├── VedraApp.swift            // @main, calls initKoin()
   ├── Theme/
   │   ├── Colors.swift          // mirrors Android color tokens
   │   └── WeatherIcons.swift    // SF Symbol map for DHMZ codes
   ├── Common/
   │   ├── UiState.swift         // sealed-style enum (loading/success/error)
   │   └── FlowExtensions.swift  // Kotlin Flow → AsyncSequence helpers
   ├── Features/
   │   ├── Home/HomeView.swift + HomeViewModel.swift
   │   ├── Forecast/...
   │   ├── Alerts/...
   │   ├── CityPicker/...
   │   ├── Marine/...
   │   └── Indices/...
   └── Navigation/Router.swift
   ```
4. **Reach parity** with the Android app, screen by screen, in this order:
   Home → CityPicker → Forecast detail → Alerts → Marine → Indices →
   Europe → Settings.
5. **Reuse, don't re-implement.** Each SwiftUI ViewModel injects a
   shared use‑case (e.g. `GetHomeWeatherUseCase`) and exposes
   `@Published var state: HomeUiState`. Do not re-parse XML, do not
   re-write feels-like math, do not duplicate networking — all of that
   stays in Kotlin.
6. **iOS-native polish:**
   - Use `WeatherKit` SF Symbols (`sun.max.fill`, `cloud.bolt.rain.fill`,
     etc.) for DHMZ symbol codes — much nicer than rasters.
   - Pull-to-refresh with the built-in `.refreshable { ... }`.
   - Haptics for severe alerts (`UIImpactFeedbackGenerator`).
   - **Live Activities** (Dynamic Island) for active red alerts.
   - **iOS Widget Extension** mirroring the Android Glance widget.
7. **Push notifications.** When the cross-platform sync detects a
   matching CAP alert, schedule a local notification on iOS via
   `UNUserNotificationCenter`. (Real APNs server is out of scope for
   a portfolio app.)

**Done when:**
- The iOS app builds and runs on iOS 17+ simulator and a physical
  device.
- All major screens shipped on Android also exist on iOS, hitting the
  same shared business logic.
- VoiceOver navigates the home screen end‑to‑end.
- The HR & EN locales both work via an `.xcstrings` catalog.
- A red CAP alert triggers a local notification and a Live Activity.

**Interview talking points:**
- *"Why ship native UI on each platform instead of Compose Multiplatform?"*
  (Trade-off: native look & feel + access to platform-only APIs vs. UI
  reuse — argue for native UI in a portfolio context.)
- *"How does Kotlin's `sealed class` map to Swift?"* (As a class with
  nested classes — you have to `switch` on it manually since Swift
  doesn't get exhaustive matching for free.)
- *"How do you safely call a `suspend` Kotlin function from Swift?"*
  (Continuations are auto-bridged to `async`; cancellation requires
  KMP-NativeCoroutines or manual `Task` cancellation propagation.)
- *"What is `@MainActor` and why does the iOS ViewModel need it?"*
- *"Differences between SwiftUI's `@State`, `@StateObject`,
  `@ObservedObject` and `@Environment`."*

---

## How to use this plan

1. Pick **one phase per study session**. Do not skip ahead.
2. Before coding, spend 20 minutes reading the official docs / a blog post on the *Concepts* list — write down what you don’t know.
3. Implement the *Tasks*. Commit per phase. Use feature branches.
4. After each phase, write a 5‑bullet summary in your notes (great for interview prep).
5. Cross off the *Done when* checklist before moving on.

## Suggested rough timeline (1 phase per evening, weekends 2)

| Week  | Phases |
|-------|--------|
| 1     | 1, 2, 3 |
| 2     | 4, 5    |
| 3     | 6, 7    |
| 4     | 8, 9    |
| 5     | 10, 11  |
| 6     | 12, 13  |
| 7     | 14      |
| 8     | 15, 16  |
| 9–10  | 17 (stretch) |
| 11–13 | 18 (SwiftUI iOS app) |

By the end you will have shipped a polished real‑world app that touches
**every major Android subsystem**, plus a fully native SwiftUI iOS
counterpart powered by your shared Kotlin codebase — perfect interview
material for Android, iOS, and KMP-focused roles alike.
