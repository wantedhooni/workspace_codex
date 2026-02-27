plugins {
    `java-library`
}

dependencies {
    compileOnly("jakarta.persistence:jakarta.persistence-api:3.1.0")
    compileOnly("org.springframework.data:spring-data-commons:3.4.2")
    compileOnly("org.springframework.data:spring-data-jpa:3.4.2")
    compileOnly("com.querydsl:querydsl-core:5.1.0")

    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api:2.1.1")
}
