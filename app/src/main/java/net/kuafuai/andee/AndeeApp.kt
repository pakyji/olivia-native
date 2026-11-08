package net.kuafuai.andee

import android.app.Application
import android.content.Context
import android.util.Log
import net.kuafuai.andee.brain.BrainHost
import net.kuafuai.andee.brain.LocalBrain
import net.kuafuai.andee.memory.Notebook
import net.kuafuai.andee.memory.NotebookDatabase
import net.kuafuai.andee.memory.TodoStore
import net.kuafuai.andee.memory.TodoDatabase
import net.kuafuai.andee.scene.SceneEngine
import net.kuafuai.andee.scene.SceneDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AndeeApp : Application() {

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun get(context: Context): AndeeApp = context.applicationContext as AndeeApp
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Core components
    lateinit var localBrain: LocalBrain
    lateinit var notebook: Notebook
    lateinit var todoStore: TodoStore
    lateinit var sceneEngine: SceneEngine

    // BrainHost implementation
    private val brainHost = object : BrainHost {
        override fun text(key: BrainText, vararg args: Any): String {
            // Load from assets/persona.txt or resources
            return when (key) {
                BrainText.NO_KEY -> loadPersona()
                else -> key.name
            }
        }

        override fun log(tag: String, message: String) {
            Log.d(tag, message)
        }

        override fun logError(tag: String, message: String, throwable: Throwable?) {
            Log.e(tag, message, throwable)
        }

        override val maxSteps: Int get() = LocalBrain.MAX_STEPS
        override val currentScene: String? get() = null // TODO: get from settings
        override val currentPersona: String? get() = null
        override val platformPrompt: String? get() = "You are running on Android. Use accessibility for app control."
        override val guardPrompt: String? get() = null

        override fun notebookRecall(topic: String, key: String?): RecallResult {
            return scope.await { notebook.recall(topic, key) }
        }

        override fun notebookRemember(topic: String, key: String, value: String): Boolean {
            return scope.await { notebook.remember(topic, key, value) }
        }

        override fun notebookForget(topic: String, key: String): Boolean {
            return scope.await { notebook.forget(topic, key) }
        }

        override fun notebookListTopics(): Map<String, Int> {
            return scope.await { notebook.listTopics() }
        }

        override fun todoList(): List<TodoItem> {
            return scope.await { todoStore.listActive() }
        }

        override fun todoAdd(minutes: Int, text: String): Boolean {
            val dueAt = if (minutes > 0) System.currentTimeMillis() + minutes * 60 * 1000L else null
            return scope.await { todoStore.add(text, dueAt) != null }
        }

        override fun todoDone(index: Int): Boolean {
            return scope.await { todoStore.markDoneByIndex(index) }
        }

        override fun sceneList(): Map<String, SceneInfo> {
            val scenes = scope.await { sceneEngine.list() }
            return scenes.mapValues { (_, v) ->
                SceneInfo(v.name, v.description, v.steps.map { step ->
                    when (step) {
                        is net.kuafuai.andee.scene.SceneEngine.SceneStep.Say ->
                            net.kuafuai.andee.brain.SceneStep("say", mapOf("text" to step.text))
                        is net.kuafuai.andee.scene.SceneEngine.SceneStep.Cmd ->
                            net.kuafuai.andee.brain.SceneStep("cmd", mapOf("action" to step.action, "args" to step.args))
                        is net.kuafuai.andee.scene.SceneEngine.SceneStep.Wait ->
                            net.kuafuai.andee.brain.SceneStep("wait", mapOf("ms" to step.ms))
                        is net.kuafuai.andee.scene.SceneEngine.SceneStep.Notify ->
                            net.kuafuai.andee.brain.SceneStep("notify", mapOf("title" to step.title, "text" to step.text))
                    }
                })
            }
        }

        override fun sceneGet(name: String): SceneInfo? {
            val scene = scope.await { sceneEngine.get(name) }
            return scene?.let {
                SceneInfo(it.name, it.description, it.steps.map { step ->
                    when (step) {
                        is net.kuafuai.andee.scene.SceneEngine.SceneStep.Say ->
                            net.kuafuai.andee.brain.SceneStep("say", mapOf("text" to step.text))
                        is net.kuafuai.andee.scene.SceneEngine.SceneStep.Cmd ->
                            net.kuafuai.andee.brain.SceneStep("cmd", mapOf("action" to step.action, "args" to step.args))
                        is net.kuafuai.andee.scene.SceneEngine.SceneStep.Wait ->
                            net.kuafuai.andee.brain.SceneStep("wait", mapOf("ms" to step.ms))
                        is net.kuafuai.andee.scene.SceneEngine.SceneStep.Notify ->
                            net.kuafuai.andee.brain.SceneStep("notify", mapOf("title" to step.title, "text" to step.text))
                    }
                })
            }
        }

        override fun sceneSave(name: String, scene: SceneInfo): Boolean {
            val sceneObj = net.kuafuai.andee.scene.Scene(
                name = name,
                description = scene.description,
                steps = scene.steps.map { step ->
                    when (step.type) {
                        "say" -> net.kuafuai.andee.scene.SceneEngine.SceneStep.Say(step.payload["text"] as String)
                        "cmd" -> net.kuafuai.andee.scene.SceneEngine.SceneStep.Cmd(step.payload["action"] as String, step.payload["args"] as? Map<String, Any> ?: emptyMap())
                        "wait" -> net.kuafuai.andee.scene.SceneEngine.SceneStep.Wait((step.payload["ms"] as Number).toLong())
                        "notify" -> net.kuafuai.andee.scene.SceneEngine.SceneStep.Notify(step.payload["title"] as String, step.payload["text"] as String)
                        else -> throw IllegalArgumentException("Unknown step type: ${step.type}")
                    }
                }
            )
            return scope.await { sceneEngine.save(sceneObj) }
        }

        override fun dispatchTool(name: String, args: Map<String, Any>): ToolResult {
            // Route to appropriate handler
            return when (name) {
                "tts.speak" -> {
                    val text = args["text"] as? String ?: ""
                    scope.launch { TtsEngine.getInstance(this@AndeeApp).speak(text) }
                    ToolResult(true, "Speaking")
                }
                "notify" -> {
                    val title = args["title"] as? String ?: "Olivia"
                    val text = args["text"] as? String ?: ""
                    scope.launch { NotificationHelper.show(this@AndeeApp, title, text) }
                    ToolResult(true, "Notified")
                }
                else -> {
                    // Forward to accessibility service or other handlers
                    ToolResult(false, null, "Tool not implemented: $name")
                }
            }
        }

        override fun onWakeWordDetected() {
            scope.launch { localBrain.enqueueWake() }
        }

        override fun onNotificationReceived(packageName: String, title: String, text: String) {
            scope.launch { localBrain.enqueueNotification(packageName, title, text) }
        }

        override fun onScreenChanged() {
            // TODO: Handle screen change
        }

        override fun onBatteryLevel(level: Int, charging: Boolean) {
            // TODO: Handle battery change
        }

        override fun onNetworkChanged(connected: Boolean) {
            // TODO: Handle network change
        }

        override fun showOverlay(message: String) {
            // TODO: Show floating ball message
        }

        override fun hideOverlay() {
            // TODO: Hide floating ball
        }

        override fun setBallExpression(expression: String) {
            // TODO: Update ball expression
        }

        override fun setBallMood(mood: String) {
            // TODO: Update ball mood
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize databases
        val notebookDb = NotebookDatabase.getInstance(this)
        val todoDb = TodoDatabase.getInstance(this)
        val sceneDb = SceneDatabase.getInstance(this)

        notebook = Notebook(notebookDb)
        todoStore = TodoStore(todoDb)
        sceneEngine = SceneEngine(sceneDb, brainHost)

        // Initialize default scenes
        scope.launch { sceneEngine.initializeDefaults() }

        // Initialize LocalBrain
        localBrain = LocalBrain(brainHost, scope)

        // Start services
        startCoreServices()

        Log.d("AndeeApp", "Olivia Native initialized")
    }

    private fun startCoreServices() {
        // Start foreground services
        BodyCoreService.start(this)
        OverlayService.start(this)
    }

    private fun loadPersona(): String {
        return try {
            assets.open("persona.txt").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "You are Olivia Thomas, a helpful personal AI assistant."
        }
    }

    override fun onTerminate() {
        scope.coroutineContext.cancelChildren()
        super.onTerminate()
    }
}