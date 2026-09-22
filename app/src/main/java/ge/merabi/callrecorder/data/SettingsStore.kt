package ge.merabi.callrecorder.data

import android.content.Context
import android.content.SharedPreferences

/**
 * მარტივი პარამეტრების შენახვა (SharedPreferences) — ჩართული/გამორთული
 * ავტომატური ჩაწერა და ავტომატური speaker.
 */
object SettingsStore {
    private const val PREFS = "call_recorder_prefs"
    private const val KEY_AUTO_RECORD = "auto_record"
    private const val KEY_AUTO_SPEAKER = "auto_speaker"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isAutoRecordEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_RECORD, true)

    fun setAutoRecordEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_RECORD, enabled).apply()
    }

    fun isAutoSpeakerEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_SPEAKER, true)

    fun setAutoSpeakerEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_SPEAKER, enabled).apply()
    }
}
