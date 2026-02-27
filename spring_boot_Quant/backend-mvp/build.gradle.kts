plugins {
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    java
}

group = "com.quant"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.0"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mariadb")
    testRuntimeOnly("com.h2database:h2")
}

val querydslGeneratedDir = layout.buildDirectory.dir("generated/querydsl")

sourceSets {
    named("main") {
        java.srcDir(querydslGeneratedDir)
    }
}

tasks.withType<JavaCompile> {
    options.generatedSourceOutputDirectory.set(querydslGeneratedDir.get().asFile)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
