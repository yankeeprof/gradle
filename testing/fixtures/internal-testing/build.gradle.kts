plugins {
    id("gradlebuild.internal.java")
}

dependencies {
    implementation("org.gradle:base-services")
    implementation("org.gradle:native")

    implementation(libs.groovy)
    implementation(libs.slf4jApi)
    implementation(libs.guava)
    implementation(libs.commonsLang)
    implementation(libs.commonsIo)
    implementation(libs.ant)
    implementation(libs.asm)
    implementation(libs.asmTree)
    implementation(libs.junit)
    implementation(libs.spock)
    implementation(libs.jsoup)
    implementation(libs.testcontainersSpock)

    runtimeOnly(libs.bytebuddy)
}
