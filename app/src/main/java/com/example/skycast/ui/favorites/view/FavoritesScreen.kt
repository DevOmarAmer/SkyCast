package com.example.skycast.ui.favorites.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skycast.R
import com.example.skycast.data.local.entity.FavoriteLocation
import com.example.skycast.ui.favorites.viewModel.FavoritesViewModel
import com.example.skycast.ui.theme.*
import com.example.skycast.ui.favorites.view.components.EmptyFavoritesContent
import com.example.skycast.ui.favorites.view.components.FavoriteItemCard
import com.example.skycast.ui.favorites.view.components.SwipeToDeleteWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateToAddPlace: () -> Unit,
    onNavigateToDetail: (FavoriteLocation) -> Unit = {}
) {
    val favorites by viewModel.favoritesList.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyDeepNavy, DarkSurface)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(SkyBlue.copy(alpha = 0.25f), SkyDeepNavy.copy(alpha = 0f))))
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.favorites_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.W600,
                    color = CloudWhite
                )
            }

            if (favorites.isEmpty()) {
                EmptyFavoritesContent()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    itemsIndexed(
                        items = favorites,
                        key = { _, loc -> loc.id }
                    ) { index, location ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(300 + index * 60)) +
                                    slideInVertically(tween(300 + index * 60)) { it / 2 }
                        ) {
                            SwipeToDeleteWrapper(
                                onDelete = { viewModel.deleteLocation(location) }
                            ) {
                                FavoriteItemCard(
                                    location = location,
                                    onDeleteClick = { viewModel.deleteLocation(location) },
                                    onItemClick = { onNavigateToDetail(location) }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToAddPlace,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp, 24.dp,24.dp, 120.dp)
                .size(64.dp ),
            containerColor = SkyBlueBright,
            contentColor = CloudWhite,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_favorite))
        }
    }
}

