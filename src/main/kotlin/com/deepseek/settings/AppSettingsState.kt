package com.deepseek.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.openapi.application.ApplicationManager

@Service(Service.Level.APP)
@State(
    name = "com.deepseek.settings.AppSettingsState",
    storages = [Storage("DeepSeekPluginSettings.xml")]
)
class AppSettingsState : PersistentStateComponent<AppSettingsState> {

    var apiKey: String = ""
    var userName: String = "Дима"
    var useFormalName: Boolean = false
    var selectedModel: String = "deepseek-coder"
    
    // Новые параметры ИИ
    var apiUrl: String = "https://api.deepseek.com"
    var temperature: Double = 0.7
    var maxTokens: Int = 4096
    
    // Интерфейс
    var theme: String = "System" // Dark, Light, System
    var wallpaperPath: String = "" // Пустое = по умолчанию (WhatsApp style)

    override fun getState(): AppSettingsState = this

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: AppSettingsState
            get() = ApplicationManager.getApplication().getService(AppSettingsState::class.java)
    }
}
