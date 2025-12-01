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
import com.example.puppydiary.utils.dogBreedList
import com.example.puppydiary.utils.catBreedList
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

val breedList = allBreedList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PuppyViewModel,
    onNavigateToGallery: () -> Unit = { }
) {
    val context = LocalContext.current
    val puppyData by viewModel.puppyData.collectAsState()
    val allPuppies by viewModel.allPuppies.collectAsState()
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    val weightRecords by viewModel.weightRecords.collectAsState()
    val vaccinations by viewModel.vaccinations.collectAsState()
    val photoMemories by viewModel.photoMemories.collectAsState()
    val recentActivities by viewModel.recentActivities.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showPuppySelector by remember { mutableStateOf(false) }
    
    // 새 반려동물 등록 다이얼로그
    var showAddPuppyDialog by remember { mutableStateOf(false) }
    var newPuppyName by remember { mutableStateOf("") }
    var newPuppyBreed by remember { mutableStateOf("") }
    var newPuppyBirthDate by remember { mutableStateOf("") }
    var newPuppyPetType by remember { mutableStateOf("강아지") }
    var showNewPuppyBreedDropdown by remember { mutableStateOf(false) }
    var showNewPuppyBirthDatePicker by remember { mutableStateOf(false) }
    val newPuppyBirthDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val currentWeight = weightRecords.lastOrNull()?.weight ?: 0f

    var showWeightDialog by remember { mutableStateOf(false) }
    var showVaccineDialog by remember { mutableStateOf(false) }
    var showDiaryDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showProfileEditDialog by remember { mutableStateOf(false) }
    var showBirthDatePicker by remember { mutableStateOf(false) }

    var weightInput by remember { mutableStateOf("") }
    var vaccineInput by remember { mutableStateOf("") }
    var nextDateInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    
    var editName by remember { mutableStateOf("") }
    var editBreed by remember { mutableStateOf("") }
    var editBirthDate by remember { mutableStateOf("") }
    var showBreedDropdown by remember { mutableStateOf(false) }

    // 최근 활동 수정/삭제 다이얼로그 상태
    var showEditWeightDialog by remember { mutableStateOf(false) }
    var showEditVaccinationDialog by remember { mutableStateOf(false) }
    var showEditDiaryDialog by remember { mutableStateOf(false) }
    var selectedWeightRecord by remember { mutableStateOf<WeightRecord?>(null) }
    var selectedVaccination by remember { mutableStateOf<Vaccination?>(null) }
    var selectedDiaryEntry by remember { mutableStateOf<DiaryEntry?>(null) }
    var editWeightInput by remember { mutableStateOf("") }
    var editVaccineInput by remember { mutableStateOf("") }
    var editNextDateInput by remember { mutableStateOf("") }
    var editCompletedInput by remember { mutableStateOf(false) }
    var editTitleInput by remember { mutableStateOf("") }
    var editContentInput by remember { mutableStateOf("") }
    var editDiaryPhotoPath by remember { mutableStateOf<String?>(null) }
    var newDiaryPhotoPath by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val birthDatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

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
    
    val diaryImagePickerLauncher = rememberLauncherForActivityResult(
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
                editDiaryPhotoPath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val newDiaryImagePickerLauncher = rememberLauncherForActivityResult(
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
                newDiaryPhotoPath = file.absolutePath
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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "펫 다이어리",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
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

                    if (showSearchBar) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("일기, 사진, 접종 검색...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    QuickActionButton(
                        icon = Icons.Default.Face,
                        label = "사진첩",
                        color = Color(0xFFE91E63),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToGallery() }
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).background(
                                    if (searchQuery.isNotEmpty()) AppColors.Primary.copy(alpha = 0.12f)
                                    else AppColors.Warm.copy(alpha = 0.12f),
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
                            Text(text = "아직 기록이 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }
            }

            val filteredActivities = if (searchQuery.isNotEmpty()) {
                val query = searchQuery.lowercase()
                val results = mutableListOf<Any>()
                diaryEntries.filter { it.title.lowercase().contains(query) || it.content.lowercase().contains(query) }.forEach { results.add(it) }
                vaccinations.filter { it.vaccine.lowercase().contains(query) }.forEach { results.add(it) }
                photoMemories.filter { it.description.lowercase().contains(query) }.forEach { results.add(it) }
                weightRecords.filter { it.date.contains(query) }.forEach { results.add(it) }
                results
            } else {
                recentActivities
            }

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

            items(filteredActivities) { activity ->
                when (activity) {
                    is DiaryEntry -> {
                        ActivityCard(
                            icon = Icons.Default.Edit,
                            iconColor = Color(0xFF9C27B0),
                            title = activity.title,
                            subtitle = "일기",
                            date = activity.date,
                            onClick = {
                                selectedDiaryEntry = activity
                                editTitleInput = activity.title
                                editContentInput = activity.content
                                editDiaryPhotoPath = activity.photo
                                showEditDiaryDialog = true
                            }
                        )
                    }
                    is WeightRecord -> {
                        ActivityCard(
                            icon = Icons.Default.Star,
                            iconColor = Color(0xFF2196F3),
                            title = "${activity.weight} kg",
                            subtitle = "몸무게 기록",
                            date = activity.date,
                            onClick = {
                                selectedWeightRecord = activity
                                editWeightInput = activity.weight.toString()
                                showEditWeightDialog = true
                            }
                        )
                    }
                    is Vaccination -> {
                        ActivityCard(
                            icon = Icons.Default.Favorite,
                            iconColor = Color(0xFF4CAF50),
                            title = activity.vaccine,
                            subtitle = "예방접종 (다음: ${activity.nextDate})",
                            date = activity.date,
                            onClick = {
                                selectedVaccination = activity
                                editVaccineInput = activity.vaccine
                                editNextDateInput = activity.nextDate
                                editCompletedInput = activity.completed
                                showEditVaccinationDialog = true
                            }
                        )
                    }
                    is PhotoMemory -> {
                        ActivityCard(
                            icon = Icons.Default.Face,
                            iconColor = Color(0xFFE91E63),
                            title = if (activity.description.isNotEmpty()) activity.description else "사진",
                            subtitle = "사진첩",
                            date = activity.date,
                            onClick = { onNavigateToGallery() }
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
            title = { Text(text = "🐾 반려동물 선택", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allPuppies.forEach { puppy ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                viewModel.selectPuppy(puppy.id)
                                showPuppySelector = false
                                scope.launch { snackbarHostState.showSnackbar("${puppy.name}(으)로 전환했습니다") }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (puppy.isSelected) Color(0xFFE91E63).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(Color(0xFFE91E63).copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = getBreedEmoji(puppy.breed), fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = puppy.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                    Text(text = puppy.breed, fontSize = 12.sp, color = Color.Gray)
                                }
                                if (puppy.isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "선택됨", tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                    
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth().clickable {
                            showPuppySelector = false
                            newPuppyName = ""
                            newPuppyBreed = ""
                            newPuppyBirthDate = ""
                            newPuppyPetType = "강아지"
                            showAddPuppyDialog = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "추가", tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "새 반려동물 등록", color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPuppySelector = false }) { Text("닫기") } }
        )
    }

    // 새 반려동물 등록 다이얼로그
    if (showAddPuppyDialog) {
        val currentBreedList = if (newPuppyPetType == "강아지") dogBreedList else catBreedList
        
        AlertDialog(
            onDismissRequest = { showAddPuppyDialog = false },
            title = { Text(text = "🐾 새 반려동물 등록", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 반려동물 종류 선택
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = newPuppyPetType == "강아지",
                            onClick = { newPuppyPetType = "강아지"; newPuppyBreed = "" },
                            label = { Text("🐕 강아지") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = newPuppyPetType == "고양이",
                            onClick = { newPuppyPetType = "고양이"; newPuppyBreed = "" },
                            label = { Text("🐈 고양이") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    OutlinedTextField(
                        value = newPuppyName,
                        onValueChange = { newPuppyName = it },
                        label = { Text("이름") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = showNewPuppyBreedDropdown,
                        onExpandedChange = { showNewPuppyBreedDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = newPuppyBreed,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(if (newPuppyPetType == "강아지") "견종" else "묘종") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showNewPuppyBreedDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showNewPuppyBreedDropdown,
                            onDismissRequest = { showNewPuppyBreedDropdown = false }
                        ) {
                            currentBreedList.forEach { breed ->
                                DropdownMenuItem(
                                    text = { Text(breed) },
                                    onClick = { newPuppyBreed = breed; showNewPuppyBreedDropdown = false }
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = newPuppyBirthDate,
                        onValueChange = { },
                        label = { Text("생년월일") },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showNewPuppyBirthDatePicker = true },
                        trailingIcon = {
                            IconButton(onClick = { showNewPuppyBirthDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "날짜 선택", tint = Color(0xFFE91E63))
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPuppyName.isNotEmpty() && newPuppyBreed.isNotEmpty() && newPuppyBirthDate.isNotEmpty()) {
                            viewModel.addNewPuppy(newPuppyName, newPuppyBreed, newPuppyBirthDate)
                            showAddPuppyDialog = false
                            scope.launch { snackbarHostState.showSnackbar("${newPuppyName}이(가) 등록되었습니다") }
                        }
                    },
                    enabled = newPuppyName.isNotEmpty() && newPuppyBreed.isNotEmpty() && newPuppyBirthDate.isNotEmpty()
                ) { Text("등록") }
            },
            dismissButton = { TextButton(onClick = { showAddPuppyDialog = false }) { Text("취소") } }
        )
    }
    
    // 새 반려동물 생년월일 DatePicker
    if (showNewPuppyBirthDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showNewPuppyBirthDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    newPuppyBirthDatePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        newPuppyBirthDate = sdf.format(Date(millis))
                    }
                    showNewPuppyBirthDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showNewPuppyBirthDatePicker = false }) { Text("취소") } }
        ) {
            DatePicker(state = newPuppyBirthDatePickerState, title = { Text(text = "생년월일 선택", modifier = Modifier.padding(start = 24.dp, top = 16.dp)) })
        }
    }

    // 프로필 수정 다이얼로그
    if (showProfileEditDialog) {
        AlertDialog(
            onDismissRequest = { showProfileEditDialog = false },
            title = { Text(text = "🐕 프로필 수정", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("이름") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    ExposedDropdownMenuBox(expanded = showBreedDropdown, onExpandedChange = { showBreedDropdown = it }) {
                        OutlinedTextField(value = editBreed, onValueChange = { }, readOnly = true, label = { Text("견종/묘종") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBreedDropdown) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = showBreedDropdown, onDismissRequest = { showBreedDropdown = false }) {
                            breedList.forEach { breed -> DropdownMenuItem(text = { Text(breed) }, onClick = { editBreed = breed; showBreedDropdown = false }) }
                        }
                    }
                    OutlinedTextField(value = editBirthDate, onValueChange = { }, label = { Text("생년월일") }, singleLine = true, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { showBirthDatePicker = true }, trailingIcon = { IconButton(onClick = { showBirthDatePicker = true }) { Icon(Icons.Default.DateRange, contentDescription = "날짜 선택", tint = Color(0xFFE91E63)) } })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotEmpty() && editBreed.isNotEmpty() && editBirthDate.isNotEmpty()) {
                        viewModel.updatePuppy(editName, editBreed, editBirthDate)
                        showProfileEditDialog = false
                        scope.launch { snackbarHostState.showSnackbar("프로필이 수정되었습니다") }
                    }
                }, enabled = editName.isNotEmpty() && editBreed.isNotEmpty() && editBirthDate.isNotEmpty()) { Text("저장") }
            },
            dismissButton = { TextButton(onClick = { showProfileEditDialog = false }) { Text("취소") } }
        )
    }
    
    if (showBirthDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showBirthDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    birthDatePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        editBirthDate = sdf.format(Date(millis))
                    }
                    showBirthDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showBirthDatePicker = false }) { Text("취소") } }
        ) {
            DatePicker(state = birthDatePickerState, title = { Text(text = "생년월일 선택", modifier = Modifier.padding(start = 24.dp, top = 16.dp)) })
        }
    }

    if (showWeightDialog) {
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text("몸무게 기록") },
            text = { OutlinedTextField(value = weightInput, onValueChange = { weightInput = it }, label = { Text("몸무게 (kg)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)) },
            confirmButton = {
                TextButton(onClick = {
                    if (weightInput.isNotEmpty()) {
                        viewModel.addWeightRecord(weightInput.toFloatOrNull() ?: 0f)
                        weightInput = ""
                        showWeightDialog = false
                        scope.launch { snackbarHostState.showSnackbar("몸무게가 저장되었습니다") }
                    }
                }) { Text("저장") }
            },
            dismissButton = { TextButton(onClick = { showWeightDialog = false }) { Text("취소") } }
        )
    }

    if (showVaccineDialog) {
        AlertDialog(
            onDismissRequest = { showVaccineDialog = false },
            title = { Text("예방접종 기록") },
            text = {
                Column {
                    OutlinedTextField(value = vaccineInput, onValueChange = { vaccineInput = it }, label = { Text("백신명") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = nextDateInput, onValueChange = { }, label = { Text("다음 접종일") }, singleLine = true, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }, trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, contentDescription = "날짜 선택", tint = Color(0xFF4CAF50)) } }, placeholder = { Text("날짜를 선택하세요") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (vaccineInput.isNotEmpty() && nextDateInput.isNotEmpty()) {
                        viewModel.addVaccination(vaccineInput, nextDateInput)
                        vaccineInput = ""; nextDateInput = ""
                        showVaccineDialog = false
                        scope.launch { snackbarHostState.showSnackbar("예방접종이 저장되었습니다") }
                    }
                }, enabled = vaccineInput.isNotEmpty() && nextDateInput.isNotEmpty()) { Text("저장") }
            },
            dismissButton = { TextButton(onClick = { vaccineInput = ""; nextDateInput = ""; showVaccineDialog = false }) { Text("취소") } }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { millis -> val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); nextDateInput = sdf.format(Date(millis)) }; showDatePicker = false }) { Text("확인") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) { DatePicker(state = datePickerState, title = { Text(text = "다음 접종일 선택", modifier = Modifier.padding(start = 24.dp, top = 16.dp)) }) }
    }

    if (showDiaryDialog) {
        AlertDialog(
            onDismissRequest = { showDiaryDialog = false; newDiaryPhotoPath = null },
            title = { Text("일기 작성") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = titleInput, onValueChange = { titleInput = it }, label = { Text("제목") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = contentInput, onValueChange = { contentInput = it }, label = { Text("내용") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "📷 사진 (선택)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (newDiaryPhotoPath != null) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(File(newDiaryPhotoPath!!)).crossfade(true).build(),
                                contentDescription = "일기 사진",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { newDiaryPhotoPath = null },
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                            ) { Icon(Icons.Default.Close, contentDescription = "사진 삭제", tint = Color.White, modifier = Modifier.size(18.dp)) }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { newDiaryImagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("사진 변경") }
                    } else {
                        OutlinedButton(onClick = { newDiaryImagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("사진 추가") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (titleInput.isNotEmpty() && contentInput.isNotEmpty()) {
                        viewModel.addDiaryEntry(titleInput, contentInput, newDiaryPhotoPath)
                        titleInput = ""; contentInput = ""; newDiaryPhotoPath = null
                        showDiaryDialog = false
                        scope.launch { snackbarHostState.showSnackbar("일기가 저장되었습니다") }
                    }
                }) { Text("저장") }
            },
            dismissButton = { TextButton(onClick = { showDiaryDialog = false; newDiaryPhotoPath = null }) { Text("취소") } }
        )
    }
    
    // 몸무게 수정/삭제 다이얼로그
    if (showEditWeightDialog && selectedWeightRecord != null) {
        AlertDialog(
            onDismissRequest = { showEditWeightDialog = false },
            title = { Text("⚖️ 몸무게 수정") },
            text = {
                Column {
                    Text(text = "날짜: ${selectedWeightRecord!!.date}", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(value = editWeightInput, onValueChange = { editWeightInput = it }, label = { Text("몸무게 (kg)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editWeightInput.toFloatOrNull()?.let { weight ->
                        viewModel.updateWeightRecord(selectedWeightRecord!!.id, weight)
                        showEditWeightDialog = false
                        scope.launch { snackbarHostState.showSnackbar("몸무게가 수정되었습니다") }
                    }
                }, enabled = editWeightInput.toFloatOrNull() != null) { Text("저장") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.deleteWeightRecord(selectedWeightRecord!!.id); showEditWeightDialog = false; scope.launch { snackbarHostState.showSnackbar("몸무게 기록이 삭제되었습니다") } }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("삭제") }
                    TextButton(onClick = { showEditWeightDialog = false }) { Text("취소") }
                }
            }
        )
    }
    
    // 접종 수정/삭제 다이얼로그
    if (showEditVaccinationDialog && selectedVaccination != null) {
        AlertDialog(
            onDismissRequest = { showEditVaccinationDialog = false },
            title = { Text("💉 예방접종 수정") },
            text = {
                Column {
                    Text(text = "접종일: ${selectedVaccination!!.date}", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(value = editVaccineInput, onValueChange = { editVaccineInput = it }, label = { Text("백신명") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editNextDateInput, onValueChange = { editNextDateInput = it }, label = { Text("다음 접종일 (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = editCompletedInput, onCheckedChange = { editCompletedInput = it }); Text("접종 완료") }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updateVaccination(selectedVaccination!!.id, editVaccineInput, editNextDateInput, editCompletedInput); showEditVaccinationDialog = false; scope.launch { snackbarHostState.showSnackbar("접종 정보가 수정되었습니다") } }, enabled = editVaccineInput.isNotEmpty() && editNextDateInput.isNotEmpty()) { Text("저장") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.deleteVaccination(selectedVaccination!!.id); showEditVaccinationDialog = false; scope.launch { snackbarHostState.showSnackbar("접종 정보가 삭제되었습니다") } }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("삭제") }
                    TextButton(onClick = { showEditVaccinationDialog = false }) { Text("취소") }
                }
            }
        )
    }
    
    // 일기 수정/삭제 다이얼로그
    if (showEditDiaryDialog && selectedDiaryEntry != null) {
        AlertDialog(
            onDismissRequest = { showEditDiaryDialog = false },
            title = { Text("📝 일기 수정") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "작성일: ${selectedDiaryEntry!!.date}", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(value = editTitleInput, onValueChange = { editTitleInput = it }, label = { Text("제목") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editContentInput, onValueChange = { editContentInput = it }, label = { Text("내용") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "📷 사진", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (editDiaryPhotoPath != null) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(File(editDiaryPhotoPath!!)).crossfade(true).build(),
                                contentDescription = "일기 사진",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { editDiaryPhotoPath = null },
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                            ) { Icon(Icons.Default.Close, contentDescription = "사진 삭제", tint = Color.White, modifier = Modifier.size(18.dp)) }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { diaryImagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("사진 변경") }
                    } else {
                        OutlinedButton(onClick = { diaryImagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("사진 추가") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editTitleInput.isNotEmpty() && editContentInput.isNotEmpty()) {
                        viewModel.updateDiaryEntry(selectedDiaryEntry!!.id, editTitleInput, editContentInput, editDiaryPhotoPath)
                        showEditDiaryDialog = false
                        scope.launch { snackbarHostState.showSnackbar("일기가 수정되었습니다") }
                    }
                }, enabled = editTitleInput.isNotEmpty() && editContentInput.isNotEmpty()) { Text("저장") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.deleteDiaryEntry(selectedDiaryEntry!!.id); showEditDiaryDialog = false; scope.launch { snackbarHostState.showSnackbar("일기가 삭제되었습니다") } }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("삭제") }
                    TextButton(onClick = { showEditDiaryDialog = false }) { Text("취소") }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCard(icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, title: String, subtitle: String, date: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), ambientColor = iconColor.copy(alpha = 0.1f), spotColor = iconColor.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(iconColor.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                Text(text = date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), ambientColor = color.copy(alpha = 0.15f), spotColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 11.sp)
        }
    }
}
