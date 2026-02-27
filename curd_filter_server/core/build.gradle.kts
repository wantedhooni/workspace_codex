val querydslVersion = rootProject.extra["querydslVersion"] as String

dependencies {
    api("org.springframework:spring-web")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-jpa")
    api("jakarta.persistence:jakarta.persistence-api")
    implementation("org.springframework:spring-context")
    implementation("com.querydsl:querydsl-core:${querydslVersion}")

    annotationProcessor("com.querydsl:querydsl-apt:${querydslVersion}:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.2.0")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api:3.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter")
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
