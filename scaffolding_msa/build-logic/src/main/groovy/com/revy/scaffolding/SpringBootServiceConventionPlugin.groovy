package com.revy.scaffolding

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.tasks.testing.Test
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

class SpringBootServiceConventionPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply("java")
        project.pluginManager.apply("org.springframework.boot")
        project.pluginManager.apply("io.spring.dependency-management")

        project.extensions.getByType(JavaPluginExtension).toolchain.languageVersion = JavaLanguageVersion.of(21)
        project.extensions.configure(DependencyManagementExtension) { DependencyManagementExtension dependencyManagement ->
            dependencyManagement.imports {
                mavenBom "org.springframework.cloud:spring-cloud-dependencies:${project.findProperty("springCloudVersion")}"
            }
        }

        project.dependencies.add("compileOnly", "org.projectlombok:lombok")
        project.dependencies.add("annotationProcessor", "org.projectlombok:lombok")
        project.dependencies.add("testCompileOnly", "org.projectlombok:lombok")
        project.dependencies.add("testAnnotationProcessor", "org.projectlombok:lombok")
        project.dependencies.add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
        project.dependencies.add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")

        project.tasks.withType(Test).configureEach {
            useJUnitPlatform()
        }
    }
}
