pluginManagement {
    includeBuild("build-logic-base")
}

plugins {
    id("gradlebuild.settings-plugins")
    id("gradlebuild.repositories")
}

includeBuild("subprojects")

rootProject.name = "gradle"
