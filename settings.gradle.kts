pluginManagement {
    (this as org.gradle.plugin.management.internal.PluginManagementSpecInternal).includeBuild("build-logic-base")
}

plugins {
    id("gradlebuild.settings-plugins")
    id("gradlebuild.repositories")
}

includeBuild("subprojects")

rootProject.name = "gradle"
