package ge.merabi.callrecorder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ge.merabi.callrecorder.data.AppDatabase
import ge.merabi.callrecorder.data.Recording
import ge.merabi.callrecorder.data.RecordingRepository
import ge.merabi.callrecorder.data.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CallRecordingService : Service() {

    companion object {
        const val ACTION_START = "ge.merabi.callrecorder.START"
        const val ACTION_STOP = "ge.merabi.callrecorder.STOP"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_CALL_TYPE = "call_type"

        private const val CHANNEL_ID = "call_recording_channel"
        private const val NOTIFICATION_ID = 1001

        fun recordingsDir(context: Context): File {
            val dir = File(context.getExternalFilesDir(null), "Recordings")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }
    }

    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTimeMs: Long = 0
    private var currentPhoneNumber: String = ""
    private var currentCallType: String = "UNKNOWN"
    private var speakerWasOnBefore = false

    private lateinit var repository: RecordingRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        repository = RecordingRepository(AppDatabase.getInstance(applicationContext).recordingDao())
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                currentPhoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
                currentCallType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "UNKNOWN"
                startForeground(NOTIFICATION_ID, buildNotification())
                startRecording()
            }
            ACTION_STOP -> {
                stopRecording()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        try {
            if (SettingsStore.isAutoSpeakerEnabled(applicationContext)) {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                speakerWasOnBefore = audioManager.isSpeakerphoneOn
                audioManager.mode = AudioManager.MODE_IN_CALL
                audioManager.isSpeakerphoneOn = true
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val safeNumber = currentPhoneNumber.ifBlank { "unknown" }.replace(Regex("[^0-9+]"), "")
            val file = File(recordingsDir(applicationContext), "call_${timeStamp}_$safeNumber.m4a")
            currentFile = file

            @Suppress("DEPRECATION")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(applicationContext)
            } else {
                MediaRecorder()
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setAudioSamplingRate(44100)
            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()

            mediaRecorder = recorder
            startTimeMs = System.currentTimeMillis()
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            // ზარი შესაძლოა ძალიან მოკლე იყო prepare/start-ის დასრულებამდე
        }
        mediaRecorder?.release()
        mediaRecorder = null

        if (SettingsStore.isAutoSpeakerEnabled(applicationContext)) {
            try {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.isSpeakerphoneOn = speakerWasOnBefore
                audioManager.mode = AudioManager.MODE_NORMAL
            } catch (e: Exception) { }
        }

        val duration = System.currentTimeMillis() - startTimeMs
        val file = currentFile

        if (file != null && file.exists() && file.length() > 0 && duration > 1000) {
            val recording = Recording(
                filePath = file.absolutePath,
                phoneNumber = currentPhoneNumber.ifBlank { "უცნობი ნომერი" },
                timestamp = startTimeMs,
                durationMs = duration,
                callType = currentCallType
            )
            serviceScope.launch { repository.insert(recording) }
        } else {
            file?.delete()
        }
        currentFile = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "ზარის ჩაწერა", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ზარი ჩაიწერება")
            .setContentText("ჩაწერა მიმდინარეობს...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
