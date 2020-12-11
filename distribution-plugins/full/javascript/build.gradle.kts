plugins {
    id("gradlebuild.distribution.api-java")
}

dependencies {
    implementation("org.gradle:base-services")
    implementation("org.gradle:logging")
    implementation("org.gradle:process-services")
    implementation("org.gradle:core-api")
    implementation("org.gradle:model-core")
    implementation("org.gradle:core")

    implementation("org.gradle:reporting")
    implementation("org.gradle:plugins")
    implementation("org.gradle:workers")
    implementation("org.gradle:dependency-management") // Required by JavaScriptExtension#getGoogleApisRepository()
    implementation("org.gradle:language-java") // Required by RhinoShellExec

    implementation(libs.groovy)
    implementation(libs.slf4jApi)
    implementation(libs.commonsIo)
    implementation(libs.inject)
    implementation(libs.rhino)
    implementation(libs.gson) // used by JsHint.coordinates
    implementation(libs.simple) // used by http package in envjs.coordinates

    testImplementation(testFixtures("org.gradle:core"))

    testRuntimeOnly("org.gradle:distributions-core") {
        because("ProjectBuilder tests load services from a Gradle distribution.")
    }
    integTestDistributionRuntimeOnly(project(":distributions-full"))
}

classycle {
    excludePatterns.add("org/gradle/plugins/javascript/coffeescript/**")
}
