plugins {
    java
    antlr
    idea
}

group = "com.github.tammo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr", "antlr4", "4.9.3")

    implementation("org.ow2.asm:asm:9.7.1")
}

tasks.generateGrammarSource {
    arguments.addAll(listOf("-visitor", "-package", "com.github.tammo"))

    outputDirectory = file("${project.buildDir}/generated-src/antlr/main/com/github/tammo")
}