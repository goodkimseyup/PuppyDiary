package com.example.puppydiary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.puppydiary.data.model.*
import com.example.puppydiary.ui.theme.AppColors
import com.example.puppydiary.viewmodel.PuppyViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenNaver(
    viewModel: PuppyViewModel,
    onNavigateToGallery: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val puppyData by viewModel.puppyData.collectAsState()
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    val weightRecords by viewModel.weightRecords.collectAsState()
    val vaccinations by viewModel.vaccinations.collectAsState()
    val recentActivities by viewModel.recentActivities.collectAsState()
    val walkRecords by viewModel.walkRecords.collectAsState()
    val mealRecords by viewModel.mealRecords.collectAsState()
    val hospitalVisits by viewModel.hospitalVisits.collectAsState()
    val medicationRecords by viewModel.medicationRecords.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    // Dialog states
    var showWeightDialog by remember { mutableStateOf(false) }
    var showVaccineDialog by remember { mutableStateOf(false) }
    var showDiaryDialog by remember { mutableStateOf(false) }
    var showWalkDialog by remember { mutableStateOf(false) }
    var showMealDialog by remember { mutableStateOf(false) }
    var showHospitalDialog by remember { mutableStateOf(false) }
    var showMedicationDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    // Input states
    var weightInput by remember { mutableStateOf("") }
    var vaccineInput by remember { mutableStateOf("") }
    var nextDateInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoPath by remember { mutableStateOf<String?>(null) }

    // Walk inputs
    var walkDurationInput by remember { mutableStateOf("") }
    var walkDistanceInput by remember { mutableStateOf("") }
    var walkNoteInput by remember { mutableStateOf("") }

    // Meal inputs
    var mealFoodNameInput by remember { mutableStateOf("") }
    var mealAmountInput by remember { mutableStateOf("") }
    var mealNoteInput by remember { mutableStateOf("") }

    // Hospital inputs
    var hospitalNameInput by remember { mutableStateOf("") }
    var hospitalReasonInput by remember { mutableStateOf("") }
    var hospitalCostInput by remember { mutableStateOf("") }
    var hospitalNoteInput by remember { mutableStateOf("") }

    // Medication inputs
    var medicationNameInput by remember { mutableStateOf("") }
    var medicationIntervalInput by remember { mutableStateOf("30") }
    var medicationNoteInput by remember { mutableStateOf("") }

    // Contact inputs
    var contactNameInput by remember { mutableStateOf("") }
    var contactPhoneInput by remember { mutableStateOf("") }
    var contactAddressInput by remember { mutableStateOf("") }
    
    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val fileName = "diary_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                photoPath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF7F9FA)),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 네이버 스타일 헤더
            item {
                NaverHeader()
            }
            
            // 검색바
            item {
                NaverSearchBar(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            
            // 프로필 카드
            item {
                puppyData?.let { puppy ->
                    NaverProfileCard(
                        puppy = puppy,
                        currentWeight = weightRecords.lastOrNull()?.weight?.toDouble() ?: 0.0,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            // 업적 카드
            item {
                val completedCount = achievements.count { it.isUnlocked }
                val totalCount = achievements.size
                AchievementCard(
                    completedCount = completedCount,
                    totalCount = totalCount,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 아이콘 그리드 (2줄)
            item {
                NaverIconGridExpanded(
                    onWeightClick = { showWeightDialog = true },
                    onVaccineClick = { showVaccineDialog = true },
                    onDiaryClick = { showDiaryDialog = true },
                    onGalleryClick = onNavigateToGallery,
                    onWalkClick = { showWalkDialog = true },
                    onMealClick = { showMealDialog = true },
                    onHospitalClick = { showHospitalDialog = true },
                    onMedicationClick = { showMedicationDialog = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            
            // 최근 활동
            item {
                Text(
                    text = "최근 활동",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            
            items(recentActivities.take(5)) { activity ->
                NaverActivityCard(
                    activity = activity,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val weight = weightInput.toFloatOrNull()
                        if (weight != null) {
                            viewModel.addWeightRecord(weight)
                            weightInput = ""
                            showWeightDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("몸무게가 기록되었습니다")
                            }
                        }
                    }
                ) {
                    Text("저장", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 예방접종 다이얼로그
    if (showVaccineDialog) {
        AlertDialog(
            onDismissRequest = { showVaccineDialog = false },
            title = { Text("예방접종 기록") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = vaccineInput,
                        onValueChange = { vaccineInput = it },
                        label = { Text("백신명") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nextDateInput,
                        onValueChange = { nextDateInput = it },
                        label = { Text("다음 접종일 (yyyy-MM-dd)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (vaccineInput.isNotBlank() && nextDateInput.isNotBlank()) {
                            viewModel.addVaccination(vaccineInput, nextDateInput)
                            vaccineInput = ""
                            nextDateInput = ""
                            showVaccineDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("예방접종이 기록되었습니다")
                            }
                        }
                    }
                ) {
                    Text("저장", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVaccineDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 일기 다이얼로그
    if (showDiaryDialog) {
        AlertDialog(
            onDismissRequest = { showDiaryDialog = false },
            title = { Text("일기 작성") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("제목") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("내용") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("사진 선택")
                    }
                    photoUri?.let { uri ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uri)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
                            viewModel.addDiaryEntry(titleInput, contentInput, photoPath)
                            titleInput = ""
                            contentInput = ""
                            photoUri = null
                            photoPath = null
                            showDiaryDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("일기가 작성되었습니다")
                            }
                        }
                    }
                ) {
                    Text("저장", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiaryDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 산책 다이얼로그
    if (showWalkDialog) {
        AlertDialog(
            onDismissRequest = { showWalkDialog = false },
            title = { Text("산책 기록") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = walkDurationInput,
                        onValueChange = { walkDurationInput = it },
                        label = { Text("산책 시간 (분)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = walkDistanceInput,
                        onValueChange = { walkDistanceInput = it },
                        label = { Text("거리 (미터, 선택사항)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = walkNoteInput,
                        onValueChange = { walkNoteInput = it },
                        label = { Text("메모 (선택사항)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val duration = walkDurationInput.toIntOrNull()
                        if (duration != null && duration > 0) {
                            val distance = walkDistanceInput.toFloatOrNull()
                            val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            viewModel.addWalkRecord(
                                startTime = now,
                                endTime = now,
                                durationMinutes = duration,
                                distanceMeters = distance,
                                note = walkNoteInput.ifBlank { null }
                            )
                            walkDurationInput = ""
                            walkDistanceInput = ""
                            walkNoteInput = ""
                            showWalkDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("산책이 기록되었습니다")
                            }
                        }
                    }
                ) {
                    Text("저장", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWalkDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 식사 다이얼로그
    if (showMealDialog) {
        AlertDialog(
            onDismissRequest = { showMealDialog = false },
            title = { Text("식사 기록") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = mealFoodNameInput,
                        onValueChange = { mealFoodNameInput = it },
                        label = { Text("사료/음식 이름") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = mealAmountInput,
                        onValueChange = { mealAmountInput = it },
                        label = { Text("급여량 (그램)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = mealNoteInput,
                        onValueChange = { mealNoteInput = it },
                        label = { Text("메모 (선택사항)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = mealAmountInput.toFloatOrNull()
                        if (mealFoodNameInput.isNotBlank() && amount != null) {
                            val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            viewModel.addMealRecord(
                                time = now,
                                foodType = "일반",
                                foodName = mealFoodNameInput,
                                amountGrams = amount,
                                note = mealNoteInput.ifBlank { null }
                            )
                            mealFoodNameInput = ""
                            mealAmountInput = ""
                            mealNoteInput = ""
                            showMealDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("식사가 기록되었습니다")
                            }
                        }
                    }
                ) {
                    Text("저장", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMealDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 병원 다이얼로그
    if (showHospitalDialog) {
        AlertDialog(
            onDismissRequest = { showHospitalDialog = false },
            title = { Text("병원 방문 기록") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = hospitalNameInput,
                        onValueChange = { hospitalNameInput = it },
                        label = { Text("병원 이름") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hospitalReasonInput,
                        onValueChange = { hospitalReasonInput = it },
                        label = { Text("방문 사유") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hospitalCostInput,
                        onValueChange = { hospitalCostInput = it },
                        label = { Text("비용 (원, 선택사항)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hospitalNoteInput,
                        onValueChange = { hospitalNoteInput = it },
                        label = { Text("메모 (선택사항)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (hospitalNameInput.isNotBlank() && hospitalReasonInput.isNotBlank()) {
                            val cost = hospitalCostInput.toIntOrNull()
                            viewModel.addHospitalVisit(
                                hospitalName = hospitalNameInput,
                                visitReason = hospitalReasonInput,
                                cost = cost,
                                note = hospitalNoteInput.ifBlank { null }
                            )
                            hospitalNameInput = ""
                            hospitalReasonInput = ""
                            hospitalCostInput = ""
                            hospitalNoteInput = ""
                            showHospitalDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("병원 방문이 기록되었습니다")
                            }
                        }
                    }
                ) {
                    Text("저장", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHospitalDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 투약 다이얼로그
    if (showMedicationDialog) {
        AlertDialog(
            onDismissRequest = { showMedicationDialog = false },
            title = { Text("투약 기록") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = medicationNameInput,
                        onValueChange = { medicationNameInput = it },
                        label = { Text("약 이름 (예: 심장사상충약)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = medicationIntervalInput,
                        onValueChange = { medicationIntervalInput = it },
                        label = { Text("투약 주기 (일)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = medicationNoteInput,
                        onValueChange = { medicationNoteInput = it },
                        label = { Text("메모 (선택사항)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val interval = medicationIntervalInput.toIntOrNull() ?: 30
                        if (medicationNameInput.isNotBlank()) {
                            // 다음 투약일 계산
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.DAY_OF_YEAR, interval)
                            val nextDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

                            viewModel.addMedicationRecord(
                                medicationType = "일반",
                                medicationName = medicationNameInput,
                                nextDate = nextDate,
                                intervalDays = interval,
                                note = medicationNoteInput.ifBlank { null }
                            )
                            medicationNameInput = ""
                            medicationIntervalInput = "30"
                            medicationNoteInput = ""
                            showMedicationDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("투약이 기록되었습니다")
                            }
                        }
                    }
                ) {
                    Text("저장", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMedicationDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 연락처 다이얼로그
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("긴급 연락처 추가") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = contactNameInput,
                        onValueChange = { contactNameInput = it },
                        label = { Text("이름 (예: 우리동물병원)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactPhoneInput,
                        onValueChange = { contactPhoneInput = it },
                        label = { Text("전화번호") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactAddressInput,
                        onValueChange = { contactAddressInput = it },
                        label = { Text("주소 (선택사항)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (contactNameInput.isNotBlank() && contactPhoneInput.isNotBlank()) {
                            viewModel.addEmergencyContact(
                                contactType = "병원",
                                name = contactNameInput,
                                phoneNumber = contactPhoneInput,
                                address = contactAddressInput.ifBlank { null }
                            )
                            contactNameInput = ""
                            contactPhoneInput = ""
                            contactAddressInput = ""
                            showContactDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("연락처가 추가되었습니다")
                            }
                        }
                    }
                ) {
                    Text("저장", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun NaverHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🐕",
                    fontSize = 28.sp
                )
                Text(
                    text = "Puppy Diary",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "알림",
                        tint = Color(0xFF6B7280)
                    )
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "설정",
                        tint = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

@Composable
fun NaverSearchBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "N",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "강아지 정보를 검색해 주세요",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = AppColors.Primary,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.PrimaryLight)
                    .padding(6.dp)
            )
        }
    }
}

@Composable
fun NaverProfileCard(
    puppy: PuppyData,
    currentWeight: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            AppColors.Primary.copy(alpha = 0.1f),
                            Color.White
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 프로필 이미지
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, AppColors.Primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (puppy.profileImage != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(puppy.profileImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = "🐕",
                                fontSize = 48.sp
                            )
                        }
                    }

                    // 정보
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = puppy.name,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🐾", fontSize = 14.sp)
                            Text(
                                text = puppy.breed,
                                fontSize = 15.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 스탯 카드들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = "🎂",
                        value = calculateAge(puppy.birthDate),
                        label = "나이",
                        backgroundColor = Color(0xFFFEF3C7),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = "⚖️",
                        value = "${currentWeight}kg",
                        label = "몸무게",
                        backgroundColor = Color(0xFFDCFCE7),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = "📅",
                        value = puppy.birthDate.take(7),
                        label = "생년월",
                        backgroundColor = Color(0xFFDBEAFE),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    icon: String,
    value: String,
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF374151),
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun NaverIconGrid(
    onWeightClick: () -> Unit,
    onVaccineClick: () -> Unit,
    onDiaryClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NaverIconItem(
                emoji = "⭐",
                text = "몸무게",
                onClick = onWeightClick
            )
            NaverIconItem(
                emoji = "❤️",
                text = "접종",
                onClick = onVaccineClick
            )
            NaverIconItem(
                emoji = "✏️",
                text = "일기",
                onClick = onDiaryClick
            )
            NaverIconItem(
                emoji = "😊",
                text = "사진첩",
                onClick = onGalleryClick
            )
        }
    }
}

@Composable
fun NaverIconItem(
    emoji: String,
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.Black
        )
    }
}

@Composable
fun NaverActivityCard(
    activity: Any,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (activity) {
                            is DiaryEntry -> Color(0xFFFEF3C7)
                            is WeightRecord -> Color(0xFFDCFCE7)
                            is Vaccination -> Color(0xFFDEF7EC)
                            is PhotoMemory -> Color(0xFFE0E7FF)
                            is WalkRecord -> Color(0xFFDBEAFE)
                            is MealRecord -> Color(0xFFFCE7F3)
                            is HospitalVisit -> Color(0xFFFFE4E6)
                            is MedicationRecord -> Color(0xFFE0E7FF)
                            else -> Color.LightGray
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (activity) {
                        is DiaryEntry -> "📝"
                        is WeightRecord -> "⚖️"
                        is Vaccination -> "💉"
                        is PhotoMemory -> "📷"
                        is WalkRecord -> "🚶"
                        is MealRecord -> "🍽️"
                        is HospitalVisit -> "🏥"
                        is MedicationRecord -> "💊"
                        else -> "📌"
                    },
                    fontSize = 20.sp
                )
            }

            // 내용
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (activity) {
                        is DiaryEntry -> activity.title
                        is WeightRecord -> "몸무게 기록"
                        is Vaccination -> activity.vaccine
                        is PhotoMemory -> "사진 추가"
                        is WalkRecord -> "산책 ${activity.durationMinutes}분"
                        is MealRecord -> activity.foodName
                        is HospitalVisit -> activity.hospitalName
                        is MedicationRecord -> activity.medicationName
                        else -> "활동"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = when (activity) {
                        is DiaryEntry -> activity.date
                        is WeightRecord -> "${activity.weight}kg - ${activity.date}"
                        is Vaccination -> activity.date
                        is PhotoMemory -> activity.date
                        is WalkRecord -> activity.date
                        is MealRecord -> "${activity.amountGrams}g - ${activity.date}"
                        is HospitalVisit -> activity.date
                        is MedicationRecord -> activity.date
                        else -> ""
                    },
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

fun calculateAge(birthDate: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birth = format.parse(birthDate) ?: return "0개월"
        val today = Date()
        val diffInMillis = today.time - birth.time
        val days = diffInMillis / (1000 * 60 * 60 * 24)
        val months = (days / 30).toInt()
        val remainingDays = (days % 30).toInt()
        "${months}개월 ${remainingDays}일"
    } catch (e: Exception) {
        "0개월"
    }
}

@Composable
fun AchievementCard(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏆", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = "업적",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$completedCount / $totalCount 달성",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(AppColors.Primary, Color(0xFF4CAF50))
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun NaverIconGridExpanded(
    onWeightClick: () -> Unit,
    onVaccineClick: () -> Unit,
    onDiaryClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onWalkClick: () -> Unit,
    onMealClick: () -> Unit,
    onHospitalClick: () -> Unit,
    onMedicationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            // 첫번째 줄
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModernIconItem(
                    emoji = "⚖️",
                    text = "몸무게",
                    backgroundColor = Color(0xFFDCFCE7),
                    onClick = onWeightClick
                )
                ModernIconItem(
                    emoji = "💉",
                    text = "접종",
                    backgroundColor = Color(0xFFDEF7EC),
                    onClick = onVaccineClick
                )
                ModernIconItem(
                    emoji = "📝",
                    text = "일기",
                    backgroundColor = Color(0xFFFEF3C7),
                    onClick = onDiaryClick
                )
                ModernIconItem(
                    emoji = "📷",
                    text = "사진첩",
                    backgroundColor = Color(0xFFE0E7FF),
                    onClick = onGalleryClick
                )
            }

            Spacer(Modifier.height(12.dp))

            // 두번째 줄
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModernIconItem(
                    emoji = "🚶",
                    text = "산책",
                    backgroundColor = Color(0xFFDBEAFE),
                    onClick = onWalkClick
                )
                ModernIconItem(
                    emoji = "🍽️",
                    text = "식사",
                    backgroundColor = Color(0xFFFCE7F3),
                    onClick = onMealClick
                )
                ModernIconItem(
                    emoji = "🏥",
                    text = "병원",
                    backgroundColor = Color(0xFFFFE4E6),
                    onClick = onHospitalClick
                )
                ModernIconItem(
                    emoji = "💊",
                    text = "투약",
                    backgroundColor = Color(0xFFE0E7FF),
                    onClick = onMedicationClick
                )
            }
        }
    }
}

@Composable
fun ModernIconItem(
    emoji: String,
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151)
        )
    }
}
