package com.example.dsl.template

class TemplateProcessor {
    private val replacements = mutableMapOf<String, String>()

    fun addReplacement(key: String, value: String): TemplateProcessor {
        replacements[key] = value
        return this
    }

    fun process(template: String): String {
        var result = template
        replacements.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }
        return result
    }

    fun clear() {
        replacements.clear()
    }
} 