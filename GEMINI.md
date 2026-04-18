# StudyTime Check Project Overview

This project is an Android application designed to track and analyze study time by subject. It helps users understand how much time they allocate to different subjects and provides insights for better time management. The project is built using Kotlin and Jetpack Compose, following a clear separation of concerns between business logic, state management, and UI.

## Architecture

The project follows a layered architecture:

-   **Core (`/core`):** Contains the pure business logic and domain models (e.g., `StudySessionUseCase`, `Session`, `Subject`). This layer is independent of the Android framework.
-   **ViewModel (`/viewmodel`):** Manages UI state using `StateFlow` and interacts with the Core layer to process data (e.g., `StudyDashboardViewModel`).
-   **UI (`/ui`):** Built with Jetpack Compose, this layer observes the ViewModel's state and renders the user interface (e.g., `HomeScreen`, `StudyTimerOverlay`).
-   **Main Entry Point:** `MainActivity.kt` serves as the entry point, setting up the Compose theme and the main screen.

## Technologies

-   **Language:** Kotlin
-   **UI Framework:** Jetpack Compose (Material3)
-   **State Management:** StateFlow & ViewModel (Android Architecture Components)
-   **Asynchronous Programming:** Kotlin Coroutines & Flow

## Building and Running

As this is a standard Android project (inferred), you can use the following commands:

-   **Build:** `./gradlew assembleDebug` (TODO: Verify if Gradle is configured)
-   **Run:** Use Android Studio to run on an emulator or a physical device.
-   **Test:** `./gradlew test` (TODO: Add unit tests for `StudySessionUseCase`)

## Development Conventions

-   **Naming:** Follow standard Kotlin/Android naming conventions (PascalCase for classes, camelCase for functions and variables).
-   **UI Patterns:** Use Jetpack Compose for all UI components. Prefer small, reusable `@Composable` functions.
-   **State Management:** Always expose UI state via `StateFlow` in ViewModels. UI components should remain stateless where possible, reacting to state changes.
-   **Business Logic:** Keep platform-independent business logic in the `core` package to ensure testability and potential portability.
-   **Comments:** Use KDoc (`/** ... */`) for class and function-level documentation, especially for complex business logic.
