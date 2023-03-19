package icu.pboymt.mayer.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import icu.pboymt.mayer.MayerAccessibilityService
import icu.pboymt.mayer.MayerFloatingService
import icu.pboymt.mayer.MayerMonitorService
import icu.pboymt.mayer.runner.MayerScript
import icu.pboymt.mayer.scripts.RingScript
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.tinylog.kotlin.Logger

/**
 * 发送广播，让 MayerAccessibilityService 停止脚本
 */
internal fun MainActivity.sendStopScriptBroadcast() {
    val intent = Intent(MayerAccessibilityService.ACTION_START_SCRIPT)
    intent.putExtra("scriptName", MayerScript.STOP_SCRIPT)
    sendBroadcast(intent)
}

/**
 * 检查悬浮窗是否开启
 */
internal fun MainActivity.checkOverlayWindowEnabled(): Boolean {
    val isVisible = MayerFloatingService.overlayVisible
    Logger.debug("Overlay window is ${if (isVisible) "visible" else "invisible"}")
    overlayVisible.value = isVisible
    return isVisible
}

/**
 * 开关悬浮窗
 */
internal fun MainActivity.toggleOverlayWindow() {
    if (checkOverlayPermissionEnabled()) {
        if (overlayVisible.value) {
            stopService(Intent(this, MayerFloatingService::class.java))
        } else {
            startService(Intent(this, MayerFloatingService::class.java))
        }
    } else {
        requestOverlayPermission()
    }
}


/**
 * 请求悬浮窗权限
 */
internal fun MainActivity.requestOverlayPermission() {
    if (checkOverlayPermissionEnabled()) {
        return
    }
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    intent.data = Uri.parse("package:$packageName")
    resultLauncher.launch(intent)
}

/**
 * 打开 AboutActivity
 */
internal fun MainActivity.openAboutActivity() {
    val intent = Intent(this, AboutActivity::class.java)
    startActivity(intent)
}

/**
 * 打开 OverlaySettingActivity
 */
internal fun MainActivity.openOverlaySettingActivity() {
    val intent = Intent(this, OverlaySettingActivity::class.java)
    startActivity(intent)
}

/**
 * 发送广播，让 MayerAccessibilityService 执行截图
 */
internal fun MainActivity.sendTakeScreenshotBroadcast() {
    val intent = Intent(MayerAccessibilityService.ACTION_SCREENSHOT)
    sendBroadcast(intent)
}

/**
 * 发送广播，让 MayerAccessibilityService 执行脚本
 */
internal fun MainActivity.sendRunScriptBroadcast() {
    val intent = Intent(MayerAccessibilityService.ACTION_START_SCRIPT)
    intent.putExtra("scriptName", RingScript.NAME)
    sendBroadcast(intent)
}

/**
 * 检查无障碍服务是否开启
 */
internal fun MainActivity.checkAccessibilityServiceEnabled(open: Boolean = false): Boolean {

    val accessibilityManager =
        getSystemService(ComponentActivity.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices =
        accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

    for (service in enabledServices) {
//            if(service.id === MayerAccessibilityService::class.simpleName){
        if (service.id.startsWith(packageName) && service.id.endsWith("MayerAccessibilityService")) {
            Logger.debug("Accessibility service is enabled")
            accessibilityServiceEnabled.value = true
            return true
        }
    }
    Logger.debug("Accessibility service is not enabled")
    accessibilityServiceEnabled.value = false
    // open accessibility service settings
    if (open) {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
    return false
}

/**
 *  检查前台服务是否开启
 */
internal fun MainActivity.checkForegroundServiceEnabled(): Boolean {
    val isRunning = MayerMonitorService.instance != null
    mayerMonitorServiceEnabled.value = isRunning
    return isRunning
}

/**
 * 切换前台服务
 */
internal fun MainActivity.toggleForegroundService() {
    val isRunning = checkForegroundServiceEnabled()
    if (isRunning) {
        stopForegroundService()
    } else {
        startForegroundService()
    }
}

/**
 * 启动前台服务
 */
internal fun MainActivity.startForegroundService() {
    val intent = Intent(this, MayerMonitorService::class.java)
    startForegroundService(intent)
}

/**
 * 停止前台服务
 */
internal fun MainActivity.stopForegroundService() {
    val intent = Intent(this, MayerMonitorService::class.java)
    stopService(intent)
}

/**
 * 从 Assets 目录中读取图片
 */
internal fun MainActivity.loadImageFromAssets() {
    val inputStream = assets.open("templates/boss-level-high.png")
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val mat = Mat()
    Utils.bitmapToMat(bitmap, mat)
    Logger.debug("Image mat: $mat")
    // Show Mat size
    val size = mat.size()
    Logger.debug("Image mat size: ${size.width}x${size.height}")
}

/**
 * 检查是否有悬浮窗权限
 */
internal fun MainActivity.checkOverlayPermissionEnabled(): Boolean {
    val result = if (!Settings.canDrawOverlays(this)) {
        Logger.debug("Overlay permission is not granted")
        false
    } else {
        Logger.debug("Overlay permission is granted")
        true
    }
    overlayPermissionEnabled.value = result
    return result
}

/**
 * 检查脚本是否正在运行
 */
internal fun MainActivity.checkScriptRunning(): Boolean {
    val result = MayerAccessibilityService.isScriptRunning
    scriptRunning.value = result
    return result
}