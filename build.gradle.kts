plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "com.deepseek.android"
version = "1.2.3-K2-BYPASS-FINAL"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    intellijPlatform {
        create("IC", "2023.3.6")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        instrumentationTools()
    }
}

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    pluginConfiguration {
        id.set("com.deepseek.android")
        name.set("DeepSeek Android Assistant")
        vendor {
            name.set("DeepSeek")
        }
        
        ideaVersion {
            sinceBuild.set("233")
            untilBuild.set("255.*")
        }
    }
}

tasks {
    register<Copy>("copyPluginToRelease") {
        dependsOn("buildPlugin")
        
        from(layout.buildDirectory.dir("distributions"))
        include("*.zip")
        into(layout.projectDirectory.dir("release"))
        
        doLast {
            println("✅ Плагин готов для установки!")
        }
    }
    
    // Отключаем проблемную задачу
    named("buildSearchableOptions") {
        enabled = false
    }
}
