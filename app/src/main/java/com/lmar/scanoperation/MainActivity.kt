package com.lmar.scanoperation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lmar.scanoperation.core.util.Expression
import com.lmar.scanoperation.ui.composable.CustomTextField
import com.lmar.scanoperation.ui.composable.IconCard
import com.lmar.scanoperation.ui.theme.ScanOperationTheme
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private var photoPath: String? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    private var reconigzerText by mutableStateOf("")
    private var imageBitmap by mutableStateOf<ImageBitmap?>(null)

    private var resultExpression by mutableStateOf("0")

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
                SelectionContainer(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Escanear Operaciones Matemáticas",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                IconCard(
                    icon = Icons.Default.Notifications,
                    description = "Apunta la cámara a una expresión matemática para escanear o puedes ingresar una expresión y presionar en \"Calcular\" para mostrar el resultado."
                )

                if(imageBitmap == null) {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        painter = painterResource(id = R.drawable.image_24),
                        contentDescription = "Imagen por defecto"
                    )
                } else {
                    imageBitmap?.let {
                        Image(
                            modifier = Modifier
                                .height(240.dp),
                            painter = BitmapPainter(it),
                            contentDescription = "Imagen capturada"
                        )
                    }
                }

                IconCard(
                    icon = Icons.Default.Check,
                    title = "Ejemplos",
                    description = "4+6 || 3+5(2-8) || (4-2)(6-3x7+8/2)"
                )

                SelectionContainer(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)
                ) {
                    CustomTextField(value = reconigzerText, label = "Expresión", onValueChange = { reconigzerText = it })
                }

                Button(
                    onClick = {
                        processExpression(reconigzerText)
                    }
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
        if(expression.isEmpty()) return
        Log.d(TAG, "Expresion Inicial: $expression")
        val exp = Expression.addAsteriskBetweenDigitsAndParentheses(expression)
        Log.d(TAG, "Expresion Final: $exp")
        val isValid = Expression.isValidExpression(exp)
        resultExpression = if(isValid) {
            Expression.evaluateExpression(exp).toString()
        } else {
            Log.d(TAG, "¡Expresion no válida!")
            Toast.makeText(this, "¡Expresión no válida!", Toast.LENGTH_SHORT).show()
            "0"
        }
        Log.e(TAG, "Resultado: $resultExpression")
    }
}

