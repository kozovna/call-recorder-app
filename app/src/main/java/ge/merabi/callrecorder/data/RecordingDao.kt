package ge.merabi.callrecorder.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Insert
    suspend fun insert(recording: Recording): Long

    @Delete
    suspend fun delete(recording: Recording)

    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<Recording>>
}
