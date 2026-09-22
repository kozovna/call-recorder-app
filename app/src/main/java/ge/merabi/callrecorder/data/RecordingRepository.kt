package ge.merabi.callrecorder.data

import kotlinx.coroutines.flow.Flow

class RecordingRepository(private val dao: RecordingDao) {
    val allRecordings: Flow<List<Recording>> = dao.getAllRecordings()
    suspend fun insert(recording: Recording): Long = dao.insert(recording)
    suspend fun delete(recording: Recording) = dao.delete(recording)
}
