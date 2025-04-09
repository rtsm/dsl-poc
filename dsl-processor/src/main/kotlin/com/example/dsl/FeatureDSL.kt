package com.example.dsl

import com.example.dsl.model.*

class FeatureBuilder {
    var packageName: String = ""
    var featureName: String = ""
    var domainModels: MutableList<DomainModel> = mutableListOf()
    var apiEndpoints: MutableList<ApiEndpoint> = mutableListOf()
    var uiStates: MutableList<UIState> = mutableListOf()
    var uiActions: MutableList<UIAction> = mutableListOf()

    fun domainModel(init: DomainModelBuilder.() -> Unit) {
        val builder = DomainModelBuilder()
        builder.init()
        domainModels.add(builder.build())
    }

    fun apiEndpoint(init: ApiEndpointBuilder.() -> Unit) {
        val builder = ApiEndpointBuilder()
        builder.init()
        apiEndpoints.add(builder.build())
    }

    fun uiState(init: UIStateBuilder.() -> Unit) {
        val builder = UIStateBuilder()
        builder.init()
        uiStates.add(builder.build())
    }

    fun uiAction(init: UIActionBuilder.() -> Unit) {
        val builder = UIActionBuilder()
        builder.init()
        uiActions.add(builder.build())
    }
}

class DomainModelBuilder {
    var name: String = ""
    var properties: MutableList<Property> = mutableListOf()

    fun property(name: String, type: String) {
        properties.add(Property(name, type))
    }

    fun build(): DomainModel = DomainModel(name, properties)
}

class ApiEndpointBuilder {
    var name: String = ""
    var path: String = ""
    var method: String = "GET"
    var requestModel: String = ""
    var responseModel: String = ""

    fun build(): ApiEndpoint = ApiEndpoint(name, path, method, requestModel, responseModel)
}

class UIStateBuilder {
    var name: String = ""
    var properties: MutableList<Property> = mutableListOf()

    fun property(name: String, type: String) {
        properties.add(Property(name, type))
    }

    fun build(): UIState = UIState(name, properties)
}

class UIActionBuilder {
    var name: String = ""
    var properties: MutableList<Property> = mutableListOf()

    fun property(name: String, type: String) {
        properties.add(Property(name, type))
    }

    fun build(): UIAction = UIAction(name, properties)
}

fun feature(init: FeatureBuilder.() -> Unit): FeatureBuilder {
    val builder = FeatureBuilder()
    builder.init()
    return builder
} 