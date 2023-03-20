package icu.pboymt.mayer

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.Keep
import icu.pboymt.mayer.runner.MayerAccessibilityHelper
import icu.pboymt.mayer.runner.MayerRunner
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger

class MayerAccessibilityService : AccessibilityService() {

    private val receiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let { dealEvent(it) }
            }
        }
    }

    private val runner = MayerRunner(MayerAccessibilityHelper(this))

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
//        serviceInfo.apply {
//            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
//            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
//            flags = AccessibilityServiceInfo.DEFAULT
//            capabilities.apply {
//                AccessibilityServiceInfo.CAPABILITY_CAN_PERFORM_GESTURES + AccessibilityServiceInfo.CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT + AccessibilityServiceInfo.CAPABILITY_CAN_TAKE_SCREENSHOT
//            }
//        }
        registerReceiver(receiver, IntentFilter(ACTION_SCREENSHOT))
        registerReceiver(receiver, IntentFilter(ACTION_START_SCRIPT))
        registerReceiver(receiver, IntentFilter(ACTION_STOP_SCRIPT))
    }

    override fun onInterrupt() {
        Logger.debug("onInterrupt")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//        Logger.debug("onAccessibilityEvent")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        unregisterReceiver(receiver)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun dealEvent(intent: Intent) {
        when (intent.action) {
            ACTION_SCREENSHOT -> {
                GlobalScope.launch {
                    runner.testScreenshot()
                }
            }
            ACTION_START_SCRIPT -> {
                GlobalScope.launch {
                    val scriptName = intent.getStringExtra("scriptName")
                    Logger.debug("start script: $scriptName")
                    if (scriptName != null) {
                        runner.startScript(scriptName)
                    }
                }
            }
            ACTION_STOP_SCRIPT -> {
                runner.stopScript()
            }
        }
    }

    @Keep
    companion object {
        var instance: MayerAccessibilityService? = null
        const val ACTION_SCREENSHOT = "icu.pboymt.mayer.accessibility.ACTION_SCREENSHOT"
        const val ACTION_START_SCRIPT = "icu.pboymt.mayer.accessibility.ACTION_START_SCRIPT"
        const val ACTION_STOP_SCRIPT = "icu.pboymt.mayer.accessibility.ACTION_STOP_SCRIPT"
        const val ACTION_SCRIPT_STATUS_NOTIFICATION =
            "icu.pboymt.mayer.accessibility.ACTION_SCRIPT_STATUS_NOTIFICATION"
        val isScriptRunning: Boolean
            get() {
                return instance?.runner?.script != null
            }
    }

}