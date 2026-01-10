package com.example.puppydiary.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.puppydiary.data.local.PuppyDatabase
import com.example.puppydiary.data.local.entity.*
import com.example.puppydiary.data.model.*
import com.example.puppydiary.utils.AchievementManager
import com.example.puppydiary.utils.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PuppyViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = PuppyDatabase.getDatabase(application)
    private val puppyDao = database.puppyDao()
    private val weightRecordDao = database.weightRecordDao()
    private val vaccinationDao = database.vaccinationDao()
    private val diaryEntryDao = database.diaryEntryDao()
    private val achievementDao = database.achievementDao()
    private val photoMemoryDao = database.photoMemoryDao()
    private val walkRecordDao = database.walkRecordDao()
    private val mealRecordDao = database.mealRecordDao()
    private val hospitalVisitDao = database.hospitalVisitDao()
    private val medicationRecordDao = database.medicationRecordDao()
    private val emergencyContactDao = database.emergencyContactDao()
    private val allergyDao = database.allergyDao()

    init {
        // 앱 시작 시 기본 업적 초기화
        viewModelScope.launch {
            initializeAchievements()
        }
    }

    private suspend fun initializeAchievements() {
        val existing = achievementDao.getAllAchievements().first()
        if (existing.isEmpty()) {
            achievementDao.insertAll(AchievementManager.getDefaultAchievements())
        }
    }

    // 현재 선택된 강아지
    val puppyData: StateFlow<PuppyData?> = puppyDao.getSelectedPuppy()
        .map { entity ->
            entity?.let {
                PuppyData(
                    id = it.id,
                    name = it.name,
                    breed = it.breed,
                    birthDate = it.birthDate,
                    profileImage = it.profileImage,
                    isSelected = it.isSelected
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 모든 강아지 목록
    val allPuppies: StateFlow<List<PuppyData>> = puppyDao.getAllPuppies()
        .map { entities ->
            entities.map {
                PuppyData(
                    id = it.id,
                    name = it.name,
                    breed = it.breed,
                    birthDate = it.birthDate,
                    profileImage = it.profileImage,
                    isSelected = it.isSelected
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 선택된 강아지의 체중 기록
    val weightRecords: StateFlow<List<WeightRecord>> = puppyData
        .flatMapLatest { puppy ->
            if (puppy != null) {
                weightRecordDao.getRecordsByPuppy(puppy.id)
            } else {
                flowOf(emptyList())
            }
        }
        .map { entities ->
            entities.map { WeightRecord(it.id, it.date, it.weight) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 선택된 강아지의 예방접종
    val vaccinations: StateFlow<List<Vaccination>> = puppyData
        .flatMapLatest { puppy ->
            if (puppy != null) {
                vaccinationDao.getVaccinationsByPuppy(puppy.id)
            } else {
                flowOf(emptyList())
            }
        }
        .map { entities ->
            entities.map { Vaccination(it.id, it.date, it.vaccine, it.nextDate, it.completed) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 선택된 강아지의 일기
    val diaryEntries: StateFlow<List<DiaryEntry>> = puppyData
        .flatMapLatest { puppy ->
            if (puppy != null) {
                diaryEntryDao.getEntriesByPuppy(puppy.id)
            } else {
                flowOf(emptyList())
            }
        }
        .map { entities ->
            entities.map { DiaryEntry(it.id, it.date, it.title, it.content, it.photo) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 선택된 강아지의 사진첩
    val photoMemories: StateFlow<List<PhotoMemory>> = puppyData
        .flatMapLatest { puppy ->
            if (puppy != null) {
                photoMemoryDao.getPhotosByPuppy(puppy.id)
            } else {
                flowOf(emptyList())
            }
        }
        .map { entities ->
            entities.map { PhotoMemory(it.id, it.photo, it.date, it.weight, it.description, it.diaryEntryId) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 선택된 강아지의 산책 기록
    val walkRecords: StateFlow<List<WalkRecord>> = puppyData
        .flatMapLatest { puppy ->
            if (puppy != null) {
                walkRecordDao.getRecordsByPuppy(puppy.id)
            } else {
                flowOf(emptyList())
            }
        }
        .map { entities ->
            entities.map { WalkRecord(it.id, it.date, it.startTime, it.endTime, it.durationMinutes, it.distanceMeters, it.note) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 선택된 강아지의 식사 기록
    val mealRecords: StateFlow<List<MealRecord>> = puppyData
        .flatMapLatest { puppy ->
            if (puppy != null) {
                mealRecordDao.getRecordsByPuppy(puppy.id)
            } else {
                flowOf(emptyList())
            }
        }
        .map { entities ->
            entities.map { MealRecord(it.id, it.date, it.time, it.foodType, it.foodName, it.amountGrams, it.note) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 선택된 강아지의 병원 방문 기록
    val hospitalVisits: StateFlow<List<HospitalVisit>> = puppyData
        .flatMapLatest { puppy ->
            if (puppy != null) {
                hospitalVisitDao.getVisitsByPuppy(puppy.id)
            } else {
                flowOf(emptyList())
            }
        }
        .map { entities ->
            entities.map { HospitalVisit(it.id, it.date, it.hospitalName, it.visitReason, it.diagnosis, it.treatment, it.prescription, it.cost, it.nextVisitDate, it.note) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 선택된 강아지의 투약 기록
    val medicationRecords: StateFlow<List<MedicationRecord>> = puppyData
        .flatMapLatest { puppy ->
            if (puppy != null) {
                medicationRecordDao.getRecordsByPuppy(puppy.id)
            } else {
                flowOf(emptyList())
            }
        }
        .map { entities ->
            entities.map { MedicationRecord(it.id, it.date, it.medicationType, it.medicationName, it.nextDate, it.intervalDays, it.note) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 선택된 강아지의 긴급 연락처
    val emergencyContacts: StateFlow<List<EmergencyContact>> = puppyData
        .flatMapLatest { puppy ->
            if (puppy != null) {
                emergencyContactDao.getContactsByPuppy(puppy.id)
            } else {
                flowOf(emptyList())
            }
        }
        .map { entities ->
            entities.map { EmergencyContact(it.id, it.contactType, it.name, it.phoneNumber, it.address, it.note) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 선택된 강아지의 알러지
    val allergies: StateFlow<List<Allergy>> = puppyData
        .flatMapLatest { puppy ->
            if (puppy != null) {
                allergyDao.getAllergiesByPuppyId(puppy.id)
            } else {
                flowOf(emptyList())
            }
        }
        .map { entities ->
            entities.map { Allergy(it.id, it.puppyId, it.allergyName, it.severity, it.symptoms, it.diagnosedDate, it.notes) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 업적 목록
    val achievements: StateFlow<List<Achievement>> = achievementDao.getAllAchievements()
        .map { entities ->
            entities.map {
                Achievement(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    icon = it.icon,
                    isUnlocked = it.isUnlocked,
                    unlockedDate = it.unlockedDate,
                    progress = it.progress,
                    category = try {
                        AchievementCategory.valueOf(it.category)
                    } catch (e: Exception) {
                        AchievementCategory.GENERAL
                    }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 달성한 업적 수
    val unlockedAchievementCount: StateFlow<Int> = achievements
        .map { list -> list.count { it.isUnlocked } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 최근 활동 통합 (선택된 강아지만)
    val recentActivities: StateFlow<List<Any>> = puppyData
        .flatMapLatest { puppy ->
            if (puppy != null) {
                combine(
                    diaryEntryDao.getEntriesByPuppy(puppy.id),
                    weightRecordDao.getRecordsByPuppy(puppy.id),
                    vaccinationDao.getVaccinationsByPuppy(puppy.id),
                    photoMemoryDao.getPhotosByPuppy(puppy.id)
                ) { diaries, weights, vaccines, photos ->
                    val activities = mutableListOf<Pair<Any, Long>>()

                    diaries.forEach { entry ->
                        activities.add(Pair(DiaryEntry(entry.id, entry.date, entry.title, entry.content, entry.photo), entry.createdAt))
                    }

                    weights.forEach { record ->
                        activities.add(Pair(WeightRecord(record.id, record.date, record.weight), record.createdAt))
                    }

                    vaccines.forEach { vaccine ->
                        activities.add(Pair(Vaccination(vaccine.id, vaccine.date, vaccine.vaccine, vaccine.nextDate, vaccine.completed), vaccine.createdAt))
                    }

                    photos.forEach { photo ->
                        activities.add(Pair(PhotoMemory(photo.id, photo.photo, photo.date, photo.weight, photo.description, photo.diaryEntryId), photo.createdAt))
                    }

                    activities.sortedByDescending { it.second }.take(10).map { it.first }
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    var selectedDateRange = mutableStateOf(DateRange.MONTH)
        private set

    // 검색 쿼리
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 검색 쿼리 설정
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // LIKE 검색 결과
    val searchResults: StateFlow<List<Any>> = combine(
        puppyData,
        _searchQuery
    ) { puppy, query ->
        Pair(puppy, query)
    }.flatMapLatest { (puppy, query) ->
        if (puppy == null || query.isBlank()) {
            flowOf(emptyList())
        } else {
            combine(
                diaryEntryDao.searchByPuppy(puppy.id, query),
                vaccinationDao.searchByPuppy(puppy.id, query),
                photoMemoryDao.searchByPuppy(puppy.id, query),
                weightRecordDao.searchByPuppy(puppy.id, query)
            ) { diaries, vaccines, photos, weights ->
                val results = mutableListOf<Pair<Any, Long>>()

                diaries.forEach { entry ->
                    results.add(Pair(DiaryEntry(entry.id, entry.date, entry.title, entry.content, entry.photo), entry.createdAt))
                }

                vaccines.forEach { vaccine ->
                    results.add(Pair(Vaccination(vaccine.id, vaccine.date, vaccine.vaccine, vaccine.nextDate, vaccine.completed), vaccine.createdAt))
                }

                photos.forEach { photo ->
                    results.add(Pair(PhotoMemory(photo.id, photo.photo, photo.date, photo.weight, photo.description, photo.diaryEntryId), photo.createdAt))
                }

                weights.forEach { record ->
                    results.add(Pair(WeightRecord(record.id, record.date, record.weight), record.createdAt))
                }

                results.sortedByDescending { it.second }.map { it.first }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 강아지 등록 (첫 번째 또는 추가)
    fun registerPuppy(name: String, breed: String, birthDate: String, profileImageUri: String?) {
        viewModelScope.launch {
            // 먼저 모든 강아지 선택 해제
            puppyDao.deselectAll()

            // 새 강아지 추가 (isSelected = true)
            val puppy = PuppyEntity(
                name = name,
                breed = breed,
                birthDate = birthDate,
                profileImage = profileImageUri,
                isSelected = true
            )
            puppyDao.insert(puppy)
        }
    }

    // 강아지 선택 (전환)
    fun selectPuppy(puppyId: Long) {
        viewModelScope.launch {
            puppyDao.deselectAll()
            puppyDao.selectPuppy(puppyId)
        }
    }

    // 강아지 삭제
    fun deletePuppy(puppyId: Long) {
        viewModelScope.launch {
            // 관련 데이터도 삭제
            weightRecordDao.deleteByPuppy(puppyId)
            vaccinationDao.deleteByPuppy(puppyId)
            diaryEntryDao.deleteByPuppy(puppyId)
            photoMemoryDao.deleteByPuppy(puppyId)
            walkRecordDao.deleteByPuppy(puppyId)
            mealRecordDao.deleteByPuppy(puppyId)
            hospitalVisitDao.deleteByPuppy(puppyId)
            medicationRecordDao.deleteByPuppy(puppyId)
            emergencyContactDao.deleteByPuppy(puppyId)
            puppyDao.deleteById(puppyId)

            // 남은 강아지 중 첫 번째를 선택
            val remaining = puppyDao.getAllPuppiesOnce()
            if (remaining.isNotEmpty()) {
                puppyDao.selectPuppy(remaining.first().id)
            }
        }
    }

    // 강아지 프로필 이미지 업데이트
    fun updateProfileImage(imageUri: String) {
        viewModelScope.launch {
            val current = puppyDao.getSelectedPuppyOnce()
            current?.let {
                puppyDao.update(it.copy(profileImage = imageUri))
            }
        }
    }

    // 강아지 프로필 전체 업데이트
    fun updatePuppy(name: String, breed: String, birthDate: String) {
        viewModelScope.launch {
            val current = puppyDao.getSelectedPuppyOnce()
            current?.let {
                puppyDao.update(
                    it.copy(
                        name = name,
                        breed = breed,
                        birthDate = birthDate
                    )
                )
            }
        }
    }

    // 새 반려동물 추가
    fun addNewPuppy(name: String, breed: String, birthDate: String) {
        viewModelScope.launch {
            // 기존 강아지들의 선택 해제
            val allPuppies = puppyDao.getAllPuppies().first()
            allPuppies.forEach { puppy ->
                if (puppy.isSelected) {
                    puppyDao.update(puppy.copy(isSelected = false))
                }
            }

            // 새 강아지 추가 (선택됨 상태로)
            val newPuppyId = puppyDao.insert(
                PuppyEntity(
                    name = name,
                    breed = breed,
                    birthDate = birthDate,
                    profileImage = null,
                    isSelected = true
                )
            )

            Log.d("PuppyDiary", "New puppy added: $name (id: $newPuppyId)")
        }
    }

    // 체중 기록 추가
    fun addWeightRecord(weight: Float) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            weightRecordDao.insert(WeightRecordEntity(puppyId = puppyId, date = today, weight = weight))
            checkAndUpdateAchievements()
        }
    }

    // 예방접종 추가
    fun addVaccination(vaccine: String, nextDate: String) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val entity = VaccinationEntity(
                puppyId = puppyId,
                date = today,
                vaccine = vaccine,
                nextDate = nextDate,
                completed = false
            )
            val insertedId = vaccinationDao.insert(entity)

            // 3일 전 알람 예약
            AlarmScheduler.scheduleVaccinationAlarm(
                context = context,
                vaccineName = vaccine,
                nextDate = nextDate,
                notificationId = insertedId.toInt()
            )
            checkAndUpdateAchievements()
        }
    }

    // 일기 추가
    fun addDiaryEntry(title: String, content: String, photo: String? = null) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            diaryEntryDao.insert(
                DiaryEntryEntity(
                    puppyId = puppyId,
                    date = today,
                    title = title,
                    content = content,
                    photo = photo
                )
            )
            checkAndUpdateAchievements()
        }
    }

    // 일기 삭제
    fun deleteDiaryEntry(id: Long) {
        viewModelScope.launch {
            diaryEntryDao.deleteById(id)
        }
    }

    // 일기 수정
    fun updateDiaryEntry(id: Long, title: String, content: String, photo: String? = null) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val existing = diaryEntries.value.find { it.id == id } ?: return@launch
            diaryEntryDao.update(
                DiaryEntryEntity(
                    id = id,
                    puppyId = puppyId,
                    date = existing.date,
                    title = title,
                    content = content,
                    photo = photo
                )
            )
        }
    }

    // 예방접종 삭제
    fun deleteVaccination(id: Long) {
        viewModelScope.launch {
            // 알람 취소
            AlarmScheduler.cancelVaccinationAlarm(context, id.toInt())
            vaccinationDao.deleteById(id)
        }
    }

    // 예방접종 수정
    fun updateVaccination(id: Long, vaccine: String, nextDate: String, completed: Boolean) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val existing = vaccinations.value.find { it.id == id } ?: return@launch

            // 기존 알람 취소
            AlarmScheduler.cancelVaccinationAlarm(context, id.toInt())

            vaccinationDao.update(
                VaccinationEntity(
                    id = id,
                    puppyId = puppyId,
                    date = existing.date,
                    vaccine = vaccine,
                    nextDate = nextDate,
                    completed = completed
                )
            )

            // 완료되지 않은 경우에만 새 알람 예약
            if (!completed && nextDate.isNotBlank()) {
                AlarmScheduler.scheduleVaccinationAlarm(
                    context = context,
                    vaccineName = vaccine,
                    nextDate = nextDate,
                    notificationId = id.toInt()
                )
            }
            checkAndUpdateAchievements()
        }
    }

    // 몸무게 삭제
    fun deleteWeightRecord(id: Long) {
        viewModelScope.launch {
            weightRecordDao.deleteById(id)
        }
    }

    // 몸무게 수정
    fun updateWeightRecord(id: Long, weight: Float) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val existing = weightRecords.value.find { it.id == id } ?: return@launch
            weightRecordDao.update(
                WeightRecordEntity(
                    id = id,
                    puppyId = puppyId,
                    date = existing.date,
                    weight = weight
                )
            )
        }
    }

    // 사진 추가
    fun addPhoto(photoPath: String, description: String = "") {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            Log.d("PuppyDiary", "Adding photo: $photoPath, date: $today, puppyId: $puppyId")
            photoMemoryDao.insert(
                PhotoMemoryEntity(
                    puppyId = puppyId,
                    photo = photoPath,
                    date = today,
                    description = description
                )
            )
            Log.d("PuppyDiary", "Photo inserted successfully")
            checkAndUpdateAchievements()
        }
    }

    // 사진 삭제
    fun deletePhoto(photoMemory: PhotoMemory) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            photoMemoryDao.delete(
                PhotoMemoryEntity(
                    id = photoMemory.id,
                    puppyId = puppyId,
                    photo = photoMemory.photo,
                    date = photoMemory.date,
                    weight = photoMemory.weight,
                    description = photoMemory.description,
                    diaryEntryId = photoMemory.diaryEntryId
                )
            )
        }
    }

    // ==================== 산책 기록 ====================
    fun addWalkRecord(startTime: String, endTime: String, durationMinutes: Int, distanceMeters: Float? = null, note: String? = null) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            walkRecordDao.insert(
                WalkRecordEntity(
                    puppyId = puppyId,
                    date = today,
                    startTime = startTime,
                    endTime = endTime,
                    durationMinutes = durationMinutes,
                    distanceMeters = distanceMeters,
                    note = note
                )
            )
            checkAndUpdateAchievements()
        }
    }

    fun deleteWalkRecord(id: Long) {
        viewModelScope.launch {
            walkRecordDao.deleteById(id)
        }
    }

    fun updateWalkRecord(id: Long, startTime: String, endTime: String, durationMinutes: Int, distanceMeters: Float?, note: String?) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val existing = walkRecords.value.find { it.id == id } ?: return@launch
            walkRecordDao.update(
                WalkRecordEntity(
                    id = id,
                    puppyId = puppyId,
                    date = existing.date,
                    startTime = startTime,
                    endTime = endTime,
                    durationMinutes = durationMinutes,
                    distanceMeters = distanceMeters,
                    note = note
                )
            )
        }
    }

    // ==================== 식사 기록 ====================
    fun addMealRecord(time: String, foodType: String, foodName: String, amountGrams: Float, note: String? = null) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            mealRecordDao.insert(
                MealRecordEntity(
                    puppyId = puppyId,
                    date = today,
                    time = time,
                    foodType = foodType,
                    foodName = foodName,
                    amountGrams = amountGrams,
                    note = note
                )
            )
            checkAndUpdateAchievements()
        }
    }

    fun deleteMealRecord(id: Long) {
        viewModelScope.launch {
            mealRecordDao.deleteById(id)
        }
    }

    fun updateMealRecord(id: Long, time: String, foodType: String, foodName: String, amountGrams: Float, note: String?) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val existing = mealRecords.value.find { it.id == id } ?: return@launch
            mealRecordDao.update(
                MealRecordEntity(
                    id = id,
                    puppyId = puppyId,
                    date = existing.date,
                    time = time,
                    foodType = foodType,
                    foodName = foodName,
                    amountGrams = amountGrams,
                    note = note
                )
            )
        }
    }

    // ==================== 병원 방문 기록 ====================
    fun addHospitalVisit(
        hospitalName: String,
        visitReason: String,
        diagnosis: String? = null,
        treatment: String? = null,
        prescription: String? = null,
        cost: Int? = null,
        nextVisitDate: String? = null,
        note: String? = null
    ) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            hospitalVisitDao.insert(
                HospitalVisitEntity(
                    puppyId = puppyId,
                    date = today,
                    hospitalName = hospitalName,
                    visitReason = visitReason,
                    diagnosis = diagnosis,
                    treatment = treatment,
                    prescription = prescription,
                    cost = cost,
                    nextVisitDate = nextVisitDate,
                    note = note
                )
            )
            checkAndUpdateAchievements()
        }
    }

    fun deleteHospitalVisit(id: Long) {
        viewModelScope.launch {
            hospitalVisitDao.deleteById(id)
        }
    }

    fun updateHospitalVisit(
        id: Long,
        hospitalName: String,
        visitReason: String,
        diagnosis: String?,
        treatment: String?,
        prescription: String?,
        cost: Int?,
        nextVisitDate: String?,
        note: String?
    ) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val existing = hospitalVisits.value.find { it.id == id } ?: return@launch
            hospitalVisitDao.update(
                HospitalVisitEntity(
                    id = id,
                    puppyId = puppyId,
                    date = existing.date,
                    hospitalName = hospitalName,
                    visitReason = visitReason,
                    diagnosis = diagnosis,
                    treatment = treatment,
                    prescription = prescription,
                    cost = cost,
                    nextVisitDate = nextVisitDate,
                    note = note
                )
            )
        }
    }

    // ==================== 투약 기록 ====================
    fun addMedicationRecord(
        medicationType: String,
        medicationName: String,
        nextDate: String? = null,
        intervalDays: Int? = null,
        note: String? = null
    ) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val insertedId = medicationRecordDao.insert(
                MedicationRecordEntity(
                    puppyId = puppyId,
                    date = today,
                    medicationType = medicationType,
                    medicationName = medicationName,
                    nextDate = nextDate,
                    intervalDays = intervalDays,
                    note = note
                )
            )

            // 다음 투약일 알림 예약
            if (nextDate != null) {
                AlarmScheduler.scheduleMedicationAlarm(
                    context = context,
                    medicationName = medicationName,
                    nextDate = nextDate,
                    notificationId = (insertedId + 10000).toInt()
                )
            }
        }
    }

    fun deleteMedicationRecord(id: Long) {
        viewModelScope.launch {
            AlarmScheduler.cancelMedicationAlarm(context, (id + 10000).toInt())
            medicationRecordDao.deleteById(id)
        }
    }

    fun updateMedicationRecord(
        id: Long,
        medicationType: String,
        medicationName: String,
        nextDate: String?,
        intervalDays: Int?,
        note: String?
    ) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            val existing = medicationRecords.value.find { it.id == id } ?: return@launch

            AlarmScheduler.cancelMedicationAlarm(context, (id + 10000).toInt())

            medicationRecordDao.update(
                MedicationRecordEntity(
                    id = id,
                    puppyId = puppyId,
                    date = existing.date,
                    medicationType = medicationType,
                    medicationName = medicationName,
                    nextDate = nextDate,
                    intervalDays = intervalDays,
                    note = note
                )
            )

            if (nextDate != null) {
                AlarmScheduler.scheduleMedicationAlarm(
                    context = context,
                    medicationName = medicationName,
                    nextDate = nextDate,
                    notificationId = (id + 10000).toInt()
                )
            }
        }
    }

    // ==================== 긴급 연락처 ====================
    fun addEmergencyContact(
        contactType: String,
        name: String,
        phoneNumber: String,
        address: String? = null,
        note: String? = null
    ) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            emergencyContactDao.insert(
                EmergencyContactEntity(
                    puppyId = puppyId,
                    contactType = contactType,
                    name = name,
                    phoneNumber = phoneNumber,
                    address = address,
                    note = note
                )
            )
        }
    }

    fun deleteEmergencyContact(id: Long) {
        viewModelScope.launch {
            emergencyContactDao.deleteById(id)
        }
    }

    fun updateEmergencyContact(
        id: Long,
        contactType: String,
        name: String,
        phoneNumber: String,
        address: String?,
        note: String?
    ) {
        viewModelScope.launch {
            val puppyId = puppyData.value?.id ?: return@launch
            emergencyContactDao.update(
                EmergencyContactEntity(
                    id = id,
                    puppyId = puppyId,
                    contactType = contactType,
                    name = name,
                    phoneNumber = phoneNumber,
                    address = address,
                    note = note
                )
            )
        }
    }

    // ==================== 업적 체크 및 업데이트 ====================
    private suspend fun checkAndUpdateAchievements() {
        val puppyId = puppyData.value?.id ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 각 카테고리별 개수 조회
        val diaryCount = diaryEntryDao.getEntriesByPuppyOnce(puppyId).size
        val weightCount = weightRecordDao.getRecordsByPuppyOnce(puppyId).size
        val vaccineList = vaccinationDao.getVaccinationsByPuppyOnce(puppyId)
        val vaccineCount = vaccineList.size
        val completedVaccineCount = vaccineList.count { it.completed }
        val photoCount = photoMemoryDao.getPhotosByPuppyOnce(puppyId).size
        val walkCount = walkRecordDao.getCountByPuppy(puppyId)
        val mealCount = mealRecordDao.getCountByPuppy(puppyId)
        val hospitalCount = hospitalVisitDao.getCountByPuppy(puppyId)

        // 모든 업적 체크 결과 수집
        val allResults = mutableListOf<AchievementManager.AchievementCheckResult>()
        allResults.addAll(AchievementManager.checkDiaryAchievements(diaryCount))
        allResults.addAll(AchievementManager.checkWeightAchievements(weightCount))
        allResults.addAll(AchievementManager.checkVaccinationAchievements(vaccineCount, completedVaccineCount))
        allResults.addAll(AchievementManager.checkPhotoAchievements(photoCount))
        allResults.addAll(AchievementManager.checkWalkAchievements(walkCount))
        allResults.addAll(AchievementManager.checkMealAchievements(mealCount))
        allResults.addAll(AchievementManager.checkHospitalAchievements(hospitalCount))

        // 업적 업데이트
        for (result in allResults) {
            val existing = achievementDao.getAchievementById(result.achievementId) ?: continue
            if (existing.progress != result.progress || existing.isUnlocked != result.isUnlocked) {
                achievementDao.update(
                    existing.copy(
                        progress = result.progress,
                        isUnlocked = result.isUnlocked,
                        unlockedDate = if (result.isUnlocked && existing.unlockedDate == null) today else existing.unlockedDate
                    )
                )
            }
        }
    }

    fun setDateRange(range: DateRange) {
        selectedDateRange.value = range
    }

    // ==================== 통계 계산 ====================
    fun calculateAge(): String {
        val puppy = puppyData.value ?: return "알 수 없음"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDate = sdf.parse(puppy.birthDate)
            val today = Date()

            if (birthDate != null) {
                val diffInMillis = today.time - birthDate.time
                val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
                val years = diffInDays / 365
                val months = (diffInDays % 365) / 30
                val days = diffInDays % 30

                if (years > 0) {
                    "${years}년 ${months}개월"
                } else {
                    "${months}개월 ${days}일"
                }
            } else "알 수 없음"
        } catch (e: Exception) {
            "알 수 없음"
        }
    }

    // 생일 D-day 계산
    fun getBirthdayDday(): String? {
        val puppy = puppyData.value ?: return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDate = sdf.parse(puppy.birthDate) ?: return null

            val today = Calendar.getInstance()
            val birthday = Calendar.getInstance().apply {
                time = birthDate
                set(Calendar.YEAR, today.get(Calendar.YEAR))
            }

            // 올해 생일이 지났으면 내년으로
            if (birthday.before(today)) {
                birthday.add(Calendar.YEAR, 1)
            }

            val diffInMillis = birthday.timeInMillis - today.timeInMillis
            val daysUntilBirthday = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

            when {
                daysUntilBirthday == 0 -> "오늘 생일!"
                daysUntilBirthday <= 30 -> "D-$daysUntilBirthday"
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentWeight(): Float = weightRecords.value.lastOrNull()?.weight ?: 0f

    fun getWeeklyGrowth(): Float {
        val records = weightRecords.value.takeLast(2)
        if (records.size < 2) return 0f
        return records.last().weight - records.first().weight
    }

    fun getAverageWeight(): Float {
        val records = weightRecords.value
        return if (records.isNotEmpty()) {
            records.map { it.weight }.average().toFloat()
        } else 0f
    }

    fun getHealthScore(): Int {
        val weightScore = calculateWeightScore()
        val vaccinationScore = calculateVaccinationScore()
        val activityScore = calculateActivityScore()
        return ((weightScore + vaccinationScore + activityScore) / 3.0).toInt()
    }

    // 오늘 산책 시간 합계
    fun getTodayWalkMinutes(): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return walkRecords.value.filter { it.date == today }.sumOf { it.durationMinutes }
    }

    // 오늘 식사 횟수
    fun getTodayMealCount(): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return mealRecords.value.count { it.date == today }
    }

    // 총 병원 비용
    fun getTotalHospitalCost(): Int {
        return hospitalVisits.value.mapNotNull { it.cost }.sum()
    }

    private fun calculateWeightScore(): Int {
        val records = weightRecords.value
        if (records.size < 2) return 50
        val growth = records.last().weight - records.first().weight
        return when {
            growth > 0.5f -> 100
            growth > 0.2f -> 80
            growth > 0f -> 60
            growth > -0.2f -> 40
            else -> 20
        }
    }

    private fun calculateVaccinationScore(): Int {
        val vacc = vaccinations.value
        val completedCount = vacc.count { it.completed }
        val totalCount = vacc.size
        return if (totalCount > 0) {
            (completedCount.toFloat() / totalCount * 100).toInt()
        } else 50
    }

    private fun calculateActivityScore(): Int {
        val entries = diaryEntries.value
        val daysInRange = selectedDateRange.value.days
        val entriesPerDay = entries.size.toFloat() / daysInRange
        return when {
            entriesPerDay >= 0.5f -> 100
            entriesPerDay >= 0.3f -> 80
            entriesPerDay >= 0.2f -> 60
            entriesPerDay >= 0.1f -> 40
            else -> 20
        }
    }

    // 알러지 관련 함수
    fun addAllergy(allergyName: String, severity: String, symptoms: String, diagnosedDate: String, notes: String = "") {
        viewModelScope.launch {
            puppyData.value?.let { puppy ->
                val entity = AllergyEntity(
                    puppyId = puppy.id,
                    allergyName = allergyName,
                    severity = severity,
                    symptoms = symptoms,
                    diagnosedDate = diagnosedDate,
                    notes = notes
                )
                allergyDao.insert(entity)
            }
        }
    }

    fun updateAllergy(id: Long, allergyName: String, severity: String, symptoms: String, diagnosedDate: String, notes: String) {
        viewModelScope.launch {
            puppyData.value?.let { puppy ->
                val entity = AllergyEntity(
                    id = id,
                    puppyId = puppy.id,
                    allergyName = allergyName,
                    severity = severity,
                    symptoms = symptoms,
                    diagnosedDate = diagnosedDate,
                    notes = notes
                )
                allergyDao.update(entity)
            }
        }
    }

    fun deleteAllergy(id: Long) {
        viewModelScope.launch {
            allergyDao.deleteById(id)
        }
    }
}
