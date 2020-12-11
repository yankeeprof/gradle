plugins {
    id("gradlebuild.distribution.api-java")
    id("gradlebuild.update-init-template-versions")
}

dependencies {
    implementation("org.gradle:base-services")
    implementation("org.gradle:logging")
    implementation("org.gradle:core-api")
    implementation("org.gradle:model-core")
    implementation("org.gradle:core")
    implementation("org.gradle:file-collections")

    implementation("org.gradle:dependency-management")
    implementation("org.gradle:platform-base")
    implementation("org.gradle:platform-native")
    implementation("org.gradle:plugins")
    implementation("org.gradle:wrapper")

    implementation(libs.groovy)
    implementation(libs.slf4jApi)
    implementation(libs.guava)
    implementation(libs.commonsLang)
    implementation(libs.inject)
    implementation(libs.plexusContainer)
    implementation(libs.maven3Compat)
    implementation(libs.maven3PluginApi)

    testImplementation("org.gradle:cli")
    testImplementation("org.gradle:base-services-groovy")
    testImplementation("org.gradle:native")
    testImplementation("org.gradle:snapshots")
    testImplementation("org.gradle:process-services")
    testImplementation(testFixtures("org.gradle:core"))
    testImplementation(testFixtures("org.gradle:platform-native"))

    testFixturesImplementation("org.gradle:base-services")

    integTestImplementation("org.gradle:native")
    integTestImplementation(libs.jetty)

    testRuntimeOnly("org.gradle:distributions-core") {
        because("ProjectBuilder tests load services from a Gradle distribution.")
    }
    integTestDistributionRuntimeOnly(project(":distributions-full"))
}
