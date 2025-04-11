# Profile Feature Documentation

## Overview
This document describes the Profile feature implementation details.

## Domain Models
### UserProfile
Properties:
- `id`: String
- `name`: String
- `email`: String
- `avatarUrl`: String

## UI States
### ProfileState
Properties:
- `isLoading`: Boolean (default: false)
- `profile`: UserProfile? (nullable)
- `accounts`: List<Account>? (nullable)
- `notificationsEnabled`: Boolean (default: false)

## UI Actions
### LoadProfile
Properties:
- `profile`: UserProfile
- `error`: String

### ProfileLoaded
Properties:
- `profile`: UserProfile
- `error`: String

### ProfileLoadFailed
Properties:
- `profile`: UserProfile
- `error`: String

## Data Sources
### Network Call: profile
- Type: Network API Call
- Path: `/api/profile`
- Response Type: `"UserProfile"`

### Local DM: "getAccounts"
- Type: Local DM Source
- Repository: `"AccountRepository"`
- Method: `"getAccounts"`
- Response Type: `"List<Account>"`

### Preference: notifications_enabled
- Type: Shared Preference
- Key: `notifications_enabled`
- Value Type: `"Boolean"`

## Generated Files
The following files are generated for this feature:
- `domain/model/*.kt`: Domain model classes
- `domain/ProfileRepository.kt`: Repository interface and implementation
- `presentation/state/ProfileState.kt`: UI state class
- `presentation/action/ProfileAction.kt`: UI action classes
- `presentation/reducer/ProfileReducer.kt`: State reducer
- `presentation/screen/ProfileScreen.kt`: UI screen
- `navigation/ProfileNavigation.kt`: Navigation setup
- `di/ProfileDI.kt`: Dependency injection setup
- `presentation/screen/ProfileViewModel.kt`: ViewModel
- `ProfileJourney.kt`: Feature journey

## State Management
The feature uses a unidirectional data flow pattern:
1. UI triggers actions
2. Actions are processed by the reducer
3. State updates trigger UI updates

## Navigation
The feature is accessible via the route: `profile`

## Dependencies
The feature depends on:
- Network client for API calls
- Preference client for shared preferences
- Dependency injection framework
- Navigation framework
