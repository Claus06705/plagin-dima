plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    // Используем самую новую систему сборки плагинов
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.deepseek.androidstudio"
version = "0.2.0-windows"

repositories {
    mavenCentral()
    google()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Указываем сборку под Android Studio Ladybug
        androidStudio("2024.2.1") 

        // Подключаем встроенные плагины, чтобы иметь доступ к классам Android и Java
        plugin("com.intellij.java")
        plugin("org.jetbrains.android")

        instrumentationTools()
    }

    // Зависимости для работы с API DeepSeek и локальной БД (из ТЗ)
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0") // Для стриминга ответа
}

// Настройка версий IDE, в которых будет работать плагин
intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild.set("242")   // Начиная с Ladybug
            untilBuild.set("252.*") // Заканчивая версиями до Narwhal
        }
    }
}

// Принудительно задаем Java 17 (требование современных версий AS)
tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}
