# Playbook Reader - Android eBook Reader with Continuous Vertical Scrolling Mode

**Playbook Reader** is an Android eBook Reader app inspired by Google Play Books, designed to address one major missing feature in Google Play Books: **Continuous Vertical Scrolling Reading Mode**.

---

## 🌟 Key Features

1. **Continuous Vertical Scroll Engine**
   - Seamlessly scroll through chapters top-to-bottom without interruption or page flip delays.
   - Automatically saves scroll offset and chapter position so you never lose your spot.
   - Smooth progress tracking with chapter indicator overlays.

2. **Google Play Books Inspired UI**
   - Modern Material 3 UI design.
   - Bookshelf Grid & List view modes.
   - "Continue Reading" quick-resume banner with progress bar.

3. **Reading Customization & Themes**
   - **Themes**: Light, Sepia, Dark, and Amoled Night mode.
   - **Typography**: Adjust Font Size, Line Spacing, and Font Styles (Serif, Sans-Serif, Monospace).
   - **Mode Toggle**: Easily switch between **Vertical Scroll Mode** and **Paged Mode**.

4. **eBook Format & File Support**
   - Full support for **EPUB** and **TXT** eBook formats.
   - Storage Access Framework (SAF) system file picker to easily import books from your device.
   - Table of Contents (TOC) quick jump drawer.

---

## 🚀 Building & Downloading APK via GitHub Actions

This repository is configured with **GitHub Actions CI/CD** (`.github/workflows/build-apk.yml`).

Every push to `main` triggers a GitHub runner that compiles the project and produces a downloadable Android APK:

1. Go to the **Actions** tab on this GitHub repository.
2. Click on the latest workflow run: `Build Playbook Reader Android APK`.
3. Scroll down to the **Artifacts** section and download `playbook-reader-app-debug.apk`.
4. Install the `.apk` on your Android device and enjoy continuous vertical scrolling!

---

## 🛠️ Project Tech Stack

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture Flow
- **Parser**: EPUB Parser (Jsoup + ZipInputStream) & Text Parser
- **Persistence**: SharedPreferences + JSON Serialization
- **Build System**: Gradle 8.9 (Kotlin DSL) + GitHub Actions CI
