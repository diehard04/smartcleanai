# SmartClean AI 🧹✨

> **100% On-Device AI Gallery Cleaner for Android**  
> Free up gigabytes of device storage in a single tap without uploading a single photo to the cloud.

---

## 🚀 Features

- **Perceptual Duplicate Detection**: Identifies exact duplicates (SHA-256) and visually similar burst shots using a 64-bit Perceptual Hash (pHash via 2D DCT) with Hamming Distance $\le 5$. Automatically tags the sharpest/highest-resolution image as **"Best Shot"**.
- **Blurry & Dark Photo Detector**: Computes discrete Laplacian Variance on downscaled grayscale bitmaps to catch motion blur and out-of-focus photos, plus mean pixel luminance for accidental dark/pocket shots.
- **WhatsApp Junk & Meme Classifier**: Runs **Google ML Kit Text Recognition v2** and **Face Detection** on-device to classify forwarded flyers, greeting cards, and memes containing heavy text and zero human faces.
- **Old Screenshots & Large Videos**: Quickly clears screenshots older than 30 days and video files $\ge 50\text{ MB}$.
- **Safe 30-Day Trash Bin**: Compliant with Android 11+ Scoped Storage using `MediaStore.createTrashRequest()`, allowing 30-day recovery from the system Gallery Trash.
- **Incremental Caching**: Powered by Room Database (`scanned_media`) based on `mediaId` and `dateModified`, making rescans over **$10\times$ faster**.
- **Background Auto-Scan**: Periodic WorkManager task scanning new media and alerting if reclaimable junk exceeds $500\text{ MB}$.

---

## 📁 Detailed Documentation

For a comprehensive guide covering the architectural layers, folder directory tree, algorithmic math, UI/UX flows, and performance benchmarks, refer to:

👉 **[PROJECT_STRUCTURE_AND_FUNCTIONALITY.md](PROJECT_STRUCTURE_AND_FUNCTIONALITY.md)**

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 1.9.23
- **UI Framework**: Jetpack Compose (Material 3) with Unidirectional Data Flow
- **Architecture**: Clean Architecture + MVVM (ViewModel + StateFlow)
- **Local Database**: Room 2.6.1 with KSP
- **Image Loading**: Coil 2.6.0 with memory & disk cache
- **On-Device Vision AI**: Google ML Kit Text Recognition v2 (16.0.1) & Face Detection (16.1.7)
- **Background Jobs**: AndroidX WorkManager 2.9.0
- **Min SDK**: API 26 (Android 8.0) | **Target SDK**: API 34 (Android 14)

---

## 🏃 Running the App

1. Open `d:\AntiGravity\SmartClean` in **Android Studio**.
2. Set Gradle JDK to **JDK 17** (or Android Studio JBR).
3. Build and launch on your device or emulator.
