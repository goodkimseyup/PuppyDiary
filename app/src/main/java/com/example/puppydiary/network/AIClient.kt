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

private const val TAG = "AIClient"

/**
 * Groq API 클라이언트 (무료, 빠름)
 * Llama 모델을 사용한 반려동물 건강 상담 챗봇
 */
object AIClient {

    private const val BASE_URL = "https://api.groq.com/openai/v1/chat/completions"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

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
    """.trimIndent()

    data class ChatMessage(
        val role: String,
        val content: String
    )

    data class ChatRequest(
        val model: String = "llama-3.3-70b-versatile",
        val messages: List<ChatMessage>,
        @SerializedName("max_tokens")
        val maxTokens: Int = 500,
        val temperature: Double = 0.7
    )

    data class ChatResponse(
        val choices: List<Choice>?
    )

    data class Choice(
        val message: ChatMessage?
    )

    data class ErrorResponse(
        val error: ErrorDetail?
    )

    data class ErrorDetail(
        val message: String?
    )

    suspend fun sendMessage(
        apiKey: String,
        userMessage: String,
        conversationHistory: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("API 키가 설정되지 않았습니다"))
            }

            val messages = mutableListOf<ChatMessage>()
            messages.add(ChatMessage("system", systemPrompt))
            messages.addAll(conversationHistory.takeLast(10))
            messages.add(ChatMessage("user", userMessage))

            val request = ChatRequest(messages = messages)
            val jsonBody = gson.toJson(request)

            Log.d(TAG, "Sending request...")

            val httpRequest = Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "Response code: ${response.code}")

            if (response.isSuccessful && responseBody != null) {
                val chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)
                val assistantMessage = chatResponse.choices?.firstOrNull()?.message?.content
                    ?: "죄송해요, 응답을 받지 못했어요."

                Result.success(assistantMessage)
            } else {
                Log.e(TAG, "Error: $responseBody")
                val errorResponse = responseBody?.let {
                    try { gson.fromJson(it, ErrorResponse::class.java) } catch (e: Exception) { null }
                }
                val errorMessage = errorResponse?.error?.message ?: "API 오류 (${response.code})"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getQuickQuestions(): List<String> = listOf(
        "우리 강아지가 밥을 잘 안 먹어요",
        "산책은 하루에 얼마나 해야 하나요?",
        "강아지가 자꾸 긁어요",
        "예방접종 주기가 어떻게 되나요?",
        "강아지 간식 추천해주세요",
        "체중 관리는 어떻게 하나요?"
    )
}
