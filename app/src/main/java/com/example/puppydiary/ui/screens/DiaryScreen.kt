package com.example.puppydiary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.puppydiary.viewmodel.PuppyViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(viewModel: PuppyViewModel) {
    val context = LocalContext.current
    val diaryEntries by viewModel.diaryEntries.collectAsState()

    // Snackbar 상태
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedEntryId by remember { mutableStateOf<Long?>(null) }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var selectedPhotoPath by remember { mutableStateOf<String?>(null) }
    var editPhotoPath by remember { mutableStateOf<String?>(null) }

    // 이미지 선택 런처 (새 일기용)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val fileName = "diary_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                selectedPhotoPath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 이미지 선택 런처 (수정용)
    val editImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val fileName = "diary_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                editPhotoPath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        onClick = {
                            titleInput = ""
                            contentInput = ""
                            selectedPhotoPath = null
                            showDialog = true
                        },
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

                        // 사진 미리보기
                        if (entry.photo != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(entry.photo))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "일기 사진",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
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
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                        // 사진 표시
                        if (entry.photo != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(entry.photo))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "일기 사진",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 250.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
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
                    Row {
                        // 수정 버튼
                        TextButton(
                            onClick = {
                                titleInput = entry.title
                                contentInput = entry.content
                                editPhotoPath = entry.photo
                                showDetailDialog = false
                                showEditDialog = true
                            }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("수정")
                        }
                        // 삭제 버튼
                        TextButton(
                            onClick = {
                                viewModel.deleteDiaryEntry(entry.id)
                                showDetailDialog = false
                                scope.launch { snackbarHostState.showSnackbar("일기가 삭제되었습니다") }
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
                }
            )
        }
    }

    // 일기 수정 다이얼로그
    if (showEditDialog && selectedEntryId != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("일기 수정") },
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
                    Spacer(modifier = Modifier.height(12.dp))

                    // 사진 선택 영역
                    Text(
                        text = "📷 사진",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (editPhotoPath != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(editPhotoPath!!))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "선택된 사진",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // 사진 삭제 버튼
                            IconButton(
                                onClick = { editPhotoPath = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        RoundedCornerShape(50)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "사진 삭제",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { editImagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("사진 변경")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { editImagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("사진 추가")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (titleInput.isNotEmpty() && contentInput.isNotEmpty()) {
                            viewModel.updateDiaryEntry(selectedEntryId!!, titleInput, contentInput, editPhotoPath)
                            titleInput = ""
                            contentInput = ""
                            editPhotoPath = null
                            showEditDialog = false
                            scope.launch { snackbarHostState.showSnackbar("일기가 수정되었습니다") }
                        }
                    },
                    enabled = titleInput.isNotEmpty() && contentInput.isNotEmpty()
                ) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = {
                    titleInput = ""
                    contentInput = ""
                    editPhotoPath = null
                    showEditDialog = false
                }) { Text("취소") }
            }
        )
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
                    Spacer(modifier = Modifier.height(12.dp))

                    // 사진 선택 영역
                    Text(
                        text = "📷 사진 (선택)",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedPhotoPath != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(selectedPhotoPath!!))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "선택된 사진",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // 사진 삭제 버튼
                            IconButton(
                                onClick = { selectedPhotoPath = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        RoundedCornerShape(50)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "사진 삭제",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("사진 변경")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("사진 추가")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (titleInput.isNotEmpty() && contentInput.isNotEmpty()) {
                            viewModel.addDiaryEntry(titleInput, contentInput, selectedPhotoPath)
                            titleInput = ""
                            contentInput = ""
                            selectedPhotoPath = null
                            showDialog = false
                            scope.launch { snackbarHostState.showSnackbar("일기가 저장되었습니다") }
                        }
                    },
                    enabled = titleInput.isNotEmpty() && contentInput.isNotEmpty()
                ) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = {
                    titleInput = ""
                    contentInput = ""
                    selectedPhotoPath = null
                    showDialog = false 
                }) { Text("취소") }
            }
        )
    }
}
