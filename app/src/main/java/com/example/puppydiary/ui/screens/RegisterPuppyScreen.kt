package com.example.puppydiary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.puppydiary.viewmodel.PuppyViewModel
import com.example.puppydiary.utils.dogBreedList
import com.example.puppydiary.utils.catBreedList
import com.example.puppydiary.utils.getBreedEmoji
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPuppyScreen(
    viewModel: PuppyViewModel,
    onRegistrationComplete: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("강아지") } // 강아지 또는 고양이
    var breed by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var savedImagePath by remember { mutableStateOf<String?>(null) }

    var showBreedDropdown by remember { mutableStateOf(false) }

    // 선택된 타입에 따른 종류 목록
    val breeds = if (petType == "강아지") dogBreedList else catBreedList

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // 이미지를 앱 내부 저장소에 복사
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val fileName = "puppy_profile_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                savedImagePath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🐾 우리 아이 등록하기",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 프로필 이미지 선택
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5))
                .border(3.dp, Color(0xFFE91E63), CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(selectedImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "프로필 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (breed.isNotEmpty()) {
                // 견종/묘종 선택 시 해당 이모지 표시
                Text(
                    text = getBreedEmoji(breed),
                    fontSize = 60.sp
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = "사진 추가",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 이름 입력
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            placeholder = { Text("반려동물 이름을 입력하세요") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 강아지/고양이 선택
        Text(
            text = "반려동물 종류",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 강아지 버튼
            FilterChip(
                selected = petType == "강아지",
                onClick = {
                    petType = "강아지"
                    breed = "" // 종류 초기화
                },
                label = { Text("🐕 강아지") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE91E63).copy(alpha = 0.2f)
                )
            )
            // 고양이 버튼
            FilterChip(
                selected = petType == "고양이",
                onClick = {
                    petType = "고양이"
                    breed = "" // 종류 초기화
                },
                label = { Text("🐱 고양이") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF9C27B0).copy(alpha = 0.2f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 견종/묘종 선택
        ExposedDropdownMenuBox(
            expanded = showBreedDropdown,
            onExpandedChange = { showBreedDropdown = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text(if (petType == "강아지") "견종" else "묘종") },
                placeholder = { Text("${if (petType == "강아지") "견종" else "묘종"}을 선택하세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                leadingIcon = if (breed.isNotEmpty()) {
                    { Text(getBreedEmoji(breed), fontSize = 20.sp) }
                } else null,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBreedDropdown) },
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = showBreedDropdown,
                onDismissRequest = { showBreedDropdown = false }
            ) {
                breeds.forEach { breedOption ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(getBreedEmoji(breedOption), fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(breedOption)
                            }
                        },
                        onClick = {
                            breed = breedOption
                            showBreedDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 생년월일 입력
        Text(
            text = "생년월일",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.Medium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = birthYear,
                onValueChange = { if (it.length <= 4) birthYear = it.filter { c -> c.isDigit() } },
                label = { Text("년") },
                placeholder = { Text("2023") },
                modifier = Modifier.weight(1.2f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = birthMonth,
                onValueChange = { if (it.length <= 2) birthMonth = it.filter { c -> c.isDigit() } },
                label = { Text("월") },
                placeholder = { Text("03") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = birthDay,
                onValueChange = { if (it.length <= 2) birthDay = it.filter { c -> c.isDigit() } },
                label = { Text("일") },
                placeholder = { Text("15") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 등록 버튼
        Button(
            onClick = {
                if (name.isNotBlank() && breed.isNotBlank() && 
                    birthYear.isNotBlank() && birthMonth.isNotBlank() && birthDay.isNotBlank()) {
                    val birthDate = "$birthYear-${birthMonth.padStart(2, '0')}-${birthDay.padStart(2, '0')}"
                    viewModel.registerPuppy(name, breed, birthDate, savedImagePath)
                    onRegistrationComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = name.isNotBlank() && breed.isNotBlank() && 
                      birthYear.length == 4 && birthMonth.isNotBlank() && birthDay.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE91E63)
            )
        ) {
            Text(
                text = "등록하기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "등록된 정보는 나중에 수정할 수 있어요",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}
