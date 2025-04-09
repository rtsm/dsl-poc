package com.example.dsl

import java.io.File

fun main() {
    // Load the DSL file
    val dslFile = File("src/main/resources/example.dsl")
    val parser = DSLParser()
    val feature = parser.parse(dslFile)

    // Generate the code
    CodeGenerator(feature).generate()
} 