package com.incoteam.realsaathi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun HostEarningRates(
    audioRate: Double,
    videoRate: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EarningRateChip(Icons.Default.Call, "Audio", audioRate, Modifier.weight(1f))
        EarningRateChip(Icons.Default.Videocam, "Video", videoRate, Modifier.weight(1f))
    }
}

@Composable
private fun EarningRateChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    rate: Double,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF8FE7C3))
        Text(
            text = "$label ₹${String.format(Locale.US, "%.2f", rate)}/min",
            color = Color.White,
            fontSize = 11.sp
        )
    }
}
