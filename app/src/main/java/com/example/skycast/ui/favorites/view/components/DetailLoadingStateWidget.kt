package com.example.skycast.ui.favorites.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skycast.R
import com.example.skycast.data.local.entity.FavoriteLocation
import com.example.skycast.ui.theme.*

@Composable
fun DetailLoadingState(location: FavoriteLocation, onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(SkyBlue.copy(alpha = 0.3f), Color.Transparent)))
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(FrostStrong, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = CloudWhite)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(location.cityName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.W600, color = CloudWhite)
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = SkyBlueBright, strokeWidth = 3.dp)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.loading_weather), color = CloudGrey, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
} 
