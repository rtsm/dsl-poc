package com.example.dsl

data class Feature(
    val packageName: String,
    val featureName: String,
    val domainModels: List<DomainModel> = emptyList(),
    val dataSources: List<DataSource> = emptyList(),
    val uiStates: List<UIState> = emptyList(),
    val uiActions: List<UIAction> = emptyList()
)

data class DomainModel(
    val name: String,
    val properties: List<Property>
)

data class Property(
    val name: String,
    val type: String
)

sealed class DataSource {
    data class NetworkCall(
        val path: String,
        val responseModel: String,
        val transformations: List<String> = emptyList()
    ) : DataSource()

    data class Preference(
        val key: String,
        val type: String
    ) : DataSource()

    data class LocalDM(
        val repository: String,
        val method: String,
        val responseModel: String
    ) : DataSource()

    data class LocalStorage(
        val path: String
    ) : DataSource()
}

data class UIState(
    val name: String,
    val properties: List<Property>
)

data class UIAction(
    val name: String,
    val properties: List<Property> = emptyList()
)