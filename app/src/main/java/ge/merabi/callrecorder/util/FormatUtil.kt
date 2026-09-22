package ge.merabi.callrecorder.util

import java.text.SimpleDateFormat
import java.util.*

object FormatUtil {
    fun formatDate(timestamp: Long): String {
        val format = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
        return format.format(Date(timestamp))
    }

    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}
