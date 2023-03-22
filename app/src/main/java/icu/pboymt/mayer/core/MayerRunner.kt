package icu.pboymt.mayer.core

import android.content.Intent
import icu.pboymt.mayer.MayerAccessibilityService
import icu.pboymt.mayer.scripts.RingScript
import icu.pboymt.mayer.scripts.SingleScript
import kotlinx.coroutines.delay
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.tinylog.kotlin.Logger


class MayerRunner(private val helper: MayerAccessibilityHelper) {

    var script: MayerScript? = null

    //    @OptIn(DelicateCoroutinesApi::class)
    suspend fun startScript(name: String) {
        if (script != null) {
            Logger.debug("Script is running")
            if (name == MayerScript.STOP_SCRIPT) {
                script?.stop()
            }
            return
        }
        try {
            val scriptClass = when (name) {
                RingScript.NAME -> RingScript::class
                SingleScript.NAME -> SingleScript::class
                else -> null
            }
            if (scriptClass != null) {
                script = scriptClass.constructors.first().call(helper)
                notifyScriptStatus()
                script!!.loadSettingsFromDataStore()
                script!!.beforePlay()
                script!!.play()
                script!!.afterPlay()
            } else {
                Logger.debug("Script not found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            notifyScriptStatus()
            Logger.info("Script stopped")
            script = null
        }
    }

    fun stopScript() {
        script?.stop()
    }

    private fun notifyScriptStatus(){
        val intent = Intent(MayerAccessibilityService.ACTION_SCRIPT_STATUS_NOTIFICATION)
        helper.ac.sendBroadcast(intent)
    }

    @Suppress("unused")
    suspend fun testGesture() {
        Logger.debug("Start running")
        helper.pressHomeBtn()
        delay(5000L)
//        val result1 = helper.click(845, 1260)
//        Logger.debug("Click result: $result1")
//        delay(1000L)
//        helper.click(200, 50)
//        delay(2000L)
        helper.swipe(100, 200, 900, 200)
        Logger.debug("End running")
    }

    suspend fun testScreenshot() {
//        helper.pressHomeBtn()
//        delay(5000L)
        val bitmap = helper.takeScreenshotBitmap()
        if (bitmap == null) {
            Logger.debug("Screenshot is null")
            return
        }
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        Logger.debug("Screenshot mat: $mat")
        // Show Mat size
        val size = mat.size()
        Logger.debug("Screenshot mat size: ${size.width}x${size.height}")
    }
}

