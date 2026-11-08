package net.kuafuai.andee.desktop

import androidx.compose.desktop.Window
import androidx.compose.material3.Button
import androidx.compose.material3.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Frame
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Olivia Native (Desktop)",
        width = 600,
        height = 800
    ) {
        DesktopApp()
    }
}

@Composable
fun DesktopApp() {
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    // Initialize desktop databases (using in-memory for desktop)
    val notebook = remember { Notebook(NotebookDatabase.getInstanceDesktop()) }
    val todoStore = remember { TodoStore(TodoDatabase.getInstanceDesktop()) }
    val sceneEngine = remember { SceneEngine(SceneDatabase.getInstanceDesktop(), desktopBrainHost) }

    val brainHost = remember { desktopBrainHost }
    val localBrain = remember { LocalBrain(brainHost, scope) }

    // Initialize scenes
    androidx.compose.runtime.LaunchedEffect(Unit) {
        sceneEngine.initializeDefaults()
    }

    DesktopScreen(localBrain, notebook, todoStore, sceneEngine)
}

val desktopBrainHost = object : BrainHost {
    override fun text(key: BrainText, vararg args: Any): String = when (key) {
        BrainText.NO_KEY -> loadPersona()
        else -> key.name
    }

    override fun log(tag: String, message: String) = println("[$tag] $message")
    override fun logError(tag: String, message: String, throwable: Throwable?) = System.err.println("[$tag] ERROR: $message")

    override val maxSteps: Int get() = LocalBrain.MAX_STEPS
    override val currentScene: String? get() = null
    override val currentPersona: String? get() = null
    override val platformPrompt: String? get() = "You are running on macOS desktop."
    override val guardPrompt: String? get() = null

    override fun notebookRecall(topic: String, key: String?): RecallResult = scope.await { notebook.recall(topic, key) }
    override fun notebookRemember(topic: String, key: String, value: String): Boolean = scope.await { notebook.remember(topic, key, value) }
    override fun notebookForget(topic: String, key: String): Boolean = scope.await { notebook.forget(topic, key) }
    override fun notebookListTopics(): Map<String, Int> = scope.await { notebook.listTopics() }

    override fun todoList(): List<TodoItem> = scope.await { todoStore.listActive() }
    override fun todoAdd(minutes: Int, text: String): Boolean {
        val dueAt = if (minutes > 0) System.currentTimeMillis() + minutes * 60 * 1000L else null
        return scope.await { todoStore.add(text, dueAt) != null }
    }
    override fun todoDone(index: Int): Boolean = scope.await { todoStore.markDoneByIndex(index) }

    override fun sceneList(): Map<String, SceneInfo> = scope.await { sceneEngine.list() }.mapValues { (_, v) ->
        SceneInfo(v.name, v.description, v.steps.map { step ->
            when (step) {
                is net.kuafuai.andee.scene.SceneEngine.SceneStep.Say -> net.kuafuai.andee.brain.SceneStep("say", mapOf("text" to step.text))
                is net.kuafuai.andee.scene.SceneEngine.SceneStep.Cmd -> net.kuafuai.andee.brain.SceneStep("cmd", mapOf("action" to step.action, "args" to step.args))
                is net.kuafuai.andee.scene.SceneEngine.SceneStep.Wait -> net.kuafuai.andee.brain.SceneStep("wait", mapOf("ms" to step.ms))
                is net.kuafuai.andee.scene.SceneEngine.SceneStep.Notify -> net.kuafuai.andee.brain.SceneStep("notify", mapOf("title" to step.title, "text" to step.text))
            }
        })
    }
    override fun sceneGet(name: String): SceneInfo? = scope.await { sceneEngine.get(name) }?.let {
        SceneInfo(it.name, it.description, it.steps.map { step ->
            when (step) {
                is net.kuafuai.andee.scene.SceneEngine.SceneStep.Say -> net.kuafuai.andee.brain.SceneStep("say", mapOf("text" to step.text))
                is net.kuafuai.andee.scene.SceneEngine.SceneStep.Cmd -> net.kuafuai.andee.brain.SceneStep("cmd", mapOf("action" to step.action, "args" to step.args))
                is net.kuafuai.andee.scene.SceneEngine.SceneStep.Wait -> net.kuafuai.andee.brain.SceneStep("wait", mapOf("ms" to step.ms))
                is net.kuafuai.andee.scene.SceneEngine.SceneStep.Notify -> net.kuafuai.andee.brain.SceneStep("notify", mapOf("title" to step.title, "text" to step.text))
            }
        })
    }
    override fun sceneSave(name: String, scene: SceneInfo): Boolean = scope.await {
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
        sceneEngine.save(sceneObj)
    }

    override fun dispatchTool(name: String, args: Map<String, Any>): ToolResult = when (name) {
        "tts.speak" -> {
            val text = args["text"] as? String ?: ""
            DesktopTts.speak(text)
            ToolResult(true, "Speaking")
        }
        "notify" -> {
            val title = args["title"] as? String ?: "Olivia"
            val text = args["text"] as? String ?: ""
            DesktopNotify.show(title, text)
            ToolResult(true, "Notified")
        }
        else -> ToolResult(false, null, "Tool not implemented: $name")
    }

    override fun onWakeWordDetected() = scope.launch { localBrain.enqueueWake() }
    override fun onNotificationReceived(packageName: String, title: String, text: String) = scope.launch { localBrain.enqueueNotification(packageName, title, text) }
    override fun onScreenChanged() = Unit
    override fun onBatteryLevel(level: Int, charging: Boolean) = Unit
    override fun onNetworkChanged(connected: Boolean) = Unit
    override fun showOverlay(message: String) = println("Overlay: $message")
    override fun hideOverlay() = println("Overlay hidden")
    override fun setBallExpression(expression: String) = println("Expression: $expression")
    override fun setBallMood(mood: String) = println("Mood: $mood")

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val localBrain by lazy { LocalBrain(this@desktopBrainHost, scope) }
}

private fun loadPersona(): String {
    return try {
        java.io.File("persona.txt").readText()
    } catch (e: Exception) {
        "You are Olivia Thomas, a helpful personal AI assistant."
    }
}

@Composable
fun DesktopScreen(
    localBrain: LocalBrain,
    notebook: Notebook,
    todoStore: TodoStore,
    sceneEngine: SceneEngine
) {
    val input by remember { mutableStateOf("") }
    val messages by remember { mutableStateOf<MutableList<String>>(mutableListOf()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Olivia Native — Desktop", fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

        // Message area
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(androidx.compose.ui.graphics.Color(0xFF1A2233))
                .padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom
            ) {
                messages.forEach { msg ->
                    Text(msg, color = androidx.compose.ui.graphics.Color.White, fontSize = 14.sp)
                }
            }
        }

        // Input area
        androidx.compose.material3.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type or say 'Olivia'...") }
            )
            Button(onClick = {
                if (input.isNotBlank()) {
                    messages.add("You: $input")
                    localBrain.enqueueUser(input)
                    input = ""
                }
            }) {
                Text("Send")
            }
        }

        // Quick actions
        androidx.compose.material3.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { localBrain.enqueueWake() }) { Text("Wake") }
            Button(onClick = { localBrain.enqueueSweep() }) { Text("Sweep") }
            Button(onClick = { localBrain.enqueueHarvest() }) { Text("Harvest") }
            Button(onClick = { localBrain.enqueueWork() }) { Text("Work") }
        }
    }
}

// Desktop-specific implementations
object DesktopTts {
    fun speak(text: String) {
        // Use macOS 'say' command
        try {
            Runtime.getRuntime().exec(arrayOf("say", text))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

object DesktopNotify {
    fun show(title: String, text: String) {
        // Use macOS 'osascript' for notifications
        try {
            val script = "display notification \"$text\" with title \"$title\""
            Runtime.getRuntime().exec(arrayOf("osascript", "-e", script))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Desktop database implementations (in-memory)
object NotebookDatabase {
    fun getInstanceDesktop(): NotebookDatabase {
        // TODO: Implement desktop Room database (or use in-memory map)
        throw UnsupportedOperationException("Desktop database not implemented yet")
    }
}

object TodoDatabase {
    fun getInstanceDesktop(): TodoDatabase {
        throw UnsupportedOperationException("Desktop database not implemented yet")
    }
}

object SceneDatabase {
    fun getInstanceDesktop(): SceneDatabase {
        throw UnsupportedOperationException("Desktop database not implemented yet")
    }
}