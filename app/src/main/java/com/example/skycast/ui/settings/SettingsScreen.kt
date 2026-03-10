package com.example.skycast.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val currentTempUnit by viewModel.tempUnit.collectAsState()
    val currentWindUnit by viewModel.windUnit.collectAsState()
    val currentLang by viewModel.language.collectAsState()
    val currentLocationMethod by viewModel.locationMethod.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("الإعدادات", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // 1. تحديد الموقع
        SettingSection(title = "طريقة تحديد الموقع") {
            RowOption("GPS (تلقائي)", currentLocationMethod == "gps") { viewModel.saveLocationMethod("gps") }
            RowOption("الخريطة (يدوي)", currentLocationMethod == "map") { viewModel.saveLocationMethod("map") }
        }

        // 2. وحدة الحرارة
        SettingSection(title = "وحدة درجة الحرارة") {
            RowOption("سلسيوس (°C)", currentTempUnit == "metric") { viewModel.saveTempUnit("metric") }
            RowOption("فهرنهايت (°F)", currentTempUnit == "imperial") { viewModel.saveTempUnit("imperial") }
            RowOption("كلفن (K)", currentTempUnit == "standard") { viewModel.saveTempUnit("standard") }
        }

        // 3. سرعة الرياح
        SettingSection(title = "وحدة سرعة الرياح") {
            RowOption("متر/ثانية", currentWindUnit == "m/s") { viewModel.saveWindUnit("m/s") }
            RowOption("ميل/ساعة", currentWindUnit == "mph") { viewModel.saveWindUnit("mph") }
        }

        // 4. اللغة
        SettingSection(title = "لغة التطبيق") {
            RowOption("English", currentLang == "en") { viewModel.saveLanguage("en") }
            RowOption("العربية", currentLang == "ar") { viewModel.saveLanguage("ar") }
        }
    }
}

@Composable
fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun RowOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}