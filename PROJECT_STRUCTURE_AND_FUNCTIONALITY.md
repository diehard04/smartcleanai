# SmartClean AI — Project Structure & Functional Architecture Documentation

SmartClean AI is an enterprise-grade Android utility application engineered in **Kotlin** and **Jetpack Compose (Material 3)**. It liberates gigabytes of device storage through **100% On-Device Vision AI** and deterministic perceptual hashing—scanning photos, burst shots, blurry captures, screenshots, and WhatsApp forwards locally without uploading any data to cloud servers.

---

## 1. High-Level Architectural Pattern

SmartClean AI follows **Clean Architecture** combined with **MVVM (Model-View-ViewModel)** and **Unidirectional Data Flow (UDF)**:

```
┌────────────────────────────────────────────────────────┐
│                      UI LAYER                          │
│   Jetpack Compose (Material 3) + StateFlow / Coroutines│
│   Screens: Onboarding, Dashboard, Scanning, Review,    │
│            Compare Modal, Summary, Settings, Ignored   │
└───────────────────────────▲────────────────────────────┘
                            │ UI State / User Events
┌───────────────────────────┴────────────────────────────┐
│                  PRESENTATION LAYER                    │
│   MainViewModel (StateFlow<MainUiState>)               │
└───────────────────────────▲────────────────────────────┘
                            │ Domain Models / Use-Cases
┌───────────────────────────┴────────────────────────────┐
│                    DOMAIN LAYER                        │
│   DuplicateAnalyzer (pHash 64-bit DCT, dHash, Hamming) │
│   BlurDetector (Laplacian Variance, Dark Pocket Shots) │
│   WhatsAppJunkClassifier (ML Kit Text OCR + Faces)    │
│   MediaFilterAnalyzer (Old Screenshots, Large Videos)  │
└───────────────────────────▲────────────────────────────┘
                            │ Data Contracts
┌───────────────────────────┴────────────────────────────┐
│                     DATA LAYER                         │
│   MediaRepositoryImpl (Incremental Caching & Pipeline) │
│   MediaStoreDataSource (Scoped Storage & Thumbnails)   │
│   AppDatabase & ScannedMediaDao (Room DB Caching)      │
└────────────────────────────────────────────────────────┘
```

---

## 2. Comprehensive Directory & File Structure

```
d:\AntiGravity\SmartClean\
│
├── build.gradle.kts                         # Root Gradle build configuration
├── settings.gradle.kts                      # Gradle repository and plugin management
├── gradle.properties                        # JVM memory options and AndroidX toggles
├── gradlew / gradlew.bat                    # Gradle Wrapper executables
├── gradle/wrapper/                          # Gradle wrapper binaries & properties
│
└── app/
    ├── build.gradle.kts                     # App module dependencies (Compose, Room, ML Kit, Coil)
    ├── proguard-rules.pro                   # ProGuard rules for release builds
    │
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml          # Permissions (READ_MEDIA_*, POST_NOTIFICATIONS) & Activity
        │   │
        │   ├── res/                         # Android Resources
        │   │   ├── drawable/                # Launchers and vector icons
        │   │   ├── values/                  # Strings, colors, and Material 3 theme XML
        │   │   └── xml/                     # Backup rules & data extraction configuration
        │   │
        │   └── java/com/example/smartcleanai/
        │       │
        │       ├── SmartCleanApplication.kt # Application class; initializes Coil cache & WorkManager
        │       ├── MainActivity.kt          # Single Activity; NavHost; Scoped Storage Trash Launcher
        │       │
        │       ├── data/                    # DATA LAYER
        │       │   ├── local/
        │       │   │   ├── AppDatabase.kt          # Room Database ("smart_clean_db")
        │       │   │   ├── ScannedMediaDao.kt      # Incremental caching, whitelist & delete queries
        │       │   │   └── ScannedMediaEntity.kt   # Database entity mapping media analysis results
        │       │   │
        │       │   ├── media/
        │       │   │   └── MediaStoreDataSource.kt # MediaStore content resolver queries & bitmap loader
        │       │   │
        │       │   └── repository/
        │       │       ├── MediaRepository.kt      # Abstract repository contract
        │       │       └── MediaRepositoryImpl.kt  # Concrete repository with multi-core parallel pipeline
        │       │
        │       ├── domain/                  # DOMAIN LAYER (Pure Business & Vision Logic)
        │       │   ├── analyzer/
        │       │   │   ├── DuplicateAnalyzer.kt    # 64-bit pHash (2D DCT), dHash, Hamming distance <= 5
        │       │   │   ├── BlurDetector.kt         # Laplacian Variance blur & average pixel luminance
        │       │   │   ├── WhatsAppJunkClassifier.kt# WhatsApp media path filter + ML Kit Text OCR & Faces
        │       │   │   └── MediaFilterAnalyzer.kt  # Old screenshots (>30 days) & Large videos (>50 MB)
        │       │   │
        │       │   └── model/
        │       │       ├── MediaCategory.kt        # Category enum (DUPLICATE, BLURRY, WHATSAPP, etc.)
        │       │       ├── MediaItemModel.kt       # Domain model with formatted size & resolution
        │       │       ├── ScanCategoryResult.kt   # Scan metrics container and duplicate cluster groups
        │       │       └── ScanProgress.kt         # Live progress state for the animated scanning UI
        │       │
        │       ├── ui/                      # PRESENTATION & UI LAYER (Jetpack Compose)
        │       │   ├── theme/
        │       │   │   ├── Color.kt                # High-contrast Dark theme palette
        │       │   │   ├── Theme.kt                # MaterialTheme setup & status bar coloring
        │       │   │   └── Type.kt                 # Typography definitions
        │       │   │
        │       │   ├── navigation/
        │       │   │   └── Screen.kt               # Sealed class defining navigation routes
        │       │   │
        │       │   ├── viewmodel/
        │       │   │   ├── MainUiState.kt          # Immutable state container for UI
        │       │   │   └── MainViewModel.kt        # MVVM StateFlow, selection logic, deletion intent
        │       │   │
        │       │   ├── onboarding/
        │       │   │   └── OnboardingScreen.kt     # Screen 1: Splash & Granular Permissions
        │       │   │
        │       │   ├── dashboard/
        │       │   │   └── DashboardScreen.kt      # Screen 2: Storage Gauge, 2x2 Categories, Lifetime saved
        │       │   │
        │       │   ├── scanning/
        │       │   │   └── ScanningScreen.kt       # Screen 3: Live Radar graphic, counters, stop scan
        │       │   │
        │       │   ├── review/
        │       │   │   ├── ReviewScreen.kt         # Screen 4: Grouped duplicates view, grid, sticky action bar
        │       │   │   └── PhotoCompareDialog.kt   # Full-screen pinch-to-zoom modal with metadata chips
        │       │   │
        │       │   ├── summary/
        │       │   │   └── SummaryScreen.kt        # Screen 5: Success animation, breakdown & 30-day trash notice
        │       │   │
        │       │   └── settings/
        │       │       ├── SettingsScreen.kt       # Screen 6: Sensitivity slider, auto-scan, Pro upgrade
        │       │       └── IgnoredMediaScreen.kt   # Whitelist/Protected photos manager
        │       │
        │       └── worker/                  # BACKGROUND SCHEDULING LAYER
        │           └── GalleryScanWorker.kt        # Weekly WorkManager task; alerts when junk > 500 MB
        │
        └── test/java/com/example/smartcleanai/domain/analyzer/
            └── AnalyzerTest.kt                     # Unit tests for hashing, clustering, blur & filters
```

---

## 3. Core Functional Specifications

### A. On-Device Scan Engine (`domain/analyzer/`)

#### 1. Duplicate & Visually Similar Photo Detection (`DuplicateAnalyzer.kt`)
* **Exact Duplicate Matching**: Groups identical files by verifying exact byte sizes followed by deterministic SHA-256 checksums.
* **Perceptual Similarity (pHash)**:
  * Computes a 64-bit Perceptual Hash using a **Fast Separable 2D Discrete Cosine Transform (DCT)** on 32×32 grayscale bitmaps.
  * Trigonometric cosine values are precomputed into a static lookup table (`COS_TABLE`), reducing computation from $1,048,576$ inner loop iterations to $\sim 10,240$ matrix operations with zero runtime `cos()` calls.
  * Two images are clustered as visually similar burst photos if their **Hamming Distance $\le 5$**.
* **Best Shot Selection**: Automatically designates the sharpest (highest Laplacian variance) and highest-resolution image as the **"Best Shot"** (unselected by default); duplicate candidates are pre-selected for cleanup.

#### 2. Blurry & Dark Pocket Photo Detector (`BlurDetector.kt`)
* **Motion & Focus Blur**: Convolves a $3 \times 3$ Laplacian kernel over 128×128 grayscale downscaled bitmaps:
  $$\nabla^2 f = \begin{bmatrix} 0 & 1 & 0 \\ 1 & -4 & 1 \\ 0 & 1 & 0 \end{bmatrix}$$
  Calculates the statistical variance ($\sigma^2$) across all sampled pixels. Images falling below the sensitivity threshold are flagged as blurry:
  * **Low Sensitivity**: threshold = 70.0 (flags only heavily blurred shots)
  * **Medium Sensitivity (Default)**: threshold = 100.0 (balanced)
  * **High Sensitivity**: threshold = 150.0 (flags subtle motion blur)
* **Dark/Pocket Shot Detection**: Computes average pixel luminance:
  $$\text{Luminance} = 0.299R + 0.587G + 0.114B$$
  Images with mean luminance $< 15$ are identified as accidental dark/pocket captures.

#### 3. WhatsApp Junk & Forwarded Meme Classifier (`WhatsAppJunkClassifier.kt`)
* **Targeted Storage Filtering**: Inspects folder paths for WhatsApp media (`Android/media/com.whatsapp/`, `WhatsApp Images`, etc.).
* **On-Device Vision AI**:
  1. Runs **Google ML Kit Text Recognition v2** to analyze text density, word count, and greeting keywords (`Good Morning`, `Shubh`, `Quotes`, `Forwarded`, `Sale`, etc.).
  2. Runs **Google ML Kit Face Detection** to check for human faces.
* **Heuristic Classification**: Images with heavy overlaid text ($\ge 8$ words or $\ge 8\%$ text bounding area coverage) and **zero human faces** are classified as junk flyers/memes.

#### 4. Old Screenshots & Large Videos Cleaner (`MediaFilterAnalyzer.kt`)
* **Screenshots**: Filters images in screenshot buckets whose file age exceeds **30 days** (`dateModified >= 30 days`).
* **Large Videos**: Identifies video files with size $\ge 50\text{ MB}$, sorted descending by file size.

---

### B. Safe Deletion & Whitelist Architecture (`data/`)

* **Android 11+ Scoped Storage Safe Trash**:
  * Employs `MediaStore.createTrashRequest(uris, isTrash = true)`. Files are moved to the Android system trash with a **30-day recovery window**.
  * Handled via `ActivityResultContracts.StartIntentSenderForResult()` in `MainActivity.kt`.
  * Fallback to `MediaStore.createDeleteRequest()` for permanent deletion if requested.
* **Incremental Room Database Caching**:
  * Caches `mediaId`, `dateModified`, `pHash`, `blurScore`, and `category` in Room DB table `scanned_media`.
  * Scans compare the MediaStore `dateModified` timestamp against cached entities. Unchanged files skip vision analysis, achieving **$< 1\text{ second}$ rescans**.
* **Whitelist / Protection**:
  * Users can mark any photo or video as "Protected/Ignored".
  * Whitelisted media is excluded from all scan calculations and deletion lists.

---

### C. Background Auto-Scan (`worker/GalleryScanWorker.kt`)

* Scheduled via **Android WorkManager** as a weekly periodic task (`PeriodicWorkRequestBuilder<GalleryScanWorker>(7, TimeUnit.DAYS)`).
* Enforces battery and storage constraints (`setRequiresBatteryNotLow(true)`).
* Triggers a high-priority local notification if reclaimable junk exceeds **500 MB**, taking the user directly to the cleanup dashboard.

---

## 4. UI / UX Design & Navigation Flow

| Screen | File | Key Features |
| :--- | :--- | :--- |
| **Screen 1: Onboarding** | `OnboardingScreen.kt` | Minimalist Material 3, Privacy Trust Badge ("100% Offline"), Granular Media Permissions request (`READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `POST_NOTIFICATIONS`). |
| **Screen 2: Dashboard** | `DashboardScreen.kt` | Animated circular storage gauge (`Used / Total GB`), 2x2 Category Cards (Duplicates, Blurry, WhatsApp, Screenshots & Videos), Lifetime space saved badge, "Smart Scan Now" button. |
| **Screen 3: Scanning** | `ScanningScreen.kt` | Animated radar pulse graphic, live photo counter (`Scanning X / Y`), real-time junk accumulator (`840 MB found`), "Stop Scan" button. |
| **Screen 4: Review** | `ReviewScreen.kt` | Category switcher tabs, Grouped View for duplicates with "Best Shot" emerald badge, 3-column grid for photos/videos, sticky bottom action bar with high-contrast "Clean Now (X GB)" button. |
| **Modal: Compare** | `PhotoCompareDialog.kt` | Full-screen pinch-to-zoom viewer, resolution/size chips, AI classification reason, "Protect / Ignore" whitelist toggle, "Make Best Shot" switcher. |
| **Screen 5: Summary** | `SummaryScreen.kt` | Spring-animated success checkmark, headline (`1.2 GB Freed!`), breakdown of cleaned files, 30-day trash notice, "Done" button. |
| **Screen 6: Settings** | `SettingsScreen.kt` | Blur sensitivity slider (Low/Medium/High), Weekly Auto-Scan switch, "Upgrade to Pro" card, navigation to Protected Media list. |
| **Screen 7: Protected** | `IgnoredMediaScreen.kt` | List of whitelisted photos with one-tap "Unprotect" restoration. |

---

## 5. Performance Optimizations Implemented

1. **Parallel Concurrency**: Multi-core scanning pipeline bounded by CPU cores (`Semaphore(parallelism = 2..6)`) in `MediaRepositoryImpl.kt`.
2. **Precomputed Separable 2D DCT**: Reduced pHash operations from $1,048,576$ to $\sim 10,240$ per photo with 0 runtime `cos()` calls.
3. **Single Bulk Pixel Access**: Eliminated 1,024 native JNI calls per photo using `Bitmap.getPixels()` and fast bitwise channel extraction.
4. **128×128 RGB_565 Thumbnails**: Decreased memory allocations by 75% and eliminated garbage collector frame drops.
5. **Incremental Batch Database Writes**: Saves cached results every 40 items so progress is retained even if the user stops the scan.

---

## 6. How to Build & Run

1. Open the project folder `d:\AntiGravity\SmartClean` in **Android Studio Hedgehog / Iguana / Jellyfish** or newer.
2. Verify that **JDK 17** (or the bundled Android Studio JBR) is configured under **Build Tools > Gradle > Gradle JDK**.
3. Sync Gradle and run the app on an Android device or emulator running **Android 8.0 (API 26) through Android 14+ (API 34)**.
4. Execute unit tests using `./gradlew.bat testDebugUnitTest` or directly within Android Studio via `AnalyzerTest.kt`.
