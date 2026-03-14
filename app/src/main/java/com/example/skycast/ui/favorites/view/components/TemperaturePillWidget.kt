package com.example.skycast.ui.favorites.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.skycast.ui.theme.CloudGrey

@Composable
fun TemperaturePill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontWeight = FontWeight.W700, fontSize = 15.sp)
        Text(text = label, color = CloudGrey, fontSize = 11.sp)
    }
}
