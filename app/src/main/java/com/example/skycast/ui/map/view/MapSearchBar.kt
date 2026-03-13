package com.example.skycast.ui.map.view

import android.location.Geocoder
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skycast.ui.theme.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale

data class PlaceSuggestion(
    val primaryText: String,   // city / locality
    val secondaryText: String, // admin area + country
    val latLng: LatLng
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchBar(
    onBack: () -> Unit,
    onSuggestionPicked: (PlaceSuggestion) -> Unit
) {
    val context = LocalContext.current

    val wc = LocalWeatherColors.current

    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Debounced autocomplete: fires 400 ms after user stops typing
    LaunchedEffect(query) {
        if (query.length >= 2) {
            delay(100)
            isSearching = true
            val results = withContext(Dispatchers.IO) {
                try {
                    Geocoder(context, Locale.getDefault())
                        .getFromLocationName(query, 6)
                        ?.mapNotNull { address ->
                            val city = address.locality
                                ?: address.subAdminArea
                                ?: address.adminArea
                                ?: return@mapNotNull null
                            val secondary = buildString {
                                val admin = address.adminArea
                                if (admin != null && admin != city) {
                                    append(admin)
                                    if (address.countryName != null) append(", ")
                                }
                                if (address.countryName != null) append(address.countryName)
                            }
                            PlaceSuggestion(
                                primaryText = city,
                                secondaryText = secondary,
                                latLng = LatLng(address.latitude, address.longitude)
                            )
                        }
                        // De-dup by primaryText so we don't show the same city twice
                        ?.distinctBy { it.primaryText }
                        ?: emptyList()
                } catch (_: Exception) { emptyList() }
            }
            suggestions = results
            isSearching = false
        } else {
            suggestions = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ── Input row ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(wc.bgBottom.copy(alpha = 0.97f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CloudWhite)
            }

            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search city or place…", color = CloudGrey) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = CloudWhite,
                    unfocusedTextColor = CloudWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = wc.accent
                )
            )

            // Trailing icon: spinner → clear → search
            when {
                isSearching -> CircularProgressIndicator(
                    color = wc.accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(20.dp)
                )
                query.isNotEmpty() -> IconButton(onClick = { query = ""; suggestions = emptyList() }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = CloudGrey)
                }
                else -> IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = wc.accent)
                }
            }
        }

        // ── Autocomplete dropdown ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = slideInVertically(tween(200)) { -10 } + fadeIn(tween(200)),
            exit  = slideOutVertically(tween(150)) { -10 } + fadeOut(tween(150))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = wc.bgBottom.copy(alpha = 0.97f)),
                border = BorderStroke(1.dp, wc.accent.copy(alpha = 0.2f))
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(suggestions, key = { it.primaryText + it.latLng.latitude }) { suggestion ->
                        SuggestionItem(
                            suggestion = suggestion,
                            isLast = suggestion == suggestions.last(),
                            onClick = {
                                query = suggestion.primaryText
                                suggestions = emptyList()
                                onSuggestionPicked(suggestion)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Private: single suggestion row ───────────────────────────────────────────
@Composable
private fun SuggestionItem(
    suggestion: PlaceSuggestion,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val wc = LocalWeatherColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(wc.heroGlow.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = wc.accent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    suggestion.primaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.W600,
                    color = CloudWhite
                )
                if (suggestion.secondaryText.isNotEmpty()) {
                    Text(
                        suggestion.secondaryText,
                        style = MaterialTheme.typography.labelSmall,
                        color = CloudGrey
                    )
                }
            }
        }

        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = wc.accent.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )
        }
    }
}
