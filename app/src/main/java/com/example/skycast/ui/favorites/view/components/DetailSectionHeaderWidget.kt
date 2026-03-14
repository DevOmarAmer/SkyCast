package com.example.skycast.ui.favorites.view.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skycast.ui.theme.CloudWhite

@Composable
fun DetailSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.W600,
        color = CloudWhite,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp, end = 20.dp)
    )
}
