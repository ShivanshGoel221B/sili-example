package com.example.videocompressor

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DCIM
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.videocompressor.ui.theme.VideoCompressorTheme
import com.iceteck.silicompressorr.SiliCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                compressVideo(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                val getpermission = Intent()
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(getpermission)
            }
        }
        setContent {
            VideoCompressorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Button(onClick = { pickMedia.launch(PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.VideoOnly)) }) {
                            Text(text = "Pick a video")
                        }
                    }
                }
            }
        }
    }

    private fun compressVideo(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val filePath = SiliCompressor.with(this@MainActivity).compressVideo(uri, resultFolder.absolutePath)
            Log.d("VideoCompressorResult", "Compressed video saved to $filePath")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val file = File(filePath)
                val resultFile = resultFolder
                file.copyTo(File(resultFile, file.name).apply {
                    createNewFile()
                }, true)
            }

        }
    }

    private val resultFolder: File
        get() {
            val folder = File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM), "VideoCompressor")
            if (!folder.exists()) {
                folder.mkdir()
            }
            return folder
        }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VideoCompressorTheme {
        Greeting("Android")
    }
}