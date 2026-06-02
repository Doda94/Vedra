# Vedra — Hands‑on Roadmap (interview‑oriented)

You'll build the rest of the app yourself. Every phase below names the
**concepts you should be ready to explain in an Android interview**, the
**hands‑on tasks** to implement them in Vedra, and a **"Done when…"** checklist.

Treat each phase as a self‑contained study session (1–4 hours).

---

## ✅ Already in place (phase 0)
- KMP project (Android + iOS), Compose Multiplatform.
- `shared` module with: XML parser, domain models, 15 file parsers,
  `LocalDhmzDataSource`, `WeatherRepository`, `FeelsLike` util.
- Unit tests for parsers + XML reader + feels‑like.

You can call `WeatherRepository().sevenDayForecastFor("ZAGREB-GRIČ")` from
any Compose screen today. Everything below is the path from data → app.

---

## Phase 1 — Bootstrap a Compose UI screen

**Concepts to master:**
- `setContent { ... }` vs Activity vs ComponentActivity.
- Compose’s mental model: **declarative + recomposition**.
- `remember { }`, `rememberSaveable { }`, configuration changes.
- Material 3 theming: `MaterialTheme`, color schemes, typography, dynamic color.
- Edge‑to‑edge / `WindowCompat.setDecorFitsSystemWindows`.

**Tasks:**
1. In `composeApp/src/commonMain/kotlin/hr/doda/vedra/App.kt`, replace the placeholder with a `Scaffold` + `TopAppBar` + body.
2. Create `ui/theme/VedraTheme.kt` with light/dark color schemes (use weather‑inspired colors).
3. Add `composeApp/src/androidMain/kotlin/hr/doda/vedra/ui/theme/Color.android.kt` to provide dynamic color on Android 12+.
4. Render a hard‑coded "Hello Vedra" home screen.

**Done when:** App runs on Android emulator and an iOS simulator with consistent theming, including dark mode toggle.

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
       data class Error(val throwable: Throwable) : UiState<Nothing>
   }
   ```
2. Create `composeApp/.../ui/home/HomeViewModel.kt` extending `ViewModel`, exposing `val state: StateFlow<HomeUiState>`. `HomeUiState` should hold:
   - currently selected city,
   - `currentObservation: UiState<CurrentObservation>`,
   - `today: UiState<NationalDailyForecast>`,
   - `forecast: UiState<CityForecast>`,
   - `activeAlerts: List<MeteoAlertInfo>`.
3. Load data in `init { viewModelScope.launch { ... } }`, using `MutableStateFlow.update { copy(...) }`.
4. In `App.kt`, get the ViewModel with `viewModel { HomeViewModel(WeatherRepository()) }` and collect with `collectAsStateWithLifecycle()`.

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
- Create `shared/.../di/SharedModule.kt` with `single { LocalDhmzDataSource() }` and `single { WeatherRepository(get()) }`.
- Create `composeApp/.../di/AppModule.kt` with `viewModel { HomeViewModel(get()) }`.
- Initialize Koin in `MainActivity.onCreate` (Android) and an `iosMain` `initKoin()` helper.
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
2. Color the card by `AlertSeverity` (yellow/orange/red).
3. Filter by region — selecting a city shows only alerts whose `regionCodes` contain its EMMA region.
4. Tapping opens a bottom sheet with full HR/EN description + instructions; toggle language.
5. Add an `expect/actual` `currentInstant()` so you can show "starts in 2 h" relative times.

**Done when:** Alerts are clearly readable, severity color is visible to color‑blind users (icon + label, not just color), TalkBack reads "Yellow thunderstorm warning, starts at 8 PM".

**Interview talking points:** *"How do you correctly handle daylight saving in Croatia?"*, *"What is `kotlinx-datetime` solving over `java.time`?"*

---

## Phase 8 — Networking layer (replace local data source)

**Concepts:**
- **Ktor client**: engines per platform (`OkHttp` Android / `Darwin` iOS).
- Logging, timeout, retry/backoff strategies.
- Error model: `Result<Failure, Success>` or sealed `DataError`.
- Caching: HTTP cache vs **app cache** with timestamps.
- ETag / If‑Modified‑Since (DHMZ XMLs change ~hourly).

**Tasks:**
1. Add Ktor: `ktor-client-core`, `ktor-client-content-negotiation`, `ktor-client-logging`, `ktor-client-okhttp` (Android), `ktor-client-darwin` (iOS).
2. Create `expect class HttpClientFactory` (or use Ktor's expect/actual engines) — return a configured `HttpClient`.
3. Create `RemoteDhmzDataSource(client: HttpClient)` mirroring `LocalDhmzDataSource.read(file)` but fetching from `https://vrijeme.hr/...`.
4. Make `WeatherRepository` accept a `DhmzDataSource` interface; bind `Local` for tests, `Remote` in production.
5. Add a thin caching layer: per‑file `Mutex` + `expirationMs` so the parsers run at most once per N minutes.

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

## Phase 17 — Stretch goals

- **Widget** showing current temperature + symbol on the Android home screen (Glance API).
- **Wear OS** companion module showing alerts.
- **CI/CD** with GitHub Actions: lint, test, screenshot, assemble, upload mapping files.
- **Crashlytics** + Firebase Performance.
- **A/B test** different forecast layouts via Remote Config.
- Add **astronomical** computations (sunrise/sunset/golden hour) from lat/lon.
- **Compose Multiplatform iOS** — actually ship the iOS build.

---

## How to use this plan

1. Pick **one phase per study session**. Do not skip ahead.
2. Before coding, spend 20 minutes reading the official docs / a blog post on the *Concepts* list — write down what you don’t know.
3. Implement the *Tasks*. Commit per phase. Use feature branches.
4. After each phase, write a 5‑bullet summary in your notes (great for interview prep).
5. Cross off the *Done when* checklist before moving on.

## Suggested rough timeline (1 phase per evening, weekends 2)

| Week | Phases |
|------|--------|
| 1    | 1, 2, 3 |
| 2    | 4, 5    |
| 3    | 6, 7    |
| 4    | 8, 9    |
| 5    | 10, 11  |
| 6    | 12, 13  |
| 7    | 14      |
| 8    | 15, 16  |

By the end you will have shipped a polished real‑world app that touches
**every major Android subsystem** — perfect interview material.
