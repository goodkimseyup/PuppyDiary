package com.example.puppydiary.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puppydiary.data.local.SettingsDataStore
import com.example.puppydiary.network.AIClient
import com.example.puppydiary.ui.theme.AppColors
import kotlinx.coroutines.launch

data class ChatMessageUI(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val settingsDataStore = remember { SettingsDataStore(context) }

    var messages by remember { mutableStateOf(listOf<ChatMessageUI>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf("") }
    var hasApiKey by remember { mutableStateOf(false) }

    // API 키 로드
    LaunchedEffect(Unit) {
        val savedKey = settingsDataStore.getOpenAIApiKey()
        apiKey = savedKey
        hasApiKey = savedKey.isNotBlank()

        // 환영 메시지
        if (messages.isEmpty()) {
            messages = listOf(
                ChatMessageUI(
                    content = "안녕하세요! 저는 퍼피닥터예요 🐕\n\n반려동물 건강에 대해 궁금한 점이 있으면 물어보세요! 체중 관리, 산책, 영양, 행동 등 다양한 질문에 답해드릴게요.\n\n⚠️ 심각한 증상은 반드시 동물병원을 방문해주세요!",
                    isUser = false
                )
            )
        }
    }

    // 메시지 전송
    fun sendMessage(text: String) {
        if (text.isBlank() || isLoading) return

        if (!hasApiKey) {
            showApiKeyDialog = true
            return
        }

        val userMessage = ChatMessageUI(content = text, isUser = true)
        messages = messages + userMessage
        inputText = ""
        isLoading = true

        scope.launch {
            // 스크롤 to bottom
            listState.animateScrollToItem(messages.size)

            val history = messages.dropLast(1).map {
                AIClient.ChatMessage(
                    role = if (it.isUser) "user" else "assistant",
                    content = it.content
                )
            }

            val result = AIClient.sendMessage(
                apiKey = apiKey,
                userMessage = text,
                conversationHistory = history
            )

            result.fold(
                onSuccess = { response ->
                    messages = messages + ChatMessageUI(content = response, isUser = false)
                },
                onFailure = { error ->
                    messages = messages + ChatMessageUI(
                        content = "죄송해요, 오류가 발생했어요 😢\n${error.message}",
                        isUser = false
                    )
                }
            )

            isLoading = false
            listState.animateScrollToItem(messages.size)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🩺", fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("퍼피닥터", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("AI 건강 상담", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { showApiKeyDialog = true }) {
                        Icon(
                            Icons.Default.Settings,
                            "설정",
                            tint = if (hasApiKey) AppColors.Primary else Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            // 메시지 목록
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }

                // 로딩 인디케이터
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            TypingIndicator()
                        }
                    }
                }
            }

            // 빠른 질문 (첫 메시지일 때만)
            if (messages.size <= 1 && !isLoading) {
                QuickQuestionsRow(
                    questions = AIClient.getQuickQuestions(),
                    onQuestionClick = { sendMessage(it) }
                )
            }

            // 입력창
            ChatInputBar(
                value = inputText,
                onValueChange = { inputText = it },
                onSend = { sendMessage(inputText) },
                isLoading = isLoading,
                hasApiKey = hasApiKey
            )
        }
    }

    // API 키 설정 다이얼로그
    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = apiKey,
            onDismiss = { showApiKeyDialog = false },
            onSave = { newKey ->
                scope.launch {
                    settingsDataStore.setOpenAIApiKey(newKey)
                    apiKey = newKey
                    hasApiKey = newKey.isNotBlank()
                    showApiKeyDialog = false
                }
            }
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessageUI) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Text("🐕", fontSize = 18.sp)
            }
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            color = if (message.isUser) AppColors.Primary else Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser) Color.White else Color.Black,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "dot$index")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 200),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha$index"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary.copy(alpha = alpha))
                )
            }
        }
    }
}

@Composable
fun QuickQuestionsRow(
    questions: List<String>,
    onQuestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "💡 자주 묻는 질문",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(questions) { question ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppColors.PrimaryLight,
                    modifier = Modifier.clickable { onQuestionClick(question) }
                ) {
                    Text(
                        text = question,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 13.sp,
                        color = AppColors.Primary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    hasApiKey: Boolean
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (hasApiKey) "메시지를 입력하세요..."
                        else "API 키를 먼저 설정하세요",
                        color = Color.Gray
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 3,
                enabled = !isLoading
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isLoading && hasApiKey,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (value.isNotBlank() && !isLoading && hasApiKey)
                            AppColors.Primary
                        else
                            Color(0xFFE0E0E0)
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "전송",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var keyInput by remember { mutableStateOf(currentKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Groq API 키 설정") },
        text = {
            Column {
                Text(
                    "AI 상담 기능을 사용하려면 Groq API 키가 필요합니다. (무료)",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("API 키") },
                    placeholder = { Text("gsk_...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "💡 https://console.groq.com/keys 에서 무료 발급",
                    fontSize = 12.sp,
                    color = AppColors.Primary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(keyInput) },
                enabled = keyInput.isNotBlank()
            ) {
                Text("저장", color = AppColors.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
