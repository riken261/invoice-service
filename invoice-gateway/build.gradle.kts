plugins {
    java
    jacoco
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Spring Cloud Gateway application for invoice services."

val springCloudVersion = rootProject.extra["springCloudVersion"] as String
val springdocOpenApiVersion = rootProject.extra["springdocOpenApiVersion"] as String

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))


    // web/gateway
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // openapi
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:$springdocOpenApiVersion")

    // monitor
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // logging
    implementation("cloud.techotakus:loggin-starter-webflux:1.1-SNAPSHOT")

}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
