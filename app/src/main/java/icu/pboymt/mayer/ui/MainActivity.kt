package icu.pboymt.mayer.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreBooleanSettingState
import com.alorma.compose.settings.ui.SettingsSwitch
import icu.pboymt.mayer.*
import icu.pboymt.mayer.R
import icu.pboymt.mayer.core.Script
import icu.pboymt.mayer.scripts.RingScript
import icu.pboymt.mayer.scripts.SingleScript
import icu.pboymt.mayer.ui.component.ServiceStatusCard
import icu.pboymt.mayer.ui.component.SystemInfo
import icu.pboymt.mayer.ui.component.TabTitle
import icu.pboymt.mayer.ui.navigation.MayerRoute
import icu.pboymt.mayer.ui.prefs.PrefScriptRingActivity
import icu.pboymt.mayer.ui.theme.MayerTheme
import icu.pboymt.mayer.utils.PrefKeys
import icu.pboymt.mayer.utils.dataStore
import org.opencv.android.OpenCVLoader
import org.tinylog.kotlin.Logger
import kotlin.reflect.KClass


class MainActivity : ComponentActivity() {

    internal val accessibilityServiceEnabled: MutableState<Boolean> = mutableStateOf(false)
    internal val mayerMonitorServiceEnabled: MutableState<Boolean> = mutableStateOf(false)
    internal val overlayPermissionEnabled: MutableState<Boolean> = mutableStateOf(false)
    internal val overlayVisible: MutableState<Boolean> = mutableStateOf(false)
    internal val scriptRunning: MutableState<Boolean> = mutableStateOf(false)

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MayerMonitorService.ACTION_PONG -> {
                    val isRunning =
                        intent.getBooleanExtra(
                            MayerMonitorService.EXTRA_MAYER_MONITOR_RUNNING,
                            false
                        )
                    Logger.debug("MayerMonitorService is running: $isRunning")
                    mayerMonitorServiceEnabled.value = isRunning
                }
                MayerFloatingService.ACTION_FLOATING_WINDOW_SHOW_EVENT -> {
                    overlayVisible.value = true
                }
                MayerFloatingService.ACTION_FLOATING_WINDOW_HIDE_EVENT -> {
                    overlayVisible.value = false
                }
                MayerAccessibilityService.ACTION_SCRIPT_STATUS_NOTIFICATION -> {
                    checkScriptRunning()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initModules()
        initReceiver()
        setContent {
            MayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MayerApp(
                        pages = listOf(
                            MayerPage(
                                route = MayerRoute.HOME,
                                title = stringResource(id = R.string.tab_home),
                                content = { TabHome() }
                            ),
                            MayerPage(
                                route = MayerRoute.SCRIPTS,
                                title = stringResource(id = R.string.tab_scripts),
                                content = { TabScripts() }
                            ),
                            MayerPage(
                                route = MayerRoute.SETTINGS,
                                title = stringResource(id = R.string.tab_settings),
                                content = { TabSettings() }
                            )
                        ),
                        scriptRunning = scriptRunning.value,
                    ) {
                        checkScriptRunning()
                        if (scriptRunning.value) {
                            sendStopScriptBroadcast()
                        } else {
                            sendRunScriptBroadcast()
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化 OpenCV
     */
    private fun initModules() {
        if (OpenCVLoader.initDebug()) {
            Logger.info { "OpenCV loaded successfully" }
        } else {
            Logger.error("OpenCV not loaded")
        }
    }

    /**
     * 注册 Broadcast Receiver
     */
    private fun initReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(MayerMonitorService.ACTION_PONG)
        intentFilter.addAction(MayerFloatingService.ACTION_FLOATING_WINDOW_SHOW_EVENT)
        intentFilter.addAction(MayerFloatingService.ACTION_FLOATING_WINDOW_HIDE_EVENT)
        intentFilter.addAction(MayerAccessibilityService.ACTION_SCRIPT_STATUS_NOTIFICATION)
        registerReceiver(broadcastReceiver, intentFilter)
    }

    /**
     * 切换前台时检查 前台服务 和 无障碍服务 是否开启
     */
    override fun onResume() {
        super.onResume()
        checkAccessibilityServiceEnabled()
        checkForegroundServiceEnabled()
        checkOverlayPermissionEnabled()
        checkOverlayWindowEnabled()
    }

    /**
     * 销毁时注销 Broadcast Receiver
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    /**
     * 打脚本设置 Activity
     */
    private fun startPrefScriptActivity(uuid: String) {
        when (uuid) {
//            SingleScript.NAME -> {
//                startActivity(Intent(this, SingleScriptPrefActivity::class.java))
//            }
            RingScript.NAME -> {
                startActivity(Intent(this, PrefScriptRingActivity::class.java))
            }
            else -> {
                Logger.warn("Unknown script uuid: $uuid")
            }
        }
    }

    /**
     * 处理悬浮窗权限回调
     */
    internal val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (checkOverlayPermissionEnabled()) {
                    // 权限已授予
                    Logger.debug("Overlay permission is granted")
                } else {
                    // 权限未授予
                    Logger.debug("Overlay permission is not granted")
                }
            }
        }


    /**
     * Tab Home
     */
    @Composable
    fun TabHome() {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ServiceStatusCard(
                    title = stringResource(R.string.status_title_accessibility_service),
                    enabled = accessibilityServiceEnabled.value,
                ) {
                    checkAccessibilityServiceEnabled(true)
                }
                if (BuildConfig.DEBUG) {
                    ServiceStatusCard(
                        title = stringResource(R.string.status_title_foreground_service),
                        enabled = mayerMonitorServiceEnabled.value,
                    ) {
                        toggleForegroundService()
                    }
                }
                ServiceStatusCard(
                    title = stringResource(R.string.status_title_floating_window),
                    enabled = overlayVisible.value
                ) {
                    toggleOverlayWindow()
                }
            }
            Column(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                SystemInfo()
                if (BuildConfig.DEBUG) {
                    Button(onClick = { openAboutActivity() }) {
                        Text(text = stringResource(id = R.string.btn_open_activity_about))
                    }
                    Button(onClick = {
                        sendTakeScreenshotBroadcast()
                    }) {
                        Text(text = stringResource(R.string.btn_take_screenshot_by_accessibility))
                    }
                    Button(onClick = { loadImageFromAssets() }) {
                        Text(text = "Load image from assets")
                    }
                    Button(onClick = { sendRunScriptBroadcast() }) {
                        Text(text = "Run script by accessibility service")
                    }
                    Button(onClick = { sendStopScriptBroadcast() }) {
                        Text(text = "Stop script by accessibility service")
                    }
                    Button(onClick = { openOverlaySettingActivity() }) {
                        Text(text = stringResource(R.string.btn_open_activity_overlay_setting))
                    }
                }

            }
        }
    }

    private val scriptList = listOf<KClass<*>>(RingScript::class, SingleScript::class).map { cls ->
        cls.annotations.find { it.annotationClass == Script::class } as Script
    }

    /**
     * Tab Scripts
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TabScripts() {
        val selectedScript = remember {
            mutableStateOf(scriptList.first().uuid)
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
        ) {
            scriptList.forEach { script ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedScript.value = script.uuid },
                    leadingContent = {
                        RadioButton(
                            selected = selectedScript.value == script.uuid,
                            onClick = { selectedScript.value = script.uuid }
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = {
                            startPrefScriptActivity(script.uuid)
                        }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    },
                    headlineText = {
                        Text(text = script.name)
                    },
                    supportingText = { Text(text = script.description) },
                    overlineText = { Text(text = script.author) },
                )
                Divider()
            }
        }
    }

    /**
     * Tab Settings
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TabSettings() {
        val stateScriptAllStartApp = rememberPreferenceDataStoreBooleanSettingState(
            key = PrefKeys.Script.All.StartApp.key,
            defaultValue = PrefKeys.Script.All.StartApp.default,
            dataStore = dataStore
        )
        val stateScriptAllStartWfBilibili = rememberPreferenceDataStoreBooleanSettingState(
            key = PrefKeys.Script.All.StartWfBilibili.key,
            defaultValue = PrefKeys.Script.All.StartWfBilibili.default,
            dataStore = dataStore
        )
        Column {
            SettingsSwitch(
                state = stateScriptAllStartApp,
                title = { Text(text = PrefKeys.Script.All.StartApp.title) }
            ) {

            }
            SettingsSwitch(
                state = stateScriptAllStartWfBilibili,
                title = { Text(text = PrefKeys.Script.All.StartWfBilibili.title) }
            ) {

            }
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openOverlaySettingActivity() },
                headlineText = {
                    Text(text = stringResource(R.string.btn_open_activity_overlay_setting))
                },
            )
        }
    }

    companion object {
        init {
            System.loadLibrary("mayer")
        }
    }

}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MayerTheme {
        TabTitle("Android")
    }
}