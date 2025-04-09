package com.example.dsl

import com.example.dsl.model.*
import java.io.File

class DSLParser {
    fun parse(file: File): FeatureBuilder {
        val content = file.readText()
        val featureBuilder = FeatureBuilder()
        
        // Simple parsing logic - in a real implementation, you would use a proper parser
        val lines = content.lines()
        var currentBlock: String? = null
        var currentModel: DomainModelBuilder? = null
        var currentEndpoint: ApiEndpointBuilder? = null
        var currentState: UIStateBuilder? = null
        var currentAction: UIActionBuilder? = null
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            when {
                trimmedLine.startsWith("feature {") -> {
                    // Start of feature block
                }
                trimmedLine.startsWith("packageName =") -> {
                    featureBuilder.packageName = trimmedLine.substringAfter("=").trim().removeSurrounding("\"")
                }
                trimmedLine.startsWith("featureName =") -> {
                    featureBuilder.featureName = trimmedLine.substringAfter("=").trim().removeSurrounding("\"")
                }
                trimmedLine.startsWith("domainModel {") -> {
                    currentBlock = "domainModel"
                    currentModel = DomainModelBuilder()
                }
                trimmedLine.startsWith("name =") && currentBlock == "domainModel" -> {
                    currentModel?.name = trimmedLine.substringAfter("=").trim().removeSurrounding("\"")
                }
                trimmedLine.startsWith("property(") && currentBlock == "domainModel" -> {
                    val propertyParts = trimmedLine.substringAfter("property(").substringBefore(")").split(",")
                    val name = propertyParts[0].trim().removeSurrounding("\"")
                    val type = propertyParts[1].trim().removeSurrounding("\"")
                    currentModel?.property(name, type)
                }
                trimmedLine.startsWith("}") && currentBlock == "domainModel" -> {
                    currentModel?.let { featureBuilder.domainModels.add(it.build()) }
                    currentBlock = null
                    currentModel = null
                }
                trimmedLine.startsWith("apiEndpoint {") -> {
                    currentBlock = "apiEndpoint"
                    currentEndpoint = ApiEndpointBuilder()
                }
                trimmedLine.startsWith("name =") && currentBlock == "apiEndpoint" -> {
                    currentEndpoint?.name = trimmedLine.substringAfter("=").trim().removeSurrounding("\"")
                }
                trimmedLine.startsWith("path =") && currentBlock == "apiEndpoint" -> {
                    currentEndpoint?.path = trimmedLine.substringAfter("=").trim().removeSurrounding("\"")
                }
                trimmedLine.startsWith("method =") && currentBlock == "apiEndpoint" -> {
                    currentEndpoint?.method = trimmedLine.substringAfter("=").trim().removeSurrounding("\"")
                }
                trimmedLine.startsWith("responseModel =") && currentBlock == "apiEndpoint" -> {
                    currentEndpoint?.responseModel = trimmedLine.substringAfter("=").trim().removeSurrounding("\"")
                }
                trimmedLine.startsWith("}") && currentBlock == "apiEndpoint" -> {
                    currentEndpoint?.let { featureBuilder.apiEndpoints.add(it.build()) }
                    currentBlock = null
                    currentEndpoint = null
                }
                trimmedLine.startsWith("uiState {") -> {
                    currentBlock = "uiState"
                    currentState = UIStateBuilder()
                }
                trimmedLine.startsWith("name =") && currentBlock == "uiState" -> {
                    currentState?.name = trimmedLine.substringAfter("=").trim().removeSurrounding("\"")
                }
                trimmedLine.startsWith("property(") && currentBlock == "uiState" -> {
                    val propertyParts = trimmedLine.substringAfter("property(").substringBefore(")").split(",")
                    val name = propertyParts[0].trim().removeSurrounding("\"")
                    val type = propertyParts[1].trim().removeSurrounding("\"")
                    currentState?.property(name, type)
                }
                trimmedLine.startsWith("}") && currentBlock == "uiState" -> {
                    currentState?.let { featureBuilder.uiStates.add(it.build()) }
                    currentBlock = null
                    currentState = null
                }
                trimmedLine.startsWith("uiAction {") -> {
                    currentBlock = "uiAction"
                    currentAction = UIActionBuilder()
                }
                trimmedLine.startsWith("name =") && currentBlock == "uiAction" -> {
                    currentAction?.name = trimmedLine.substringAfter("=").trim().removeSurrounding("\"")
                }
                trimmedLine.startsWith("property(") && currentBlock == "uiAction" -> {
                    val propertyParts = trimmedLine.substringAfter("property(").substringBefore(")").split(",")
                    val name = propertyParts[0].trim().removeSurrounding("\"")
                    val type = propertyParts[1].trim().removeSurrounding("\"")
                    currentAction?.property(name, type)
                }
                trimmedLine.startsWith("}") && currentBlock == "uiAction" -> {
                    currentAction?.let { featureBuilder.uiActions.add(it.build()) }
                    currentBlock = null
                    currentAction = null
                }
            }
        }
        
        return featureBuilder
    }
} 