/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gradlebuild

import com.gradle.publish.PluginBundleExtension
import gradlebuild.pluginpublish.extension.PluginPublishExtension
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import java.time.Year

plugins {
    id("gradlebuild.module-identity")
    `maven-publish`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
}

extensions.create<PluginPublishExtension>("pluginPublish", the<GradlePluginDevelopmentExtension>(), the<PluginBundleExtension>())

tasks.validatePlugins.configure {
    enableStricterValidation.set(true)
}

// Remove gradleApi() and gradleTestKit() as we want to compile/run against Gradle modules
// TODO consider splitting `java-gradle-plugin` to provide only what's necessary here
afterEvaluate {
    configurations.all {
        dependencies.remove(project.dependencies.gradleApi())
        dependencies.remove(project.dependencies.gradleTestKit())
    }
}

pluginBundle {
    tags = listOf("Kotlin", "DSL")
    website = "https://github.com/gradle/kotlin-dsl"
    vcsUrl = "https://github.com/gradle/kotlin-dsl"
}

publishing.publications.withType<MavenPublication>() {
    if (name == "pluginMaven") {
        groupId = project.group.toString()
        artifactId = moduleIdentity.baseName.get()
    }
}

// publish plugin to local repository for integration testing -----------------
// See AbstractPluginTest
val localRepository = layout.buildDirectory.dir("repository")

val publishPluginsToTestRepository by tasks.registering {
    dependsOn("publishPluginMavenPublicationToTestRepository")
    // This should be unified with publish-public-libraries if possible
    doLast {
        localRepository.get().asFileTree.matching { include("**/maven-metadata.xml") }.forEach {
            it.writeText(it.readText().replace("\\Q<lastUpdated>\\E\\d+\\Q</lastUpdated>\\E".toRegex(), "<lastUpdated>${Year.now().value}0101000000</lastUpdated>"))
        }
        localRepository.get().asFileTree.matching { include("**/*.module") }.forEach {
            val content = it.readText()
                .replace("\"buildId\":\\s+\"\\w+\"".toRegex(), "\"buildId\": \"\"")
                .replace("\"size\":\\s+\\d+".toRegex(), "\"size\": 0")
                .replace("\"sha512\":\\s+\"\\w+\"".toRegex(), "\"sha512\": \"\"")
                .replace("\"sha1\":\\s+\"\\w+\"".toRegex(), "\"sha1\": \"\"")
                .replace("\"sha256\":\\s+\"\\w+\"".toRegex(), "\"sha256\": \"\"")
                .replace("\"md5\":\\s+\"\\w+\"".toRegex(), "\"md5\": \"\"")
            it.writeText(content)
        }
    }
}

afterEvaluate {
    val writeFuturePluginVersions by tasks.registering(WriteProperties::class) {
        outputFile = layout.buildDirectory.file("generated-resources/future-plugin-versions/future-plugin-versions.properties").get().asFile
    }
    sourceSets.main.get().output.dir(
        writeFuturePluginVersions.map { it.outputFile.parentFile }
    )

    publishing {
        repositories {
            maven {
                name = "test"
                url = uri(localRepository)
            }
        }
    }

    gradlePlugin {
        plugins.all {

            val plugin = this

            publishPluginsToTestRepository.configure {
                dependsOn("publish${plugin.name.capitalize()}PluginMarkerMavenPublicationToTestRepository")
            }

            writeFuturePluginVersions {
                property(plugin.id, version)
            }
        }
    }

    // For local consumption by tests - this should be unified with publish-public-libraries if possible
    configurations.create("localLibsRepositoryElements") {
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named("gradle-local-repository"))
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EMBEDDED))
        }
        isCanBeResolved = false
        isCanBeConsumed = true
        isVisible = false
        outgoing.artifact(localRepository) {
            builtBy(publishPluginsToTestRepository)
        }
    }
}
