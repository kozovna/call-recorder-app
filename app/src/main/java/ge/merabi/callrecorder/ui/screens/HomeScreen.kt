package ge.merabi.callrecorder.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ge.merabi.callrecorder.data.Recording
import ge.merabi.callrecorder.data.SettingsStore
import ge.merabi.callrecorder.util.AudioPlayerUtil
import ge.merabi.callrecorder.util.FormatUtil
import ge.merabi.callrecorder.util.ShareUtil
import ge.merabi.callrecorder.viewmodel.RecordingViewModel

@Composable
fun HomeScreen(viewModel: RecordingViewModel, hasAllPermissions: Boolean, onRequestPermissions: () -> Unit) {
    val context = LocalContext.current
    val recordings by viewModel.recordings.collectAsState()

    var autoRecord by remember { mutableStateOf(SettingsStore.isAutoRecordEnabled(context)) }
    var autoSpeaker by remember { mutableStateOf(SettingsStore.isAutoSpeakerEnabled(context)) }
    var playingPath by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("ზარების ჩამწერი") })

        if (!hasAllPermissions) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    "სამუშაოდ საჭიროა რამდენიმე ნებართვა (მიკროფონი, ზარის სტატუსი, შეტყობინებები).",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRequestPermissions, modifier = Modifier.fillMaxWidth()) {
                    Text("ნებართვების მინიჭება")
                }
            }
            return
        }

        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("ავტომატური ჩაწერა", fontWeight = FontWeight.SemiBold)
                    Text("ყველა ზარი ავტომატურად ჩაიწერება", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = autoRecord, onCheckedChange = {
                    autoRecord = it
                    SettingsStore.setAutoRecordEnabled(context, it)
                })
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("ავტომატური Speaker", fontWeight = FontWeight.SemiBold)
                    Text("საჭიროა მეორე მხარის ხმის ჩასაწერად", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = autoSpeaker, onCheckedChange = {
                    autoSpeaker = it
                    SettingsStore.setAutoSpeakerEnabled(context, it)
                })
            }
        }

        Divider()

        Text(
            "ჩანაწერები (${recordings.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        if (recordings.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ჩანაწერები ჯერ არ არის", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recordings, key = { it.id }) { recording ->
                    RecordingRow(
                        recording = recording,
                        isPlaying = playingPath == recording.filePath,
                        onPlayToggle = {
                            if (playingPath == recording.filePath) {
                                AudioPlayerUtil.stop()
                                playingPath = null
                            } else {
                                AudioPlayerUtil.play(recording.filePath) { playingPath = null }
                                playingPath = recording.filePath
                            }
                        },
                        onShare = { ShareUtil.shareRecording(context, recording.filePath) },
                        onDelete = {
                            if (playingPath == recording.filePath) {
                                AudioPlayerUtil.stop()
                                playingPath = null
                            }
                            viewModel.deleteRecording(recording)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordingRow(
    recording: Recording,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (recording.callType == "INCOMING") Icons.Filled.CallReceived else Icons.Filled.CallMade,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(recording.phoneNumber, fontWeight = FontWeight.SemiBold)
                Text(
                    "${FormatUtil.formatDate(recording.timestamp)} • ${FormatUtil.formatDuration(recording.durationMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onPlayToggle) {
                Icon(if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = "დაკვრა")
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Filled.Share, contentDescription = "გაზიარება")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "წაშლა", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
