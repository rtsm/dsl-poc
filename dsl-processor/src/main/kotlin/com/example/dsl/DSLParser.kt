package com.example.dsl

import java.io.File
import java.util.*

class DSLParser {
    private val featureBuilder = FeatureBuilder()
    private val domainModelBuilder = DomainModelBuilder()
    private val uiStateBuilder = UIStateBuilder()
    private val uiActionBuilder = UIActionBuilder()
    private val dataSourceBuilder = DataSourceBuilder()

    fun parse(file: File): Feature {
        val content = file.readText()
        val lines = content.lines()
        var currentBuilder: Any? = null

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            when {
                trimmedLine.startsWith("feature") -> {
                    val name = trimmedLine.substringAfter("feature").trim()
                    featureBuilder.featureName = name
                    featureBuilder.packageName = name.lowercase(Locale.getDefault())
                }
                trimmedLine == "domainModel {" -> currentBuilder = domainModelBuilder
                trimmedLine == "uiState {" -> currentBuilder = uiStateBuilder
                trimmedLine == "uiAction {" -> currentBuilder = uiActionBuilder
                trimmedLine == "dataSources {" -> currentBuilder = dataSourceBuilder
                trimmedLine == "}" -> {
                    when (currentBuilder) {
                        is DomainModelBuilder -> {
                            featureBuilder.domainModels.add(domainModelBuilder.build())
                            currentBuilder = null
                        }
                        is UIStateBuilder -> {
                            featureBuilder.uiStates.add(uiStateBuilder.build())
                            currentBuilder = null
                        }
                        is UIActionBuilder -> {
                            featureBuilder.uiActions.add(uiActionBuilder.build())
                            currentBuilder = null
                        }
                        is DataSourceBuilder -> {
                            featureBuilder.dataSources.addAll(dataSourceBuilder.build())
                            currentBuilder = null
                        }
                    }
                }
                currentBuilder is DomainModelBuilder -> {
                    if (trimmedLine.contains("property")) {
                        val parts = trimmedLine.substringAfter("property").trim().split(":")
                        val name = parts[0].trim().removeSurrounding("\"")
                        val type = parts[1].trim().removeSurrounding("\"")
                        domainModelBuilder.property(name, type)
                    } else if (trimmedLine.contains("name")) {
                        domainModelBuilder.name = trimmedLine.substringAfter("name").trim().removeSurrounding("\"")
                    }
                }
                currentBuilder is UIStateBuilder -> {
                    if (trimmedLine.contains("name")) {
                        uiStateBuilder.name = trimmedLine.substringAfter("name").trim().removeSurrounding("\"")
                    } else if (trimmedLine.contains("property")) {
                        val parts = trimmedLine.substringAfter("property").trim().split(":")
                        val name = parts[0].trim().removeSurrounding("\"")
                        val type = parts[1].trim().removeSurrounding("\"")
                        uiStateBuilder.property(name, type)
                    }
                }
                currentBuilder is UIActionBuilder -> {
                    if (trimmedLine.contains("name")) {
                        uiActionBuilder.name = trimmedLine.substringAfter("name").trim().removeSurrounding("\"")
                    } else if (trimmedLine.contains("property")) {
                        val parts = trimmedLine.substringAfter("property").trim().split(":")
                        val name = parts[0].trim().removeSurrounding("\"")
                        val type = parts[1].trim().removeSurrounding("\"")
                        uiActionBuilder.property(name, type)
                    }
                }
                currentBuilder is DataSourceBuilder -> {
                    when {
                        trimmedLine.startsWith("networkCall") -> {
                            val parts = trimmedLine.substringAfter("networkCall").trim().split(",")
                            val path = parts[0].trim().removeSurrounding("\"")
                            val responseModel = parts[1].trim()
                            val transformations = parts.drop(2).map { it.trim() }.toTypedArray()
                            dataSourceBuilder.networkCall(path, responseModel, *transformations)
                        }
                        trimmedLine.startsWith("preference") -> {
                            val parts = trimmedLine.substringAfter("preference").trim().split(",")
                            val key = parts[0].trim().removeSurrounding("\"")
                            val type = parts[1].trim()
                            dataSourceBuilder.preference(key, type)
                        }
                        trimmedLine.startsWith("localDM") -> {
                            val parts = trimmedLine.substringAfter("localDM").trim().split(",")
                            val repository = parts[0].trim()
                            val method = parts[1].trim()
                            val responseModel = parts[2].trim()
                            dataSourceBuilder.localDM(repository, method, responseModel)
                        }
                        trimmedLine.startsWith("localStorage") -> {
                            val path = trimmedLine.substringAfter("localStorage").trim().removeSurrounding("\"")
                            dataSourceBuilder.localStorage(path)
                        }
                    }
                }
            }
        }

        return featureBuilder.build()
    }
} 