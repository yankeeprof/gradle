import gradlebuild.basics.BuildEnvironment
import gradlebuild.integrationtests.tasks.IntegrationTest

plugins {
    id("gradlebuild.distribution.api-java")
}

val integTestRuntimeResources: Configuration by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = false
}
val integTestRuntimeResourcesClasspath: Configuration by configurations.creating {
    extendsFrom(integTestRuntimeResources)
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        // play test apps MUST be found as exploded directory
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements::class.java, LibraryElements.RESOURCES))
    }
    isTransitive = false
}

dependencies {
    implementation("org.gradle:base-services")
    implementation("org.gradle:files")
    implementation("org.gradle:messaging")
    implementation("org.gradle:process-services")
    implementation("org.gradle:logging")
    implementation("org.gradle:worker-processes")
    implementation("org.gradle:core-api")
    implementation("org.gradle:model-core")
    implementation("org.gradle:core")
    implementation("org.gradle:file-collections")
    implementation("org.gradle:snapshots")

    implementation("org.gradle:dependency-management")
    implementation("org.gradle:workers")
    implementation("org.gradle:plugins")
    implementation("org.gradle:platform-base")
    implementation("org.gradle:platform-jvm")
    implementation("org.gradle:language-jvm")
    implementation("org.gradle:language-java")
    implementation("org.gradle:language-scala")
    implementation("org.gradle:testing-base")
    implementation("org.gradle:testing-jvm")
    implementation("org.gradle:diagnostics")
    implementation("org.gradle:reporting")

    implementation(project(":javascript"))

    implementation(libs.groovy)
    implementation(libs.slf4jApi)
    implementation(libs.guava)
    implementation(libs.commonsLang)
    implementation(libs.inject)

    testImplementation("org.gradle:native")
    testImplementation("org.gradle:resources")
    testImplementation("org.gradle:base-services-groovy")

    integTestImplementation(libs.ant)
    integTestRuntimeOnly("org.gradle:composite-builds")
    integTestRuntimeOnly("org.gradle:testing-junit-platform")
    integTestRuntimeOnly(project(":ide-play"))

    testFixturesApi("org.gradle:platform-base") {
        because("Test fixtures export the Platform class")
    }
    testFixturesApi(testFixtures("org.gradle:core"))
    testFixturesApi(testFixtures("org.gradle:platform-native"))
    testFixturesApi(testFixtures("org.gradle:language-jvm"))
    testFixturesApi("org.gradle:internal-integ-testing")
    testFixturesImplementation("org.gradle:process-services")
    testFixturesImplementation(libs.commonsIo)
    testFixturesImplementation(libs.commonsHttpclient)
    testFixturesImplementation(libs.slf4jApi)
    testFixturesApi(testFixtures("org.gradle:language-scala"))
    testFixturesApi(testFixtures("org.gradle:language-java"))

    testImplementation(testFixtures("org.gradle:dependency-management"))
    testImplementation(testFixtures("org.gradle:diagnostics"))
    testImplementation(testFixtures("org.gradle:platform-base"))

    integTestDistributionRuntimeOnly(project(":distributions-full"))

    testRuntimeOnly("org.gradle:distributions-core") {
        because("Tests instantiate DefaultClassLoaderRegistry which requires a 'gradle-plugins.properties' through DefaultPluginModuleRegistry")
    }
    integTestRuntimeResources(testFixtures(project(":platform-play")))
}

strictCompile {
    ignoreRawTypes() // deprecated raw types
    ignoreDeprecations() // uses deprecated software model types
}

val integTestPrepare by tasks.registering(IntegrationTest::class) {
    systemProperties["org.gradle.integtest.executer"] = "embedded"
    if (BuildEnvironment.isCiServer) {
        systemProperties["org.gradle.integtest.multiversion"] = "all"
    }
    include("org/gradle/play/prepare/**")
    maxParallelForks = 1
}

tasks.withType<IntegrationTest>().configureEach {
    if (name != "integTestPrepare") {
        dependsOn(integTestPrepare)
        exclude("org/gradle/play/prepare/**")
        // this is a workaround for which we need a better fix:
        // it sets the platform play test fixtures resources directory in front
        // of the classpath, so that we can find them when executing tests in
        // an exploded format, rather than finding them in the test fixtures jar
        classpath = integTestRuntimeResourcesClasspath + classpath
    }
}

integTest.usesSamples.set(true)
