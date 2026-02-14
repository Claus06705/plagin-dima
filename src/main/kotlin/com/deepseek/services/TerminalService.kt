package com.deepseek.services

import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.InputStreamReader

object TerminalService {

    fun execute(
        project: Project, 
        command: String, 
        onOutput: (String) -> Unit, 
        onResult: (Boolean) -> Unit
    ) {
        Thread {
            try {
                val process = ProcessBuilder(*command.split(" ").toTypedArray())
                    .directory(java.io.File(project.basePath ?: ""))
                    .redirectErrorStream(true)
                    .start()

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    onOutput(line!!)
                }

                val exitCode = process.waitFor()
                onResult(exitCode == 0)
            } catch (e: Exception) {
                onOutput("Error: ${e.message}")
                onResult(false)
            }
        }.start()
    }
}
