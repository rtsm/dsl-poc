 feature {
     packageName = "profile"
     featureName = "Profile"

     // Define domain models
     domainModel {
         name = "UserProfile"
         property("id", "String")
         property("name", "String")
         property("email", "String")
         property("avatarUrl", "String")
     }

     // Define API endpoints
     apiEndpoint {
         name = "getUserProfile"
         path = "/api/user/profile"
         method = "GET"
         responseModel = "UserProfile"
     }

     // Define UI state
     uiState {
         name = "ProfileState"
         property("isLoading", "Boolean")
         property("userProfile", "UserProfile?")
         property("error", "String?")
     }

     // Define UI actions
     uiAction {
         name = "LoadProfile"
     }
     uiAction {
         name = "ProfileLoaded"
         property("profile", "UserProfile")
     }
     uiAction {
         name = "ProfileLoadFailed"
         property("error", "String")
     }
 }