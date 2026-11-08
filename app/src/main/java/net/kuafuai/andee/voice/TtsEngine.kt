package net.kuafuai.andee.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * TtsEngine - Text-to-Speech with female voice priority, mood support.
 * Andee-style: supports mood markers [happy], [calm], etc.
 */
class TtsEngine private constructor(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        @Volatile private var INSTANCE: TtsEngine? = null

        fun getInstance(context: Context): TtsEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = TtsEngine(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val tts = TextToSpeech(context, this)
    private val utteranceQueue = ConcurrentLinkedQueue<Utterance>()
    private var isReady = false
    private var currentLocale = Locale.US

    data class Utterance(
        val text: String,
        val locale: Locale,
        val mood: String? = null,
        val callback: (Boolean) -> Unit = { }
    )

    override fun onInit(status: Int) {
        isReady = status == TextToSpeech.SUCCESS
        if (isReady) {
            setLanguage(Locale.US)
            processQueue()
        }
    }

    fun speak(text: String, locale: Locale = Locale.US, mood: String? = null): Boolean {
        return speak(text, locale, mood, { })
    }

    fun speak(text: String, locale: Locale = Locale.US, mood: String? = null, callback: (Boolean) -> Unit): Boolean {
        if (!isReady) {
            utteranceQueue.add(Utterance(text, locale, mood, callback))
            return false
        }

        // Apply mood to speech parameters
        val (rate, pitch) = when (mood) {
            "happy" -> 1.1f to 1.1f
            "calm" -> 0.95f to 1.0f
            "curious" -> 1.05f to 1.05f
            "concerned" -> 0.9f to 0.95f
            "playful" -> 1.15f to 1.15f
            "quiet" -> return callback(true) // Suppress TTS
            else -> 1.0f to 1.0f
        }

        tts.setLanguage(locale)
        tts.speechRate = rate
        tts.pitch = pitch

        val utteranceId = "olivia_${System.currentTimeMillis()}"
        val result = tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)

        if (result == TextToSpeech.SUCCESS) {
            callback(true)
        } else {
            callback(false)
        }
        return result == TextToSpeech.SUCCESS
    }

    fun stop() {
        tts.stop()
        utteranceQueue.clear()
    }

    fun setLanguage(locale: Locale): Boolean {
        if (!isReady) {
            currentLocale = locale
            return false
        }
        val result = tts.setLanguage(locale)
        if (result != TextToSpeech.LANG_NOT_SUPPORTED && result != TextToSpeech.LANG_MISSING_DATA) {
            currentLocale = locale
            return true
        }
        // Fallback
        val fallback = when {
            locale.language == "hi" || locale.language == "ur" -> Locale.forLanguageTag("hi-IN")
            locale.language == "it" -> Locale.ITALY
            else -> Locale.US
        }
        tts.setLanguage(fallback)
        currentLocale = fallback
        return false
    }

    private fun processQueue() {
        while (!utteranceQueue.isEmpty()) {
            val utterance = utteranceQueue.poll() ?: break
            speak(utterance.text, utterance.locale, utterance.mood, utterance.callback)
        }
    }

    fun shutdown() {
        tts.shutdown()
        INSTANCE = null
    }
}