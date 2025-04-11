package com.example.dsl

import com.example.dsl.template.*
import java.io.File
import java.util.*

class CodeGenerator(private val feature: Feature) {
    private val templateProcessor = TemplateProcessor()
    private val codeScanner = CodeScanner()

    init {
        // Initialize the code scanner
        codeScanner.scan()
    }

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
                "    val ${property.name.removeSurrounding("\"")}: ${property.type.removeSurrounding("\"")}"
            }

            val content = templateProcessor
                .addReplacement("package_name", feature.packageName)
                .addReplacement("class_name", model.name.removeSurrounding("\""))
                .addReplacement("properties", properties)
                .process(DomainModelTemplate.TEMPLATE)

            val modelName = model.name.removeSurrounding("\"")
            writeFile("domain/model/$modelName.kt", content)
            templateProcessor.clear()
        }
    }

    private fun generateRepository() {
        // Collect all response models that need imports
        val responseModels = feature.dataSources.mapNotNull { dataSource ->
            when (dataSource) {
                is DataSource.NetworkCall -> dataSource.responseModel.removeSurrounding("\"")
                is DataSource.LocalDM -> dataSource.responseModel.removeSurrounding("\"")
                else -> null
            }
        }.distinct()

        // Extract base types from generic types and collect all model types
        val allModelTypes = mutableSetOf<String>()
        extractTypeFromGenerics(responseModels, allModelTypes)

        // Get repository names from LocalDM data sources
        val repositoryNames = feature.dataSources
            .filterIsInstance<DataSource.LocalDM>()
            .map { it.repository.removeSurrounding("\"") }
            .distinct()

        // Generate imports for models and repositories
        val modelImports = mutableListOf<String>()
        
        // Add imports for current feature's domain models
        feature.domainModels.forEach { model ->
            val modelName = model.name.removeSurrounding("\"")
            modelImports.add("import com.example.${feature.packageName}.domain.model.$modelName")
        }
        
        // Add imports for models from other features
        allModelTypes.forEach { modelType ->
            if (!feature.domainModels.any { it.name.removeSurrounding("\"") == modelType }) {
                val import = codeScanner.getModelImports(listOf(modelType)).firstOrNull()
                if (import != null) {
                    modelImports.add(import)
                }
            }
        }
        
        // Add repository imports using repositoryPackages
        val repositoryImports = repositoryNames.mapNotNull { repositoryName ->
            codeScanner.findRepositoryPackage(repositoryName)?.let { packageName ->
                "import $packageName.$repositoryName"
            }
        }
        
        // Combine all imports
        val allImports = (modelImports + repositoryImports).distinct().joinToString("\n")

        // Generate repository methods
        val repositoryMethods = feature.dataSources.joinToString("\n") { dataSource ->
            val methodName = when (dataSource) {
                is DataSource.NetworkCall -> dataSource.path.split("/").last()
                is DataSource.Preference -> "get${dataSource.key.split("_").joinToString("") {
                    it.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                }}"
                is DataSource.LocalDM -> dataSource.method.removeSurrounding("\"")
                is DataSource.LocalStorage -> dataSource.path.split("/").last()
            }
            
            val returnType = when (dataSource) {
                is DataSource.NetworkCall -> dataSource.responseModel.removeSurrounding("\"")
                is DataSource.Preference -> dataSource.type.removeSurrounding("\"")
                is DataSource.LocalDM -> dataSource.responseModel.removeSurrounding("\"")
                is DataSource.LocalStorage -> "ByteArray"
            }
            
            "    fun $methodName(): Flow<$returnType>"
        }

        // Generate deserialize methods
        val deserializeMethods = responseModels.joinToString("\n\n") { model ->
            val isGeneric = model.startsWith("List<") || model.startsWith("Map<")
            if (isGeneric) {
                val baseType = when {
                    model.startsWith("List<") -> model.removeSurrounding("List<", ">")
                    model.startsWith("Map<") -> model.removeSurrounding("Map<", ">").split(",").first().trim()
                    else -> model
                }
                """    @OptIn(ExperimentalStdlibApi::class)
    private fun deserialize${baseType}List(responseBody: String): $model {
        return gson.fromJson(responseBody, typeOf<$model>().javaType)
    }"""
            } else {
                """    private fun deserialize$model(responseBody: String): $model {
        return gson.fromJson(responseBody, $model::class.java)
    }"""
            }
        }

        // Generate repository implementation methods
        val repositoryImplMethods = feature.dataSources.joinToString("\n\n") { dataSource ->
            val methodName = when (dataSource) {
                is DataSource.NetworkCall -> dataSource.path.split("/").last()
                is DataSource.Preference -> "get${dataSource.key.split("_").joinToString("") {
                    it.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                }}"
                is DataSource.LocalDM -> dataSource.method.removeSurrounding("\"")
                is DataSource.LocalStorage -> dataSource.path.split("/").last()
            }
            
            val implementation = when (dataSource) {
                is DataSource.NetworkCall -> 
                    "return networkClient.request(\"${dataSource.path}\").map { deserialize${dataSource.responseModel.removeSurrounding("\"")}(it) }"
                
                is DataSource.Preference -> {
                    val defaultValue = when (dataSource.type.removeSurrounding("\"")) {
                        "String" -> "\"\""
                        "Int" -> "0"
                        "Boolean" -> "false"
                        "Long" -> "0L"
                        "Float" -> "0f"
                        else -> "null"
                    }
                    "return flow { emit(preferenceClient.getPreference(\"${dataSource.key}\", $defaultValue)) }"
                }
                
                is DataSource.LocalDM -> 
                    "return (inject<${dataSource.repository.removeSurrounding("\"")}>()).value.${dataSource.method.removeSurrounding("\"")}()"
                
                is DataSource.LocalStorage -> 
                    """return flow {
                val file = File("${dataSource.path}")
                if (file.exists()) {
                    emit(file.readBytes())
                } else {
                    throw IllegalStateException("File not found: ${'$'}{file.path}")
                }
            }"""
            }
            
            val modelName = when (dataSource) {
                is DataSource.NetworkCall -> dataSource.responseModel.removeSurrounding("\"")
                is DataSource.Preference -> dataSource.type.removeSurrounding("\"")
                is DataSource.LocalDM -> dataSource.responseModel.removeSurrounding("\"")
                is DataSource.LocalStorage -> "ByteArray"
            }
            
            templateProcessor
                .addReplacement("repository_method", methodName)
                .addReplacement("model_name", modelName)
                .addReplacement("implementation", implementation)
                .process(RepositoryImplTemplate.TEMPLATE)
        }

        val content = templateProcessor
            .addReplacement("package_name", feature.packageName)
            .addReplacement("repository_name", "${feature.featureName}Repository")
            .addReplacement("repository_impl_name", "${feature.featureName}RepositoryImpl")
            .addReplacement("repository_methods", repositoryMethods)
            .addReplacement("repository_impl_methods", "$repositoryImplMethods\n\n$deserializeMethods")
            .addReplacement("model_imports", allImports)
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

    private fun extractTypeFromGenerics(
        responseModels: List<String>,
        allModelTypes: MutableSet<String>
    ) {
        responseModels.map{ it.removeSuffix("?")}.forEach { model ->
            when {
                model.startsWith("List<") -> {
                    val baseType = model.removeSurrounding("List<", ">")
                    allModelTypes.add(baseType)
                }

                model.startsWith("Map<") -> {
                    val types = model.removeSurrounding("Map<", ">").split(",")
                    types.forEach { allModelTypes.add(it.trim()) }
                }

                else -> allModelTypes.add(model)
            }
        }
    }

    private fun generateUIState() {
        // Add error field to properties
        val properties = (feature.uiStates.flatMap { it.properties } + Property("error", "String?"))
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

        // Generate imports for current feature's domain models
        val currentFeatureImports = domainModelTypes.joinToString("\n") { type ->
            "import com.example.${feature.packageName}.domain.model.${type.removeSuffix("?")}"
        }

        // Collect model types from other features
        val otherFeatureModelTypes = feature.uiStates.flatMap { it.properties }
            .map { it.type }
            .filter { type -> 
                !feature.domainModels.any { it.name == type.removeSuffix("?") }
            }
            .distinct()

        val allModelTypes = mutableSetOf<String>()
        extractTypeFromGenerics(otherFeatureModelTypes, allModelTypes)

        // Generate imports for models from other features using modelPackages
        val otherFeatureImports = codeScanner.getModelImports(allModelTypes.toList()).joinToString("\n")

        // Combine all imports
        val allImports = if (otherFeatureImports.isNotEmpty()) {
            "$currentFeatureImports\n$otherFeatureImports"
        } else {
            currentFeatureImports
            }

        val content = templateProcessor
            .addReplacement("package_name", feature.packageName)
            .addReplacement("state_name", "${feature.featureName}State")
            .addReplacement("properties", properties)
            .addReplacement("domain_model_imports", allImports)
            .process(UIStateTemplate.TEMPLATE)

        writeFile("presentation/state/${feature.featureName}State.kt", content)
        templateProcessor.clear()
    }

    private fun generateUIActions() {
        val actionClasses = feature.uiActions.joinToString("\n\n") { action ->
            val properties = when {
                action.name.startsWith("Load") && !action.name.endsWith("Loaded") && !action.name.endsWith("Failed") -> {
                    // LoadProfile should be a data object with no properties
                    ""
                }
                action.name.endsWith("Loaded") -> {
                    // ProfileLoaded should only have the loaded data
                    action.properties.filter { it.name != "error" }
                        .joinToString(",\n") { property ->
                            "        val ${property.name}: ${property.type}"
                        }
                }
                action.name.endsWith("Failed") -> {
                    // ProfileLoadFailed should only have error
                    "        val error: String"
                }
                else -> {
                    action.properties.joinToString(",\n") { property ->
                "        val ${property.name}: ${property.type}"
                    }
                }
            }

            if (properties.isEmpty()) {
                """
    data object ${action.name} : ${feature.featureName}Action()"""
            } else {
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
                action.name.startsWith("Load") && !action.name.endsWith("Loaded") && !action.name.endsWith("Failed") -> "isLoading = true"
                action.name.endsWith("Loaded") -> "isLoading = false"
                action.name.endsWith("Failed") -> "isLoading = false, error = action.error"
                else -> ""
            }
            
            // Match action properties to state properties by type
            val propertyMappings = when {
                action.name.endsWith("Loaded") -> {
                    // For Loaded actions, only map the loaded data and exclude error
                    action.properties.filter { it.name != "error" }
                        .map { actionProperty ->
                            "${actionProperty.name} = action.${actionProperty.name}"
                        }
                }
                else -> emptyList() // For other actions, don't map properties
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
        val firstEndpoint = feature.dataSources.firstOrNull()
        val repositoryMethod = firstEndpoint?.let { dataSource ->
            when (dataSource) {
                is DataSource.NetworkCall -> dataSource.path.split("/").last()
                is DataSource.Preference -> "get${
                    dataSource.key.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                }"
                is DataSource.LocalDM -> dataSource.method
                is DataSource.LocalStorage -> dataSource.path.split("/").last()
            }
        } ?: ""
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
        val firstEndpoint = feature.dataSources.firstOrNull()
        val repositoryMethod = firstEndpoint?.let { dataSource ->
            when (dataSource) {
                is DataSource.NetworkCall -> dataSource.path.split("/").last()
                is DataSource.Preference -> "get${
                    dataSource.key.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                }"
                is DataSource.LocalDM -> dataSource.method
                is DataSource.LocalStorage -> dataSource.path.split("/").last()
            }
        } ?: ""
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
            
            // Data Sources
            appendLine("## Data Sources")
            feature.dataSources.forEach { dataSource ->
                when (dataSource) {
                    is DataSource.NetworkCall -> {
                        appendLine("### Network Call: ${dataSource.path.split("/").last()}")
                        appendLine("- Type: Network API Call")
                        appendLine("- Path: `${dataSource.path}`")
                        appendLine("- Response Type: `${dataSource.responseModel}`")
                        if (dataSource.transformations.isNotEmpty()) {
                            appendLine("- Transformations: ${dataSource.transformations.joinToString(", ")}")
                        }
                    }
                    is DataSource.Preference -> {
                        appendLine("### Preference: ${dataSource.key}")
                        appendLine("- Type: Shared Preference")
                        appendLine("- Key: `${dataSource.key}`")
                        appendLine("- Value Type: `${dataSource.type}`")
                    }
                    is DataSource.LocalDM -> {
                        appendLine("### Local DM: ${dataSource.method}")
                        appendLine("- Type: Local DM Source")
                        appendLine("- Repository: `${dataSource.repository}`")
                        appendLine("- Method: `${dataSource.method}`")
                        appendLine("- Response Type: `${dataSource.responseModel}`")
                    }
                    is DataSource.LocalStorage -> {
                        appendLine("### Local Storage: ${dataSource.path.split("/").last()}")
                        appendLine("- Type: File Storage")
                        appendLine("- Path: `${dataSource.path}`")
                        appendLine("- Response Type: `ByteArray`")
                    }
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
            appendLine("- Preference client for shared preferences")
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