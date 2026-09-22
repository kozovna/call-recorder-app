package ge.merabi.callrecorder.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareUtil {
    fun shareRecording(context: Context, filePath: String) {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(context, "ge.merabi.callrecorder.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "ჩანაწერის გაზიარება"))
    }
}
