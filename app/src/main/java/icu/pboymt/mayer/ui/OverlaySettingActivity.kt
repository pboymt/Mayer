package icu.pboymt.mayer.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import icu.pboymt.mayer.MayerFloatingService
import icu.pboymt.mayer.ui.component.PageTitle
import icu.pboymt.mayer.ui.theme.MayerTheme
import org.tinylog.kotlin.Logger

class OverlaySettingActivity : ComponentActivity() {

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia(), ::processPickedImage)
    private val imageBitmap: MutableState<ImageBitmap?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (imageBitmap.value != null) {
                        BackgroundImage()
                    }
                    MainContent()
                }
            }
        }
    }

    /**
     * 处理从相册选择的图片
     */
    private fun processPickedImage(uri: Uri?) {
        if (uri == null) {
            Logger.debug("Picked image uri is null")
            return
        }
        Logger.debug("Picked image uri: $uri")
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        imageBitmap.value = bitmap.asImageBitmap()
//        val mat = Mat()
//        Utils.bitmapToMat(bitmap, mat)
//        Logger.debug("Picked image mat: $mat")
//        // Show Mat size
//        val size = mat.size()
//        Logger.debug("Picked image mat size: ${size.width}x${size.height}")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent() {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            containerColor = Color.Black.copy(alpha = 0.2f),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 16.dp)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End,
            ) {
                PageTitle("Overlay Setting")
                Button(onClick = {
                    pickMedia.launch(PickVisualMediaRequest())
                }) {
                    Text("Pick Image")
                }
                FloatingWindowSizeSlider()
            }
        }
    }

    @Composable
    fun BackgroundImage() {
        // Load to Image
        Image(
            bitmap = imageBitmap.value!!,
            contentDescription = "Template",
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.BottomCenter,
        )
    }

    @Composable
    fun FloatingWindowSizeSlider() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Floating Window Size")
            Slider(
                value = MayerFloatingService.floatingSize.toFloat(),
                onValueChange = {
                    MayerFloatingService.floatingSize = it.toInt()
                },
                valueRange = 1f..100f,
                steps = 100,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            Text(
                text = "${MayerFloatingService.floatingSize}dp",
                modifier = Modifier.padding(horizontal = 4.dp)
            )

        }
    }
}

@Composable
fun Greeting2(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview3() {
    MayerTheme {
        Greeting2("Android")
    }
}