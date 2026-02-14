package com.deepseek.ui

import com.deepseek.DeepSeekService
import com.deepseek.context.AndroidContextAnalyzer
import com.deepseek.llm.PromptManager
import com.deepseek.services.TerminalService
import com.deepseek.settings.AppSettingsState
import com.deepseek.utils.EditorUtils
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.util.*
import javax.swing.JComponent

class DeepSeekChatToolWindow(private val project: Project) {

    private val browser = JBCefBrowser()
    private val sendQuery = JBCefJSQuery.create(browser)
    private val stopQuery = JBCefJSQuery.create(browser)
    private val executeActionQuery = JBCefJSQuery.create(browser)
    private val analyzer = AndroidContextAnalyzer(project)
    private val deepSeekService = ApplicationManager.getApplication().getService(DeepSeekService::class.java)

    private var currentMode = "chat"
    private var agentSubMode = "ask"

    val content: JComponent get() = browser.component

    init {
        setupQueries()
        loadInitialPage()
    }

    private fun setupQueries() {
        sendQuery.addHandler { data ->
            val parts = data.split("|", limit = 3)
            currentMode = parts[0]
            agentSubMode = parts[1]
            val userText = parts[2]

            val projectIdentity = analyzer.getProjectIdentity()
            val openFiles = EditorUtils.getAllOpenFilesContent(project)
            
            val systemPrompt = PromptManager.buildSystemPrompt(projectIdentity, openFiles) + """
                
                ${if (currentMode == "agent") "AGENT MODE ACTIVE. Use <create file=\"name\">content</create>, <execute>command</execute>." else ""}
            """.trimIndent()

            val messageId = UUID.randomUUID().toString()
            var fullResponse = ""

            deepSeekService.streamMessage("deepseek-coder", userText, systemPrompt, { chunk ->
                fullResponse += chunk
                val escaped = chunk.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n")
                ApplicationManager.getApplication().invokeLater {
                    browser.cefBrowser.executeJavaScript("window.updateAiStream('$messageId', '$escaped')", "", 0)
                }
            }, {
                processResponse(fullResponse)
                ApplicationManager.getApplication().invokeLater {
                    browser.cefBrowser.executeJavaScript("window.stopThinking()", "", 0)
                }
            }, { error ->
                ApplicationManager.getApplication().invokeLater {
                    val errorHtml = "<span style=\"color: #f85149;\">Error: $error</span>"
                    browser.cefBrowser.executeJavaScript("window.updateAiStream('$messageId', '$errorHtml')", "", 0)
                    browser.cefBrowser.executeJavaScript("window.stopThinking()", "", 0)
                }
            })
            null
        }

        stopQuery.addHandler {
            deepSeekService.stopStreaming()
            null
        }

        executeActionQuery.addHandler { actionData ->
            val parts = actionData.split("|", limit = 2)
            runAction(parts[0], parts[1])
            null
        }

        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                browser?.executeJavaScript("""
                    window.javaSend = function(mode, submode, text) { ${sendQuery.inject("mode + '|' + submode + '|' + text")} };
                    window.javaStop = function() { ${stopQuery.inject("")} };
                    window.javaConfirmAction = function(type, payload) { ${executeActionQuery.inject("type + '|' + payload")} };
                """.trimIndent(), frame?.url, 0)
            }
        }, browser.cefBrowser)
    }

    private fun processResponse(response: String) {
        if (currentMode == "agent") {
            val createRegex = "<create file=\"([^\"]+)\">([\\s\\S]*?)</create>".toRegex()
            val executeRegex = "<execute>([\\s\\S]*?)</execute>".toRegex()

            createRegex.findAll(response).forEach { match ->
                handleAction("create", match.groupValues[1], match.groupValues[2])
            }
            executeRegex.findAll(response).forEach { match ->
                handleAction("execute", "Terminal", match.groupValues[1])
            }
        }
    }

    private fun handleAction(type: String, title: String, payload: String) {
        if (agentSubMode == "auto") {
            runAction(type, payload)
        } else {
            val escapedPayload = payload.replace("'", "\\'").replace("\n", "\\n")
            ApplicationManager.getApplication().invokeLater {
                browser.cefBrowser.executeJavaScript("window.askConfirmation('$type', '$title', '$escapedPayload')", "", 0)
            }
        }
    }

    private fun runAction(type: String, payload: String) {
        when (type) {
            "create" -> EditorUtils.createFile(project, "agent_file.kt", payload)
            "execute" -> TerminalService.execute(project, payload, {}, {})
        }
    }

    private fun loadInitialPage() {
        val settings = AppSettingsState.instance
        val wallpaperStyle = if (settings.wallpaperPath.isNotEmpty()) {
            "background-image: url('file:///${settings.wallpaperPath.replace("\\", "/")}'); background-size: cover;"
        } else {
            "background-color: #0d1117; background-image: radial-gradient(#1f2937 1px, transparent 0); background-size: 24px 24px;"
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/styles/tokyo-night-dark.min.css">
                <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/highlight.min.js"></script>
                <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
                <style>
                    :root { --bg: #0d1117; --accent: #58a6ff; --panel: #161b22; --text: #c9d1d9; }
                    body { background: var(--bg); color: var(--text); font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; margin: 0; display: flex; flex-direction: column; height: 100vh; }
                    .header-container { background: var(--panel); border-bottom: 1px solid #30363d; padding: 10px; z-index: 20; }
                    .header-top { display: flex; align-items: center; margin-bottom: 8px; }
                    .logo { width: 24px; height: 24px; border-radius: 4px; margin-right: 10px; border: 1px solid var(--accent); }
                    .header-title { font-size: 13px; font-weight: 600; color: var(--accent); }
                    
                    .mode-selector { display: flex; gap: 8px; }
                    .mode-btn { padding: 4px 12px; border-radius: 6px; font-size: 11px; cursor: pointer; border: 1px solid #30363d; background: #21262d; color: #8b949e; flex: 1; text-align: center; transition: 0.2s; }
                    .mode-btn.active { background: var(--accent); color: #0d1117; border-color: var(--accent); font-weight: bold; }
                    .agent-opts { display: none; margin-left: auto; align-items: center; gap: 5px; font-size: 11px; }
                    
                    .messages { flex: 1; overflow-y: auto; padding: 15px; display: flex; flex-direction: column; gap: 12px; $wallpaperStyle }
                    .msg { max-width: 85%; padding: 10px 14px; border-radius: 12px; font-size: 13px; position: relative; box-shadow: 0 1px 2px rgba(0,0,0,0.2); }
                    .ai { background: #21262d; align-self: flex-start; border-bottom-left-radius: 2px; }
                    .user { background: #056162; align-self: flex-end; color: #e9edef; border-bottom-right-radius: 2px; }
                    
                    .input-area { padding: 12px; background: var(--panel); border-top: 1px solid #30363d; }
                    .input-container { display: flex; align-items: center; background: #0d1117; border-radius: 24px; padding: 4px 12px; border: 1px solid #30363d; gap: 8px; }
                    input { flex: 1; background: transparent; border: none; color: white; outline: none; padding: 8px 0; font-size: 13px; }
                    .action-btn { background: none; border: none; cursor: pointer; padding: 5px; display: flex; align-items: center; justify-content: center; border-radius: 50%; color: #8b949e; }
                    .action-btn:hover { background: rgba(255,255,255,0.1); color: white; }
                    .send-icon { color: var(--accent); }
                    .stop-icon { color: #f85149; display: none; }
                    
                    .thinking { display: none; padding: 10px; gap: 4px; align-self: flex-start; }
                    .dot { width: 6px; height: 6px; background: var(--accent); border-radius: 50%; animation: bounce 1.4s infinite ease-in-out both; }
                    .dot:nth-child(1) { animation-delay: -0.32s; }
                    .dot:nth-child(2) { animation-delay: -0.16s; }
                    @keyframes bounce { 0%, 80%, 100% { transform: scale(0); } 40% { transform: scale(1.0); } }
                </style>
            </head>
            <body>
                <div class="header-container">
                    <div class="header-top">
                        <img src="file:///D:/PLAGINY/plagin-dima/src/main/resources/pluginIcon.jpg" class="logo" onerror="this.style.display='none'">
                        <div class="header-title">DeepSeek Agent</div>
                    </div>
                    <div class="mode-selector">
                        <div id="btnChat" class="mode-btn active" onclick="setMode('chat')">ЧАТ</div>
                        <div id="btnAgent" class="mode-btn" onclick="setMode('agent')">АГЕНТ</div>
                        <div id="agentOpts" class="agent-opts">
                            <select id="subMode" style="background:transparent; color:#c9d1d9; border:none; outline:none;">
                                <option value="ask">Ask</option>
                                <option value="auto">Auto</option>
                            </select>
                        </div>
                    </div>
                </div>
                
                <div class="messages" id="chat">
                    <div class="msg ai">Привет, ${settings.userName}! Я готов к работе.</div>
                </div>
                
                <div class="thinking" id="thinking">
                    <div class="dot"></div><div class="dot"></div><div class="dot"></div>
                </div>
                
                <div class="input-area">
                    <div class="input-container">
                        <input type="text" id="userInput" placeholder="Напиши сообщение..." autofocus />
                        <button class="action-btn" id="stopBtn" onclick="stopRequest()" title="Stop">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" class="stop-icon" id="stopIcon"><rect x="6" y="6" width="12" height="12" rx="2"/></svg>
                        </button>
                        <button class="action-btn" onclick="send()">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" class="send-icon"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
                        </button>
                    </div>
                </div>
                
                <script>
                    const chat = document.getElementById('chat');
                    const input = document.getElementById('userInput');
                    let currentMode = 'chat';
                    let activeStreams = {};

                    function setMode(m) {
                        currentMode = m;
                        document.getElementById('btnChat').classList.toggle('active', m === 'chat');
                        document.getElementById('btnAgent').classList.toggle('active', m === 'agent');
                        document.getElementById('agentOpts').style.display = (m === 'agent' ? 'flex' : 'none');
                    }

                    window.stopThinking = () => {
                        document.getElementById('thinking').style.display = 'none';
                        document.getElementById('stopIcon').style.display = 'none';
                    };

                    function stopRequest() {
                        window.javaStop();
                        stopThinking();
                    }

                    window.updateAiStream = (id, chunk) => {
                        document.getElementById('thinking').style.display = 'none';
                        if (!activeStreams[id]) {
                            const div = document.createElement('div');
                            div.className = 'msg ai';
                            chat.appendChild(div);
                            activeStreams[id] = { el: div, text: '' };
                        }
                        activeStreams[id].text += chunk;
                        activeStreams[id].el.innerHTML = marked.parse(activeStreams[id].text);
                        chat.scrollTop = chat.scrollHeight;
                    };
                    
                    window.askConfirmation = (type, title, payload) => {
                        const div = document.createElement('div');
                        div.className = 'msg ai';
                        div.innerHTML = '<strong>Action Required:</strong> ' + type + ' ' + title + '<br><button onclick="window.javaConfirmAction(\'' + type + '\', \'' + payload.replace(/\n/g, '\\n') + '\')">Execute</button>';
                        chat.appendChild(div);
                        chat.scrollTop = chat.scrollHeight;
                    };

                    window.addMessage = (text, type) => {
                        const div = document.createElement('div');
                        div.className = 'msg ' + type;
                        div.innerText = text;
                        chat.appendChild(div);
                        chat.scrollTop = chat.scrollHeight;
                    };

                    function send() {
                        const text = input.value.trim();
                        if (!text) return;
                        addMessage(text, 'user');
                        document.getElementById('thinking').style.display = 'flex';
                        document.getElementById('stopIcon').style.display = 'block';
                        window.javaSend(currentMode, document.getElementById('subMode').value, text);
                        input.value = '';
                    }
                    input.addEventListener('keypress', (e) => { if(e.key === 'Enter') send(); });
                </script>
            </body>
            </html>
        """.trimIndent()
        browser.loadHTML(htmlContent)
    }
}
