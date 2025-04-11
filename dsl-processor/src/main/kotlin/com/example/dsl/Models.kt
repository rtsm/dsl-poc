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