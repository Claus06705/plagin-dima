package com.deepseek.llm

object TemplateManager {

    fun getTemplate(command: String, args: String): String {
        return when (command) {
            "/compose" -> """
                Создай Jetpack Compose компонент. 
                Название: ${args.ifBlank { "MyComponent" }}
                Требования: Используй Material3, добавь Preview.
            """.trimIndent()

            "/activity" -> """
                Создай Android Activity.
                Название: ${args.ifBlank { "MainActivity" }}
                Требования: Добавь соответствующий XML лейаут, зарегистрируй в манифесте (как текст), используй ViewBinding.
            """.trimIndent()

            "/viewmodel" -> """
                Создай ViewModel для: $args
                Требования: Используй StateFlow для состояния, добавь базовый обработчик ошибок, используй viewModelScope.
            """.trimIndent()

            else -> ""
        }
    }
}
