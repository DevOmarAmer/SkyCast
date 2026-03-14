package com.example.skycast.data.repository

//import com.example.skycast.BuildConfig // تأكد من استدعاء الـ BuildConfig الخاص بتطبيقك
import com.example.skycast.BuildConfig

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIAssistantRepositoryImpl : IAIAssistantRepository {

    // تهيئة نموذج Gemini (نستخدم flash لأنه الأسرع والأخف للموبايل)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override suspend fun getWeatherSummary(
        tempC: Int,
        condition: String,
        windSpeed: Int,
        isRainy: Boolean
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                // هندسة الأوامر (Prompt Engineering) للحصول على أفضل رد
                val prompt = """
                    أنت مساعد طقس محلي ذكي وودود. بناءً على بيانات الطقس الحالية التالية:
                    - درجة الحرارة: $tempC درجة مئوية
                    - حالة الجو: $condition
                    - سرعة الرياح: $windSpeed متر/ثانية
                    - احتمالية المطر: ${if (isRainy) "نعم" else "لا"}
                    
                    اكتب ملخصاً قصيراً (حد أقصى 3 سطور) باللغة العربية، بأسلوب محادثة لطيف. 
                    انصحني بماذا أرتدي وكيف أستعد لليوم. لا تستخدم تنسيقات معقدة، فقط نص عادي ومريح للقراءة.
                """.trimIndent()

                // إرسال الطلب لـ Gemini
                val response = generativeModel.generateContent(prompt)

                // إرجاع النص أو رسالة بديلة في حال كان الرد فارغاً
                response.text ?: "أتمنى لك يوماً سعيداً! الجو يبدو متقلباً، فكن مستعداً."
            } catch (e: Exception) {
                // معالجة الأخطاء (مثل انقطاع الإنترنت أثناء سؤال الـ AI)
                "عذراً، لم أتمكن من جلب نصيحة الذكاء الاصطناعي حالياً. راقب درجات الحرارة!"
            }
        }
    }
}