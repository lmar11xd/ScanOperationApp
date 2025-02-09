package com.lmar.scanoperation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lmar.scanoperation.core.util.Expression
import com.lmar.scanoperation.ui.composable.CustomTextField
import com.lmar.scanoperation.ui.theme.ScanOperationTheme
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var photoPath: String? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    private var reconigzerText by mutableStateOf("10+4(40-15)")
    private var imageBitmap by mutableStateOf<ImageBitmap?>(null)

    private var resultExpression by mutableStateOf("")

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                captureImage()
            } else {
                Toast.makeText(this, "No tiene permiso para acceder a la cámara", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if(success) {
                photoPath?.let { path ->
                    val bitmap = BitmapFactory.decodeFile(path)
                    recognizeText(bitmap)
                    imageBitmap = bitmap.asImageBitmap()
                }
            }
        }

        setContent {
            ScanOperationTheme {
                TextRecognitionScreen()
            }
        }
    }

    private fun recognizeText(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { ocrText ->
                reconigzerText = ocrText.text
                processExpression(ocrText.text)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Falló al escanear texto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", "jpg", storageDir).apply {
            photoPath = absolutePath
        }
    }

    private fun captureImage() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Ocurrió un error al crear el archivo", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            val photoUri: Uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", it)
            takePictureLauncher.launch(photoUri)
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun TextRecognitionScreen() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scanner),
                        contentDescription = "Escanear operación"
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scan Operation App",
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                if(imageBitmap == null) {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        painter = painterResource(id = R.drawable.image_24),
                        contentDescription = "Imagen por defecto"
                    )
                } else {
                    imageBitmap?.let {
                        Image(
                            modifier = Modifier
                                .height(200.dp),
                            painter = BitmapPainter(it),
                            contentDescription = "Imagen capturada"
                        )
                    }
                }

                SelectionContainer(
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                ) {
                    CustomTextField(value = reconigzerText, label = "Operación", onValueChange = { reconigzerText = it })
                }

                Button(
                    onClick = {}
                ) {
                    Text(text = "Calcular")
                }

                SelectionContainer(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    CustomTextField(value = resultExpression, label = "Resultado", onValueChange = { resultExpression = it })
                }

            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun TextRecognitionScreenPreview() {
        ScanOperationTheme {
            TextRecognitionScreen()
        }
    }

    private fun processExpression(expression: String) {
        val isValid = Expression.isValidExpression(expression)
        resultExpression = if(isValid) {
            Expression.evaluateExpression(expression).toString()
        } else {
            Toast.makeText(this, "¡Expresión no válida!", Toast.LENGTH_SHORT).show()
            "0"
        }
    }
}

