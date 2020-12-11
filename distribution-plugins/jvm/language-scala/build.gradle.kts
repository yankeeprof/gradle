plugins {
    id("gradlebuild.distribution.api-java")
}

dependencies {
    implementation("org.gradle:base-services")
    implementation("org.gradle:files")
    implementation("org.gradle:logging")
    implementation("org.gradle:process-services")
    implementation("org.gradle:worker-processes")
    implementation("org.gradle:persistent-cache")
    implementation("org.gradle:core-api")
    implementation("org.gradle:model-core")
    implementation("org.gradle:core")

    implementation("org.gradle:workers")
    implementation("org.gradle:platform-base")
    implementation("org.gradle:platform-jvm")
    implementation("org.gradle:language-java")
    implementation("org.gradle:language-jvm")

    implementation(libs.groovy) // for 'Task.property(String propertyName) throws groovy.lang.MissingPropertyException'
    implementation(libs.ant)
    implementation(libs.slf4jApi)
    implementation(libs.guava)
    implementation(libs.inject)

    testImplementation("org.gradle:file-collections")
    testImplementation("org.gradle:files")
    testImplementation(testFixtures("org.gradle:core"))
    testImplementation(testFixtures("org.gradle:platform-base"))
    testImplementation(testFixtures("org.gradle:plugins"))

    integTestImplementation(libs.commonsLang)
    integTestImplementation(libs.ant)

    testFixturesApi(testFixtures("org.gradle:language-jvm"))
    testFixturesImplementation("org.gradle:base-services")
    testFixturesImplementation("org.gradle:core-api")
    testFixturesImplementation("org.gradle:model-core")
    testFixturesImplementation("org.gradle:platform-base")
    testFixturesImplementation(testFixtures("org.gradle:language-jvm"))

    compileOnly("org.scala-sbt:zinc_2.12:1.3.5")

    testRuntimeOnly(project(":distributions-jvm")) {
        because("ProjectBuilder tests load services from a Gradle distribution.")
    }
    integTestDistributionRuntimeOnly(project(":distributions-jvm"))
}

strictCompile {
    ignoreDeprecations() // uses deprecated software model types
}

classycle {
    excludePatterns.add("org/gradle/api/internal/tasks/scala/**")
    excludePatterns.add("org/gradle/language/scala/internal/toolchain/**")
}
