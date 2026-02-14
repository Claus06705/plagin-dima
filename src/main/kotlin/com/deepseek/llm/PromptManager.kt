package com.deepseek.llm

import com.deepseek.settings.AppSettingsState

object PromptManager {

    fun buildSystemPrompt(
        projectContext: Map<String, Any>,
        openFiles: Map<String, String>
    ): String {
        val settings = AppSettingsState.instance
        val name = if (settings.useFormalName) "Дмитрий" else settings.userName

        var prompt = """
            You are DeepSeek Android Agent for a user named $name.
            
            PROJECT CONTEXT:
            Name: ${projectContext["projectName"]}
            Features: ${projectContext["features"]}
            
            OPEN FILES:
        """.trimIndent()

        openFiles.forEach { (fileName, content) ->
            prompt += "\n--- $fileName ---\n$content\n"
        }

        prompt += """
            
            INSTRUCTIONS:
            - If the request is unclear, use simple questions to clarify.
            - In Agent mode, use <create> and <execute> tags.
        """.trimIndent()

        return prompt
    }
}
