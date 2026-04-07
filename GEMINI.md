# PlovdivTransit

PlovdivTransit is an Android application designed to provide public transit information for Plovdiv, Bulgaria. It features real-time navigation, route searching, and user authentication.

## Project Overview

- **Core Goal**: A comprehensive transit app for Plovdiv, utilizing GTFS data and maps.
- **Technologies**:
    - **Language**: Kotlin
    - **UI Framework**: Jetpack Compose
    - **Maps**: osmdroid
    - **Authentication**: Firebase Auth
    - **Networking**: Retrofit & Gson
    - **Data Source**: GTFS (General Transit Feed Specification) & Geoapify API

## Architecture

The project follows modern Android development practices:
- **Repositories**: `GtfsRepository`, `GeoapifyRepository`, and `RoutingRepository` handle data access and logic.
- **Authentication**: `AuthManager` encapsulates Firebase Auth logic.
- **UI**: Screens like `HomeScreen`, `LoginScreen`, `RegisterScreen`, and `SearchScreen` are implemented using Jetpack Compose.
- **Navigation**: Managed in `MainActivity.kt`.

## Building and Running

### Prerequisites
- Android Studio or IntelliJ IDEA with the Android plugin.
- JDK 17 (as configured in `app/build.gradle.kts`).
- A `GEOAPIFY_API_KEY`.

### Build Commands
- **Assemble Debug APK**: `./gradlew assembleDebug`
- **Install on Device**: `./gradlew installDebug`
- **Run Tests**: `./gradlew test`

### Configuration
The app requires a Geoapify API key. You can provide it in your `local.properties` file (not included in version control):
```properties
GEOAPIFY_API_KEY=your_api_key_here
```

## Development Conventions

- **Compose**: All new UI should be built using Jetpack Compose.
- **Kotlin**: Use Kotlin Coroutines for asynchronous operations (as seen in Repositories).
- **Styling**: Adhere to the `PlovdivTransitTheme` defined in the `ui.theme` package.
- **Data Handling**: New data sources should be wrapped in a Repository.
