# RIVON AI — Your Personal Android Intelligence

**RIVON** is a production-grade, futuristic Android personal assistant delivering an experience comparable to Samsung Bixby, Google Assistant, and a futuristic JARVIS-style intelligence. Built natively with **Kotlin**, **Jetpack Compose**, and **Material 3**.

---

## ⚡ Core Capabilities

### 1. AI Intelligence Core (OpenRouter)
- **Multi-Model Intelligence**: Connects to OpenRouter's API catalog with support for frontier models (Llama 3.3 70B, Gemini 2.0 Flash, Mistral Small 24B, DeepSeek R1, Qwen 2.5 72B, Claude 3.5 Sonnet, GPT-4o).
- **Free Model Prioritization**: Automatically prioritizes and filters zero-cost free models.
- **Dynamic Catalog Discovery**: Fetches real-time model catalog and context information.
- **Intelligent Auto-Fallback**: If the selected model experiences rate limits, timeouts, or downtime, RIVON automatically routes your request through the next available free model in the pipeline.

### 2. Neural Voice Engine (ElevenLabs + Android TTS)
- **ElevenLabs Speech Generation**: Official API integration for ultra-natural, expressive neural voices (Rachel, Antoni, Domi, Bella, Adam, Elli, Josh, Arnold).
- **Voice Preview & Speed Control**: Listen to voice previews and adjust speech rate (0.5x to 2.0x).
- **Low-Latency Audio Caching**: Caches synthesized speech to prevent redundant API quota consumption.
- **Seamless Local Fallback**: When offline or if ElevenLabs quota is exhausted, seamlessly falls back to Android's native `TextToSpeech` engine.
- **Audio Interruption**: Any ongoing speech is immediately paused and cancelled when you tap the mic or start speaking.

### 3. Full Android Phone Control Engine
RIVON acts as an intelligent Android device operator, not just a chatbot:
- **Application Discovery & Launching**: Discovers all installed apps via Android `PackageManager`.
- **Level 1 (Intents & Deep Links)**: Launch apps and system screens across Instagram, WhatsApp, Spotify, YouTube, Chrome, Camera, Gallery, Maps, Gmail, etc.
- **Level 2 (App-Specific Actions)**:
  - *"Open YouTube and search relaxing music"*
  - *"Play Top Hits on Spotify"*
  - *"Message Rahul on WhatsApp saying I'll be home soon"*
  - *"Search Chrome for gaming laptops"*
  - *"Set volume to 50 percent"*
  - *"Set an alarm for 6 AM"*
  - *"Open Wi-Fi Settings / Bluetooth Settings"*
  - *"Open Camera / Gallery"*
- **Level 3 (Accessibility Automation Service)**: Optional user-authorized `RivonAccessibilityService` to click visible on-screen buttons and input text into apps upon explicit voice commands.
- **Safety Risk Classification**: Actions are categorized as **LOW** (opening apps, searches), **MEDIUM** (volume, alarms, emails), and **HIGH** (sending WhatsApp messages, phone calls). High-risk actions present an interactive confirmation modal before execution.

### 4. Hands-Free Wake Word System
- **"Hey Rivon" Voice Trigger**: Optional background listening engine.
- **Foreground Service with Mic Indicator**: Visible status notification guarantees complete transparency and zero hidden recordings.
- **Battery Friendly**: Employs periodic listening cycles and sleep intervals to conserve device battery.

### 5. Multilingual Natural Language Understanding
Understands varied phrasings in:
- **English**: *"Open Instagram"*, *"Tell Mom on WhatsApp I am coming"*
- **Nepali**: *"Instagram khol"*, *"Instagram open gardeu"*, *"Rahul lai WhatsApp ma message gara"*
- **Hinglish**: *"Instagram kholo"*, *"Rahul ko WhatsApp pe message bhejo"*, *"YouTube pe gaana chalao"*

### 6. Persistent Local Memory (Room Database)
- **Private SQLite Storage**: All conversation history and user-approved facts (preferences, contact nicknames, notes) are stored locally in Room.
- **Zero Cloud Leakage**: No private personal data, telemetry, or messages are ever collected or sold.
- **Memory Management**: Complete user control to inspect, add, delete, or wipe memory.

---

## 🎨 Futuristic Design & Animated AI Orb
- **Deep Black Background**: `#030712`
- **Midnight Blue Surfaces**: `#080F1F` & `#0D182E`
- **Electric Blue & Cyan Lighting**: `#2979FF` & `#00F0FF`
- **Living Animated Orb**:
  - `IDLE`: Gentle cosmic breathing glow
  - `LISTENING`: Real-time reactive expansion to microphone audio decibels (RMS)
  - `THINKING`: Rapid dual-counter rotating neural rings
  - `SPEAKING`: Dynamic pulsating waveforms and frequency bars
  - `EXECUTING`: High-tech cyber gears for device automation
  - `ERROR`: Subtle warning amber/red aura

---

## 🛠️ Build & Configuration
The project is built with Gradle Kotlin DSL:
```bash
gradle assembleDebug
gradle :app:testDebugUnitTest
```
API keys can be configured via `.env`, the Secrets panel, or directly in the app's **API Key Configuration** settings screen.
