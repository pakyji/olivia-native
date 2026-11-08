package net.kuafuai.andee.brain

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * LocalBrain - The on-device agent loop.
 * Runs entirely on-device, no cloud dependency.
 * Turn kinds: USER, WAKE, NOTIFY, PEER, SWEEP, HARVEST, WORK
 */
class LocalBrain(
    private val host: BrainHost,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    private val history = mutableListOf<HistoryEntry>()
    private val turnQueue = ConcurrentLinkedQueue<Turn>()
    private val isProcessing = AtomicBoolean(false)
    private val stepCounter = AtomicInteger(0)
    private val repeatCounter = AtomicInteger(0)
    private var lastToolName: String? = null

    companion object {
        const val MAX_HISTORY = 60
        const val MAX_HISTORY_CHARS = 60_000
        const val KEEP_TAIL_MESSAGES = 16
        const val MAX_STEPS = 10
        const val MAX_REPEATS = 3
        const val DEFAULT_TOOL_TIMEOUT_MS = 25_000L
    }

    sealed class TurnKind {
        object USER : TurnKind()
        object WAKE : TurnKind()
        object NOTIFY : TurnKind()
        object PEER : TurnKind()
        object SWEEP : TurnKind()
        object HARVEST : TurnKind()
        object WORK : TurnKind()
    }

    data class Turn(
        val kind: TurnKind,
        val input: String? = null,
        val metadata: Map<String, Any> = emptyMap()
    )

    data class HistoryEntry(
        val role: String, // "user" | "assistant" | "system"
        val content: String,
        val timestamp: Long = System.currentTimeMillis(),
        val topicId: String? = null,
        val toolCalls: List<ToolCall> = emptyList()
    )

    data class ToolCall(
        val name: String,
        val args: Map<String, Any>,
        val result: String?,
        val success: Boolean,
        val durationMs: Long
    )

    // --- Public API ---

    fun enqueueUser(input: String) {
        turnQueue.add(Turn(TurnKind.USER, input))
        processQueue()
    }

    fun enqueueWake() {
        turnQueue.add(Turn(TurnKind.WAKE))
        processQueue()
    }

    fun enqueueNotification(packageName: String, title: String, text: String) {
        turnQueue.add(Turn(TurnKind.NOTIFY, null, mapOf(
            "package" to packageName,
            "title" to title,
            "text" to text
        )))
        processQueue()
    }

    fun enqueueSweep() {
        turnQueue.add(Turn(TurnKind.SWEEP))
        processQueue()
    }

    fun enqueueHarvest() {
        turnQueue.add(Turn(TurnKind.HARVEST))
        processQueue()
    }

    fun enqueueWork() {
        turnQueue.add(Turn(TurnKind.WORK))
        processQueue()
    }

    // --- Main Processing Loop ---

    private fun processQueue() {
        scope.launch {
            if (!isProcessing.compareAndSet(false, true)) return@launch
            try {
                while (true) {
                    val turn = turnQueue.poll() ?: break
                    runTurn(turn)
                }
            } finally {
                isProcessing.set(false)
            }
        }
    }

    private suspend fun runTurn(turn: Turn) {
        stepCounter.set(0)
        repeatCounter.set(0)
        lastToolName = null

        val systemPrompt = buildSystemPrompt(turn)
        val messages = buildMessages(systemPrompt, turn)

        var steps = 0
        while (steps < MAX_STEPS) {
            steps++
            stepCounter.set(steps)

            val response = callModel(messages)
            if (response == null) {
                host.logError("LocalBrain", "Model call failed")
                break
            }

            val parsed = parseResponse(response)
            messages.add(HistoryEntry("assistant", response))

            if (parsed.toolCalls.isNotEmpty()) {
                for (toolCall in parsed.toolCalls) {
                    val result = executeTool(toolCall)
                    messages.add(HistoryEntry("tool", result, toolCalls = listOf(toolCall.copy(result = result, success = !result.startsWith("ERROR:")))))

                    // Check for repeat tool calls
                    if (toolCall.name == lastToolName) {
                        repeatCounter.incrementAndGet()
                        if (repeatCounter.get() >= MAX_REPEATS) {
                            host.log("LocalBrain", "Max repeats reached for ${toolCall.name}")
                            break
                        }
                    } else {
                        repeatCounter.set(0)
                        lastToolName = toolCall.name
                    }
                }
                continue // Continue loop for tool results
            }

            // No tool calls - final response
            if (parsed.text.isNotBlank()) {
                speakResponse(parsed.text, turn.kind)
            }
            break
        }

        // Post-turn processing
        trimHistory()
        maybeFoldTopics()
        maybeHarvest()
    }

    // --- Prompt Building ---

    private fun buildSystemPrompt(turn: Turn): String {
        val base = host.text(BrainText.NO_KEY) // Load from persona.txt via host
        var prompt = base

        if (host.currentScene != null) {
            prompt += "\n\n[ACTIVE SCENE: ${host.currentScene}]"
        }
        if (host.currentPersona != null) {
            prompt += "\n\n[PERSONA: ${host.currentPersona}]"
        }
        if (host.platformPrompt != null) {
            prompt += "\n\n[PLATFORM: ${host.platformPrompt}]"
        }
        if (host.guardPrompt != null) {
            prompt += "\n\n[GUARD: ${host.guardPrompt}]"
        }

        // Add language instruction
        prompt += "\n\nRESPOND STRICTLY IN: ${getCurrentLanguage()}"

        return prompt
    }

    private fun getCurrentLanguage(): String {
        // TODO: Get from host settings
        return "English"
    }

    private fun buildMessages(systemPrompt: String, turn: Turn): MutableList<HistoryEntry> {
        val messages = mutableListOf<HistoryEntry>()
        messages.add(HistoryEntry("system", systemPrompt))

        // Add relevant history (trimmed)
        val relevantHistory = history.takeLast(KEEP_TAIL_MESSAGES)
        messages.addAll(relevantHistory)

        // Add turn input
        when (turn.kind) {
            TurnKind.USER -> turn.input?.let { messages.add(HistoryEntry("user", it)) }
            TurnKind.WAKE -> messages.add(HistoryEntry("user", "[WAKE WORD DETECTED]"))
            TurnKind.NOTIFY -> {
                val pkg = turn.metadata["package"] as String
                val title = turn.metadata["title"] as String
                val text = turn.metadata["text"] as String
                messages.add(HistoryEntry("user", "[NOTIFICATION] $pkg: $title — $text"))
            }
            TurnKind.SWEEP -> messages.add(HistoryEntry("user", "[SWEEP] Check overdue todos, diary, scene suggestions"))
            TurnKind.HARVEST -> messages.add(HistoryEntry("user", "[HARVEST] Summarize old conversation"))
            TurnKind.WORK -> messages.add(HistoryEntry("user", "[WORK] Self-driven background turn"))
            TurnKind.PEER -> messages.add(HistoryEntry("user", "[PEER] Message from paired device"))
        }

        return messages
    }

    // --- Model Interaction ---

    private suspend fun callModel(messages: List<HistoryEntry>): String? {
        // TODO: Implement actual LLM call (local or via hub)
        // For now, return mock response
        return mockModelResponse(messages)
    }

    private fun mockModelResponse(messages: List<HistoryEntry>): String {
        // Mock implementation - replace with actual LLM
        val lastUser = messages.lastOrNull { it.role == "user" }?.content ?: ""
        return when {
            lastUser.contains("[WAKE]") -> "Yes? How can I help? [calm]"
            lastUser.contains("[NOTIFICATION]") -> "Got a notification. Want me to check it? [curious]"
            lastUser.contains("[SWEEP]") -> "Checking your todos... [calm]"
            lastUser.contains("todo") -> "todo 30 Call mom"
            lastUser.contains("recall") -> "recall self name"
            else -> "I understand. [happy]"
        }
    }

    private data class ParsedResponse(
        val text: String,
        val toolCalls: List<ToolCall>
    )

    private fun parseResponse(response: String): ParsedResponse {
        // Parse tool calls from response format: TOOL: name {args}
        val toolCalls = mutableListOf<ToolCall>()
        var text = response

        // Simple regex parsing for tool calls
        val toolPattern = "TOOL:\\s*(\\w+)\\s*\\{([^}]*)\\}".toRegex()
        text = text.replace(toolPattern) { match ->
            val name = match.groupValues[1]
            val argsStr = match.groupValues[2]
            val args = parseArgs(argsStr)
            toolCalls.add(ToolCall(name, args, null, false, 0))
            ""
        }

        // Parse mood markers
        val moodPattern = "\\[(happy|calm|curious|concerned|playful|quiet|END)\\]".toRegex()
        text = text.replace(moodPattern) { match ->
            val mood = match.groupValues[1]
            host.setBallMood(mood)
            ""
        }

        return ParsedResponse(text.trim(), toolCalls)
    }

    private fun parseArgs(argsStr: String): Map<String, Any> {
        val args = mutableMapOf<String, Any>()
        // Simple key=value parsing
        argsStr.split(",").forEach { pair ->
            val parts = pair.split("=")
            if (parts.size == 2) {
                args[parts[0].trim()] = parts[1].trim().removeSurrounding("\"")
            }
        }
        return args
    }

    // --- Tool Execution ---

    private fun executeTool(toolCall: ToolCall): String {
        val startTime = System.currentTimeMillis()
        val result = host.dispatchTool(toolCall.name, toolCall.args)
        val duration = System.currentTimeMillis() - startTime

        return if (result.success) {
            result.result ?: "OK"
        } else {
            "ERROR: ${result.error ?: "Unknown error"}"
        }
    }

    // --- Response Handling ---

    private fun speakResponse(text: String, kind: TurnKind) {
        // Strip mood markers for actual speech
        val cleanText = text.replace("\\[(happy|calm|curious|concerned|playful|quiet|END)\\]".toRegex(), "")

        if (!cleanText.isBlank() && !cleanText.contains("[quiet]")) {
            host.dispatchTool("tts.speak", mapOf("text" to cleanText))
        }

        host.log("LocalBrain", "Response: $cleanText")
    }

    // --- History Management ---

    private fun trimHistory() {
        // Trim by count
        while (history.size > MAX_HISTORY) {
            history.removeAt(0)
        }
        // Trim by chars
        var totalChars = history.sumOf { it.content.length }
        while (totalChars > MAX_HISTORY_CHARS && history.size > KEEP_TAIL_MESSAGES) {
            val removed = history.removeAt(0)
            totalChars -= removed.content.length
        }
    }

    private fun maybeFoldTopics() {
        // TODO: Implement topic folding logic
    }

    private fun maybeHarvest() {
        // TODO: Implement harvest logic
    }
}