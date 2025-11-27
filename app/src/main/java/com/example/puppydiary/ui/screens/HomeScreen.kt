package com.example.puppydiary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puppydiary.data.model.DiaryEntry
import com.example.puppydiary.data.model.PhotoMemory
import com.example.puppydiary.data.model.Vaccination
import com.example.puppydiary.data.model.WeightRecord
import com.example.puppydiary.ui.components.PuppyProfileCard
import com.example.puppydiary.viewmodel.PuppyViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// 견종 목록
val breedList = listOf(
    "말티즈", "푸들", "포메라니안", "치와와", "시츄",
    "요크셔테리어", "비숑프리제", "골든리트리버", "래브라도리트리버", "진돗개",
    "웰시코기", "비글", "닥스훈트", "슈나우저", "보더콜리",
    "사모예드", "시바이누", "프렌치불독", "불독", "허스키",
    "믹스견", "기타"
)

// 최근 활동 통합 타입
sealed class RecentActivity(val date: String, val timestamp: Long) {
    data class Weight(val record: WeightRecord, val d: String, val t: Long) : RecentActivity(d, t)
    data class Vaccine(val vaccination: Vaccination, val d: String, val t: Long) : RecentActivity(d, t)
    data class Diary(val entry: DiaryEntry, val d: String, val t: Long) : RecentActivity(d, t)
    data class Photo(val photo: PhotoMemory, val d: String, val t: Long) : RecentActivity(d, t)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: PuppyViewModel) {
    val context = LocalContext.current
    val puppyData by viewModel.puppyData.collectAsState()
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    val weightRecords by viewModel.weightRecords.collectAsState()
    val vaccinations by viewModel.vaccinations.collectAsState()
    val photoMemories by viewModel.photoMemories.collectAsState()

    // Snackbar 상태
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 현재 몸무게 (weightRecords가 변경될 때마다 자동 업데이트)
    val currentWeight = weightRecords.lastOrNull()?.weight ?: 0f

    // 최근 활동 통합 (날짜순 정렬)
    val recentActivities by remember {
        derivedStateOf {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val activities = mutableListOf<RecentActivity>()

            // 일기 추가
            diaryEntries.forEach { entry ->
                val timestamp = try {
                    dateFormat.parse(entry.date)?.time ?: 0L
                } catch (e: Exception) { 0L }
                activities.add(RecentActivity.Diary(entry, entry.date, timestamp))
            }

            // 몸무게 추가
            weightRecords.forEach { record ->
                val timestamp = try {
                    dateFormat.parse(record.date)?.time ?: 0L
                } catch (e: Exception) { 0L }
                activities.add(RecentActivity.Weight(record, record.date, timestamp))
            }

            // 접종 추가
            vaccinations.forEach { vaccine ->
                val timestamp = try {
                    dateFormat.parse(vaccine.date)?.time ?: 0L
                } catch (e: Exception) { 0L }
                activities.add(RecentActivity.Vaccine(vaccine, vaccine.date, timestamp))
            }

            // 사진 추가
            photoMemories.forEach { photo ->
                val timestamp = try {
                    dateFormat.parse(photo.date)?.time ?: 0L
                } catch (e: Exception) { 0L }
                activities.add(RecentActivity.Photo(photo, photo.date, timestamp))
            }

            // 최신순 정렬 후 상위 5개
            activities.sortedByDescending { it.timestamp }.take(5)
        }
    }

    var showWeightDialog by remember { mutableStateOf(false) }
    var showVaccineDialog by remember { mutableStateOf(false) }
    var showDiaryDialog by remember { mutableStateOf(false) }
    var showDiaryDetailDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showProfileEditDialog by remember { mutableStateOf(false) }
    var showBirthDatePicker by remember { mutableStateOf(false) }
    var selectedDiaryId by remember { mutableStateOf<Long?>(null) }

    var weightInput by remember { mutableStateOf("") }
    var vaccineInput by remember { mutableStateOf("") }
    var nextDateInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    
    // 프로필 수정용 상태
    var editName by remember { mutableStateOf("") }
    var editBreed by remember { mutableStateOf("") }
    var editBirthDate by remember { mutableStateOf("") }
    var showBreedDropdown by remember { mutableStateOf(false) }

    // DatePicker 상태
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    
    val birthDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    // 이미지 선택 런처 (프로필 이미지 변경용)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val fileName = "puppy_profile_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.updateProfileImage(file.absolutePath)
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
            Column {
                Text(
                    text = "🐾 펫 다이어리",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                puppyData?.let { data ->
                    PuppyProfileCard(
                        puppyData = data,
                        age = viewModel.calculateAge(),
                        currentWeight = currentWeight,
                        onImageClick = { imagePickerLauncher.launch("image/*") },
                        onEditClick = {
                            // 현재 값으로 초기화
                            editName = data.name
                            editBreed = data.breed
                            editBirthDate = data.birthDate
                            showProfileEditDialog = true
                        }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Star,
                    label = "몸무게",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f),
                    onClick = { showWeightDialog = true }
                )
                QuickActionButton(
                    icon = Icons.Default.Favorite,
                    label = "접종",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    onClick = { showVaccineDialog = true }
                )
                QuickActionButton(
                    icon = Icons.Default.Edit,
                    label = "일기",
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f),
                    onClick = { showDiaryDialog = true }
                )
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "최근 활동",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    
                    if (recentActivities.isEmpty()) {
                        Text(
                            text = "아직 기록이 없습니다",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 최근 활동 목록
        items(recentActivities) { activity ->
            when (activity) {
                is RecentActivity.Diary -> {
                    ActivityCard(
                        icon = Icons.Default.Edit,
                        iconColor = Color(0xFF9C27B0),
                        title = activity.entry.title,
                        subtitle = "일기",
                        date = activity.date,
                        onClick = {
                            selectedDiaryId = activity.entry.id
                            showDiaryDetailDialog = true
                        }
                    )
                }
                is RecentActivity.Weight -> {
                    ActivityCard(
                        icon = Icons.Default.Star,
                        iconColor = Color(0xFF2196F3),
                        title = "${activity.record.weight} kg",
                        subtitle = "몸무게 기록",
                        date = activity.date,
                        onClick = { }
                    )
                }
                is RecentActivity.Vaccine -> {
                    ActivityCard(
                        icon = Icons.Default.Favorite,
                        iconColor = Color(0xFF4CAF50),
                        title = activity.vaccination.vaccine,
                        subtitle = "예방접종 (다음: ${activity.vaccination.nextDate})",
                        date = activity.date,
                        onClick = { }
                    )
                }
                is RecentActivity.Photo -> {
                    ActivityCard(
                        icon = Icons.Default.Face,
                        iconColor = Color(0xFFE91E63),
                        title = if (activity.photo.description.isNotEmpty()) activity.photo.description else "사진",
                        subtitle = "사진첩",
                        date = activity.date,
                        onClick = { }
                    )
                }
            }
        }
        }
    }

    // 프로필 수정 다이얼로그
    if (showProfileEditDialog) {
        AlertDialog(
            onDismissRequest = { showProfileEditDialog = false },
            title = { 
                Text(
                    text = "🐕 프로필 수정",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 이름 입력
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("이름") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // 견종 선택 (드롭다운)
                    ExposedDropdownMenuBox(
                        expanded = showBreedDropdown,
                        onExpandedChange = { showBreedDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = editBreed,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("견종") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBreedDropdown)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showBreedDropdown,
                            onDismissRequest = { showBreedDropdown = false }
                        ) {
                            breedList.forEach { breed ->
                                DropdownMenuItem(
                                    text = { Text(breed) },
                                    onClick = {
                                        editBreed = breed
                                        showBreedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // 생년월일 선택
                    OutlinedTextField(
                        value = editBirthDate,
                        onValueChange = { },
                        label = { Text("생년월일") },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBirthDatePicker = true },
                        trailingIcon = {
                            IconButton(onClick = { showBirthDatePicker = true }) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "날짜 선택",
                                    tint = Color(0xFFE91E63)
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editName.isNotEmpty() && editBreed.isNotEmpty() && editBirthDate.isNotEmpty()) {
                            viewModel.updatePuppy(editName, editBreed, editBirthDate)
                            showProfileEditDialog = false
                            scope.launch { snackbarHostState.showSnackbar("프로필이 수정되었습니다") }
                        }
                    },
                    enabled = editName.isNotEmpty() && editBreed.isNotEmpty() && editBirthDate.isNotEmpty()
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileEditDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
    
    // 생년월일 DatePicker 다이얼로그
    if (showBirthDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showBirthDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        birthDatePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            editBirthDate = sdf.format(Date(millis))
                        }
                        showBirthDatePicker = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBirthDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(
                state = birthDatePickerState,
                title = {
                    Text(
                        text = "생년월일 선택",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                }
            )
        }
    }

    // 일기 상세 다이얼로그
    if (showDiaryDetailDialog && selectedDiaryId != null) {
        val selectedEntry = diaryEntries.find { it.id == selectedDiaryId }
        selectedEntry?.let { entry ->
            AlertDialog(
                onDismissRequest = { showDiaryDetailDialog = false },
                title = { Text(entry.title) },
                text = {
                    Column {
                        Text(
                            text = entry.date,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(text = entry.content)
                        if (entry.photo != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "📷 사진 첨부됨",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDiaryDetailDialog = false }) {
                        Text("닫기")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDiaryEntry(entry.id)
                            showDiaryDetailDialog = false
                            scope.launch { snackbarHostState.showSnackbar("일기가 삭제되었습니다") }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("삭제")
                    }
                }
            )
        }
    }

    // 몸무게 다이얼로그
    if (showWeightDialog) {
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text("몸무게 기록") },
            text = {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("몸무게 (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (weightInput.isNotEmpty()) {
                            viewModel.addWeightRecord(weightInput.toFloatOrNull() ?: 0f)
                            weightInput = ""
                            showWeightDialog = false
                            scope.launch { snackbarHostState.showSnackbar("몸무게가 저장되었습니다") }
                        }
                    }
                ) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) { Text("취소") }
            }
        )
    }

    // 예방접종 다이얼로그
    if (showVaccineDialog) {
        AlertDialog(
            onDismissRequest = { showVaccineDialog = false },
            title = { Text("예방접종 기록") },
            text = {
                Column {
                    OutlinedTextField(
                        value = vaccineInput,
                        onValueChange = { vaccineInput = it },
                        label = { Text("백신명") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 날짜 선택 필드
                    OutlinedTextField(
                        value = nextDateInput,
                        onValueChange = { },
                        label = { Text("다음 접종일") },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "날짜 선택",
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                        },
                        placeholder = { Text("날짜를 선택하세요") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (vaccineInput.isNotEmpty() && nextDateInput.isNotEmpty()) {
                            viewModel.addVaccination(vaccineInput, nextDateInput)
                            vaccineInput = ""
                            nextDateInput = ""
                            showVaccineDialog = false
                            scope.launch { snackbarHostState.showSnackbar("예방접종이 저장되었습니다") }
                        }
                    },
                    enabled = vaccineInput.isNotEmpty() && nextDateInput.isNotEmpty()
                ) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    vaccineInput = ""
                    nextDateInput = ""
                    showVaccineDialog = false 
                }) { Text("취소") }
            }
        )
    }

    // DatePicker 다이얼로그 (접종일용)
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            nextDateInput = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "다음 접종일 선택",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                }
            )
        }
    }

    // 일기 작성 다이얼로그
    if (showDiaryDialog) {
        AlertDialog(
            onDismissRequest = { showDiaryDialog = false },
            title = { Text("일기 작성") },
            text = {
                Column {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("제목") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("내용") },
                        minLines = 3
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
                            showDiaryDialog = false
                            scope.launch { snackbarHostState.showSnackbar("일기가 저장되었습니다") }
                        }
                    }
                ) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = { showDiaryDialog = false }) { Text("취소") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    date: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = date,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = color,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}
