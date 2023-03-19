package icu.pboymt.mayer.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import icu.pboymt.mayer.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemInfo() {
    val ctx = LocalContext.current.applicationContext

    Column {
        // 应用 Version Name
        SimpleListItem(title = "应用版本号", content = BuildConfig.VERSION_NAME)
        // 应用 Version Code
        SimpleListItem(title = "应用构建号", content = BuildConfig.VERSION_CODE.toString())
        // OpenCV Version Name
        SimpleListItem(
            title = "OpenCV 版本",
            content = org.opencv.android.OpenCVLoader.OPENCV_VERSION
        )
        // Android Version Name
        SimpleListItem(title = "Android 版本", content = android.os.Build.VERSION.RELEASE)
        // Android Version Code
        SimpleListItem(title = "Android API", content = android.os.Build.VERSION.SDK_INT.toString())
        // Screen Width
        SimpleListItem(
            title = "屏幕宽度",
            content = ctx.resources.displayMetrics.widthPixels.toString()
        )
        // Screen Height
        SimpleListItem(
            title = "屏幕高度",
            content = ctx.resources.displayMetrics.heightPixels.toString()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleListItem(
    title: String,
    content: String,
    onClick: (() -> Unit)? = null
) {
    val modifier = onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
    ListItem(
        modifier = modifier,
        headlineText = {
            Text(text = title)
        },
        trailingContent = {
            Text(text = content)
        }
    )
}