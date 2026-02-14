rootProject.name = "plagin-dima"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google() // Обязательно для Android-зависимостей
        intellijPlatform {
            defaultRepositories() // Магия JetBrains: отсюда скачается Android Studio
        }
    }
}
