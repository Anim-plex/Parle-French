package com.example.domain

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class FrenchTtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.FRENCH)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("FrenchTTS", "French language not supported on this device TTS engine")
            } else {
                isInitialized = true
                tts?.setSpeechRate(0.92f) // Slightly relaxed pace for foreign language learners
            }
        } else {
            Log.e("FrenchTTS", "TTS Initialization failed")
        }
    }

    fun speak(text: String, slow: Boolean = false) {
        if (!isInitialized) return
        tts?.setSpeechRate(if (slow) 0.75f else 0.92f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "parle_tts_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
