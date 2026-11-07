package com.aybay.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

// ভয়েস স্ক্রিন থেকে Add ফর্মে লেখা পাঠানোর জায়গা।
object VoiceText {
    var text: String = ""
}

private fun startListening(recognizer: SpeechRecognizer?) {
    if (recognizer == null) return
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
    }
    try {
        recognizer.startListening(intent)
    } catch (e: Exception) {
    }
}

@Composable
fun VoiceScreen(onUseText: (String) -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf(tr("মাইক চাপ দিয়ে বলুন", "Tap the mic and speak")) }
    var heardText by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    val recognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(recognizer) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                listening = true
                status = tr("শুনছি...", "Listening...")
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                listening = false
                status = tr("শোনা শেষ", "Done listening")
            }

            override fun onError(error: Int) {
                listening = false
                status = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH ->
                        tr("কিছু শোনা যায়নি", "Nothing heard")

                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        tr("সময় শেষ", "Timed out")

                    else -> tr("আবার চেষ্টা করুন", "Try again")
                }
            }

            override fun onResults(results: Bundle?) {
                listening = false
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = list?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    heardText = text
                    status = tr("শোনা গেছে", "Heard")
                } else {
                    status = tr("কিছু শোনা যায়নি", "Nothing heard")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = list?.firstOrNull() ?: ""
                if (text.isNotBlank()) heardText = text
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        recognizer?.setRecognitionListener(listener)
        onDispose { recognizer?.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            status = tr("শুনছি...", "Listening...")
            startListening(recognizer)
        } else {
            status = tr("মাইকের অনুমতি দিন", "Allow microphone permission")
        }
    }

    fun onMicTap() {
        if (recognizer == null) {
            status = tr("এই ফোনে ভয়েস চেনা যায় না", "Voice recognition not available on this phone")
            return
        }
        if (listening) {
            recognizer.stopListening()
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            status = tr("শুনছি...", "Listening...")
            startListening(recognizer)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = tr("ভয়েস", "Voice"),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(if (listening) RedMain else JomaBlue, CircleShape)
                .clickable { onMicTap() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = tr("ভয়েস", "Voice"),
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = status,
            fontSize = 15.sp,
            color = GreyText
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (heardText.isNotBlank()) {
            OutlinedTextField(
                value = heardText,
                onValueChange = { heardText = it },
                label = { Text(text = tr("শোনা লেখা", "Heard text")) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onUseText(heardText) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
            ) {
                Text(text = tr("যোগ করুন ফর্মে নিন", "Take to Add Form"), fontSize = 15.sp)
            }
        } else {
            EmptyState(
                "🎙️",
                tr("এখনো কিছু বলা হয়নি", "Nothing spoken yet")
            )
        }
    }
}
