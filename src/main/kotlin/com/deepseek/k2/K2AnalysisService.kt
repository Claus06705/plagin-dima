package com.deepseek.k2

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtFile

class K2AnalysisService(private val project: Project) {

    fun fileUsesCompose(ktFile: KtFile): Boolean {
        // Temporary disabled due to Analysis API version mismatch
        return ktFile.importDirectives.any { 
            it.importPath?.pathStr?.contains("androidx.compose") == true 
        }
    }
}
