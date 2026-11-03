<div align="center">

# 🚀 Collabbit (Finder Mobile)

### *The Ultimate Student Collaboration, Project Discovery & Live Learning Platform*

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android&logoColor=white)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-purple.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Multi--Module-orange.svg)](https://developer.android.com/topic/architecture)
[![DI](https://img.shields.io/badge/DI-Koin-brightgreen.svg)](https://insert-koin.io/)
[![Network](https://img.shields.io/badge/Network-Ktor%20Client-blue.svg)](https://ktor.io/)
[![Database](https://img.shields.io/badge/Database-Room-lightgrey.svg)](https://developer.android.com/training/data-storage/room)

</div>

---

## 📌 Overview

**Collabbit** is an Android mobile application engineered for university students, creators, developers, and designers to discover peers, collaborate on real-world projects, learn through live sessions, and build authentic professional portfolios.

Built with **Jetpack Compose (Material 3)**, **Kotlin Coroutines & Flow**, **Koin**, **Ktor**, and a **Clean Multi-Module Architecture**, Collabbit delivers a high-performance native experience across light and dark themes.

---

## ✨ Key Features

### 🎴 1. Explore Swipe Deck
- **Tinder-Style Discovery**: Swipe through student builder profiles and project ideas.
- **Gesture Feedback**: Dynamic pass (Red) and connect (Green) card border glowing effects during drag gestures.
- **Send Notes**: Attach personalized notes when connecting with peers.
- **Match Percentage**: Intelligent compatibility scores based on shared skills, interests, and campus affiliation.

### 🤝 2. Collaboration Projects
- **Project Showcase**: Create, edit, and explore projects across Hackathons, Cobuilding, Research, and Study groups.
- **Refactored Role Management**: Clear role names and descriptions for open team positions.
- **Nested Social Opinion Trees**: Threaded community feedback and discussion trees with role badges (**PROJECT LEAD** vs **PEER**) and right-aligned inline reply flow.
- **Team Management**: Project leads can manage applicant requests and team members.

### 🎓 3. Live Sessions & Workshops
- **Interactive Workshops**: Enroll in live sessions hosted by campus instructors.
- **Seat Availability Visualizer**: Real-time interactive seat matrix grid with booked vs. available indicators.
- **URL Previews**: OpenGraph rich link preview cards extracted automatically from session descriptions.
- **Profile Completion Check**: Automatic 80%+ profile completion check before enrollment.

### 🏆 4. Hexagon Aura Gamification
- **Hexagon Badges**: Hexagon-shaped level badges (Newcomer, Spark, Rising, Glowing, Radiant, Blazing, Legendary).
- **Pulse & Glow Animations**: Active unlocked badges feature expanding/shrinking scale pulse animations with dynamic aura glowing rings.
- **Auto-Centered Carousel**: Automatically centers the active badge in the profile header carousel.

### 🔔 5. Notifications & FCM Token Sync
- **Firebase Push Notifications**: Real-time updates for connection requests, feedback replies, and live sessions.
- **Reliable Token Syncing**: Background FCM device token store with 3-day skip decision logic and exponential backoff retry manager (`PUT api/v1/users/me/device-tokens`).

---

## 🏗️ Architecture & Module Structure

Collabbit follows **Clean Architecture** principles decoupled into feature modules:

```text
finder-mobile/
├── app/                      # Main application entry point & DI orchestration
├── core/
│   ├── common/               # UI states, extension utilities, String/Date helpers
│   ├── database/             # Room SQLite database & DAOs (User profile, posts)
│   ├── designsystem/         # Unified M3 Theme, UserAvatar, Shimmer, LinkPreviewCard
│   ├── firebase/             # Firebase Auth & Messaging initialization
│   ├── navigation/           # Type-safe Compose navigation routes & deep links
│   ├── network/              # Ktor HttpClient setup & NetworkConfig
│   └── notifications/        # FCM service & background token sync manager
└── feature/
    ├── auth/                 # Google Sign-In & Onboarding screens
    ├── chat/                 # Direct messaging & project conversations
    ├── collab/               # Collaboration project creation, editing, & feedback
    ├── explore_swipe/        # Swipe deck cards, match algorithms, & notes
    ├── feed/                 # Community posts, rich link attachments, & replies
    ├── notification/         # Notification inbox & action handlers
    ├── opportunities/        # Live sessions, seat matrix, & enrollment
    └── profile/              # User profile, education history, & Aura level stats
```

---

## 🛠️ Technology Stack

| Category | Library / Tech |
| :--- | :--- |
| **Language** | Kotlin 2.0+ |
| **UI Framework** | Jetpack Compose, Material 3, Canvas Graphics |
| **Architecture** | Clean Architecture, Multi-Module, Unidirectional Data Flow (UDF) |
| **Dependency Injection** | Koin (`koin-androidx-compose`) |
| **Asynchronous** | Kotlin Coroutines, StateFlow, SharedFlow |
| **Networking** | Ktor Client (`ktor-client-core`, `ktor-client-content-negotiation`, `kotlinx-serialization`) |
| **Local Persistence** | Room Database, Encrypted Storage |
| **Image Loading** | Coil Compose |
| **Link Preview** | RichLinkPreview (OpenGraph Metadata Scraper) |
| **Media & Audio/Video** | AndroidX Media3 (ExoPlayer) |
| **Push Notifications** | Firebase Cloud Messaging (FCM) & Coroutines Token Sync |
| **Testing** | JUnit, MockK, Kotlinx Coroutines Test, Turbine |

---

## 🧪 Testing & Quality Assurance

The codebase includes unit tests for repositories, viewmodels, and data model logic.

### Running Unit Tests

```bash
# Run all unit tests across modules
./gradlew test

# Run feature-specific unit tests
./gradlew :feature:collab:testDebugUnitTest
./gradlew :app:testProdDebugUnitTest
```

---

## ⚙️ Setup & Installation

### Prerequisites
- **Android Studio**: Jellyfish / Ladybug or newer
- **JDK**: Version 17+
- **Android SDK**: Min SDK 24 (`Android 7.0`), Target SDK 36 (`Android 15`)

### Getting Started

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/finder-mobile.git
   cd finder-mobile
   ```

2. **Configure Firebase**:
   - Place your `google-services.json` file inside the `app/` directory.

3. **Build the Project**:
   ```bash
   # Assemble Debug APK
   ./gradlew assembleDebug
   ```

4. **Run on Device / Emulator**:
   Select the `app` run configuration in Android Studio and press **Run** (`Shift + F10`).

---

## 📄 ProGuard & Release Security

Release builds are obfuscated and shrunk using R8. Key rules for `@Serializable` DTO models and Kotlinx Serialization are preserved in `app/proguard-rules.pro`:

```proguard
-keepattributes *Annotation*,Signature,InnerClasses
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
```

---

<div align="center">
Made with ❤️ for students & builders worldwide.
</div>
