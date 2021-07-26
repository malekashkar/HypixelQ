plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "com.bedwarsq"
version = "1.0-SNAPSHOT"

val logbackVersion = "1.2.3"
val kmongoVersion = "4.2.4"
val jdaVersion = "4.3.0_295"
val jdaKtxVersion = "985db81"
val kotlinxCoroutinesVersion = "1.5.0"
val kotlinxSerializationJsonVersion = "1.1.0"
val ktorVersion = "1.6.0"
val snakeYamlVersion = "1.28"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("com.github.shyiko.skedule:skedule:0.4.0")

    implementation("com.github.minndevelopment:jda-ktx:${jdaKtxVersion}")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
    }

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")

    implementation("org.litote.kmongo:kmongo:$kmongoVersion")
    implementation("org.litote.kmongo:kmongo-id-serialization:$kmongoVersion")
    implementation("org.litote.kmongo:kmongo-coroutine-serialization:$kmongoVersion")

    implementation("org.yaml:snakeyaml:$snakeYamlVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-java:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }
}