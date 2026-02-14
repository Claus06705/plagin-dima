package com.deepseek.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class AppSettingsConfigurable : Configurable {

    private var mySettingsComponent: AppSettingsComponent? = null

    override fun getDisplayName(): String = "DeepSeek AI"

    override fun createComponent(): JComponent? {
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent?.panel
    }

    override fun isModified(): Boolean {
        val settings = AppSettingsState.instance
        return mySettingsComponent?.apiKey != settings.apiKey ||
                mySettingsComponent?.userName != settings.userName ||
                mySettingsComponent?.apiUrl != settings.apiUrl ||
                mySettingsComponent?.temperature != settings.temperature ||
                mySettingsComponent?.maxTokens != settings.maxTokens ||
                mySettingsComponent?.theme != settings.theme ||
                mySettingsComponent?.wallpaperPath != settings.wallpaperPath
    }

    override fun apply() {
        val settings = AppSettingsState.instance
        settings.apiKey = mySettingsComponent?.apiKey ?: ""
        settings.userName = mySettingsComponent?.userName ?: "Дима"
        settings.apiUrl = mySettingsComponent?.apiUrl ?: "https://api.deepseek.com"
        settings.temperature = mySettingsComponent?.temperature ?: 0.7
        settings.maxTokens = mySettingsComponent?.maxTokens ?: 4096
        settings.theme = mySettingsComponent?.theme ?: "System"
        settings.wallpaperPath = mySettingsComponent?.wallpaperPath ?: ""
    }

    override fun reset() {
        val settings = AppSettingsState.instance
        mySettingsComponent?.apiKey = settings.apiKey
        mySettingsComponent?.userName = settings.userName
        mySettingsComponent?.apiUrl = settings.apiUrl
        mySettingsComponent?.temperature = settings.temperature
        mySettingsComponent?.maxTokens = settings.maxTokens
        mySettingsComponent?.theme = settings.theme
        mySettingsComponent?.wallpaperPath = settings.wallpaperPath
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}

class AppSettingsComponent {
    val panel: JPanel
    private val apiKeyText = JBTextField()
    private val apiUrlText = JBTextField()
    private val userNameText = JBTextField()
    private val tempSpinner = JSpinner(SpinnerNumberModel(0.7, 0.0, 2.0, 0.1))
    private val tokensSpinner = JSpinner(SpinnerNumberModel(4096, 1, 32768, 128))
    private val themeCombo = ComboBox(arrayOf("System", "Dark", "Light"))
    private val wallpaperText = JBTextField()

    var apiKey: String get() = apiKeyText.text; set(v) { apiKeyText.text = v }
    var apiUrl: String get() = apiUrlText.text; set(v) { apiUrlText.text = v }
    var userName: String get() = userNameText.text; set(v) { userNameText.text = v }
    var temperature: Double get() = tempSpinner.value as Double; set(v) { tempSpinner.value = v }
    var maxTokens: Int get() = tokensSpinner.value as Int; set(v) { tokensSpinner.value = v }
    var theme: String get() = themeCombo.selectedItem as String; set(v) { themeCombo.selectedItem = v }
    var wallpaperPath: String get() = wallpaperText.text; set(v) { wallpaperText.text = v }

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("API Key:"), apiKeyText, 1, false)
            .addLabeledComponent(JBLabel("API URL:"), apiUrlText, 1, false)
            .addSeparator()
            .addLabeledComponent(JBLabel("Temperature:"), tempSpinner, 1, false)
            .addLabeledComponent(JBLabel("Max Tokens:"), tokensSpinner, 1, false)
            .addSeparator()
            .addLabeledComponent(JBLabel("Theme:"), themeCombo, 1, false)
            .addLabeledComponent(JBLabel("Wallpaper Path (Optional):"), wallpaperText, 1, false)
            .addLabeledComponent(JBLabel("User Name:"), userNameText, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
}
