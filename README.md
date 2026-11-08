# Olivia Native 🎙️

**True native Android AI agent — like Andee, but with Olivia's personality.**

Runs entirely on-device: Kotlin brain, AccessibilityService hands, WindowManager overlay face, offline wake word. No Termux, no WebView, no Node.js.

---

## 🏗 Architecture (Mirrors Andee)

```
olivia-native/
├── core/                    # Shared brain (Kotlin/JVM) — NO Android deps
│   ├── brain/               # LocalBrain, BrainHost interface, prompt
│   ├── memory/              # Notebook (8 topics), TodoStore (Room)
│   ├── scene/               # SceneEngine (reusable routines)
│   └── net/                 # HubClient (WebSocket for peer pairing)
├── app/                     # Android body
│   ├── service/
│   │   ├── BodyCoreService.kt      # Foreground service (brain alive)
│   │   ├── OverlayService.kt       # WindowManager floating ball
│   │   └── ScreenBodyService.kt    # AccessibilityService (hands)
│   ├── ui/
│   │   ├── SettingsActivity.kt     # Setup + permission grants
│   │   └── OverlayPermissionActivity.kt
│   ├── voice/
│   │   ├── TtsEngine.kt            # Female-first TTS with moods
│   │   └── WakeWordDetector.kt     # Offline MFCC+DTW ("Olivia")
│   └── AndeeApp.kt                 # Application class, DI
├── desktop/                 # macOS Compose Desktop (optional)
└── .github/workflows/       # CI: APK + DMG builds
```

---

## ✨ Features (Andee-Grade)

| Feature | Implementation |
|---------|----------------|
| **App Control** | AccessibilityService + 8×12 labelled grid — tap any app by cell label |
| **UI Overlay** | WindowManager floating ball (draggable, persistent) |
| **Brain** | `LocalBrain.kt` — on-device turn loop (USER/WAKE/NOTIFY/SWEEP/HARVEST/WORK) |
| **Memory** | Room DB: Notebook (8 topics), TodoStore (due times), SceneEngine |
| **Voice** | Offline wake word (MFCC+DTW), TTS with mood markers `[happy]`, `[calm]` |
| **Proactive** | 20-min heartbeat: overdue todos, diary prompts, scene suggestions |
| **Self-Review** | End-of-conversation reflection (quiet, updates notebook) |
| **Peer Pairing** | QR + WebSocket (same architecture as Andee) |
| **Desktop** | Compose Desktop for macOS (same brain, native TTS/notify) |

---

## 🚀 Quick Start

### Prerequisites
- JDK 17+
- Android SDK 34 (for APK)
- macOS 14+ (for DMG)

### Build APK
```bash
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Build DMG (macOS)
```bash
./gradlew :desktop:packageDmg
# Output: desktop/build/compose/binaries/main/dmg/*.dmg
```

### Install on Device
1. Install APK
2. Open app → **Grant Overlay Permission** (floating ball)
3. Open app → **Enable Accessibility Service** (app control)
4. Tap floating ball → **Allow Microphone** (voice)
5. Say "Olivia" or tap ball to start

---

## 🎙 Voice Commands

| Command | Action |
|---------|--------|
| `"Olivia"` | Wake word (always listening when enabled) |
| `"time?"` / `"battery status"` | Instant local response |
| `"open whatsapp"` / `"open camera"` | Launch any installed app |
| `"scroll down"` / `"go back"` | Navigation via accessibility |
| `"tap A3"` | Tap grid cell (8×12 labelled grid) |
| `"todo 30 Call mom"` | Schedule reminder |
| `"todolist"` | Show pending todos |
| `"scene morning"` | Run morning briefing |
| `"notebook"` | Show memory dump |

---

## 🧠 Memory System (8 Topics)

| Topic | Purpose |
|-------|---------|
| `self` | User identity, preferences |
| `cares` | Projects, goals, health |
| `diary` | Daily logs, reflections |
| `plan` | Upcoming commitments |
| `people` | Contacts, relationships |
| `wiki` | Reference knowledge |
| `memories` | Shared moments |
| `todos` | Active promises (mirrored in TodoStore) |

Commands: `recall [topic] [key]`, `remember <topic> <key> <value>`, `forget <topic> <key>`

---

## 🎭 Mood Markers (TTS Expression)

Include in responses: `[happy]`, `[calm]`, `[curious]`, `[concerned]`, `[playful]`, `[quiet]`, `[END]`

---

## ⚙️ Configuration

| File | Purpose |
|------|---------|
| `core/src/main/resources/persona.txt` | System prompt (identity, rules, commands) |
| `app/src/main/res/xml/accessibility_service_config.xml` | Accessibility service config |
| `app/src/main/AndroidManifest.xml` | Permissions, services, receivers |

---

## 🔐 Permissions (Graceful Degradation)

| Permission | Feature | If Denied |
|------------|---------|-----------|
| `SYSTEM_ALERT_WINDOW` | Floating ball | Ball hidden, chat in app only |
| `BIND_ACCESSIBILITY_SERVICE` | App control | No tap/scroll/type, only intents |
| `RECORD_AUDIO` | Voice input | Text-only mode |
| `CAMERA` | Eyes (future) | No vision |
| `USB_PERMISSION` | Robot dog | No dog control |

---

## 🏃 CI/CD

Every push to `main` triggers:
- **Android**: `./gradlew :app:assembleDebug` → APK to Releases (`native-apk-v<run>`)
- **macOS**: `./gradlew :desktop:packageDmg` → DMG to Releases (`native-dmg-v<run>`)

---

## 📜 License

Apache-2.0 with Andee-style conditions (no multi-tenant SaaS, no white-label stripping).

---

## 🙏 Credits

Architecture inspired by **Andee** (kuafuai/andee) — the original on-device AI agent with accessibility hands, overlay face, and local brain.

Built with: Kotlin, Room, Compose, Coroutines, OkHttp, ML Kit, USB Serial.