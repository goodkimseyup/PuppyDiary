package com.example.puppydiary.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import com.example.puppydiary.utils.allBreedList
import com.example.puppydiary.utils.getBreedEmoji
import com.example.puppydiary.ui.theme.AppColors
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// 견종/묘종 목록은 PetUtils에서 가져옴
val breedList = allBreedList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: PuppyViewModel, navController: NavController) {
    val context = LocalContext.current
    val puppyData by viewModel.puppyData.collectAsState()
    val allPuppies by viewModel.allPuppies.collectAsState()
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    val weightRecords by viewModel.weightRecords.collectAsState()
    val vaccinations by viewModel.vaccinations.collectAsState()
    val photoMemories by viewModel.photoMemories.collectAsState()
    val recentActivities by viewModel.recentActivities.collectAsState()

    // 디버깅용 로그
    LaunchedEffect(recentActivities) {
        Log.d("PuppyDiary", "HomeScreen recentActivities: ${recentActivities.size} items")
        recentActivities.forEach { activity ->
            Log.d("PuppyDiary", "  - ${activity::class.simpleName}: $activity")
        }
    }

    // Snackbar 상태
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 검색 상태
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // 강아지 선택 다이얼로그
    var showPuppySelector by remember { mutableStateOf(false) }

    // 현재 몸무게 (weightRecords가 변경될 때마다 자동 업데이트)
    val currentWeight = weightRecords.lastOrNull()?.weight ?: 0f

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
                // 타이틀 + 강아지 선택 + 검색
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "나의반쪽",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        // 다견 표시 (2마리 이상일 때)
                        if (allPuppies.size > 1) {
                            Surface(
                                modifier = Modifier.padding(start = 10.dp),
                                color = AppColors.Primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "${allPuppies.size}마리",
                                    fontSize = 12.sp,
                                    color = AppColors.Primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // 강아지 전환 버튼
                        Surface(
                            onClick = { showPuppySelector = true },
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.Secondary.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "강아지 선택",
                                tint = AppColors.Secondary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        // 검색 버튼
                        Surface(
                            onClick = { showSearchBar = !showSearchBar },
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.Primary.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                imageVector = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "검색",
                                tint = AppColors.Primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // 검색바
                if (showSearchBar) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "일기, 사진, 접종 검색...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "지우기")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = AppColors.Primary
                            )
                        )
                    }
                }

                puppyData?.let { data ->
                    PuppyProfileCard(
                        puppyData = data,
                        age = viewModel.calculateAge(),
                        currentWeight = currentWeight,
                        birthdayDday = viewModel.getBirthdayDday(),
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = AppColors.Primary.copy(alpha = 0.1f),
                        spotColor = AppColors.Primary.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (searchQuery.isNotEmpty()) 
                                        AppColors.Primary.copy(alpha = 0.12f)
                                    else 
                                        AppColors.Warm.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (searchQuery.isNotEmpty()) Icons.Default.Search else Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (searchQuery.isNotEmpty()) AppColors.Primary else AppColors.Warm,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "검색 결과" else "최근 활동",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (recentActivities.isEmpty() && searchQuery.isEmpty()) {
                        Text(
                            text = "아직 기록이 없습니다",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 검색 결과 필터링
        val filteredActivities = if (searchQuery.isNotEmpty()) {
            val query = searchQuery.lowercase()
            val results = mutableListOf<Any>()

            // 일기 검색
            diaryEntries.filter {
                it.title.lowercase().contains(query) ||
                it.content.lowercase().contains(query)
            }.forEach { results.add(it) }

            // 접종 검색
            vaccinations.filter {
                it.vaccine.lowercase().contains(query)
            }.forEach { results.add(it) }

            // 사진 설명 검색
            photoMemories.filter {
                it.description.lowercase().contains(query)
            }.forEach { results.add(it) }

            // 몸무게는 날짜로 검색
            weightRecords.filter {
                it.date.contains(query)
            }.forEach { results.add(it) }

            results
        } else {
            recentActivities
        }

        // 검색 결과 없음 표시
        if (searchQuery.isNotEmpty() && filteredActivities.isEmpty()) {
            item {
                Text(
                    text = "\"$searchQuery\"에 대한 검색 결과가 없습니다",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        // 활동 목록
        items(filteredActivities) { activity ->
            when (activity) {
                is DiaryEntry -> {
                    ActivityCard(
                        icon = Icons.Default.Edit,
                        iconColor = Color(0xFF9C27B0),
                        title = activity.title,
                        subtitle = "일기",
                        date = activity.date,
                        onClick = { navController.navigate("diary") }
                    )
                }
                is WeightRecord -> {
                    ActivityCard(
                        icon = Icons.Default.Star,
                        iconColor = Color(0xFF2196F3),
                        title = "${activity.weight} kg",
                        subtitle = "몸무게 기록",
                        date = activity.date,
                        onClick = { navController.navigate("stats") }
                    )
                }
                is Vaccination -> {
                    ActivityCard(
                        icon = Icons.Default.Favorite,
                        iconColor = Color(0xFF4CAF50),
                        title = activity.vaccine,
                        subtitle = "예방접종 (다음: ${activity.nextDate})",
                        date = activity.date,
                        onClick = { navController.navigate("stats") }
                    )
                }
                is PhotoMemory -> {
                    ActivityCard(
                        icon = Icons.Default.Face,
                        iconColor = Color(0xFFE91E63),
                        title = if (activity.description.isNotEmpty()) activity.description else "사진",
                        subtitle = "사진첩",
                        date = activity.date,
                        onClick = { navController.navigate("gallery") }
                    )
                }
            }
        }
        }
    }

    // 강아지 선택/추가 다이얼로그
    if (showPuppySelector) {
        AlertDialog(
            onDismissRequest = { showPuppySelector = false },
            title = { 
                Text(
                    text = "🐾 반려동물 선택",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 강아지 목록
                    allPuppies.forEach { puppy ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectPuppy(puppy.id)
                                    showPuppySelector = false
                                    scope.launch { snackbarHostState.showSnackbar("${puppy.name}(으)로 전환했습니다") }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (puppy.isSelected) 
                                    Color(0xFFE91E63).copy(alpha = 0.1f) 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 프로필 이미지 또는 이모지
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            Color(0xFFE91E63).copy(alpha = 0.1f),
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = getBreedEmoji(puppy.breed), fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = puppy.name,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = puppy.breed,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                if (puppy.isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "선택됨",
                                        tint = Color(0xFFE91E63),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // 새 강아지 추가 버튼
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPuppySelector = false
                                navController.navigate("register")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "추가",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "새 반려동물 등록",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPuppySelector = false }) {
                    Text("닫기")
                }
            }
        )
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = iconColor.copy(alpha = 0.1f),
                spotColor = iconColor.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘 배경
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        iconColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = date,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
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
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = color.copy(alpha = 0.15f),
                spotColor = color.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 20.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}
