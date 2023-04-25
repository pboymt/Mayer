package icu.pboymt.mayer.core

import org.opencv.core.Mat
import org.opencv.core.Rect

@Suppress("unused")
data class MayerTemplate(val mat: Mat, val roi: Region, val anchor: Anchor, val sr: Ratio) {

    /**
     * 模板的宽度
     */
    val width: Int
        get() = mat.width()

    /**
     * 模板的高度
     */
    val height: Int
        get() = mat.height()

    /**
     * 在传入的屏幕截图中查找模板
     */
    fun find(screen: Mat) {
        // 先根据 sr 对屏幕截图进行裁剪
        val screenRegion = getScreenRegion(screen)
        val screenRoi = screen.submat(screenRegion)
    }

    /**
     * 获取模板所处场景在屏幕截图中的有效区域
     */
    private fun getScreenRegion(screen: Mat): Rect {
        return when (sr) {
            Ratio.RATIO_9_16 -> {
                val w = screen.width()
                val h = screen.width() / 9 * 16
                val x = 0
                val y = (screen.height() - h) / 2
                Rect(x, y, w, h)
            }
            Ratio.RATIO_1_2 -> {
                val w = screen.width()
                val h = screen.width() * 2
                val x = 0
                val y = (screen.height() - h) / 2
                Rect(x, y, w, h)
            }
            Ratio.RATIO_3_4 -> {
                val w = screen.width()
                val h = screen.height() * 4 / 3
                val x = 0
                val y = (screen.height() - h) / 2
                Rect(x, y, w, h)
            }
            Ratio.RATIO_FULL -> {
                val w = screen.width()
                val h = screen.height()
                val x = 0
                val y = 0
                Rect(x, y, w, h)
            }
        }
    }

    /**
     * 获取模板在屏幕截图中的存在区域
     */
    private fun getTemplateROI(screenRegion: Rect): Rect {
        return when (anchor) {
            Anchor.TOP_LEFT -> {
                val x = screenRegion.x + screenRegion.width * roi.x / 100
                val y = screenRegion.y + screenRegion.height * roi.y / 100
                val w = screenRegion.width * roi.w / 100
                val h = screenRegion.height * roi.h / 100
                Rect(x, y, w, h)
            }
            Anchor.TOP_RIGHT -> {
                val x = screenRegion.x - screenRegion.width * roi.x / 100
                val y = screenRegion.y + screenRegion.height * roi.y / 100
                val w = screenRegion.width * roi.w / 100
                val h = screenRegion.height * roi.h / 100
                Rect(x, y, w, h)
            }
            Anchor.BOTTOM_LEFT -> {
                val x = screenRegion.x + screenRegion.width * roi.x / 100
                val y = screenRegion.y - screenRegion.height * roi.y / 100
                val w = screenRegion.width * roi.w / 100
                val h = screenRegion.height * roi.h / 100
                Rect(x, y, w, h)
            }
            Anchor.BOTTOM_RIGHT -> {
                val x = screenRegion.x - screenRegion.width * roi.x / 100
                val y = screenRegion.y - screenRegion.height * roi.y / 100
                val w = screenRegion.width * roi.w / 100
                val h = screenRegion.height * roi.h / 100
                Rect(x, y, w, h)
            }
            Anchor.CENTER_LEFT -> {
                val x = screenRegion.x + screenRegion.width * roi.x / 100
                val y = screenRegion.y + screenRegion.height / 2 + screenRegion.height * roi.y / 100
                val w = screenRegion.width * roi.w / 100
                val h = screenRegion.height * roi.h / 100
                Rect(x, y, w, h)
            }
            Anchor.CENTER_RIGHT -> {
                val x = screenRegion.x - screenRegion.width * roi.x / 100
                val y = screenRegion.y + screenRegion.height / 2 + screenRegion.height * roi.y / 100
                val w = screenRegion.width * roi.w / 100
                val h = screenRegion.height * roi.h / 100
                Rect(x, y, w, h)
            }
            Anchor.CENTER_TOP -> {
                val x = screenRegion.x + screenRegion.width / 2 + screenRegion.width * roi.x / 100
                val y = screenRegion.y + screenRegion.height * roi.y / 100
                val w = screenRegion.width * roi.w / 100
                val h = screenRegion.height * roi.h / 100
                Rect(x, y, w, h)
            }
            Anchor.CENTER_BOTTOM -> {
                val x = screenRegion.x + screenRegion.width / 2 + screenRegion.width * roi.x / 100
                val y = screenRegion.y - screenRegion.height * roi.y / 100
                val w = screenRegion.width * roi.w / 100
                val h = screenRegion.height * roi.h / 100
                Rect(x, y, w, h)
            }
            Anchor.CENTER -> {
                val x = screenRegion.x + screenRegion.width / 2 + screenRegion.width * roi.x / 100
                val y = screenRegion.y + screenRegion.height / 2 + screenRegion.height * roi.y / 100
                val w = screenRegion.width * roi.w / 100
                val h = screenRegion.height * roi.h / 100
                Rect(x, y, w, h)
            }
        }
    }

    /**
     * 模板在屏幕中的锚点
     */
    enum class Anchor(val value: Int) {
        CENTER(0),
        TOP_LEFT(1),
        TOP_RIGHT(2),
        BOTTOM_LEFT(3),
        BOTTOM_RIGHT(4),
        CENTER_LEFT(5),
        CENTER_RIGHT(6),
        CENTER_TOP(7),
        CENTER_BOTTOM(8),
    }

    /**
     * 模板所处场景在屏幕中的有效区域，均以屏幕中央为中心
     */
    enum class Ratio(val value: Int) {
        RATIO_FULL(0),
        RATIO_1_2(1),
        RATIO_3_4(2),
        RATIO_9_16(3),
    }

    /**
     * 模板在屏幕中的存在区域，所有值均为 -100 到 100 的整数，计算时会除以 100
     */
    data class Region(val x: Int, val y: Int, val w: Int, val h: Int)
}
