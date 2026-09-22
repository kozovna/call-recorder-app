package ge.merabi.callrecorder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ge.merabi.callrecorder.data.Recording
import ge.merabi.callrecorder.data.RecordingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class RecordingViewModel(private val repository: RecordingRepository) : ViewModel() {

    val recordings: StateFlow<List<Recording>> = repository.allRecordings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteRecording(recording: Recording) {
        viewModelScope.launch {
            repository.delete(recording)
            File(recording.filePath).delete()
        }
    }
}

class RecordingViewModelFactory(private val repository: RecordingRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordingViewModel::class.java)) {
            return RecordingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
