package icu.pboymt.mayer.core

import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.tinylog.kotlin.Logger

class MayerTemplatesHelper(private val helper: MayerAccessibilityHelper) {

    private val imagePool = mutableMapOf<String, Mat>()
    private val screenWidth = helper.ac.resources.displayMetrics.widthPixels

    fun getImage(name: String): Mat? {
        return if (imagePool.containsKey(name)) {
            imagePool[name]
        } else {
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

    private fun loadImage(name: String): Mat {
        return helper.ac.assets.open(name).use {
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
            }else{
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