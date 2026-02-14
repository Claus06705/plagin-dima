package com.deepseek.utils

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.openapi.fileTypes.FileTypeManager

object EditorUtils {

    fun insertCodeAtCaret(project: Project, code: String) {
        ApplicationManager.getApplication().invokeLater {
            val editor: Editor? = FileEditorManager.getInstance(project).selectedTextEditor
            if (editor != null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    editor.document.insertString(editor.caretModel.offset, code)
                }
            }
        }
    }

    fun createFile(project: Project, fileName: String, content: String) {
        ApplicationManager.getApplication().invokeLater {
            val projectDir = project.guessProjectDir() ?: return@invokeLater
            WriteCommandAction.runWriteCommandAction(project) {
                // Find file type by extension safely
                val fileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName)
                val psiFile = PsiFileFactory.getInstance(project)
                    .createFileFromText(fileName, fileType, content)
                
                val basePsiDir = PsiManager.getInstance(project).findDirectory(projectDir)
                basePsiDir?.add(psiFile)
                
                VfsUtil.findRelativeFile(fileName, projectDir)?.let {
                    FileEditorManager.getInstance(project).openFile(it, true)
                }
            }
        }
    }

    fun showDiff(project: Project, originalCode: String, modifiedCode: String) {
        ApplicationManager.getApplication().invokeLater {
            val factory = DiffContentFactory.getInstance()
            val request = SimpleDiffRequest(
                "DeepSeek Comparison",
                factory.create(originalCode),
                factory.create(modifiedCode),
                "Current Code",
                "DeepSeek Suggestion"
            )
            DiffManager.getInstance().showDiff(project, request)
        }
    }

    fun getSelectedText(project: Project): String? {
        return FileEditorManager.getInstance(project).selectedTextEditor?.selectionModel?.selectedText
    }

    fun getAllOpenFilesContent(project: Project): Map<String, String> {
        val contents = mutableMapOf<String, String>()
        FileEditorManager.getInstance(project).selectedFiles.forEach { file ->
            com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file)?.let {
                contents[file.name] = it.text
            }
        }
        return contents
    }
}
