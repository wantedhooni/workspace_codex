plugins {
    id("org.springframework.boot") version "3.2.6" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
}

allprojects {
    group = "com.scaffolding"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}
