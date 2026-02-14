package com.deepseek.context

import com.intellij.openapi.project.Project
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile

class AndroidContextAnalyzer(private val project: Project) {

    fun getProjectIdentity(): Map<String, Any> {
        val identity = mutableMapOf<String, Any>()
        identity["projectName"] = project.name
        
        val features = mutableMapOf<String, Boolean>()
        features["hasCompose"] = hasComposeUsage()
        
        identity["features"] = features
        return identity
    }

    private fun hasComposeUsage(): Boolean {
        val scope = GlobalSearchScope.projectScope(project)
        val kotlinFiles = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, scope)

        for (file in kotlinFiles) {
            val psiFile = PsiManager.getInstance(project).findFile(file)
            if (psiFile is KtFile) {
                // Simple import-based check to avoid Analysis API version issues
                val hasComposeImport = psiFile.importDirectives.any { 
                    it.importPath?.pathStr?.contains("androidx.compose") == true 
                }
                if (hasComposeImport) return true
            }
        }
        return false
    }
}
