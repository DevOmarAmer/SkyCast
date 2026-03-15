<div align="center">

  <img src="https://via.placeholder.com/800x250/0F172A/38BDF8?text=SkyCast:+AI-Powered+Smart+Weather+Assistant" alt="SkyCast Banner" width="100%">

  <h1>🌤️ SkyCast</h1>
  <p><b>A Next-Generation, AI-Powered Smart Weather Assistant Built with Modern Native Android</b></p>

  <a href="https://kotlinlang.org/">
    <img src="https://img.shields.io/badge/Kotlin-1.9+-Blue?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  </a>
  <a href="https://developer.android.com/jetpack/compose">
    <img src="https://img.shields.io/badge/Jetpack_Compose-Success?style=for-the-badge&logo=android&logoColor=white" alt="Jetpack Compose">
  </a>
  <a href="https://developer.android.com/topic/architecture">
    <img src="https://img.shields.io/badge/Clean_Architecture-MVVM-orange?style=for-the-badge" alt="Clean Architecture">
  </a>
  <a href="https://ai.google.dev/">
    <img src="https://img.shields.io/badge/Google_Gemini-AI-purple?style=for-the-badge&logo=google&logoColor=white" alt="Google Gemini">
  </a>
  <a href="https://opensource.org/licenses/MIT">
    <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge" alt="License: MIT">
  </a>

</div>

<br/>

## 📋 Table of Contents
1. [Overview](#-overview)
2. [Key Features](#-key-features)
3. [Architecture & Tech Stack](#-architecture--tech-stack)
4. [Project Structure](#-project-structure)
5. [Screenshots & Demo](#-screenshots--demo)
6. [Getting Started](#-getting-started)
7. [Roadmap](#-roadmap)
8. [License](#-license)
9. [Contact](#-contact)

---

## 📖 Overview
**SkyCast** is a highly scalable, robust Native Android application that redefines how users interact with weather data. Moving beyond standard temperature displays, SkyCast integrates **Google's Gemini AI SDK** to act as a personal meteorological assistant, providing human-like, actionable insights (e.g., outfit recommendations, commute advice). 

Furthermore, it features a highly reliable background task scheduling system for **smart condition-based alerts**, built strictly on top of **Clean Architecture** principles to ensure top-tier performance and maintainability.

---

## ✨ Key Features
| Feature | Description |
| :--- | :--- |
| **🤖 AI Weather Analyst** | Integrates `gemini-1.5-flash` to generate detailed, conversational morning briefings and daily weather analyses based on complex data points. |
| **🚨 Smart Condition Alerts** | Uses `WorkManager` to run silent background checks and notify you if specific custom conditions are met (e.g., "Temp drops below 10°C" or "Rain expected"). |
| **☕ Morning Briefings** | A scheduled daily push notification summarizing the day ahead, with auto-cancellation if disabled. |
| **🌙 Modern UI/UX** | Fully declarative UI built with **Jetpack Compose**, featuring smooth animations, glassmorphism, and dynamic state handling (Loading, Success, Error). |
| **🌍 Offline & Localization** | Support for multiple languages (Arabic & English) and offline viewing of favorite locations via **Room Database**. |

---

## 🛠 Architecture & Tech Stack
This project strictly follows **Clean Architecture** principles and the **MVVM (Model-View-ViewModel)** design pattern. It enforces **Unidirectional Data Flow (UDF)** to ensure high performance, testability, and a clear separation of concerns.

### 🧩 Core Technologies:
* **UI Layer:** Jetpack Compose, Material Design 3, Navigation Compose.
* **State Management:** Kotlin Coroutines, `StateFlow`, `SharedFlow`, `collectAsStateWithLifecycle`.
* **Network Layer:** Retrofit2, OkHttp3, Gson (for OpenWeather API).
* **Local Storage:** Room Database (Entities & DAOs), DataStore / SharedPreferences.
* **Background Processing:** WorkManager (Periodic & One-Time requests).
* **AI Integration:** Google Generative AI SDK (`gemini-1.5-flash`).
* **Dependency Injection:** Manual DI (AppContainer) for strict architectural boundaries.
* **Testing:** JUnit4, MockK, Turbine (for testing Flows and ViewModels).

---

## 📁 Project Structure
The codebase is modularized by feature within core architectural layers (Package by Feature / Layer Hybrid):

```text
com.example.skycast
│
├── data/                  # 🗄️ Data Layer (Network, Local DB, Repositories)
│   ├── local/             # Room DB, DAOs, Entities
│   ├── remote/            # Retrofit, API Services, DTOs
│   ├── repository/        # Repository Implementations (Single Source of Truth)
│   └── service/           # WorkManager Workers (MorningBrief, ConditionAlerts)
│
├── domain/                # 🧠 Domain Layer (Interfaces, Business Models)
│
├── ui/                    # 📱 Presentation Layer (Jetpack Compose + ViewModels)
│   ├── home/              # AI Report & Current Weather UI
│   ├── alerts/            # Smart Alerts UI & Condition Dialogs
│   ├── favorites/         # Favorite Locations Management
│   └── theme/             # Material 3 Colors, Typography, Shapes
│
├── di/                    # 💉 Dependency Injection (AppContainer)
│
└── utils/                 # ⚙️ Core Utilities (Constants, Locale Helpers, Notifications)
