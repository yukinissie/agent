plugins {
    kotlin("jvm") version "2.2.0"
    id("org.gauge") version "2.3.0"
}

group = "com.yukinissie"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.thoughtworks.gauge:gauge-java:0.12.0")
    implementation(kotlin("stdlib"))
    testImplementation("org.assertj:assertj-core:3.27.3")
}