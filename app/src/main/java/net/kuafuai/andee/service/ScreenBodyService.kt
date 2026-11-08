package net.kuafuai.andee.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import net.kuafuai.andee.brain.ToolResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ScreenBodyService - AccessibilityService (Andee's "hands").
 * Reads UI tree, performs gestures, taps, scrolls, types text.
 * Implements the 8x12 labelled grid system.
 */
class ScreenBodyService : AccessibilityService() {

    companion object {
        const val TAG = "ScreenBodyService"

        // Grid constants (Andee's 8x12 labelled grid)
        const val GRID_COLS = 8
        const val GRID_ROWS = 12
        const val CELL_LABEL_PREFIX = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

        // Action names for BrainHost.dispatchTool
        const val ACTION_SCREEN_DUMP = "screen.dump"
        const val ACTION_SCREEN_TAP = "screen.tap"
        const val ACTION_SCREEN_SWIPE = "screen.swipe"
        const val ACTION_SCREEN_TYPE = "screen.type"
        const val ACTION_SCREEN_BACK = "screen.back"
        const val ACTION_SCREEN_HOME = "screen.home"
        const val ACTION_SCREEN_RECENTS = "screen.recents"
        const val ACTION_SCREEN_OPEN_APP = "screen.open_app"
        const val ACTION_SCREEN_FIND_TEXT = "screen.find_text"
        const val ACTION_SCREEN_FIND_ID = "screen.find_id"
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ScreenBodyService created")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events (window changes, notifications, etc.)
        event?.let {
            when (it.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    // Screen changed - notify brain
                    notifyScreenChanged()
                }
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                    // Notification received
                    handleNotificationEvent(it)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "ScreenBodyService interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "ScreenBodyService connected")

        // Configure service info
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
                or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            notificationTimeout = 100
            packageNames = null // All packages
        }
        setServiceInfo(serviceInfo)
    }

    // --- Public API for BrainHost ---

    fun dumpUiTree(): ToolResult {
        val root = rootInActiveWindow ?: return ToolResult(false, error = "No active window")
        val json = dumpNode(root, 0, 50) // Max depth 50, limit size
        return ToolResult(true, json)
    }

    fun dumpUiTreeWithGrid(): ToolResult {
        val root = rootInActiveWindow ?: return ToolResult(false, error = "No active window")
        val json = dumpNodeWithGrid(root, 0, 50)
        return ToolResult(true, json)
    }

    fun tap(x: Int, y: Int): ToolResult {
        return performGesture(
            android.view.accessibility.GestureDescription.Builder()
                .addStroke(
                    android.view.accessibility.GestureDescription.StrokeDescription(
                        android.graphics.Path().apply { moveTo(x.toFloat(), y.toFloat()) },
                        0, 50
                    )
                )
                .build()
        ) { ToolResult(true, "Tapped $x,$y") } ?: ToolResult(false, error = "Gesture failed")
    }

    fun tapCell(cellLabel: String): ToolResult {
        val coords = cellLabelToCoords(cellLabel.uppercase())
            ?: return ToolResult(false, error = "Invalid cell label: $cellLabel")
        return tap(coords.first, coords.second)
    }

    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Int = 300): ToolResult {
        val path = android.graphics.Path().apply {
            moveTo(startX.toFloat(), startY.toFloat())
            lineTo(endX.toFloat(), endY.toFloat())
        }
        return performGesture(
            android.view.accessibility.GestureDescription.Builder()
                .addStroke(
                    android.view.accessibility.GestureDescription.StrokeDescription(path, 0, duration.toLong())
                )
                .build()
        ) { ToolResult(true, "Swiped") } ?: ToolResult(false, error = "Gesture failed")
    }

    fun swipeDirection(direction: String, duration: Int = 300): ToolResult {
        val display = getDisplayMetrics()
        return when (direction.lowercase()) {
            "down" -> swipe(display.widthPixels / 2, display.heightPixels * 3 / 4, display.widthPixels / 2, display.heightPixels / 4, duration)
            "up" -> swipe(display.widthPixels / 2, display.heightPixels / 4, display.widthPixels / 2, display.heightPixels * 3 / 4, duration)
            "left" -> swipe(display.widthPixels * 3 / 4, display.heightPixels / 2, display.widthPixels / 4, display.heightPixels / 2, duration)
            "right" -> swipe(display.widthPixels / 4, display.heightPixels / 2, display.widthPixels * 3 / 4, display.heightPixels / 2, duration)
            else -> ToolResult(false, error = "Invalid direction: $direction")
        }
    }

    fun typeText(text: String): ToolResult {
        // Use ACTION_SET_TEXT for focused editable field
        val root = rootInActiveWindow ?: return ToolResult(false, error = "No active window")
        val focused = findFocusedEditable(root)
            ?: return ToolResult(false, error = "No focused editable field")

        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return if (focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) {
            ToolResult(true, "Typed: $text")
        } else {
            ToolResult(false, error = "Type failed")
        }
    }

    fun pressBack(): ToolResult {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) { ToolResult(true, "Back pressed") }
            ?: ToolResult(false, error = "Back failed")
    }

    fun pressHome(): ToolResult {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) { ToolResult(true, "Home pressed") }
            ?: ToolResult(false, error = "Home failed")
    }

    fun pressRecents(): ToolResult {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS) { ToolResult(true, "Recents pressed") }
            ?: ToolResult(false, error = "Recents failed")
    }

    fun openApp(packageName: String): ToolResult {
        val pm = packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
            ?: return ToolResult(false, error = "App not found: $packageName")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        return ToolResult(true, "Opening $packageName")
    }

    fun findText(text: String): ToolResult {
        val root = rootInActiveWindow ?: return ToolResult(false, error = "No active window")
        val results = mutableListOf<AccessibilityNodeInfo>()
        findNodesByText(root, text, results)
        return ToolResult(true, results.map { nodeToJson(it) }.joinToString(",") { "[$it]" })
    }

    fun findById(resourceId: String): ToolResult {
        val root = rootInActiveWindow ?: return ToolResult(false, error = "No active window")
        val results = mutableListOf<AccessibilityNodeInfo>()
        findNodesById(root, resourceId, results)
        return ToolResult(true, results.map { nodeToJson(it) }.joinToString(",") { "[$it]" })
    }

    // --- Internal Helpers ---

    private fun notifyScreenChanged() {
        val app = AndeeApp.get(this)
        scope.launch { app.localBrain.enqueueSweep() }
    }

    private fun handleNotificationEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: "unknown"
        val text = event.getText().joinToString(" ")
        val title = text.take(50)

        val app = AndeeApp.get(this)
        scope.launch { app.brainHost.onNotificationReceived(pkg, title, text) }
    }

    private fun dumpNode(node: AccessibilityNodeInfo, depth: Int, maxDepth: Int): String {
        if (depth > maxDepth) return ""
        val sb = StringBuilder()
        sb.append("{")
        sb.append("\"id\":\"${node.viewIdResourceName}\",")
        sb.append("\"class\":\"${node.className}\",")
        sb.append("\"text\":\"${node.text}\",")
        sb.append("\"desc\":\"${node.contentDescription}\",")
        sb.append("\"bounds\":${boundsToJson(node)},")
        sb.append("\"clickable\":${node.isClickable},")
        sb.append("\"focusable\":${node.isFocusable},")
        sb.append("\"editable\":${node.isEditable},")
        sb.append("\"children\":[")

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (i > 0) sb.append(",")
            sb.append(dumpNode(child, depth + 1, maxDepth))
        }
        sb.append("]}")
        return sb.toString()
    }

    private fun dumpNodeWithGrid(node: AccessibilityNodeInfo, depth: Int, maxDepth: Int): String {
        if (depth > maxDepth) return ""
        val sb = StringBuilder()
        sb.append("{")
        sb.append("\"id\":\"${node.viewIdResourceName}\",")
        sb.append("\"class\":\"${node.className}\",")
        sb.append("\"text\":\"${node.text}\",")
        sb.append("\"desc\":\"${node.contentDescription}\",")
        sb.append("\"bounds\":${boundsToJson(node)},")
        sb.append("\"clickable\":${node.isClickable},")
        sb.append("\"grid_label\":\"${boundsToGridLabel(node)}\",")
        sb.append("\"children\":[")

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (i > 0) sb.append(",")
            sb.append(dumpNodeWithGrid(child, depth + 1, maxDepth))
        }
        sb.append("]}")
        return sb.toString()
    }

    private fun boundsToJson(node: AccessibilityNodeInfo): String {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        return "{\"left\":${rect.left},\"top\":${rect.top},\"right\":${rect.right},\"bottom\":${rect.bottom}}"
    }

    private fun boundsToGridLabel(node: AccessibilityNodeInfo): String {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        val display = getDisplayMetrics()
        val cellW = display.widthPixels / GRID_COLS
        val cellH = display.heightPixels / GRID_ROWS
        val centerX = (rect.left + rect.right) / 2
        val centerY = (rect.top + rect.bottom) / 2
        val col = (centerX / cellW).coerceIn(0, GRID_COLS - 1)
        val row = (centerY / cellH).coerceIn(0, GRID_ROWS - 1)
        return "${CELL_LABEL_PREFIX[col]}${row + 1}"
    }

    private fun cellLabelToCoords(label: String): Pair<Int, Int>? {
        if (label.length < 2) return null
        val colChar = label[0]
        val rowStr = label.substring(1)
        val col = CELL_LABEL_PREFIX.indexOf(colChar)
        val row = rowStr.toIntOrNull()?.minus(1)
        if (col < 0 || col >= GRID_COLS || row == null || row !in 0..GRID_ROWS-1) return null

        val display = getDisplayMetrics()
        val cellW = display.widthPixels / GRID_COLS
        val cellH = display.heightPixels / GRID_ROWS
        val x = col * cellW + cellW / 2
        val y = row * cellH + cellH / 2
        return Pair(x, y)
    }

    private fun findFocusedEditable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isFocused && node.isEditable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findFocusedEditable(child)
            if (found != null) return found
        }
        return null
    }

    private fun findNodesByText(node: AccessibilityNodeInfo, text: String, results: MutableList<AccessibilityNodeInfo>) {
        if (node.text?.contains(text, true) == true || node.contentDescription?.contains(text, true) == true) {
            results.add(node)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findNodesByText(child, text, results)
        }
    }

    private fun findNodesById(node: AccessibilityNodeInfo, resourceId: String, results: MutableList<AccessibilityNodeInfo>) {
        if (node.viewIdResourceName?.endsWith(resourceId) == true) {
            results.add(node)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findNodesById(child, resourceId, results)
        }
    }

    private fun nodeToJson(node: AccessibilityNodeInfo): String {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        return "{\"id\":\"${node.viewIdResourceName}\",\"class\":\"${node.className}\",\"text\":\"${node.text}\",\"bounds\":${boundsToJson(node)}}"
    }

    private fun getDisplayMetrics(): android.util.DisplayMetrics {
        return resources.displayMetrics
    }

    private fun performGesture(gesture: android.view.accessibility.GestureDescription, callback: () -> ToolResult): ToolResult? {
        val completed = java.util.concurrent.atomic.AtomicBoolean(false)
        var result: ToolResult? = null

        dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: android.view.accessibility.GestureDescription) {
                result = callback()
                completed.set(true)
            }
            override fun onCancelled(gestureDescription: android.view.accessibility.GestureDescription) {
                result = ToolResult(false, error = "Gesture cancelled")
                completed.set(true)
            }
        }, handler)

        // Wait for completion (with timeout)
        var waited = 0
        while (!completed.get() && waited < 2000) {
            Thread.sleep(50)
            waited += 50
        }
        return result
    }
}