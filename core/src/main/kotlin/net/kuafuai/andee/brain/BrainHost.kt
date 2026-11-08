package net.kuafuai.andee.brain

/**
 * Platform-agnostic interface that the brain uses to interact with the host (Android/Desktop).
 * The brain never sees platform types — only this interface.
 */
interface BrainHost {
    // --- Localization & Logging ---
    fun text(key: BrainText, vararg args: Any): String
    fun log(tag: String, message: String)
    fun logError(tag: String, message: String, throwable: Throwable? = null)

    // --- Settings ---
    val maxSteps: Int
    val currentScene: String?
    val currentPersona: String?
    val platformPrompt: String?
    val guardPrompt: String?

    // --- Notebook / Memory ---
    fun notebookRecall(topic: String, key: String?): RecallResult
    fun notebookRemember(topic: String, key: String, value: String): Boolean
    fun notebookForget(topic: String, key: String): Boolean
    fun notebookListTopics(): Map<String, Int>

    // --- Todos ---
    fun todoList(): List<TodoItem>
    fun todoAdd(minutes: Int, text: String): Boolean
    fun todoDone(index: Int): Boolean

    // --- Scenes ---
    fun sceneList(): Map<String, SceneInfo>
    fun sceneGet(name: String): SceneInfo?
    fun sceneSave(name: String, scene: SceneInfo): Boolean

    // --- Tool Dispatch ---
    fun dispatchTool(name: String, args: Map<String, Any>): ToolResult

    // --- Platform Events ---
    fun onWakeWordDetected()
    fun onNotificationReceived(packageName: String, title: String, text: String)
    fun onScreenChanged()
    fun onBatteryLevel(level: Int, charging: Boolean)
    fun onNetworkChanged(connected: Boolean)

    // --- UI / Overlay ---
    fun showOverlay(message: String)
    fun hideOverlay()
    fun setBallExpression(expression: String)
    fun setBallMood(mood: String)
}

enum class BrainText {
    NO_KEY,
    ERROR_GENERIC,
    ERROR_NETWORK,
    ERROR_PERMISSION,
    NARRATE_OPEN_APP,
    NARRATE_SEND_SMS,
    NARRATE_SCROLL,
    NARRATE_TAP,
    NARRATE_ZIP_PROJECT,
    NARRATE_GIT_PUSH,
    NARRATE_RUN_CODE,
    REVIEW_NAME,
    REVIEW_SUMMARY,
    REVIEW_PROMISES,
    REVIEW_DIARY,
    REVIEW_PEOPLE,
    REVIEW_WIKI,
    REVIEW_MEMORIES,
    REVIEW_TODOS,
    TOPIC_FOLDED,
    TOPIC_UNFOLDED
}

data class RecallResult(
    val found: Boolean,
    val value: String? = null,
    val allEntries: Map<String, String> = emptyMap()
)

data class TodoItem(
    val id: Long,
    val text: String,
    val dueAt: Long?,
    val done: Boolean,
    val createdAt: Long
)

data class SceneInfo(
    val name: String,
    val description: String,
    val steps: List<SceneStep>
)

data class SceneStep(
    val type: String, // "say", "cmd", "wait"
    val payload: Map<String, Any>
)

data class ToolResult(
    val success: Boolean,
    val result: String? = null,
    val error: String? = null
)