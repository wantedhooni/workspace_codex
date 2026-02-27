val querydslVersion = rootProject.extra["querydslVersion"] as String

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.querydsl:querydsl-jpa:${querydslVersion}:jakarta")

    annotationProcessor("com.querydsl:querydsl-apt:${querydslVersion}:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.2.0")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api:3.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<JavaCompile>().configureEach {
    options.generatedSourceOutputDirectory = layout.buildDirectory.dir("generated/sources/annotationProcessor/java/main").get().asFile
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/sources/annotationProcessor/java/main"))
        }
    }
}
