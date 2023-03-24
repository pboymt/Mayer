package icu.pboymt.mayer.core.modules

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.hardware.SensorManager
import android.view.OrientationEventListener
import icu.pboymt.mayer.core.MayerAccessibilityHelper

/**
 * 屏幕参数工具模块
 */
@Suppress("MemberVisibilityCanBePrivate")
class MayerDisplay(val helper: MayerAccessibilityHelper) {
    private var mWidth = 0
    private var mHeight = 0
    private var mDensity = 0f
    private var mOrientation = ORIENTATION_LANDSCAPE

    private val orientationListener: OrientationEventListener =
        object : OrientationEventListener(helper.service, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                refresh()
            }
        }

    val width: Int
        get() = mWidth
    val height: Int
        get() = mHeight
    val density: Float
        get() = mDensity
    val isLandscape: Boolean
        get() = when (mOrientation) {
            ORIENTATION_LANDSCAPE -> true
            ORIENTATION_PORTRAIT -> false
            else -> false
        }

    init {
        refresh()
        orientationListener.enable()
    }

    fun refresh() {
        mWidth = helper.service.resources.displayMetrics.widthPixels
        mHeight = helper.service.resources.displayMetrics.heightPixels
        mDensity = helper.service.resources.displayMetrics.density
        mOrientation = helper.service.resources.configuration.orientation
    }

    fun release() {
        orientationListener.disable()
    }
}