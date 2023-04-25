package icu.pboymt.mayer.core

import icu.pboymt.mayer.core.modules.MayerDisplay
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.tinylog.kotlin.Logger

class MayerTemplatesHelper(private val script: MayerScript) {

    private val helper = script.helper
    private val imagePool = mutableMapOf<String, Mat>()
    private val screenWidth = helper.service.resources.displayMetrics.widthPixels

    fun getImage(name: String): Mat? {
        return if (imagePool.containsKey(name)) {
            imagePool[name]
        } else {
            Logger.warn("Image is not preloaded: $name")
            val mat = loadImage(name)
            imagePool[name] = mat
            mat
        }
    }

    fun loadImages(vararg names: String) {
        names.forEach {
            if (!imagePool.containsKey(it)) {
                Logger.debug("Load image: $it")
                val mat = loadImage(it)
                imagePool[it] = mat
            }
        }
    }

    fun newLoadImages(vararg names: String) {
        val assetsType = chooseAssets()
        names.forEach {
            if (!imagePool.containsKey(it)) {
                Logger.debug("Load image: $it")
                val mat = newLoadImage(it, assetsType)
                imagePool[it] = mat
            }
        }
    }

    private fun chooseAssets(): String? {
        return when (script.display.width) {
            540 -> if (script.display.height >= 960) "540x960+" else null
            720 -> if (script.display.height >= 1280) "720x1280+" else null
            1080 -> if (script.display.height >= 1920) "1080x1920+" else null
            1220 -> if (script.display.height >= 2170) "1220x2170+" else null
            1440 -> if (script.display.height >= 2560) "1440x2560+" else null
            2160 -> if (script.display.height >= 3840) "2160x3840+" else null
            else -> null
        }
    }

    private fun loadImage(name: String): Mat {
        return helper.service.assets.open(name).use {
            val bitmap = android.graphics.BitmapFactory.decodeStream(it)
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)
            bitmap.recycle()
            if (screenWidth != 1080) {
                val scale = screenWidth / 1080.0
                val newSize = mat.size().let { size ->
                    Size(size.width * scale, size.height * scale)
                }
                val newMat = Mat(newSize, mat.type())
                Imgproc.resize(mat, newMat, mat.size(), scale, scale)
                mat.release()
                newMat
            } else {
                mat
            }
        }
    }

    private fun newLoadImage(name: String, assetsType: String?): Mat {
        val assetPath = "templates/${assetsType ?: "1080x1920+"}/$name.tpl"
        return helper.service.assets.open(assetPath).use {
            val bitmap = android.graphics.BitmapFactory.decodeStream(it)
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)
            bitmap.recycle()
            if (assetsType == null) {
                val scale = screenWidth / 1080.0
                val newSize = mat.size().let { size ->
                    Size(size.width * scale, size.height * scale)
                }
                val newMat = Mat(newSize, mat.type())
                Imgproc.resize(mat, newMat, mat.size(), scale, scale)
                mat.release()
                newMat
            } else {
                mat
            }
        }
    }

    @Suppress("unused")
    fun releaseImage(name: String) {
        if (imagePool.containsKey(name)) {
            imagePool[name]?.release()
            imagePool.remove(name)
        }
    }

    fun releaseAll() {
        imagePool.forEach {
            it.value.release()
        }
        imagePool.clear()
    }

}