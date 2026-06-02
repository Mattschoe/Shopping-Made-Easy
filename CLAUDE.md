# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Planning & Exploration

When planning a task, use the file map and architecture section in this CLAUDE.md to identify the specific files relevant to the task, then read those files directly to get their current state. Do not do broad codebase exploration (grep sweeps, find commands, reading many files) when the relevant files are already documented here. The file map is a directory — use it to go straight to the 2-3 files that matter instead of exploring 8-10 defensively.

The codebase is bilingual: code identifiers and KDoc are mostly English, but inline comments, log messages, and all user-facing strings are in Danish. Match the surrounding language when editing.

**Always run the `implementation-critic` agent before finishing off a plan** — after implementing a plan, use it to critically review the changes and address anything it surfaces before declaring the work done.

## Build & Development Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build minified/shrunk release APK
./gradlew assembleRelease

# Run all unit tests
./gradlew testDebugUnitTest

# Run a single unit test class
./gradlew testDebugUnitTest --tests "weberstudio.app.billigsteprodukter.ExampleUnitTest"

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

## Architecture

Single-module Android app (`app/`) — Kotlin, Jetpack Compose, Room, manual dependency injection via the `Application` subclass. The app ("Billigste Produkter" / Shopping Made Easy) lets users **photograph Danish supermarket receipts**, OCR them, parse products + prices per store, and track them across a product database, budgets, and shopping lists.

**Package structure** (`app/src/main/java/weberstudio/app/billigsteprodukter/`):

- `data/` — Room database, entities, DAOs, and repositories. Each domain has an interface (e.g. `ReceiptRepository`) + an `Offline*` implementation backed by Room/DataStore.
- `logic/` — Business logic that isn't UI or persistence: receipt parsers, OCR image preprocessing, fuzzy matching, the camera coordinator, activity logging, formatting.
- `ui/` — Compose UI: pages (each with a ViewModel), reusable components, navigation, theme.
- `ReceiptApp.kt` — `Application` subclass; the manual DI container. Lazily constructs the database and all repositories. Exposes itself via `ReceiptApp.instance`.
- `MainActivity.kt` — Single Activity. Locks portrait, enables edge-to-edge, initializes Google Mobile Ads + OpenCV, requests CAMERA permission, hosts the nav graph.

**Key architectural decisions:**
- **Manual DI through `ReceiptApp`** (no Hilt/Dagger). ViewModels are `AndroidViewModel`s that cast `application` to `ReceiptApp` and pull repositories off it. There is no DI container object passed around — the `Application` *is* the container.
- **Repository pattern**: interface in `data/<domain>/`, `Offline*` impl wrapping a DAO. Repositories expose `Flow`s for observation; UI state is built reactively with `combine` / `flatMapLatest` / `stateIn(SharingStarted.WhileSubscribed(5_000))`.
- **Type-safe navigation** via `PageNavigation`, a sealed interface of `@Serializable` route types (`data object` for argument-less pages, `data class` for pages with arguments, e.g. `ReceiptScanning(id: Long)`, `Budget(year: Int, month: Int)`). Navigate by passing a route instance (`navController.navigate(PageNavigation.ReceiptScanning(0))`); the host declares pages with the reified `composable<PageNavigation.X> { }` and reads typed args via `backStackEntry.toRoute<PageNavigation.X>()`. Current-destination checks use `destination.hasRoute<PageNavigation.X>()`. Requires the `kotlin-serialization` Gradle plugin.
- **Room with exported schemas + real migrations** — schema version is currently 11. `exportSchema = true`; the KSP `room.schemaLocation` arg writes a JSON per version to `app/schemas/` (committed; never delete one). **Bumping the schema requires a migration — do not rely on destructive wipes.** A scoped `fallbackToDestructiveMigrationFrom(true, 1..11)` only covers pre-baseline (legacy <11) databases that have no exported schema to migrate from; from 11 onward a missing migration crashes at startup by design. Migrations are validated by `MigrationTest` (`androidTest`) via `MigrationTestHelper`.
- **`Store` enum is the central domain key.** Products, receipts, parsers, and logos are all keyed off it. A `Product`'s logical identity is `ProductID(store, name)` (its `businessID`), distinct from its Room `databaseID`.

## Receipt Scanning Pipeline

This is the core feature and the most complex part of the codebase. Flow (orchestrated by `ReceiptViewModel.processImage`):

1. **Capture** — `CameraCoordinator` (a ViewModel scoped to the Activity) holds the captured image URI as a `PendingCapture`. It is a dumb data holder, not business logic.
2. **First OCR pass** — `ImagePreprocessor.preprocessForMlKit` (grayscale/blur via OpenCV + RenderScript) → ML Kit `TextRecognition` on the full image.
3. **Store detection** — `ParserFactory.detectStore` fuzzy-matches each OCR line against each `Store`'s `topAnchors` (store name variants) using `FuzzyMatcher` (Jaro-Winkler + Levenshtein from Apache commons-text).
4. **Crop + second OCR pass** — `ImagePreprocessor.preprocessAndCropForMlKit` crops between the store's `topAnchors` and `bottomAnchors` (e.g. "TOTAL"/"AT BETALE") for a cleaner read. Falls back to the full image if cropping fails.
5. **Parse** — `ParserFactory.parseReceipt` returns the right `StoreParser`, whose `parse()` produces a `ParsedImageText(store, products, total, scanErrors)`.
6. **Persist + report** — products + receipt saved via `ReceiptRepository`; `ScanValidation` (per-product `ScanError`s) handed back through `CameraCoordinator` for the UI to surface; the scan is logged via `ActivityLogger`.

**Parsers** live in `logic/parsers/`, one per store, all implementing `StoreParser` (and registered in `ParserFactory.parseReceipt`). The hard problem each solves is **spatial**: receipt OCR returns text lines with corner coordinates, and a parser must associate a product *name* line with its *price* line on the same visual row. The geometry helpers on the `StoreParser` interface do this:
- `doesLinesCollide(lineA, lineB, awayOffset)` — raycasts along a line's direction to find the price to its right, tolerant of the photo being rotated up to ~180°.
- `getLineAboveUsingReference` / `getLineBelowUsingReference` — find the nearest line in a direction defined relative to an anchor line (e.g. "Total").
- `isQuantityLine` — detects "2 x 14,00"-style multiplier lines.

Implemented parsers: **Netto, Coop365, Lidl, SuperBrugsen** (the substantial files). **Bilka, Føtex, Menu, Rema1000** currently `throw NotImplementedException` — they are registered but not yet built. Coop365 has two variants (`CoopParserQuantityAbove` / `CoopParserQuantityBelow`) selected by a user setting (`Coop365Option`) because that store prints quantity above or below the price depending on configuration.

## File Map

All paths relative to `app/src/main/java/weberstudio/app/billigsteprodukter/`.

**App entry & DI:**
- `ReceiptApp.kt` — `Application` / manual DI container. All repositories + `ActivityLogger` constructed here, lazily.
- `MainActivity.kt` — single Activity; permissions, OpenCV/Ads init, hosts nav graph.

**Data — persistence (`data/`):**
- `AppDatabase.kt` — Room DB (`app_database`), version 11, `exportSchema = true` with real migrations (scoped destructive fallback only for legacy <11). Lists all entities + DAOs.
- `Entities.kt` — **all Room entities in one file**: `Product`, `Receipt`, `Budget`, `ExtraExpense`, `RecentActivity`, `ShoppingList`, `ShoppingListCrossRef`, plus query-helper relations (`ReceiptWithProducts`, `ShoppingListWithProducts`) and `ProductID`. Also `Product.isEqualTo` (optionally fuzzy) and `ActivityType`.
- `Converters.kt` — Room `TypeConverters` (Store, dates, Month/Year, etc.).
- `AdsID.kt` — AdMob ad unit IDs per page.
- `receipt/` — `ReceiptRepository` (interface, large surface: products, receipts, totals, favorites, search) + `OfflineReceiptRepository` + `ReceiptDao`.
- `budget/` — `BudgetRepository` / `OfflineBudgetRepository` / `BudgetDao`.
- `shoppingList/` — `ShoppingListRepository` / `OfflineShoppingListRepository` / `ShoppingListDao`.
- `recentactivity/` — `ActivityRepository` / `OfflineActivityRepository` / `RecentActivityDao`.
- `settings/` — `SettingsRepository` (DataStore-backed) + `OfflineSettingsRepository`. Defines `Theme`, `Coop365Option`, `TotalOption` enums and onboarding/first-scan flags. **Note:** `OfflineSettingsRepository` also takes a `ReceiptDao` (for `deleteAllProducts`).

**Logic — business logic (`logic/`):**
- `Store.kt` — **central `Store` enum** (ID, logo drawable). `topAnchors` / `bottomAnchors` extension props drive store detection + cropping.
- `parsers/StoreParser.kt` — parser interface + shared geometry helpers + data classes (`ParsedLine`, `ParsedImageText`, `ScanValidation`, `ScanError`).
- `parsers/ParserFactory.kt` — **registration point**: `detectStore()` and `parseReceipt()` map a `Store` → its `StoreParser`.
- `parsers/{Netto,Coop,Lidl,SuperBrugsen}Parser*.kt` — implemented parsers.
- `parsers/{Bilka,Foetex,Menu,Rema}Parser.kt` — stubs that throw `NotImplementedException`.
- `ImagePreProcessor.kt` — OpenCV/RenderScript image cleanup + anchor-based cropping for ML Kit. (Header notes parts are AI-generated.)
- `components/FuzzyMatcher.kt` — Jaro-Winkler + Levenshtein fuzzy string matching. Used for store detection and product de-duplication.
- `components/MatchScoreCalculator.kt` — scoring helper for matching.
- `Formatter.kt` — text normalization (`normalizeText`) + numeric/`isIshEqualTo` helpers.
- `CameraCoordinator.kt` — Activity-scoped ViewModel holding pending capture + pending scan validation between camera and receipt screens.
- `ActivityLogger.kt` / `ActivityViewModel.kt` — records and exposes recent-activity feed (receipts scanned, budgets created, lists created).
- `Logger.kt` — file-based debug logger (`app_debug.log` in filesDir, 5MB cap). Use `Logger.log(tag, msg)`.

**UI — navigation (`ui/navigation/`):**
- `PageNavigation.kt` — **registration point**: sealed interface of `@Serializable` type-safe routes.
- `ApplicationNavigationHost.kt` — `NavHost`; per-route ViewModel creation, `PageShell` wrapping, dialogs/FABs.

**UI — pages (`ui/pages/`)** — each folder has a screen composable + a ViewModel:
- `home/` — `MainPage` (dashboard: recent activity, budget summary). `MainPageViewModel`.
- `receiptScanning/` — `ReceiptContent` + `ReceiptViewModel` (owns the scanning pipeline above) + `ReceiptScanningContent`.
- `database/` — `Database` product-database browser + `DataBaseViewModel`.
- `budget/` — `BudgetPage`, `BudgetTips`, `BudgetViewModel`.
- `shoppingList/` — `ShoppingList` (lists + detail/undermenu) + `ShoppingListViewModels` (multiple VMs in one file).
- `settings/` — `Settings` + `SettingsViewModel`.

**UI — components (`ui/components/`):** `PageShell` (scaffold with top/bottom bars — wrap every page), `Camera`, `ReceiptScanningContent`, `ProductCard`, `Dialogs`, `DropDownMenu`, `SearchBar`, `FloatingActionButtons` (`AddFAB`), `SkeletonComponents`, `AdsUI`, `StoreImage`, `LogoBarHandler`, `ErrorHandling`.

**UI — other:** `ui/ParsingState.kt` — `ParsingState` (NotActivated/InProgress/Success/Error) + `ReceiptUIState` (Empty/Loading/Success). `ui/theme/` — `Color`, `Theme`, `Type`.

**Tests:** `test/.../ExampleUnitTest.kt`, `androidTest/.../ExampleInstrumentedTest.kt` (both stubs — no real test coverage yet).

## Registration Points (Adding New Things)

**Implementing / adding a store parser:**
1. Ensure the `Store` enum entry exists in `Store.kt` with a logo drawable, and define its `topAnchors` + `bottomAnchors`.
2. Create/replace the parser in `logic/parsers/` implementing `StoreParser`; use `Netto`/`SuperBrugsen` parsers as references and lean on the geometry helpers in `StoreParser`.
3. Wire it into both `ParserFactory.detectStore` (fuzzy anchor match) and `ParserFactory.parseReceipt` (Store → parser mapping).

**New navigation page:**
1. Add a `@Serializable` route to `PageNavigation` (`data object` for no args, `data class` with typed fields for arguments).
2. Add a `composable<PageNavigation.X> { }` block in `ApplicationNavigationHost` (read args via `backStackEntry.toRoute<PageNavigation.X>()`), create the ViewModel there, and wrap content in `PageShell`.
3. Create the page composable + its ViewModel under `ui/pages/<name>/`.

**New persisted data:**
1. Add/modify the `@Entity` in `Entities.kt`, register it in `AppDatabase`'s `entities` list, add `TypeConverters` in `Converters.kt` if needed, and **bump the DB version**.
2. **Provide a migration** for the bump — never rely on a destructive wipe. Prefer `@AutoMigration(from = N, to = N+1)` (in the `@Database` `autoMigrations`) for additive changes (new tables/columns with defaults); use an `AutoMigrationSpec` or a manual `Migration(N, N+1)` added via `.addMigrations(...)` for renames, deletes, or type changes. Build once so KSP emits the new `app/schemas/<version>.json`, and **commit it**.
3. Add a `migrateNToN+1` case in `MigrationTest` (`androidTest`) and run `connectedAndroidTest` to validate.
4. Add DAO queries; expose them through the domain's `*Repository` interface + `Offline*` implementation.

**New repository:** add the interface + `Offline*` impl under `data/<domain>/`, then construct it lazily in `ReceiptApp` so ViewModels can reach it.

## Key Conventions

- **ViewModels** are `AndroidViewModel` / `ViewModel`; they obtain dependencies by casting `application` to `ReceiptApp`. Reactive state via `Flow` + `stateIn(SharingStarted.WhileSubscribed(5_000))`.
- **Product identity**: compare with `Product.isEqualTo(...)` (store + price + name, optionally fuzzy) — not `==`. `businessID` (`ProductID`) is the logical key; `databaseID` is the Room PK.
- **Receipt totals** can be displayed as the OCR'd receipt total or the summed product total, controlled by `TotalOption` in settings — handle both.
- **User-facing strings are Danish**, hardcoded in code (not `strings.xml`-driven for the most part). Keep new user-facing text Danish.
- **Logging**: prefer `Logger.log(tag, msg)` (persisted to file) over raw `Log.d` for traceable flow logging.
- **UI verification**: after any Android UI change that affects what the user sees, verify on a connected device/emulator (e.g. via `/android-verify` or the `android` CLI) before reporting the task done.

## SDK Targets & Dependencies

- **minSdk**: 26, **targetSdk/compileSdk**: 36, **versionName** 1.2.2.
- **JVM**: source/target Java 11, Kotlin `jvmTarget = 11`.
- **Compose BOM**: 2025.11.01. Kotlin 2.2.21, AGP 8.13.1, Room 2.8.4 (via KSP).
- Notable libs: ML Kit text-recognition (OCR), OpenCV 4.12 (image preprocessing), CameraX, DataStore Preferences (settings), Google Play Services Ads (AdMob), Lottie (animations), Apache commons-text (fuzzy matching), kotlinx-serialization.
- `release` build type has `isMinifyEnabled` + `isShrinkResources` on (`proguard-rules.pro`).
