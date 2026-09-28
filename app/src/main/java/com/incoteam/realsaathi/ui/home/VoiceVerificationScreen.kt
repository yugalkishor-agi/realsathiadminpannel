package com.incoteam.realsaathi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun VoiceVerificationScreen(
    prompt: String, reviewMessage: String, recording: Boolean, hasRecording: Boolean,
    playing: Boolean,
    onHold: () -> Unit, onRelease: () -> Unit, onPlay: () -> Unit, onRetake: () -> Unit,
    onStopPlay: () -> Unit, onSubmit: () -> Unit, submitting: Boolean, onBack: () -> Unit
) {
    val currentOnHold = rememberUpdatedState(onHold)
    val currentOnRelease = rememberUpdatedState(onRelease)
    ProfilePageScaffold(
        title = "Voice identification",
        onBack = onBack,
        backgroundBrush = OnboardingBackgroundBrush
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("To confirm your identity, record yourself saying this line", color = TextSubtle,
                fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text(prompt, Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(CardBg).border(1.dp, Accent2.copy(alpha = .35f), RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp), color = Color.White,
                fontSize = 21.sp, lineHeight = 31.sp, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center)
            if (reviewMessage.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(reviewMessage, Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(DangerRed.copy(alpha = .12f)).padding(12.dp), color = DangerRed,
                    fontSize = 13.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(24.dp))
            Text(if (recording) "Recording… release to finish" else "Tap and hold to speak",
                color = Color.White.copy(alpha = .88f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(14.dp))
            Box(Modifier.size(82.dp).clip(CircleShape)
                .background(if (recording) DangerRed else Color.White)
                .pointerInput(Unit) { detectTapGestures(onPress = {
                    currentOnHold.value(); try { awaitRelease() } finally { currentOnRelease.value() }
                }) }
                .semantics {
                    role = Role.Button
                    contentDescription = if (recording) "Stop voice recording" else "Start voice recording"
                    onClick { if (recording) onRelease() else onHold(); true }
                }, contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Mic, contentDescription = null,
                    tint = if (recording) Color.White else Color(0xFF542483),
                    modifier = Modifier.size(34.dp))
            }
            if (hasRecording) {
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = if (playing) onStopPlay else onPlay, Modifier.weight(1f)) {
                        Icon(if (playing) Icons.Filled.Stop else Icons.Filled.PlayArrow, null)
                        Spacer(Modifier.width(6.dp)); Text(if (playing) "Stop" else "Listen")
                    }
                    OutlinedButton(onClick = onRetake, Modifier.weight(1f)) {
                        Icon(Icons.Filled.Replay, null); Spacer(Modifier.width(6.dp)); Text("Record again")
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = onSubmit, enabled = hasRecording && !recording && !submitting,
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent2)) {
                Text(if (submitting) "Submitting…" else "Submit for verification", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
