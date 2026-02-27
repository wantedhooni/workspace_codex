plugins {
    java
    id("org.springframework.boot") version "4.0.3" apply false
}

group = "com.curd.template"
version = "0.0.1-SNAPSHOT"

extra["querydslVersion"] = "5.1.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")

    group = rootProject.group
    version = rootProject.version

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    dependencies {
        "implementation"(platform("org.springframework.boot:spring-boot-dependencies:4.0.3"))
        "testImplementation"(platform("org.springframework.boot:spring-boot-dependencies:4.0.3"))
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
