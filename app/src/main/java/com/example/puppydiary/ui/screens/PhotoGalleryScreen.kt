package com.example.puppydiary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.puppydiary.data.model.PhotoMemory
import com.example.puppydiary.viewmodel.PuppyViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGalleryScreen(viewModel: PuppyViewModel) {
    val context = LocalContext.current
    val photoMemories by viewModel.photoMemories.collectAsState()
    
    // Snackbar 상태
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var showPhotoDetailDialog by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<PhotoMemory?>(null) }
    var descriptionInput by remember { mutableStateOf("") }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    
    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val fileName = "photo_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                selectedImagePath = file.absolutePath
                showAddPhotoDialog = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                containerColor = Color(0xFFE91E63)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "사진 추가",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 헤더
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "📷",
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "사진첩",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${photoMemories.size}장",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            if (photoMemories.isEmpty()) {
                // 빈 상태
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🐕",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "아직 사진이 없어요",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "오른쪽 아래 + 버튼을 눌러\n우리 강아지 사진을 추가해보세요!",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // 사진 그리드
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(photoMemories) { photo ->
                        PhotoGridItem(
                            photo = photo,
                            onClick = {
                                selectedPhoto = photo
                                showPhotoDetailDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 사진 추가 다이얼로그 (설명 입력)
    if (showAddPhotoDialog && selectedImagePath != null) {
        AlertDialog(
            onDismissRequest = { 
                showAddPhotoDialog = false
                selectedImagePath = null
                descriptionInput = ""
            },
            title = { Text("사진 추가") },
            text = {
                Column {
                    // 선택한 이미지 미리보기
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(selectedImagePath!!))
                                .crossfade(true)
                                .build(),
                            contentDescription = "선택한 사진",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("설명 (선택사항)") },
                        placeholder = { Text("이 사진에 대해 적어주세요") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedImagePath?.let {
                            viewModel.addPhoto(it, descriptionInput)
                        }
                        showAddPhotoDialog = false
                        selectedImagePath = null
                        descriptionInput = ""
                        scope.launch { snackbarHostState.showSnackbar("사진이 저장되었습니다") }
                    }
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddPhotoDialog = false
                        selectedImagePath = null
                        descriptionInput = ""
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
    
    // 사진 상세보기 다이얼로그
    if (showPhotoDetailDialog && selectedPhoto != null) {
        Dialog(
            onDismissRequest = { 
                showPhotoDetailDialog = false
                selectedPhoto = null
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    // 사진
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(selectedPhoto!!.photo))
                                .crossfade(true)
                                .build(),
                            contentDescription = "사진",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // 닫기 버튼
                        IconButton(
                            onClick = { 
                                showPhotoDetailDialog = false
                                selectedPhoto = null
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "닫기",
                                tint = Color.White
                            )
                        }
                    }
                    
                    // 정보
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedPhoto!!.date,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        
                        if (selectedPhoto!!.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedPhoto!!.description,
                                fontSize = 15.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 삭제 버튼
                        OutlinedButton(
                            onClick = {
                                selectedPhoto?.let { viewModel.deletePhoto(it) }
                                showPhotoDetailDialog = false
                                selectedPhoto = null
                                scope.launch { snackbarHostState.showSnackbar("사진이 삭제되었습니다") }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            )
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
            }
        }
    }
}

@Composable
fun PhotoGridItem(
    photo: PhotoMemory,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(File(photo.photo))
                .crossfade(true)
                .build(),
            contentDescription = "사진",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 설명이 있으면 표시
        if (photo.description.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
                    .padding(4.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
