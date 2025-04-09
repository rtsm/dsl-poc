# DSL-POC: Domain-Specific Language for Mobile Feature Generation

## Overview
DSL-POC is an innovative project that introduces a Domain-Specific Language (DSL) for generating complete mobile features. It automates the creation of boilerplate code, enforces consistent architecture patterns, and significantly reduces development time for new features.

## Key Features
- **Declarative Feature Definition**: Define features using a clean, intuitive DSL
- **Complete Feature Generation**: Generates all necessary components for a feature
- **Consistent Architecture**: Enforces clean architecture and best practices
- **Type-Safe**: Leverages Kotlin's type system for compile-time safety
- **Documentation Generation**: Automatically creates comprehensive documentation
- **Customizable**: Easy to extend and modify generated code

## Project Structure
```
dsl-poc/
├── mobile/                    # Android application
│   ├── app/                  # Main application module
│   ├── definitions/          # Feature definitions
│   └── tools/               # Development tools
└── dsl-processor/           # DSL processor
    └── src/main/kotlin/     # DSL implementation
```

## Architecture
The project follows a clean architecture approach with the following layers:
- **Domain Layer**: Contains business logic and models
- **Data Layer**: Handles data operations and repositories
- **Presentation Layer**: Manages UI state and actions
- **Navigation Layer**: Handles screen navigation
- **DI Layer**: Manages dependency injection

## DSL Language Structure

The DSL provides a declarative way to define mobile features. Here's a detailed breakdown of all available components:

The DSL file should contain:
- **Package Name**: Defines where generated files will be placed
- **Domain Models**: Data structures used in the feature
- **UI States**: State classes for managing screen state
- **UI Actions**: Actions that can be triggered from the UI
- **API Endpoints**: Backend API endpoints and their response types
- **Navigation**: Screen routing information

Each section is optional, but typically a feature will need at least:
- One domain model
- One UI state
- One UI action
- One API endpoint (if it needs to fetch data)

### 1. Feature Definition
```kotlin
feature("FeatureName") {
    packageName = "com.example.feature"  // Required: Package for generated files

    // All other components are optional
}
```

### 2. Domain Models
Define data structures used in your feature:
```kotlin
domainModel("User") {
    property("id", "String")              // Required property
    property("name", "String")            // Required property
    property("email", "String?")          // Optional property (nullable)
    property("age", "Int")                // Primitive type
    property("address", "Address")        // Custom type
    property("tags", "List<String>")      // Collection type
}
```

### 3. UI States
Define the state of your UI:
```kotlin
uiState("LoginState") {
    property("email", "String")           // Text input
    property("password", "String")        // Password input
    property("isLoading", "Boolean")      // Loading state
    property("error", "String?")          // Error message
    property("user", "User?")             // Data model
}
```

### 4. UI Actions
Define user interactions and system events:
```kotlin
// Simple action without parameters
uiAction("LoginClicked")

// Action with parameters
uiAction("EmailChanged", "email" to "String")
uiAction("PasswordChanged", "password" to "String")

// Action with multiple parameters
uiAction("UserLoggedIn",
    "user" to "User",
    "token" to "String"
)
```

### 5. API Endpoints
Define network calls:
```kotlin
// GET request
apiEndpoint("getUser", "/api/user/{id}", "User")

// POST request with request body
apiEndpoint("login", "/api/login", "AuthResponse", "LoginRequest")

// PUT request
apiEndpoint("updateProfile", "/api/profile", "User", "UpdateProfileRequest")

// DELETE request
apiEndpoint("deleteAccount", "/api/account", "Unit")
```

### 6. Navigation
Define screen routing:
```kotlin
navigation {
    route = "profile/{userId}"           // Required: Route pattern
    arguments = listOf(                  // Optional: Route arguments
        "userId" to "String",
        "tab" to "String?"
    )
    deepLinks = listOf(                  // Optional: Deep links
        "app://profile/{userId}",
        "https://example.com/profile/{userId}"
    )
}
```

### 7. Complete Example
Here's a complete example of a Login feature:
```kotlin
feature("Login") {
    packageName = "com.example.login"

    // Domain Models
    domainModel("LoginRequest") {
        property("email", "String")
        property("password", "String")
    }

    domainModel("AuthResponse") {
        property("token", "String")
        property("user", "User")
    }

    // UI State
    uiState("LoginState") {
        property("email", "String")
        property("password", "String")
        property("isLoading", "Boolean")
        property("error", "String?")
    }

    // UI Actions
    uiAction("EmailChanged", "email" to "String")
    uiAction("PasswordChanged", "password" to "String")
    uiAction("LoginClicked")
    uiAction("LoginSuccess", "authResponse" to "AuthResponse")
    uiAction("LoginError", "error" to "String")

    // API Endpoints
    apiEndpoint("login", "/api/login", "AuthResponse", "LoginRequest")

    // Navigation
    navigation {
        route = "login"
        deepLinks = listOf("app://login")
    }
}
```

### 8. Best Practices
1. **Naming Conventions**:
   - Use PascalCase for model and state names
   - Use camelCase for property names
   - Use past tense for action names (e.g., `UserLoggedIn`)

2. **State Management**:
   - Keep UI state minimal
   - Use nullable types for optional values
   - Include loading and error states

3. **API Design**:
   - Use RESTful endpoints
   - Include both request and response types
   - Use appropriate HTTP methods

4. **Navigation**:
   - Use descriptive route names
   - Include all required arguments
   - Add deep links for external access

## Getting Started

### Prerequisites
- JDK 17 or higher
- Android Studio
- Gradle 7.0 or higher

### Installation
1. Clone the repository:
```bash
git clone https://github.com/your-username/dsl-poc.git
cd dsl-poc
```

2. Build the project:
```bash
./gradlew build
```
