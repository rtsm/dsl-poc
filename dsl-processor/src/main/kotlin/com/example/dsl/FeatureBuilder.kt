package com.example.dsl

class FeatureBuilder {
    var packageName: String = ""
    var featureName: String = ""
    val domainModels = mutableListOf<DomainModel>()
    val dataSources = mutableListOf<DataSource>()
    val uiStates = mutableListOf<UIState>()
    val uiActions = mutableListOf<UIAction>()

    fun build(): Feature {
        return Feature(
            packageName = packageName,
            featureName = featureName,
            domainModels = domainModels,
            dataSources = dataSources,
            uiStates = uiStates,
            uiActions = uiActions
        )
    }
}

class DomainModelBuilder {
    var name: String = ""
    private val properties = mutableListOf<Property>()

    fun property(name: String, type: String) {
        properties.add(Property(name, type))
    }

    fun build(): DomainModel {
        return DomainModel(name, properties)
    }
}

class UIStateBuilder {
    var name: String = ""
    private val properties = mutableListOf<Property>()

    fun property(name: String, type: String) {
        properties.add(Property(name, type))
    }

    fun build(): UIState {
        return UIState(name, properties)
    }
}

class UIActionBuilder {
    var name: String = ""
    private val properties = mutableListOf<Property>()

    fun property(name: String, type: String) {
        properties.add(Property(name, type))
    }

    fun build(): UIAction {
        return UIAction(name, properties)
    }
}

class DataSourceBuilder {
    private val dataSources = mutableListOf<DataSource>()

    fun networkCall(path: String, responseModel: String, vararg transformations: String) {
        dataSources.add(DataSource.NetworkCall(path, responseModel, transformations.toList()))
    }

    fun preference(key: String, type: String) {
        dataSources.add(DataSource.Preference(key, type))
    }

    fun localDM(repository: String, method: String, responseModel: String) {
        dataSources.add(DataSource.LocalDM(repository, method, responseModel))
    }

    fun localStorage(path: String) {
        dataSources.add(DataSource.LocalStorage(path))
    }

    fun build(): List<DataSource> = dataSources
}

