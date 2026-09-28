package com.incoteam.realsaathi.ui.home

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import java.io.File

internal class AccountVoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var output: File? = null

    fun start(): Boolean = runCatching {
        stopPlayback()
        val file = File(context.cacheDir, "account_voice_${System.currentTimeMillis()}.m4a")
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare(); start()
        }
        output = file
        true
    }.getOrElse { releaseRecorder(); false }

    fun stop(): File? = runCatching {
        recorder?.stop()
        val file = output?.takeIf { it.exists() && it.length() > 0L }
        releaseRecorder()
        file
    }.getOrElse { releaseRecorder(); null }

    fun play(file: File, onComplete: () -> Unit = {}) {
        stopPlayback()
        player = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build())
            setDataSource(file.absolutePath)
            setOnCompletionListener { onComplete(); stopPlayback() }
            prepare()
            start()
        }
    }

    fun stopPlayback() { runCatching { player?.stop() }; player?.release(); player = null }
    private fun releaseRecorder() { recorder?.release(); recorder = null }
    fun release() { runCatching { recorder?.stop() }; releaseRecorder(); stopPlayback() }
}
