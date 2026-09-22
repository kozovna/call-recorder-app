package ge.merabi.callrecorder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class Recording(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val phoneNumber: String,
    val timestamp: Long,
    val durationMs: Long,
    val callType: String // "INCOMING" or "OUTGOING" or "UNKNOWN"
)
