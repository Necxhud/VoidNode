pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application" ->
                    useModule(
                        "com.android.tools.build:gradle:${requested.version}"
                    )

                "org.jetbrains.kotlin.android" ->
                    useModule(
                        "org.jetbrains.kotlin.android:org.jetbrains.kotlin.android.gradle.plugin:${requested.version}"
                    )

                "org.jetbrains.kotlin.plugin.compose" ->
                    useModule(
                        "org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:${requested.version}"
                    )
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VoidNode"
include(":app")
