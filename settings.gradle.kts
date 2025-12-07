rootProject.name = "TriAppDriver"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        google()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
        maven("https://api.mapbox.com/downloads/v2/releases/maven") {
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox-sdk"
                password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN")
                    .orElse(System.getenv("MAPBOX_DOWNLOADS_TOKEN"))
                    .get()
            }
        }
    }
}

include(":composeApp")
