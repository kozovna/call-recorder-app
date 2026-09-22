package ge.merabi.callrecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ge.merabi.callrecorder.data.AppDatabase
import ge.merabi.callrecorder.data.RecordingRepository
import ge.merabi.callrecorder.data.SettingsStore
import ge.merabi.callrecorder.service.CallRecordingService
import ge.merabi.callrecorder.ui.screens.HomeScreen
import ge.merabi.callrecorder.ui.theme.CallRecorderTheme
import ge.merabi.callrecorder.viewmodel.RecordingViewModel
import ge.merabi.callrecorder.viewmodel.RecordingViewModelFactory

class MainActivity : ComponentActivity() {

    private fun requiredPermissions(): Array<String> {
        val perms = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return perms.toTypedArray()
    }

    private fun hasAllPermissions(): Boolean =
        requiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun startMonitoring() {
        val intent = Intent(this, CallRecordingService::class.java).apply {
            action = CallRecordingService.ACTION_START_MONITORING
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopMonitoring() {
        val intent = Intent(this, CallRecordingService::class.java).apply {
            action = CallRecordingService.ACTION_STOP_MONITORING
        }
        ContextCompat.startForegroundService(this, intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = RecordingRepository(database.recordingDao())
        val factory = RecordingViewModelFactory(repository)

        // თუ ნებართვები უკვე მინიჭებულია და auto-record ჩართულია, სერვისი მაშინვე ვრთავთ
        if (hasAllPermissions() && SettingsStore.isAutoRecordEnabled(applicationContext)) {
            startMonitoring()
        }

        setContent {
            var permissionsGranted by remember { mutableStateOf(hasAllPermissions()) }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                permissionsGranted = hasAllPermissions()
                if (permissionsGranted && SettingsStore.isAutoRecordEnabled(applicationContext)) {
                    startMonitoring()
                }
            }

            CallRecorderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: RecordingViewModel = viewModel(factory = factory)
                    HomeScreen(
                        viewModel = viewModel,
                        hasAllPermissions = permissionsGranted,
                        onRequestPermissions = { permissionLauncher.launch(requiredPermissions()) },
                        onAutoRecordToggle = { enabled ->
                            if (enabled) startMonitoring() else stopMonitoring()
                        }
                    )
                }
            }
        }
    }
}
