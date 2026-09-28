package com.incoteam.realsaathi.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun VerificationProgressScreen(message: String, onHelp: () -> Unit, onCustomer: () -> Unit) {
    ProfilePageScaffold(
        title = "Verification in progress",
        onBack = {},
        showBackButton = false,
        backgroundBrush = OnboardingBackgroundBrush
    ) {
        Spacer(Modifier.height(20.dp))
        Text("Our team will reach out to you within 24 hours via phone call.", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        Text("Your host application is under review. This screen will remain here until the team completes verification.", color = TextSubtle)
        if (message.isNotBlank()) { Spacer(Modifier.height(12.dp)); Text(message, color = DangerRed) }
        Spacer(Modifier.weight(1f))
        Button(onClick = onHelp, modifier = Modifier.fillMaxWidth()) { Text("Need help? Visit Help & Support") }
        Spacer(Modifier.height(10.dp))
        Button(onClick = onCustomer, modifier = Modifier.fillMaxWidth()) { Text("Become a customer") }
    }
}
