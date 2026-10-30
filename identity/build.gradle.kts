plugins { kotlin("jvm") version "2.4.10" }
repositories { mavenCentral() }
dependencies {
    implementation("cash.z.ecc.android:kotlin-bip39-jvm:1.0.9")
    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
