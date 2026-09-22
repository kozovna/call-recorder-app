package ge.merabi.callrecorder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import ge.merabi.callrecorder.data.SettingsStore
import ge.merabi.callrecorder.service.CallRecordingService

/**
 * უსმენს სისტემურ ზარის სტატუსის ცვლილებებს და მართავს ჩაწერის სერვისს.
 * იმისთვის, რომ ამოვიცნოთ შემომავალი თუ გამავალი ზარია, ვინახავთ
 * წინა მდგომარეობას სტატიკურ ცვლადში ამ პროცესის სიცოცხლის განმავლობაში.
 */
class PhoneStateReceiver : BroadcastReceiver() {

    companion object {
        private var lastState: String = TelephonyManager.EXTRA_STATE_IDLE
        private var wasRinging = false
        private var currentNumber: String = ""
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.intent.action.PHONE_STATE") return
        if (!SettingsStore.isAutoRecordEnabled(context)) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        if (!incomingNumber.isNullOrBlank()) currentNumber = incomingNumber

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                wasRinging = true
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (lastState != TelephonyManager.EXTRA_STATE_OFFHOOK) {
                    val callType = if (wasRinging) "INCOMING" else "OUTGOING"
                    val serviceIntent = Intent(context, CallRecordingService::class.java).apply {
                        action = CallRecordingService.ACTION_START
                        putExtra(CallRecordingService.EXTRA_PHONE_NUMBER, currentNumber)
                        putExtra(CallRecordingService.EXTRA_CALL_TYPE, callType)
                    }
                    context.startForegroundService(serviceIntent)
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (lastState != TelephonyManager.EXTRA_STATE_IDLE) {
                    val serviceIntent = Intent(context, CallRecordingService::class.java).apply {
                        action = CallRecordingService.ACTION_STOP
                    }
                    context.startForegroundService(serviceIntent)
                }
                wasRinging = false
                currentNumber = ""
            }
        }
        lastState = state
    }
}
