import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    id("org.springframework.boot.experimental.thin-launcher") version "1.0.31.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
}

version = "0.0.1"
extra["springCloudVersion"] = "2022.0.4"
val grpcSBVersion = "3.0.0.RELEASE"

dependencies {
    implementation(project(":grpc-stubs"))
    implementation(kotlin("reflect"))
    implementation("org.springframework.cloud:spring-cloud-starter-function-web")
    implementation("org.springframework.cloud:spring-cloud-function-context")
    implementation("org.springframework.cloud:spring-cloud-function-adapter-aws")
    implementation("org.springframework.cloud:spring-cloud-function-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.0")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.3")
    implementation("net.devh:grpc-client-spring-boot-starter:$grpcSBVersion")
    implementation("io.grpc:grpc-services:1.62.2") // health
    implementation("org.reflections:reflections:0.10.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("build") {
    dependsOn("thinJar", "shadowJar")
}

tasks.shadowJar {
    mustRunAfter(tasks.named("thinJar"))
    archiveClassifier.set("aws")

    manifest.inheritFrom(tasks.getByName<Jar>("thinJar").manifest)

    dependencies {
        exclude("org.springframework.cloud:spring-cloud-function-web")
    }

    mergeServiceFiles()
    append("META-INF/spring.handlers")
    append("META-INF/spring.schemas")
    append("META-INF/spring.tooling")
    append("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
    append("META-INF/spring/org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration.imports")
    transform(PropertiesFileTransformer::class.java) {
        paths = listOf("META-INF/spring.factories")
        mergeStrategy = "append"
    }
}
