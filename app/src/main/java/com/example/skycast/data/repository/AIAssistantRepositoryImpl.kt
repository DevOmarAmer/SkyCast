package com.example.skycast.data.repository

import com.example.skycast.BuildConfig
import com.example.skycast.data.model.ForecastItem
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIAssistantRepositoryImpl : IAIAssistantRepository {

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-flash-latest",
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
                val result = response.text?.takeIf { it.isNotBlank() }
                android.util.Log.d("AIAssistant", "AI generated: $result")
                result
            } catch (e: Exception) {
                android.util.Log.e("AIAssistant", "Error generating AI summary: ${e.message}", e)
                null
            }
        }
    }

    override suspend fun getDetailedMorningAnalysis(
        cityName: String,
        forecastList: List<ForecastItem>,
        language: String
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val dailyData = forecastList.take(8).joinToString("\n") { 
                    "- ${it.dateText}: ${it.main.temp}°C, ${it.weatherInfo.firstOrNull()?.description}" 
                }

                val isArabic = language.lowercase().startsWith("ar")
                val langName = if (isArabic) "Arabic" else "English"
                
                val prompt = if (isArabic) {
                    """
                        أنت خبير أرصاد جوية محترف ومساعد شخصي ذكي. 
                        قم بتحليل بيانات الطقس التالية لمدينة $cityName للساعات القادمة:
                        $dailyData
                        
                        المطلوب هو كتابة تقرير مفصل واحترافي باللغة العربية باستخدام تنسيق Markdown.
                        يجب أن يكون التقرير طويلًا ومقسمًا إلى الأقسام التالية بوضوح:
                        
                        1. 🌤️ ملخص اليوم: اشرح حالة الجو العامة وكيف سيتغير خلال اليوم.
                        2. 👕 نصائح الملابس: اقترح قطع الملابس المناسبة بناءً على درجات الحرارة وفرص الأمطار.
                        3. 🚗 القيادة والأنشطة: هل الجو مناسب للقيادة أو ممارسة الرياضة في الخارج؟
                        4. 💡 نصيحة صحية: قدم نصيحة طبية أو وقائية تناسب حالة الجو (مثلاً شرب الكثير من الماء أو الحذر من الغبار).

                        استخدم نبرة صوت ودودة ومطمئنة، وتجنب الرموز المعقدة الزائدة، اجعل النص سهل القراءة ومنظمًا.
                    """.trimIndent()
                } else {
                    """
                        You are a professional meteorologist and smart personal assistant.
                        Analyze the following weather data for $cityName for the upcoming hours:
                        $dailyData
                        
                        The requirement is to write a detailed and professional report in English using Markdown formatting.
                        The report must be long and clearly divided into the following sections:
                        
                        1. 🌤️ Daily Overview: Explain the general weather condition and how it will change during the day.
                        2. 👕 Outfit Recommendations: Suggest appropriate clothing based on temperatures and rain chances.
                        3. 🚗 Commute & Activities: Is the weather suitable for driving or outdoor sports?
                        4. 💡 Health & Safety Tip: Provide medical or preventive advice fitting the weather (e.g., drink plenty of water or caution against dust).

                        Use a friendly and reassuring tone, avoid excessive complex symbols, and make the text easy to read and organized.
                    """.trimIndent()
                }

                val response = generativeModel.generateContent(prompt)
                val result = response.text?.takeIf { it.isNotBlank() }
                android.util.Log.d("AIAssistant", "Detailed Analysis generated in $langName")
                result
            } catch (e: Exception) {
                android.util.Log.e("AIAssistant", "Error generating detailed analysis: ${e.message}", e)
                null
            }
        }
    }
}