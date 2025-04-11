feature Profile

domainModel {
    name "UserProfile"
    property "id": "String"
    property "name": "String"
    property "email": "String"
    property "avatarUrl": "String"
}

uiState {
    name "ProfileState"
    property "isLoading": "Boolean"
    property "profile": "UserProfile?"
    property "accounts": "List<Account>?"
    property "notificationsEnabled": "Boolean"
}

uiAction {
    name "LoadProfile"
}

uiAction {
    name "ProfileLoaded"
    property "profile": "UserProfile"
}

uiAction {
    name "ProfileLoadFailed"
    property "error": "String"
}

dataSources {
    networkCall "/api/profile", "UserProfile"
    localDM "AccountRepository", "getAccounts", "List<Account>"
    preference "notifications_enabled", "Boolean"
}