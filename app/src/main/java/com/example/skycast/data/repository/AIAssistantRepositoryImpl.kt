package com.example.skycast.data.repository

//import com.example.skycast.BuildConfig // تأكد من استدعاء الـ BuildConfig الخاص بتطبيقك
import com.example.skycast.BuildConfig

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIAssistantRepositoryImpl : IAIAssistantRepository {

    // تهيئة نموذج Gemini (نستخدم flash لأنه الأسرع والأخف للموبايل)
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    override suspend fun getWeatherSummary(
        tempC: Int,
        condition: String,
        windSpeed: Int,
        isRainy: Boolean,
        language: String
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val langName = if (language.lowercase().startsWith("ar")) "Arabic" else "English"
                val prompt = """
                    You are a smart and friendly local weather assistant. Based on the following current weather data:
                    - Temperature: $tempC °C
                    - Condition: $condition
                    - Wind Speed: $windSpeed m/s
                    - Rain Expected: ${if (isRainy) "Yes" else "No"}
                    
                    Write a short morning brief (max 3 sentences) in $langName, using a friendly conversational tone. 
                    Advise me on what to wear and how to prepare for the day. Do not use complex formatting (e.g. asterisks); just plain text that is easy to read.
                    Add a couple of suitable emojis for a nice presentation.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                response.text?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
        }
    }
}