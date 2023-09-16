package com.fwhyn.mobill.ui.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.fwhyn.mobill.R
import com.fwhyn.mobill.ui.theme.MoBillTheme
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScanActivity : ComponentActivity() {

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, BarcodeScanActivity::class.java)
            context.startActivity(starter)
        }
    }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    // Blocking camera operations are performed using this executor
    private lateinit var cameraExecutor: ExecutorService
    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            MoBillTheme {
                // A surface container using the 'background' color from the theme
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    ScanInstruction()


                }
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        // Shut down our background executor
        cameraExecutor.shutdown()
    }
}

@Composable
fun ScanInstruction(
    modifier: Modifier = Modifier,
    text: String = LocalContext.current.getString(R.string.please_scan_barcode_or_qrcode)
) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = modifier.padding(16.dp),
            text = text,
            color = Color.Black
        )
    }
}

@Composable
fun CameraPreview(cameraProviderFuture: ListenableFuture<ProcessCameraProvider>) {

    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = PreviewView.ScaleType.FILL_START
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                post {
                    cameraProviderFuture.addListener(
                        Runnable
                        {
                            val cameraProvider = cameraProviderFuture.get()
                            bindPreview(
                                cameraProvider,
                                lifecycleOwner,
                                this,
                            )
                        },
                        ContextCompat.getMainExecutor(context)
                    )
                }
            }
        }
    )
}

fun bindPreview(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    cameraPreview: PreviewView,
) {
//    val preview: androidx.camera.core.Preview = androidx.camera.core.Preview.Builder()
//        .setTargetAspectRatio(RATIO_16_9)
//        .build()
//
//    val cameraSelector: CameraSelector = CameraSelector.Builder()
//        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//        .build()
//
//    preview.setSurfaceProvider(cameraPreview.surfaceProvider)
//
//    // ----------------------------------------------------------------
//    if (isDestroyed || isFinishing) {
//        //This check is to avoid an exception when trying to re-bind use cases but user closes the activity.
//        //java.lang.IllegalArgumentException: Trying to create use case mediator with destroyed lifecycle.
//        return
//    }
//
//    cameraProvider.unbindAll()
//
//    val imageAnalysis = ImageAnalysis.Builder()
//        .setTargetResolution(Size(cameraPreview.width, cameraPreview.height))
//        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//        .build()
//
//    val orientationEventListener = object : OrientationEventListener(this as Context) {
//        override fun onOrientationChanged(orientation: Int) {
//            // Monitors orientation values to determine the target rotation value
//            val rotation: Int = when (orientation) {
//                in 45..134 -> Surface.ROTATION_270
//                in 135..224 -> Surface.ROTATION_180
//                in 225..314 -> Surface.ROTATION_90
//                else -> Surface.ROTATION_0
//            }
//
//            imageAnalysis.targetRotation = rotation
//        }
//    }
//    orientationEventListener.enable()
//
//    //switch the analyzers here, i.e. MLKitBarcodeAnalyzer, etc
//    class ScanningListener : ScanningResultListener {
//        override fun onScanned(result: String) {
//            runOnUiThread {
//                imageAnalysis.clearAnalyzer()
//                cameraProvider.unbindAll()
//                ScannerResultDialog.newInstance(
//                    result,
//                    object : ScannerResultDialog.DialogDismissListener {
//                        override fun onDismiss() {
//                            bindPreview(cameraProvider)
//                        }
//                    })
//                    .show(supportFragmentManager, ScannerResultDialog::class.java.simpleName)
//            }
//        }
//    }
//
//    val analyzer: ImageAnalysis.Analyzer = MLKitBarcodeAnalyzer(ScanningListener())
//
//    imageAnalysis.setAnalyzer(cameraExecutor, analyzer)
//
//    preview.setSurfaceProvider(cameraPreview.surfaceProvider)
//
//    val camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis, preview)
//
////    if (camera.cameraInfo.hasFlashUnit() == true) {
////        binding.ivFlashControl.visibility = View.VISIBLE
////
////        binding.ivFlashControl.setOnClickListener {
////            camera.cameraControl.enableTorch(!flashEnabled)
////        }
////
////        camera.cameraInfo.torchState.observe(this) {
////            it?.let { torchState ->
////                if (torchState == TorchState.ON) {
////                    flashEnabled = true
////                    binding.ivFlashControl.setImageResource(R.drawable.ic_round_flash_on)
////                } else {
////                    flashEnabled = false
////                    binding.ivFlashControl.setImageResource(R.drawable.ic_round_flash_off)
////                }
////            }
////        }
////    }
}

// ----------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun AllPreview() {
    MoBillTheme {
        ScanInstruction()
    }
}