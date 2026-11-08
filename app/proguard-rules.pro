# Keep all core module classes
-keep class net.kuafuai.andee.brain.** { *; }
-keep class net.kuafuai.andee.memory.** { *; }
-keep class net.kuafuai.andee.scene.** { *; }
-keep class net.kuafuai.andee.net.** { *; }

# Keep Room entities and DAOs
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *

# Keep kotlinx.serialization
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class *

# Keep OkHttp and WebSocket
-keep class okhttp3.** { *; }
-keep class org.java_websocket.** { *; }

# Keep ML Kit
-keep class com.google.mlkit.** { *; }

# Keep USB Serial
-keep class com.felhr.usbserial.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep AndroidX
-keep class androidx.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }