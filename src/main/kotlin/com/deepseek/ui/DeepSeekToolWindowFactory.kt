package com.deepseek.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class DeepSeekToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatToolWindow = DeepSeekChatToolWindow(project)
        val content = ContentFactory.getInstance().createContent(chatToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
