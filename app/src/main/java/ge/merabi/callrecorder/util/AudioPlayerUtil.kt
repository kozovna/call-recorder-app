package ge.merabi.callrecorder.util

import android.media.MediaPlayer

/**
 * მსუბუქი wrapper MediaPlayer-ისთვის — ერთდროულად ერთი ჩანაწერის დასაკრავად.
 */
object AudioPlayerUtil {
    private var player: MediaPlayer? = null
    private var currentPath: String? = null

    fun isPlaying(path: String): Boolean = currentPath == path && player?.isPlaying == true

    fun play(path: String, onCompletion: () -> Unit) {
        stop()
        try {
            player = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    onCompletion()
                    stop()
                }
                start()
            }
            currentPath = path
        } catch (e: Exception) {
            stop()
        }
    }

    fun stop() {
        try {
            player?.stop()
            player?.release()
        } catch (e: Exception) { }
        player = null
        currentPath = null
    }
}
