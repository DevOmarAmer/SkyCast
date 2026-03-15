package com.example.skycast.ui.home.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycast.ui.home.viewModel.MorningAnalysisState
import com.example.skycast.ui.home.viewModel.MorningAnalysisViewModel
import com.example.skycast.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorningAIAnalysisScreen(
    viewModel: MorningAnalysisViewModel,
    cityName: String,
    onBack: () -> Unit
) {
    val state by viewModel.analysisState.collectAsState()
    val lang  by viewModel.language.collectAsState(initial = "en")
    val isRtl = lang == "ar"

    CompositionLocalProvider(LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(com.example.skycast.R.string.morning_analysis_title), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(com.example.skycast.R.string.back_to_home))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SkyNavy,
                        titleContentColor = CloudWhite,
                        navigationIconContentColor = CloudWhite
                    )
                )
            },
            containerColor = SkyNavy
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            listOf(SkyNavy, SkyDeepNavy)
                        )
                    )
            ) {
                when (val current = state) {
                    is MorningAnalysisState.Loading -> LoadingAnalysis()
                    is MorningAnalysisState.Success -> AnalysisContent(current.markdownContent)
                    is MorningAnalysisState.Error   -> ErrorContent(current.message, onBack)
                }
            }
        }
    }
}

@Composable
fun LoadingAnalysis() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = SunGold.copy(alpha = alpha),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(com.example.skycast.R.string.morning_analysis_loading),
            color = CloudWhite,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            modifier = Modifier.width(200.dp).clip(RoundedCornerShape(4.dp)),
            color = SunGold,
            trackColor = SkyBluePale.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun AnalysisContent(content: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Hero Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SkyBlueBright.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = SunGold,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(com.example.skycast.R.string.morning_analysis_hero_ready),
                    color = CloudWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        // Markdown-like display (Simple split by sections for beauty)
        val sections = content.split("\n\n")
        sections.forEach { section ->
            val cleanSection = section
                .replace("**", "") // Remove bold markdown
                .replace("---", "") // Remove horizontal rules
                .replace("___", "")
                .trim()
            
            if (cleanSection.isNotBlank() && cleanSection.length > 3) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = DarkSurface.copy(alpha = 0.4f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = cleanSection,
                        modifier = Modifier.padding(20.dp),
                        color = CloudWhite,
                        lineHeight = 26.sp,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun ErrorContent(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(com.example.skycast.R.string.morning_analysis_error_title), color = CloudWhite, fontWeight = FontWeight.Bold)
        Text(text = message, color = CloudGrey, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = SkyBlueBright)) {
            Text(stringResource(com.example.skycast.R.string.back_to_home))
        }
    }
}
