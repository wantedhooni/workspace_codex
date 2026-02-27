dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.data:spring-data-commons")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
}
