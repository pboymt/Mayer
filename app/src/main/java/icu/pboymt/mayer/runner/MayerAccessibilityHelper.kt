package icu.pboymt.mayer.runner

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.Path
import android.os.Build
import android.view.Display
import androidx.annotation.RequiresApi
import org.opencv.android.Utils
import org.opencv.core.Mat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MayerAccessibilityHelper(val ac: AccessibilityService) {

    private var lastScreenshot: Bitmap? = null
    private var lastScreenshotMat: Mat? = null
    private var lastScreenshotTime: Long = 0
    private var lastScreenshotMatTime: Long = 0

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun takeScreenshotBitmap() = suspendCoroutine {
        if (lastScreenshot != null && System.currentTimeMillis() - lastScreenshotTime < 1000) {
            it.resume(lastScreenshot)
            return@suspendCoroutine
        }
        lastScreenshot?.recycle()
        lastScreenshot = null
        ac.takeScreenshot(
            Display.DEFAULT_DISPLAY, ac.mainExecutor,
            object : AccessibilityService.TakeScreenshotCallback {
                override fun onSuccess(screenshot: AccessibilityService.ScreenshotResult) {
                    val bitmap =
                        Bitmap.wrapHardwareBuffer(screenshot.hardwareBuffer, screenshot.colorSpace)
                    // set bitmap mutable
                    val nBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true)
                    bitmap?.recycle()
                    screenshot.hardwareBuffer.close()
                    lastScreenshotTime = System.currentTimeMillis()
                    lastScreenshot = nBitmap
                    it.resume(nBitmap)
                }

                override fun onFailure(errorCode: Int) {
                    it.resume(null)
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun takeScreenshotMat() = suspendCoroutine {
        if (lastScreenshotMat != null && System.currentTimeMillis() - lastScreenshotMatTime < 1000) {
            it.resume(lastScreenshotMat)
            return@suspendCoroutine
        }
        lastScreenshotMat?.release()
        lastScreenshotMat = null
        ac.takeScreenshot(
            Display.DEFAULT_DISPLAY, ac.mainExecutor,
            object : AccessibilityService.TakeScreenshotCallback {
                override fun onSuccess(screenshot: AccessibilityService.ScreenshotResult) {
                    val bitmap =
                        Bitmap.wrapHardwareBuffer(screenshot.hardwareBuffer, screenshot.colorSpace)
                    // set bitmap mutable
                    val nBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true)
                    bitmap?.recycle()
                    val mat = Mat()
                    Utils.bitmapToMat(nBitmap, mat)
                    lastScreenshotMatTime = System.currentTimeMillis()
                    lastScreenshotMat = mat
                    it.resume(mat)
                    nBitmap?.recycle()
                    screenshot.hardwareBuffer.close()
                }

                override fun onFailure(errorCode: Int) {
                    it.resume(null)
                }
            }
        )
    }

    fun pressHomeBtn() {
        ac.performGlobalAction(GLOBAL_ACTION_HOME)
    }

    suspend fun click(x: Int, y: Int) = suspendCoroutine {
        val clickPath = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
            lineTo(x.toFloat(), y.toFloat())
        }
        val clickStroke = GestureDescription.StrokeDescription(clickPath, 0, 100)
        val clickGesture = GestureDescription.Builder().addStroke(clickStroke).build()
        if (ac.dispatchGesture(clickGesture, null, null)) {
            it.resume(true)
        } else {
            it.resume(false)
        }
    }

    suspend fun swipe(x1: Int, y1: Int, x2: Int, y2: Int) = suspendCoroutine {
        val swipePath = Path().apply {
            moveTo(x1.toFloat(), y1.toFloat())
            lineTo(x2.toFloat(), y2.toFloat())
        }
        val swipeStroke = GestureDescription.StrokeDescription(swipePath, 0, 100)
        val swipeGesture = GestureDescription.Builder().addStroke(swipeStroke).build()
        if (ac.dispatchGesture(swipeGesture, null, null)) {
            it.resume(true)
        } else {
            it.resume(false)
        }
    }

    fun currentPackageName() = ac.rootInActiveWindow.packageName.toString()

}