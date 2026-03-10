package com.example.skycast.ui.alerts

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.work.*
import com.example.skycast.utils.WeatherWorker
import java.util.concurrent.TimeUnit

@Composable
fun AlertsScreen() {
    val context = LocalContext.current
    var selectedDuration by remember { mutableStateOf(15L) }
    var selectedType by remember { mutableStateOf("notification") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("تنبيهات الطقس", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("تفعيل التنبيه بعد:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedDuration == 15L, onClick = { selectedDuration = 15L })
                    Text("15 دقيقة")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = selectedDuration == 60L, onClick = { selectedDuration = 60L })
                    Text("ساعة واحدة")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("نوع التنبيه:", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedType == "notification",
                            onClick = { selectedType = "notification" }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedType == "notification", onClick = { selectedType = "notification" })
                    Text("إشعار صامت (Notification)")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedType == "alarm",
                            onClick = { selectedType = "alarm" }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedType == "alarm", onClick = { selectedType = "alarm" })
                    Text("صوت منبه (Alarm)")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // send data to worker
                val inputData = Data.Builder()
                    .putString("alert_type", selectedType)
                    .build()


                val weatherWorkRequest = OneTimeWorkRequestBuilder<WeatherWorker>()
                    .setInitialDelay(selectedDuration, TimeUnit.MINUTES)
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "WeatherAlert",
                    ExistingWorkPolicy.REPLACE, // replace if another one exict
                    weatherWorkRequest
                )

                Toast.makeText(context , "Alert Added", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("حفظ التنبيه", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                WorkManager.getInstance(context).cancelUniqueWork("WeatherAlert")
                Toast.makeText(context , "All Alerts Canceled", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("إيقاف جميع التنبيهات", color = MaterialTheme.colorScheme.error)
        }
    }
}