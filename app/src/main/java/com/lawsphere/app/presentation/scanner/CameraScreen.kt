package com.lawsphere.app.presentation.scanner

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.GlassSurface
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    generativeModel: GenerativeModel,
    onTextRecognized: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var scannedText by remember { mutableStateOf("") }
    var aiSimplifiedText by remember { mutableStateOf("") }
    var isProcessingAI by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (scannedText.isEmpty()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val provider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder().build()
                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                    } catch (e: Exception) { Log.e("Camera", "Failed", e) }
                    previewView
                }
            )

            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp)) {
                Button(
                    onClick = {
                        val mainExecutor = ContextCompat.getMainExecutor(context)
                        imageCapture?.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val mediaImage = image.image
                                if (mediaImage != null) {
                                    val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
                                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                    recognizer.process(inputImage)
                                        .addOnSuccessListener { visionText ->
                                            scannedText = visionText.text
                                            image.close()
                                        }
                                }
                            }
                        })
                    },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                ) {
                    Icon(Icons.Default.Camera, contentDescription = null, tint = Color.Black)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Document Scanned", color = AccentGold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = GlassSurface),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                    ) {
                        Text(scannedText, color = Color.White, modifier = Modifier.padding(16.dp), fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isProcessingAI) {
                        CircularProgressIndicator(color = AccentGold)
                        Text("Gemini is simplifying...", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }

                    AnimatedVisibility(
                        visible = aiSimplifiedText.isNotEmpty(),
                        enter = fadeIn() + expandVertically()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AccentGold),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Simplified Meaning", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Text(aiSimplifiedText, color = Color.Black, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isProcessingAI = true
                                    try {
                                        val response = generativeModel.generateContent("Explain this legal text in very simple words for a common citizen: $scannedText")
                                        aiSimplifiedText = response.text ?: "Could not simplify."
                                    } catch (e: Exception) {
                                        aiSimplifiedText = "Error: ${e.localizedMessage}"
                                    }
                                    isProcessingAI = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            enabled = !isProcessingAI
                        ) {
                            Text("Simplify with Gemini", color = Color.Black)
                        }

                        IconButton(
                            onClick = { onTextRecognized(scannedText) },
                            modifier = Modifier.background(AccentGold, CircleShape)
                        ) {
                            Icon(Icons.Default.Send, null, tint = Color.Black)
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.padding(top = 48.dp, end = 16.dp).align(Alignment.TopEnd).background(Color.Black.copy(0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}