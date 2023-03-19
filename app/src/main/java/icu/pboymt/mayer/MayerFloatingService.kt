package icu.pboymt.mayer

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.icu.text.SimpleDateFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import icu.pboymt.mayer.ui.overlay.MyLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger

class MayerFloatingService : Service() {

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("HH:mm:ss")
    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private var isOverlayVisible = false
    private val floatingWindowSize: MutableState<Int> = mutableStateOf(16)
    private val floatingWindowTime: MutableState<Long> = mutableStateOf(0L)
    private val floatingWindowText: MutableState<String> = mutableStateOf("悬浮窗已启动")

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_FLOATING_WINDOW_LOG -> {
                    floatingWindowText.value = intent.getStringExtra("log") ?: "空日志"
                    floatingWindowTime.value =
                        intent.getLongExtra("time", System.currentTimeMillis())
                }
            }
        }
    }

    /**
     * Format System.currentTimeMillis() to Date to String
     */
    @Suppress("SpellCheckingInspection")
    private val Long.hhmmss: String
        @SuppressLint("SimpleDateFormat")
        get() {
            val date = java.util.Date(this)
            return sdf.format(date)
        }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        instance = this
        registerReceiver(broadcastReceiver, IntentFilter(ACTION_FLOATING_WINDOW_LOG))
        showFloatingWindow()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Logger.debug("onDestroy")
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
        hideFloatingWindow()
        instance = null
    }

    /**
     * 检测悬浮窗权限
     */
    private val floatingWindowPermission: Boolean
        get() {
            return Settings.canDrawOverlays(this)
        }

    /**
     * 获取布局参数
     */
    private val layoutParams: WindowManager.LayoutParams
        get() {
            return WindowManager.LayoutParams().apply {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                gravity = Gravity.START or Gravity.BOTTOM
                x = 0
                y = 0
                format = PixelFormat.RGBA_8888
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
        }

    private val composeView by lazy {
        ComposeView(this.applicationContext).apply {
            setContent {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(floatingWindowSize.value.dp)
                        .background(Color.Black)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = floatingWindowSize.value.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxHeight()
//                                .fillMaxWidth(),
//                            contentAlignment = Alignment.CenterStart,
//                        ) {
//
//                        }
                        Text(
                            text = floatingWindowTime.value.hhmmss,
                            fontSize = floatingWindowSize.value.sp / 2,
                            color = Color.White,
                            modifier = Modifier.padding(end = floatingWindowSize.value.dp / 2)
                        )
                        Text(
                            text = floatingWindowText.value,
                            fontSize = floatingWindowSize.value.sp / 2,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    /**
     * 创建悬浮窗，使用 Android Jetpack Compose，并显示
     */
    private fun showFloatingWindow() {
        if (!floatingWindowPermission) {
            return
        }
        if (isOverlayVisible) {
            return
        }
        Logger.debug("showFloatingWindow")
        val lifecycleOwner = MyLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        ViewTreeLifecycleOwner.set(composeView, lifecycleOwner)
        composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        val viewModelStore = ViewModelStore()
        ViewTreeViewModelStoreOwner.set(composeView) { viewModelStore }
        val coroutineContext = AndroidUiDispatcher.CurrentThread
        val runRecomposeScope = CoroutineScope(coroutineContext)
        val recompose = Recomposer(coroutineContext)
        composeView.compositionContext = recompose
        runRecomposeScope.launch {
            recompose.runRecomposeAndApplyChanges()
        }
        windowManager.addView(composeView, layoutParams)
        isOverlayVisible = true
        Logger.info("悬浮窗创建成功")
        tellShow()
    }

    /**
     * 隐藏悬浮窗
     */
    private fun hideFloatingWindow() {
        Logger.debug("hideFloatingWindow")
        if (!isOverlayVisible) {
            return
        }
        windowManager.removeView(composeView)
        isOverlayVisible = false
        tellHide()
    }

    private fun tellShow() {
        val intent = Intent(ACTION_FLOATING_WINDOW_SHOW_EVENT)
        sendBroadcast(intent)
    }

    private fun tellHide() {
        val intent = Intent(ACTION_FLOATING_WINDOW_HIDE_EVENT)
        sendBroadcast(intent)
    }

    companion object {
        const val ACTION_FLOATING_WINDOW_SHOW_EVENT =
            "icu.pboymt.mayer.ACTION_FLOATING_WINDOW_SHOW_EVENT"
        const val ACTION_FLOATING_WINDOW_HIDE_EVENT =
            "icu.pboymt.mayer.ACTION_FLOATING_WINDOW_HIDE_EVENT"
        const val ACTION_FLOATING_WINDOW_LOG = "icu.pboymt.mayer.ACTION_FLOATING_WINDOW_LOG"
        const val EXTRA_FLOATING_LOG_TEXT = "icu.pboymt.mayer.EXTRA_FLOATING_LOG_TEXT"
        const val EXTRA_FLOATING_LOG_TIME = "icu.pboymt.mayer.EXTRA_FLOATING_LOG_TIME"

        // a instance of MayerFloatingService
        private var instance: MayerFloatingService? = null

        val overlayVisible: Boolean
            get() {
                return if (instance == null) {
                    Logger.debug("MayerFloatingService's instance is null")
                    false
                } else {
                    val result = instance!!.isOverlayVisible
                    Logger.debug("MayerFloatingService's overlayVisible is $result")
                    result
                }
            }
        var floatingSize: Int
            get() = instance?.floatingWindowSize?.value ?: 0
            set(value) {
                instance?.floatingWindowSize?.value = value
            }
        var floatingTime: Long
            get() = instance?.floatingWindowTime?.value ?: 0
            set(value) {
                instance?.floatingWindowTime?.value = value
            }
        var floatingText: String
            get() = instance?.floatingWindowText?.value ?: ""
            set(value) {
                instance?.floatingWindowText?.value = value
            }
    }
}