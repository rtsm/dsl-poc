package com.example.dsl

import com.example.dsl.template.*
import java.io.File

class CodeGenerator(private val feature: FeatureBuilder) {
    private val templateProcessor = TemplateProcessor()

    fun generate() {
        generateDomainModels()
        generateRepository()
        generateUIState()
        generateUIActions()
        generateReducer()
        generateDI()
        generateNavigation()
        generateViewModel()
        generateScreen()
        generateJourney()
        generateDocumentation()
    }

    private fun generateDomainModels() {
        feature.domainModels.forEach { model ->
            val properties = model.properties.joinToString(",\n") { property ->
                "    val ${property.name}: ${property.type}"
            }

            val content = templateProcessor
                .addReplacement("package_name", feature.packageName)
                .addReplacement("class_name", model.name)
                .addReplacement("properties", properties)
                .process(DomainModelTemplate.TEMPLATE)

            writeFile("domain/model/${model.name}.kt", content)
            templateProcessor.clear()
        }
    }

    private fun generateRepository() {
        // Generate repository methods
        val repositoryMethods = feature.apiEndpoints.joinToString("\n") { endpoint ->
            "    fun ${endpoint.name}(): Flow<${endpoint.responseModel}>"
        }

        // Generate repository implementation methods
        val repositoryImplMethods = feature.apiEndpoints.joinToString("\n\n") { endpoint ->
            templateProcessor
                .addReplacement("repository_method", endpoint.name)
                .addReplacement("model_name", endpoint.responseModel)
                .addReplacement("endpoint_path", endpoint.path)
                .process(RepositoryImplTemplate.TEMPLATE)
        }

        val content = templateProcessor
            .addReplacement("package_name", feature.packageName)
            .addReplacement("repository_name", "${feature.featureName}Repository")
            .addReplacement("repository_impl_name", "${feature.featureName}RepositoryImpl")
            .addReplacement("repository_methods", repositoryMethods)
            .addReplacement("repository_impl_methods", repositoryImplMethods)
            .process(RepositoryTemplate.TEMPLATE)

        val path = "domain/${feature.featureName}Repository.kt"
        // Skip if file already exists
        if (File(getFullFilePath(path)).exists()) {
            println("Skipping generation of existing file: $path")
            return
        } else {
            writeFile(path, content)
        }
        templateProcessor.clear()
    }

    private fun generateUIState() {
        val properties = feature.uiStates.flatMap { it.properties }
            .joinToString(",\n") { property ->
                val defaultValue = when {
                    property.type.endsWith("?") -> " = null"
                    property.type == "Boolean" -> " = false"
                    else -> ""
                }
                "    val ${property.name}: ${property.type}$defaultValue"
            }

        // Collect all domain model types used in properties
        val domainModelTypes = feature.uiStates.flatMap { it.properties }
            .map { it.type }
            .filter { type -> 
                feature.domainModels.any { it.name == type.removeSuffix("?") }
            }
            .distinct()

        // Generate imports for domain models
        val domainModelImports = domainModelTypes.joinToString("\n") { type ->
            "import com.example.${feature.packageName}.domain.model.${type.removeSuffix("?")}"
        }

        val content = templateProcessor
            .addReplacement("package_name", feature.packageName)
            .addReplacement("state_name", "${feature.featureName}State")
            .addReplacement("properties", properties)
            .addReplacement("domain_model_imports", domainModelImports)
            .process(UIStateTemplate.TEMPLATE)

        writeFile("presentation/state/${feature.featureName}State.kt", content)
        templateProcessor.clear()
    }

    private fun generateUIActions() {
        val actionClasses = feature.uiActions.joinToString("\n\n") { action ->
            if (action.properties.isEmpty()) {
                """
    data object ${action.name} : ${feature.featureName}Action()"""
            } else {
                val properties = action.properties.joinToString(",\n") { property ->
                    "        val ${property.name}: ${property.type}"
                }
                """
    data class ${action.name}(
$properties
    ) : ${feature.featureName}Action()"""
            }
        }

        // Collect all domain model types used in action properties
        val domainModelTypes = feature.uiActions.flatMap { it.properties }
            .map { it.type }
            .filter { type -> 
                feature.domainModels.any { it.name == type.removeSuffix("?") }
            }
            .distinct()

        // Generate imports for domain models
        val domainModelImports = domainModelTypes.joinToString("\n") { type ->
            "import com.example.${feature.packageName}.domain.model.${type.removeSuffix("?")}"
        }

        val content = templateProcessor
            .addReplacement("package_name", feature.packageName)
            .addReplacement("action_name", "${feature.featureName}Action")
            .addReplacement("action_classes", actionClasses)
            .addReplacement("domain_model_imports", domainModelImports)
            .process(UIActionTemplate.TEMPLATE)

        writeFile("presentation/action/${feature.featureName}Action.kt", content)
        templateProcessor.clear()
    }

    private fun generateReducer() {
        val reducerCases = feature.uiActions.joinToString("\n") { action ->
            val stateUpdates = when {
                action.name.startsWith("Load") -> "isLoading = true"
                action.name.endsWith("Loaded") || action.name.endsWith("Failed") -> "isLoading = false"
                else -> ""
            }
            
            // Match action properties to state properties by type
            val propertyMappings = action.properties.map { actionProperty ->
                // Find a state property with the same type
                val matchingStateProperty = feature.uiStates.flatMap { it.properties }
                    .find { stateProperty -> 
                        // Remove nullable suffix for comparison
                        val actionType = actionProperty.type.removeSuffix("?")
                        val stateType = stateProperty.type.removeSuffix("?")
                        actionType == stateType
                    }
                
                // If found, map the action property to the state property
                if (matchingStateProperty != null) {
                    "${matchingStateProperty.name} = action.${actionProperty.name}"
                } else {
                    // If no match found, just use the action property name
                    "${actionProperty.name} = action.${actionProperty.name}"
                }
            }.joinToString(",\n                    ")
            
            val allProperties = if (stateUpdates.isNotEmpty()) {
                if (propertyMappings.isNotEmpty()) {
                    "$stateUpdates,\n                    $propertyMappings"
                } else {
                    stateUpdates
                }
            } else {
                propertyMappings
            }
            
            """
            is ${feature.featureName}Action.${action.name} -> state.mutateWithoutEffects {
                state.invoke().copy(
                    $allProperties
                )
            }"""
        }

        val content = templateProcessor
            .addReplacement("package_name", feature.packageName)
            .addReplacement("reducer_name", "${feature.featureName}Reducer")
            .addReplacement("state_name", "${feature.featureName}State")
            .addReplacement("action_name", "${feature.featureName}Action")
            .addReplacement("reducer_cases", reducerCases)
            .process(ReducerTemplate.TEMPLATE)

        writeFile("presentation/reducer/${feature.featureName}Reducer.kt", content)
        templateProcessor.clear()
    }

    private fun generateDI() {
        // Collect all domain model types used in the feature
        val domainModelTypes = feature.domainModels.map { it.name }
            .distinct()

        // Generate imports for domain models
        val domainModelImports = domainModelTypes.joinToString("\n") { type ->
            "import com.example.${feature.packageName}.domain.model.$type"
        }

        val content = templateProcessor
            .addReplacement("package_name", feature.packageName)
            .addReplacement("repository_name", "${feature.featureName}Repository")
            .addReplacement("repository_impl_name", "${feature.featureName}RepositoryImpl")
            .addReplacement("reducer_name", "${feature.featureName}Reducer")
            .addReplacement("state_name", "${feature.featureName}State")
            .addReplacement("domain_model_imports", domainModelImports)
            .process(DITemplate.TEMPLATE)

        writeFile("di/${feature.featureName}DI.kt", content)
        templateProcessor.clear()
    }

    private fun generateNavigation() {
        val content = templateProcessor
            .addReplacement("package_name", feature.packageName)
            .addReplacement("screen_name", "${feature.featureName}Screen")
            .addReplacement("state_name", "${feature.featureName}State")
            .addReplacement("action_name", "${feature.featureName}Action")
            .addReplacement("route_name", feature.packageName)
            .process(NavigationTemplate.TEMPLATE)

        writeFile("navigation/${feature.featureName}Navigation.kt", content)
        templateProcessor.clear()
    }

    private fun generateScreen() {
        // Generate state property displays
        val stateProperties = feature.uiStates.flatMap { it.properties }
            .filter { it.name != "isLoading" } // Skip isLoading as it's already displayed
            .joinToString("\n            ") { property ->
                "Text(text = \"${property.name}: ${if (property.type.endsWith("?")) "\${state.${property.name} ?: \"null\"}" else "\${state.${property.name}}"}\")"
            }

        // Find the first API endpoint to use for repository method
        val firstEndpoint = feature.apiEndpoints.firstOrNull()
        val repositoryMethod = firstEndpoint?.name ?: ""
        val actionLoaded = feature.uiActions.find { it.name.endsWith("Loaded") }?.name ?: ""
        val actionFailed = feature.uiActions.find { it.name.endsWith("Failed") }?.name ?: ""
        val actionLoading = feature.uiActions.find { it.name.startsWith("Load") }?.name ?: ""

        val content = templateProcessor
            .addReplacement("package_name", feature.packageName)
            .addReplacement("screen_name", "${feature.featureName}Screen")
            .addReplacement("state_name", "${feature.featureName}State")
            .addReplacement("action_name", "${feature.featureName}Action")
            .addReplacement("feature_name", feature.featureName)
            .addReplacement("state_properties", stateProperties)
            .addReplacement("repository_name", "${feature.featureName}Repository")
            .addReplacement("viewmodel_name", "${feature.featureName}ViewModel")
            .addReplacement("repository_method", repositoryMethod)
            .addReplacement("action_loaded", actionLoaded)
            .addReplacement("action_failed", actionFailed)
            .addReplacement("action_loading", actionLoading)
            .process(ScreenTemplate.TEMPLATE)

        val path = "presentation/screen/${feature.featureName}Screen.kt"
        // Skip if file already exists
        if (File(getFullFilePath(path)).exists()) {
            println("Skipping generation of existing file: $path")
            return
        } else {
            writeFile(path, content)
        }
        templateProcessor.clear()
    }

    private fun generateViewModel() {
        // Find the first API endpoint to use for repository method
        val firstEndpoint = feature.apiEndpoints.firstOrNull()
        val repositoryMethod = firstEndpoint?.name ?: ""
        val actionLoaded = feature.uiActions.find { it.name.endsWith("Loaded") }?.name ?: ""
        val actionFailed = feature.uiActions.find { it.name.endsWith("Failed") }?.name ?: ""
        val actionLoading = feature.uiActions.find { it.name.startsWith("Load") }?.name ?: ""

        val content = templateProcessor
            .addReplacement("package_name", feature.packageName)
            .addReplacement("viewmodel_name", "${feature.featureName}ViewModel")
            .addReplacement("state_name", "${feature.featureName}State")
            .addReplacement("action_name", "${feature.featureName}Action")
            .addReplacement("repository_name", "${feature.featureName}Repository")
            .addReplacement("repository_method", repositoryMethod)
            .addReplacement("action_loaded", actionLoaded)
            .addReplacement("action_failed", actionFailed)
            .addReplacement("action_loading", actionLoading)
            .process(ViewModelTemplate.TEMPLATE)

        val path = "presentation/screen/${feature.featureName}ViewModel.kt"
        // Skip if file already exists
        if (File(getFullFilePath(path)).exists()) {
            println("Skipping generation of existing file: $path")
            return
        } else {
            writeFile(path, content)
        }
        
        templateProcessor.clear()
    }

    private fun generateJourney() {
        val content = templateProcessor
            .addReplacement("package_name", feature.packageName)
            .addReplacement("journey_name", "${feature.featureName}Journey")
            .addReplacement("state_name", "${feature.featureName}State")
            .addReplacement("action_name", "${feature.featureName}Action")
            .process(JourneyTemplate.TEMPLATE)

        writeFile("${feature.featureName}Journey.kt", content)
        templateProcessor.clear()
    }

    private fun generateDocumentation() {
        val content = buildString {
            appendLine("# ${feature.featureName} Feature Documentation")
            appendLine()
            
            // Feature Overview
            appendLine("## Overview")
            appendLine("This document describes the ${feature.featureName} feature implementation details.")
            appendLine()
            
            // Domain Models
            appendLine("## Domain Models")
            feature.domainModels.forEach { model ->
                appendLine("### ${model.name}")
                appendLine("Properties:")
                model.properties.forEach { property ->
                    appendLine("- `${property.name}`: ${property.type}")
                }
                appendLine()
            }
            
            // UI States
            appendLine("## UI States")
            feature.uiStates.forEach { state ->
                appendLine("### ${state.name}")
                appendLine("Properties:")
                state.properties.forEach { property ->
                    val defaultValue = when {
                        property.type.endsWith("?") -> " (nullable)"
                        property.type == "Boolean" -> " (default: false)"
                        else -> ""
                    }
                    appendLine("- `${property.name}`: ${property.type}$defaultValue")
                }
                appendLine()
            }
            
            // UI Actions
            appendLine("## UI Actions")
            feature.uiActions.forEach { action ->
                appendLine("### ${action.name}")
                if (action.properties.isNotEmpty()) {
                    appendLine("Properties:")
                    action.properties.forEach { property ->
                        appendLine("- `${property.name}`: ${property.type}")
                    }
                } else {
                    appendLine("No properties")
                }
                appendLine()
            }
            
            // API Endpoints
            appendLine("## API Endpoints")
            feature.apiEndpoints.forEach { endpoint ->
                appendLine("### ${endpoint.name}")
                appendLine("- Path: `${endpoint.path}`")
                if (endpoint.responseModel != null) {
                    appendLine("- Response Type: `${endpoint.responseModel}`")
                }
                appendLine()
            }
            
            // Generated Files
            appendLine("## Generated Files")
            appendLine("The following files are generated for this feature:")
            appendLine("- `domain/model/*.kt`: Domain model classes")
            appendLine("- `domain/${feature.featureName}Repository.kt`: Repository interface and implementation")
            appendLine("- `presentation/state/${feature.featureName}State.kt`: UI state class")
            appendLine("- `presentation/action/${feature.featureName}Action.kt`: UI action classes")
            appendLine("- `presentation/reducer/${feature.featureName}Reducer.kt`: State reducer")
            appendLine("- `presentation/screen/${feature.featureName}Screen.kt`: UI screen")
            appendLine("- `navigation/${feature.featureName}Navigation.kt`: Navigation setup")
            appendLine("- `di/${feature.featureName}DI.kt`: Dependency injection setup")
            appendLine("- `presentation/screen/${feature.featureName}ViewModel.kt`: ViewModel")
            appendLine("- `${feature.featureName}Journey.kt`: Feature journey")
            appendLine()
            
            // State Management
            appendLine("## State Management")
            appendLine("The feature uses a unidirectional data flow pattern:")
            appendLine("1. UI triggers actions")
            appendLine("2. Actions are processed by the reducer")
            appendLine("3. State updates trigger UI updates")
            appendLine()
            
            // Navigation
            appendLine("## Navigation")
            appendLine("The feature is accessible via the route: `${feature.packageName}`")
            appendLine()
            
            // Dependencies
            appendLine("## Dependencies")
            appendLine("The feature depends on:")
            appendLine("- Network client for API calls")
            appendLine("- Dependency injection framework")
            appendLine("- Navigation framework")
        }

        val path = "README.md"
        if (File(getFullFilePath(path)).exists()) {
            println("Skipping generation of existing file: $path")
            return
        } else {
            writeFile(path, content)
        }
        writeFile(path, content)
        templateProcessor.clear()
    }

    private fun writeFile(path: String, content: String) {
        val file = File(getFullFilePath(path))
        file.parentFile.mkdirs()
        file.writeText(content)
        println("Generated file: ${file.name}")
    }

    private fun getFullFilePath(path: String) = "./app/src/main/java/com/example/${feature.packageName}/$path"
} 