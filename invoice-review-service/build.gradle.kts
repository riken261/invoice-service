plugins {
    java
    jacoco
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Spring MVC review service for invoice services."

val springCloudVersion = rootProject.extra["springCloudVersion"] as String
val springdocOpenApiVersion = rootProject.extra["springdocOpenApiVersion"] as String

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // web
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // openapi
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocOpenApiVersion")

    // monitor
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // database
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")
    implementation("com.baomidou:mybatis-plus-spring-boot4-starter:3.5.16")

    // logging
    implementation("cloud.techotakus:logging-starter:1.1-SNAPSHOT")
    implementation("cloud.techotakus:invoice-logging-feign-starter:1.1-SNAPSHOT")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.github.openfeign:feign-micrometer")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    // testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // pojo
    implementation("cloud.techotakus:common-pojo:26.07.008-SNAPSHOT")

    // mapstruct
    // Source: https://mvnrepository.com/artifact/org.mapstruct/mapstruct
    implementation("org.mapstruct:mapstruct:1.7.0.Beta2")
    // Source: https://mvnrepository.com/artifact/org.mapstruct/mapstruct-processor
    annotationProcessor("org.mapstruct:mapstruct-processor:1.7.0.Beta2")

    // support
    implementation("cloud.techotakus:support-starter:26.07.003-SNAPSHOT")

}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}
