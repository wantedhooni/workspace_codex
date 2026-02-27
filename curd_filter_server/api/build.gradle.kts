plugins {
    id("org.springframework.boot")
}

val testcontainersVersion = "1.20.6"

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":app"))
    implementation(project(":infra"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:${testcontainersVersion}")
    testImplementation("org.testcontainers:postgresql:${testcontainersVersion}")
}

tasks.named<Jar>("jar") {
    enabled = false
}
