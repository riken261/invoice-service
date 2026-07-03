plugins {
    base
    id("org.springframework.boot") version "4.0.7" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

extra["springBootVersion"] = "4.0.7"
extra["springCloudVersion"] = "2025.1.2"
extra["springdocOpenApiVersion"] = "3.0.3"

val giteaWebhookRef = providers.environmentVariable("GITEA_WEBHOOK_REF").orNull
val releaseTagId = giteaWebhookRef
    ?.takeIf { it.startsWith("refs/tags/") }
    ?.removePrefix("refs/tags/")
    ?.takeIf(String::isNotBlank)

group = "cloud.techotakus"
version = if (!releaseTagId.isNullOrBlank()) {
    "26.07.001-r$releaseTagId"
} else {
    "26.07.001-SNAPSHOT"
}

allprojects {
    group = rootProject.group
    version = rootProject.version
}

subprojects {
    plugins.withId("org.springframework.boot") {
        tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
            archiveClassifier.set("boot")
        }
    }
}
