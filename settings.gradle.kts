pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven {
            url = uri("https://repo.techotakus.cloud/maven-group")
            credentials {
                username = "admin"
                password = "nexusadmin"
            }
        }
    }
}

rootProject.name = "invoice-service"

include(
    "invoice-gateway",
    "invoice-file-service",
    "invoice-core-service",
    "invoice-ocr-service",
    "invoice-review-service",
    "invoice-claim-service",
    "invoice-integration-service",
    "invoice-authorization-service",
    "invoice-common-service",
)
