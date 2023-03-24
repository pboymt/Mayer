package icu.pboymt.mayer.core

import android.content.Intent
import android.util.Log
import icu.pboymt.mayer.core.modules.MayerDisplay
import icu.pboymt.mayer.utils.dataStore
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import org.tinylog.kotlin.Logger
import kotlin.system.measureTimeMillis

@Suppress("unused")
abstract class MayerScript(val helper: MayerAccessibilityHelper) {

    private val tpl = MayerTemplatesHelper(this)
    protected val ds = helper.service.applicationContext.dataStore

    protected var running = true

    init {
        Log.i(TAG, "Script ${this::class.java.simpleName} loading")
        for (method in this::class.java.declaredMethods) {
            val imagesAnnotation = method.getAnnotation(Step::class.java)
            if (imagesAnnotation != null) {
                tpl.loadImages(*imagesAnnotation.templates)
            }
        }
        Log.i(TAG, "Script ${this::class.java.simpleName} loaded")
    }

    /**
     * 从数据存储中加载设置
     */
    open suspend fun loadSettingsFromDataStore() {
        Logger.info("Loading settings from data store")
    }

    /**
     * 脚本开始前执行
     */
    open suspend fun beforePlay() {
        Log.i(TAG, "Script ${this::class.java.simpleName} start running")
    }

    /**
     * 脚本结束后执行
     */
    fun afterPlay() {
        tpl.releaseAll()
        display.release()
        Log.i(TAG, "Script ${this::class.java.simpleName} end running")
    }

    /**
     * 脚本主体
     */
    abstract suspend fun play()

    /**
     * 停止脚本
     */
    fun stop() {
        running = false
    }

    /// 快捷方法

    /**
     * 在屏幕截图中查找模板图片，并返回模板尺寸和匹配度最高的点、匹配度
     */
    private suspend fun findMaxMatch(template: String): Triple<Pair<Int, Int>, Pair<Int, Int>, Double> {
        var x: Int
        var y: Int
        var width: Int
        var height: Int
        var threshold: Double
        val timeCost = measureTimeMillis {
            val screen = helper.takeScreenshotMat()!!
            val point = tpl.getImage(template)!!
            val result = Mat()

            Imgproc.matchTemplate(screen, point, result, Imgproc.TM_CCOEFF_NORMED)
            val minMaxLoc = Core.minMaxLoc(result)
            val maxLoc = minMaxLoc.maxLoc
            x = maxLoc.x.toInt()
            y = maxLoc.y.toInt()
            width = point.width()
            height = point.height()
            threshold = minMaxLoc.maxVal
            result.release()
        }
        Log.d(TAG, "Find $template in ${timeCost}ms")
        return Triple(Pair(x, y), Pair(width, height), threshold)
    }

    /**
     * 在屏幕截图中的指定区域查找模板图片，并返回模板尺寸和匹配度最高的点、匹配度
     */
    suspend fun findMaxMatchInRegion(
        template: String,
        region: Rect
    ): Triple<Pair<Int, Int>, Pair<Int, Int>, Double> {
        var x = region.x
        var y = region.y
        var width: Int
        var height: Int
        var threshold: Double
        val timeCost = measureTimeMillis {
            val screen = helper.takeScreenshotMat()!!
            val point = tpl.getImage(template)!!
            val result = Mat()
            val roi = Mat(screen, region)
            Imgproc.matchTemplate(roi, point, result, Imgproc.TM_CCOEFF_NORMED)
            val minMaxLoc = Core.minMaxLoc(result)
            val maxLoc = minMaxLoc.maxLoc
            x += maxLoc.x.toInt()
            y += maxLoc.y.toInt()
            width = point.width()
            height = point.height()
            threshold = minMaxLoc.maxVal
            result.release()
        }
        Log.d(TAG, "Find $template in ${timeCost}ms")
        return Triple(Pair(x, y), Pair(width, height), threshold)
    }

    /**
     * 点击图片
     */
    suspend fun click(template: String, threshold: Double = 0.9): Boolean {
        val result = findMaxMatch(template)
        val (pos, size, thr) = result
        if (thr < threshold) return false
        // 根据图片尺寸随机点击
        val (x, y) = pos
        val (width, height) = size
        val randomX = (x + (Math.random() * width).toInt())
        val randomY = (y + (Math.random() * height).toInt())
        return helper.click(randomX, randomY)
    }

    /**
     * 查找图片是否存在
     */
    suspend fun exists(template: String, threshold: Double = 0.9): Boolean {
        val result = findMaxMatch(template)
        val (_, _, thr) = result
        return thr >= threshold
    }

    /**
     * 根据包名打开应用
     */
    fun launchPackage(packageName: String): Boolean {
        return try {
            val packageManager = helper.service.packageManager
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            helper.service.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    val currentPackage: String
        get() {
            return helper.currentPackageName()
        }

    val display = MayerDisplay(helper)

    inner class Logging {
        fun i(message: String, vararg args: Any?) {
            Logger.info(message, *args)
        }
    }

    val log = Logging()

    companion object {
        private const val TAG = "MayerScript"
        const val STOP_SCRIPT = "__STOP_SCRIPT__"
    }

}