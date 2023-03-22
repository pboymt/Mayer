// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("mayer");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("mayer")
//      }
//    }
#include <android/hardware_buffer_jni.h>
#include <opencv2/core/mat.hpp>
#include "mayer.h"


//// Convert Android HardwareBuffer to OpenCV Mat
//jlong hardwareBufferToMat(AHardwareBuffer* hardwareBuffer) {
//    AHardwareBuffer_Desc bufferDesc;
//    AHardwareBuffer_describe(hardwareBuffer, &bufferDesc);
//    // Get all pixels data
//    void* buffer;
//    AHardwareBuffer_lock(hardwareBuffer, AHARDWAREBUFFER_USAGE_CPU_READ_OFTEN, -1, nullptr, &buffer);
//    // Create OpenCV Mat
//    auto *mat = new cv::Mat(bufferDesc.height, bufferDesc.width, CV_8UC4, buffer);
//    AHardwareBuffer_unlock(hardwareBuffer, nullptr);
//    return (jlong) mat;
//}

//// JNI function icu.pboymt.mayer.core.MayerAccessibilityHelper.hardwareBufferToMat
//extern "C" JNIEXPORT jlong JNICALL
//Java_icu_pboymt_mayer_core_MayerAccessibilityHelper_hardwareBufferToMat(
//        JNIEnv *env,
//        jobject thiz,
//        jobject hardwareBuffer) {
//    return hardwareBufferToMat(AHardwareBuffer_fromHardwareBuffer(env, hardwareBuffer));
//}


