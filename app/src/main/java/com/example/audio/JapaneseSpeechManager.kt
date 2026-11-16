package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import java.io.File
import java.util.Locale

class JapaneseSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private val recordingFile: File by lazy {
        File(context.cacheDir, "speaking_practice.m4a")
    }

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.w("JapaneseTTS", "TTS init failed: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val res = tts?.setLanguage(Locale.JAPANESE)
            isReady = res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED
            if (!isReady) {
                tts?.setLanguage(Locale.JAPAN)
                isReady = true
            }
            tts?.setSpeechRate(0.92f)
        }
    }

    fun speak(textJp: String, slow: Boolean = false) {
        if (textJp.isBlank()) return
        try {
            tts?.setSpeechRate(if (slow) 0.68f else 0.95f)
            tts?.speak(textJp, TextToSpeech.QUEUE_FLUSH, null, "jp_${textJp.hashCode()}")
        } catch (e: Exception) {
            Log.w("JapaneseTTS", "Speak error: ${e.message}")
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) { }
    }

    fun startRecording(): Boolean {
        return try {
            stopPlayback()
            stopRecording()
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setOutputFile(recordingFile.absolutePath)
            mr.prepare()
            mr.start()
            recorder = mr
            true
        } catch (e: Exception) {
            Log.w("SpeakingRec", "Failed to start recording: ${e.message}")
            false
        }
    }

    fun stopRecording(): Boolean {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            recordingFile.exists() && recordingFile.length() > 0L
        } catch (e: Exception) {
            recorder = null
            false
        }
    }

    fun hasRecording(): Boolean = recordingFile.exists() && recordingFile.length() > 0L

    fun playRecording(onComplete: () -> Unit = {}) {
        if (!hasRecording()) {
            onComplete()
            return
        }
        try {
            stopPlayback()
            player = MediaPlayer().apply {
                setDataSource(recordingFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    onComplete()
                }
                start()
            }
        } catch (e: Exception) {
            Log.w("SpeakingPlay", "Playback failed: ${e.message}")
            onComplete()
        }
    }

    fun stopPlayback() {
        try {
            player?.stop()
            player?.release()
        } catch (_: Exception) { }
        player = null
    }

    fun release() {
        stopSpeaking()
        stopRecording()
        stopPlayback()
        try {
            tts?.shutdown()
        } catch (_: Exception) { }
        tts = null
    }
}
