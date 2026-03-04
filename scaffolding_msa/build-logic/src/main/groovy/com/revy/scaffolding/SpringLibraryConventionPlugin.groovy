package com.revy.scaffolding

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

class SpringLibraryConventionPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply("java-library")
        project.pluginManager.apply("io.spring.dependency-management")

        project.extensions.getByType(JavaPluginExtension).toolchain.languageVersion = JavaLanguageVersion.of(21)
        project.extensions.configure(DependencyManagementExtension) { DependencyManagementExtension dependencyManagement ->
            dependencyManagement.imports {
                mavenBom "org.springframework.boot:spring-boot-dependencies:${project.findProperty("springBootVersion")}"
            }
        }

        project.dependencies.add("compileOnly", "org.projectlombok:lombok")
        project.dependencies.add("annotationProcessor", "org.projectlombok:lombok")
        project.dependencies.add("testImplementation", "org.junit.jupiter:junit-jupiter")

        project.tasks.withType(Test).configureEach {
            useJUnitPlatform()
        }
    }
}
