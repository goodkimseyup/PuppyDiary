package com.example.puppydiary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puppydiary.viewmodel.PuppyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(viewModel: PuppyViewModel) {
    val diaryEntries by viewModel.diaryEntries.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedEntryId by remember { mutableStateOf<Long?>(null) }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📝 일기",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    FloatingActionButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFF9C27B0)
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = "일기 추가",
                            tint = Color.White
                        )
                    }
                }
            }

            if (diaryEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📖",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "아직 작성된 일기가 없어요",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "우리 아이의 일상을 기록해보세요!",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            items(diaryEntries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        selectedEntryId = entry.id
                        showDetailDialog = true
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = entry.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = entry.date,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = entry.content,
                            color = Color.Gray,
                            lineHeight = 20.sp,
                            maxLines = 3
                        )

                        if (entry.photo != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "📷 사진 첨부됨",
                                fontSize = 12.sp,
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                }
            }
        }
    }

    // 일기 상세 다이얼로그
    if (showDetailDialog && selectedEntryId != null) {
        val selectedEntry = diaryEntries.find { it.id == selectedEntryId }
        selectedEntry?.let { entry ->
            AlertDialog(
                onDismissRequest = { showDetailDialog = false },
                title = { 
                    Text(
                        text = entry.title,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = entry.date,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = entry.content,
                            lineHeight = 24.sp
                        )
                        if (entry.photo != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "📷 사진 첨부됨",
                                fontSize = 12.sp,
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDetailDialog = false }) {
                        Text("닫기")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDiaryEntry(entry.id)
                            showDetailDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("삭제")
                    }
                }
            )
        }
    }

    // 일기 작성 다이얼로그
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("일기 작성") },
            text = {
                Column {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("제목") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("내용") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (titleInput.isNotEmpty() && contentInput.isNotEmpty()) {
                            viewModel.addDiaryEntry(titleInput, contentInput)
                            titleInput = ""
                            contentInput = ""
                            showDialog = false
                        }
                    },
                    enabled = titleInput.isNotEmpty() && contentInput.isNotEmpty()
                ) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    titleInput = ""
                    contentInput = ""
                    showDialog = false 
                }) { Text("취소") }
            }
        )
    }
}
