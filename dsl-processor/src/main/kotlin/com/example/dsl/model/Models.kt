package com.example.dsl.model

data class Property(
    val name: String,
    val type: String
)

data class DomainModel(
    val name: String,
    val properties: List<Property>
)

data class ApiEndpoint(
    val name: String,
    val path: String,
    val method: String,
    val requestModel: String?,
    val responseModel: String
)

data class UIState(
    val name: String,
    val properties: List<Property>
)

data class UIAction(
    val name: String,
    val properties: List<Property>
) 