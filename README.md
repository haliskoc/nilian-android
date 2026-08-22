# 🌿 Nilian — Personal Life Operating System

> **A calm, minimalist, and private Personal Life Operating System for Android (Phone & Tablet).**  
> Built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, and **GitHub Actions CI/CD**.

[![Build Nilian APK](https://github.com/haliskoc/nilian-android/actions/workflows/build-apk.yml/badge.svg)](https://github.com/haliskoc/nilian-android/actions/workflows/build-apk.yml)
[![Platform](https://img.shields.io/badge/Platform-Android_Phone_%26_Tablet-3DDC84?logo=android)](https://android.com)
[![UI](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Storage-Room_Offline_First-FF6D00?logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📱 Product Overview

**Nilian** is a distraction-free, privacy-first personal life operating system tailored for students and entrepreneurs. It seamlessly unifies **tasks**, **calendar events**, **habit streaks**, **24-hour time blocks**, and **long-term goals** into one coherent, calm dashboard.

### 🌟 Key Philosophy: *Calm Tech*
- **Zero Stress / Anti-Burnout:** Eliminates overwhelming notifications and aggressive gamification in favor of clean structure, thoughtful load assessments, and peaceful micro-interactions.
- **100% Offline-First:** No accounts, no cloud servers, no trackers. All data stays strictly on your device.
- **Admin PIN Security:** Protected by a master PIN / biometric lock upon launch.
- **Zero AI Overhead:** 100% deterministic, rule-based intelligence running locally.

---

## ✨ Features & Architecture

### 1. 📊 Today Dashboard
- **Day Progress:** Visual circular progress tracker with completed task metrics.
- **Next Up Hero Card:** Real-time visibility into the next upcoming lecture, meeting, or deep work session.
- **Habit Pills:** One-tap completion chips with active consecutive streak indicators (🔥).
- **1-Tap View Switcher:** Seamless transition between **Daily Timeline** and **Weekly Overview**.
- **Free Focus Slots:** Automatically identifies open slots ($\ge 30$ mins) between events.

### 2. ⏳ 24-Hour Interactive Timeline
- Continuous 24h visual schedule with category color coding (Sleep, Workout, Study, Deep Work, Rest, Buffer).
- Real-time current time indicator line.
- **Collision Detection:** Visual conflict warnings when events or time blocks overlap.

### 3. 🎯 Tasks & Automated Rollover
- Categorized by priority (High, Medium, Low) and estimated duration.
- Goal linkage.
- **Auto-Rollover Engine:** Uncompleted tasks from previous days automatically roll forward to today.

### 4. 🔁 Habits Hub & Streak Calculator
- Customized frequency (e.g., Weekdays, Daily, Custom days).
- **Rest-Day Aware Streaks:** Smart streak calculation that preserves ongoing momentum across non-scheduled rest days.
- 7-day visual dot compliance matrix.

### 5. 🏔️ Goal Navigator
- Long-term vision goals with milestone tracking and progress rollups computed from connected tasks and habits.

### 6. 📱 Adaptive Design (Phone & Tablet)
- **Phone (Compact):** Fluid Bottom Navigation Bar (`NavigationBar`).
- **Tablet (Expanded / Foldable):** Fixed Left Navigation Rail (`NavigationRail`) with widescreen canvas.

### 7. 💾 Offline Backup & Restore
- One-click JSON export/import to backup or migrate your entire database without third-party servers.

---

## 🛠️ Tech Stack & Libraries

- **Language:** Kotlin 2.0.20
- **UI Framework:** Jetpack Compose + Material 3 (1.3.0) + WindowSizeClass
- **Architecture:** Clean Architecture + MVVM + MVI Unidirectional Data Flow (StateFlow)
- **Persistence:** Room Database 2.6.1 + Jetpack DataStore Preferences
- **Asynchrony:** Kotlin Coroutines 1.9.0 & Reactive Flow
- **CI/CD:** GitHub Actions automated APK build & artifact distribution

---

## 🚀 Downloading & Installing the APK

You do not need Android Studio to install Nilian. GitHub Actions builds the APK automatically on every commit:

1. Navigate to the **[Actions Tab](https://github.com/haliskoc/nilian-android/actions)** in this repository.
2. Select the latest successful workflow run (**"Build Nilian APK"**).
3. Scroll down to the **Artifacts** section and download **`Nilian-Debug-APK`**.
4. Transfer and install the `.apk` file directly on your Android phone or tablet.

---

## 💻 Building from Source

```bash
# Clone the repository
git clone https://github.com/haliskoc/nilian-android.git
cd nilian-android

# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK locally
./gradlew assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📂 Project Structure

```
nilian-android/
├── .github/
│   └── workflows/
│       └── build-apk.yml               # GitHub Actions CI/CD Pipeline
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/com/nilian/app/
│       │       ├── NilianApp.kt        # App Context & Service Container
│       │       ├── MainActivity.kt     # Single Activity Entry & Adaptive Host
│       │       ├── core/               # Database, Security DataStore, Theme, UI Components
│       │       ├── data/               # Room Entities, DAOs, and Repositories
│       │       ├── domain/             # Domain Models, Use Cases & Deterministic Engines
│       │       └── presentation/       # ViewModels, Adaptive Layout & Compose Screens
│       └── test/                       # Comprehensive Deterministic Logic Unit Tests
├── gradle/
│   └── libs.versions.toml              # Version Catalog
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 📄 License
Released under the [MIT License](LICENSE).
