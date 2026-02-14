package com.deepseek

import com.deepseek.settings.AppSettingsState
import com.intellij.openapi.components.Service
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class DeepSeekService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
        
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private var currentCall: Call? = null

    fun stopStreaming() {
        currentCall?.cancel()
        currentCall = null
    }

    fun streamMessage(
        model: String,
        userMessage: String, 
        systemPrompt: String,
        onChunk: (String) -> Unit, 
        onComplete: () -> Unit, 
        onError: (String) -> Unit
    ) {
        val settings = AppSettingsState.instance
        val apiKey = settings.apiKey
        
        println("DeepSeekService: Starting request with API key length: ${apiKey.length}")
        
        if (apiKey.isBlank()) {
            onError("API Key is empty! Please check Settings -> Tools -> DeepSeek AI")
            return
        }

        val jsonBody = """
            {
                "model": "$model",
                "messages": [
                    {"role": "system", "content": ${escapeJson(systemPrompt)}},
                    {"role": "user", "content": ${escapeJson(userMessage)}}
                ],
                "temperature": ${settings.temperature},
                "max_tokens": ${settings.maxTokens},
                "stream": true
            }
        """.trimIndent()

        val baseUrl = settings.apiUrl.removeSuffix("/")
        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        stopStreaming() 
        val call = client.newCall(request)
        currentCall = call

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e.message != "Canceled") {
                    println("DeepSeek Network Error: ${e.message}")
                    onError("Network Error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                println("DeepSeek Response Code: ${response.code}")
                
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    println("DeepSeek API Error Body: $errorBody")
                    onError("API Error ${response.code}: $errorBody")
                    return
                }
                
                response.body?.source()?.let { source ->
                    try {
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            // println("Stream Line: $line") // Раскомментировать для полной отладки
                            
                            if (line.startsWith("data: ")) {
                                val data = line.substring(6).trim()
                                if (data == "[DONE]") {
                                    onComplete()
                                    break
                                }
                                extractContent(data)?.let { onChunk(it) }
                            }
                        }
                    } catch (e: Exception) { 
                        if (e.message != "Socket closed") {
                            println("DeepSeek Stream Exception: ${e.message}")
                            onError("Stream Error: ${e.message}") 
                        }
                    } finally {
                        currentCall = null
                    }
                }
            }
        })
    }

    private fun extractContent(json: String): String? {
        val marker = "\"content\":\""
        val start = json.indexOf(marker)
        if (start == -1) return null
        
        val contentStart = start + marker.length
        val result = StringBuilder()
        var escaped = false
        
        for (i in contentStart until json.length) {
            val c = json[i]
            if (escaped) {
                when (c) {
                    'n' -> result.append('\n')
                    't' -> result.append('\t')
                    'r' -> result.append('\r')
                    '\\' -> result.append('\\')
                    '"' -> result.append('"')
                    else -> result.append(c)
                }
                escaped = false
            } else if (c == '\\') {
                escaped = true
            } else if (c == '"') {
                break
            } else {
                result.append(c)
            }
        }
        return result.toString()
    }

    private fun escapeJson(text: String): String {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
    }
}
