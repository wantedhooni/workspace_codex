plugins {
    id("org.springframework.boot") version "3.3.7" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    kotlin("jvm") version "1.9.25" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
}
