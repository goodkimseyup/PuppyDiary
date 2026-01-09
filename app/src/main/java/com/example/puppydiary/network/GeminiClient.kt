package com.example.puppydiary.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

private const val TAG = "GeminiClient"

/**
 * Google Gemini API 클라이언트
 * 무료 AI를 사용한 반려동물 건강 상담 챗봇
 */
object GeminiClient {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    // 시스템 프롬프트: 반려동물 건강 전문가 역할
    private val systemPrompt = """
        당신은 친절하고 전문적인 반려동물 건강 상담사입니다.
        이름은 "퍼피닥터"입니다.

        역할:
        - 반려동물(주로 강아지)의 건강, 영양, 행동에 대해 조언합니다
        - 보호자의 걱정을 공감하며 친절하게 답변합니다
        - 이모지를 적절히 사용해서 친근하게 대화합니다

        주의사항:
        - 심각한 증상은 반드시 "동물병원 방문을 권장합니다"라고 안내합니다
        - 약물 처방이나 구체적인 의료 행위는 추천하지 않습니다
        - 답변은 간결하게 (3-5문장) 유지합니다
        - 한국어로 답변합니다

        금지사항:
        - 특정 약물명 추천
        - 진단 내리기
        - 수술/시술 권유
    """.trimIndent()

    data class ChatMessage(
        val role: String,
        val content: String
    )

    // Gemini API 요청 형식
    data class GeminiRequest(
        val contents: List<Content>,
        val generationConfig: GenerationConfig? = null
    )

    data class Content(
        val role: String,
        val parts: List<Part>
    )

    data class Part(
        val text: String
    )

    data class GenerationConfig(
        val temperature: Double = 0.7,
        val maxOutputTokens: Int = 500
    )

    // Gemini API 응답 형식
    data class GeminiResponse(
        val candidates: List<Candidate>?
    )

    data class Candidate(
        val content: ContentResponse?
    )

    data class ContentResponse(
        val parts: List<Part>?,
        val role: String?
    )

    data class ErrorResponse(
        val error: ErrorDetail?
    )

    data class ErrorDetail(
        val message: String?,
        val code: Int?
    )

    /**
     * AI에게 메시지 보내기
     */
    suspend fun sendMessage(
        apiKey: String,
        userMessage: String,
        conversationHistory: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("API 키가 설정되지 않았습니다"))
            }

            // 대화 기록을 Gemini 형식으로 변환
            val contents = mutableListOf<Content>()

            // 이전 대화 기록 추가 (최근 10개만)
            conversationHistory.takeLast(10).forEach { msg ->
                contents.add(
                    Content(
                        role = if (msg.role == "user") "user" else "model",
                        parts = listOf(Part(msg.content))
                    )
                )
            }

            // 새 사용자 메시지에 시스템 프롬프트 포함
            val fullMessage = if (conversationHistory.isEmpty()) {
                "$systemPrompt\n\n사용자 질문: $userMessage"
            } else {
                userMessage
            }

            contents.add(
                Content(
                    role = "user",
                    parts = listOf(Part(fullMessage))
                )
            )

            val request = GeminiRequest(
                contents = contents,
                generationConfig = GenerationConfig()
            )

            val jsonBody = gson.toJson(request)

            val httpRequest = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            Log.d(TAG, "Sending request to Gemini API...")
            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "Response code: ${response.code}")
            Log.d(TAG, "Response body: $responseBody")

            if (response.isSuccessful && responseBody != null) {
                val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
                val assistantMessage = geminiResponse.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: "죄송해요, 응답을 받지 못했어요. 다시 시도해주세요."

                Result.success(assistantMessage)
            } else {
                Log.e(TAG, "API Error: ${response.code} - $responseBody")
                val errorResponse = responseBody?.let {
                    try {
                        gson.fromJson(it, ErrorResponse::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
                val errorMessage = errorResponse?.error?.message ?: "API 오류가 발생했습니다 (${response.code})"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * 빠른 건강 질문 생성
     */
    fun getQuickQuestions(): List<String> = listOf(
        "우리 강아지가 밥을 잘 안 먹어요",
        "산책은 하루에 얼마나 해야 하나요?",
        "강아지가 자꾸 긁어요",
        "예방접종 주기가 어떻게 되나요?",
        "강아지 간식 추천해주세요",
        "체중 관리는 어떻게 하나요?"
    )
}
