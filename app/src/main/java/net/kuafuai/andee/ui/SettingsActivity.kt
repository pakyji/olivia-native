package net.kuafuai.andee.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Column
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.px
import net.kuafuai.andee.AndeeApp
import net.kuafuai.andee.service.OverlayService
import net.kuafuai.andee.service.ScreenBodyService

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SettingsScreen() }
    }
}

@Composable
fun SettingsScreen() {
    val app = AndeeApp.get(this)
    val overlayPermission by remember { mutableStateOf(OverlayService.isOverlayPermissionGranted(this)) }
    val accessibilityEnabled by remember { mutableStateOf(isAccessibilityEnabled()) }

    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Olivia Native Settings", fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

        Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Column(modifier = androidx.compose.ui.Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Permissions", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)

                PermissionRow(
                    title = "Overlay Permission (Floating Ball)",
                    description = "Required for the floating ball overlay",
                    granted = overlayPermission,
                    onClick = { requestOverlayPermission() }
                )

                PermissionRow(
                    title = "Accessibility Service",
                    description = "Required for app control (tap, scroll, type)",
                    granted = accessibilityEnabled,
                    onClick = { openAccessibilitySettings() }
                )

                PermissionRow(
                    title = "Microphone",
                    description = "Required for voice input",
                    granted = checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED,
                    onClick = { requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 1) }
                )
            }
        }

        Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Column(modifier = androidx.compose.ui.Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Brain Settings", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)

                Text("Model: Local (on-device)", fontSize = 14.sp)
                Text("Language: English", fontSize = 14.sp)
                Text("Persona: Olivia Thomas", fontSize = 14.sp)
            }
        }

        Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Column(modifier = androidx.compose.ui.Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Actions", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)

                Button(
                    onClick = { testWakeWord() },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                ) {
                    Text("Test Wake Word Detection")
                }

                Button(
                    onClick = { testTts() },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Test TTS")
                }

                Button(
                    onClick = { viewNotebook() },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("View Notebook")
                }

                Button(
                    onClick = { viewTodos() },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("View Todos")
                }

                Button(
                    onClick = { viewScenes() },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("View Scenes")
                }
            }
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.Row(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = granted,
            onCheckedChange = { if (!granted) onClick() },
            enabled = !granted
        )
    }
}

private fun isAccessibilityEnabled(): Boolean {
    val enabledServices = android.provider.Settings.Secure.getString(
        contentResolver,
        android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: ""
    return enabledServices.contains("net.kuafuai.andee/.service.ScreenBodyService")
}

private fun openAccessibilitySettings() {
    val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
    startActivity(intent)
}

private fun requestOverlayPermission() {
    OverlayService.requestOverlayPermission(this, 100)
}

private fun testWakeWord() {
    // TODO: Trigger wake word test
}

private fun testTts() {
    net.kuafuai.andee.voice.TtsEngine.getInstance(this).speak("Hello! Olivia Native is ready.", mood = "happy")
}

private fun viewNotebook() {
    // TODO: Open notebook viewer
}

private fun viewTodos() {
    // TODO: Open todo viewer
}

private fun viewScenes() {
    // TODO: Open scene viewer
}