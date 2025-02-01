plugins {
    java
    antlr
    scala
}

group = "com.github.tammo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr", "antlr4", "4.9.3")

    implementation("org.ow2.asm:asm:9.7.1")
    implementation("org.scala-lang:scala3-library_3:3.6.2")

    testImplementation("org.scalatest:scalatest_3:3.2.19")
    testImplementation("org.scalatestplus:junit-5-11_3:3.2.19.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.test {
    useJUnitPlatform {
        includeEngines("scalatest")
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

tasks.generateGrammarSource {
    arguments.addAll(listOf("-visitor", "-package", "com.github.tammo"))

    outputDirectory = file("${project.buildDir}/generated-src/antlr/main/com/github/tammo")
}