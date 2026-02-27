plugins {
    id("java")
    id("io.spring.dependency-management")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.6")
    }
}

dependencies {
    implementation(project(":domain:domain-core"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
