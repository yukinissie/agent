plugins {
    kotlin("jvm") version "2.2.0"
    id("org.gauge") version "1.8.2"
    id("java")
}

group="com.yukinissie"
version="1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    test {
        resources {
            srcDir("fixtures")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.thoughtworks.gauge:gauge-java:0.12.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
    implementation("com.uzabase.playtest2:playtest2:0.0.9")
    implementation("com.uzabase.playtest2:playtest-http:0.0.9")
    testImplementation("org.assertj:assertj-core:3.27.4")
}

tasks.register<Copy>("copyClassesToGaugeBin") {
    dependsOn("compileTestKotlin")
    from(tasks.compileTestKotlin.get().destinationDirectory)
    into("$projectDir/gauge_bin")
}

tasks.register<Copy>("copyDependenciesToGaugeBin") {
    from(configurations.runtimeClasspath)
    into("$projectDir/gauge_bin/libs")
}

tasks.register("prepareGauge") {
    dependsOn("copyClassesToGaugeBin", "copyDependenciesToGaugeBin")
    description = "Prepare Gauge environment with compiled classes and dependencies"
}
