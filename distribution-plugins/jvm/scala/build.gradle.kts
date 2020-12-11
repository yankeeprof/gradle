plugins {
    id("gradlebuild.distribution.api-java")
}

dependencies {
    implementation("org.gradle:base-services")
    implementation("org.gradle:logging")
    implementation("org.gradle:worker-processes")
    implementation("org.gradle:file-collections")
    implementation("org.gradle:core-api")
    implementation("org.gradle:model-core")
    implementation("org.gradle:core")
    implementation("org.gradle:process-services")

    implementation("org.gradle:workers")
    implementation("org.gradle:platform-base")
    implementation("org.gradle:platform-jvm")
    implementation("org.gradle:language-jvm")
    implementation("org.gradle:language-java")
    implementation("org.gradle:language-scala")
    implementation("org.gradle:plugins")
    implementation("org.gradle:reporting")
    implementation("org.gradle:dependency-management")

    implementation(libs.groovy)
    implementation(libs.guava)
    implementation(libs.inject)

    testImplementation("org.gradle:base-services-groovy")
    testImplementation("org.gradle:files")
    testImplementation("org.gradle:resources")
    testImplementation(libs.slf4jApi)
    testImplementation(libs.commonsIo)
    testImplementation(testFixtures("org.gradle:core"))
    testImplementation(testFixtures("org.gradle:plugins"))
    testImplementation(testFixtures("org.gradle:language-jvm"))
    testImplementation(testFixtures("org.gradle:language-java"))

    integTestImplementation("org.gradle:jvm-services")
    integTestImplementation(testFixtures(project(":language-scala")))

    testRuntimeOnly("org.gradle:distributions-core") {
        because("ProjectBuilder tests load services from a Gradle distribution.")
    }
    integTestDistributionRuntimeOnly(project(":distributions-jvm"))
}

classycle {
    excludePatterns.add("org/gradle/api/internal/tasks/scala/**")
    excludePatterns.add("org/gradle/api/tasks/ScalaRuntime*")
}

integTest.usesSamples.set(true)
