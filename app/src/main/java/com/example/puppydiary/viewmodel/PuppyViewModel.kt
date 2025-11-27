package com.example.puppydiary.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.puppydiary.data.local.PuppyDatabase
import com.example.puppydiary.data.local.entity.*
import com.example.puppydiary.data.model.*
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

    // 강아지 데이터 (Flow)
    val puppyData: StateFlow<PuppyData?> = puppyDao.getPuppy()
        .map { entity ->
            entity?.let {
                PuppyData(
                    name = it.name,
                    breed = it.breed,
                    birthDate = it.birthDate,
                    profileImage = it.profileImage
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 체중 기록
    val weightRecords: StateFlow<List<WeightRecord>> = weightRecordDao.getAllRecords()
        .map { entities ->
            entities.map { WeightRecord(it.date, it.weight) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 예방접종
    val vaccinations: StateFlow<List<Vaccination>> = vaccinationDao.getAllVaccinations()
        .map { entities ->
            entities.map { Vaccination(it.date, it.vaccine, it.nextDate, it.completed) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 일기
    val diaryEntries: StateFlow<List<DiaryEntry>> = diaryEntryDao.getAllEntries()
        .map { entities ->
            entities.map { DiaryEntry(it.id, it.date, it.title, it.content, it.photo) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 사진첩
    val photoMemories: StateFlow<List<PhotoMemory>> = photoMemoryDao.getAllPhotoMemories()
        .map { entities ->
            entities.map { PhotoMemory(it.id, it.photo, it.date, it.weight, it.description, it.diaryEntryId) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 최근 활동 통합
    val recentActivities: StateFlow<List<Any>> = combine(
        diaryEntryDao.getAllEntries(),
        weightRecordDao.getAllRecords(),
        vaccinationDao.getAllVaccinations(),
        photoMemoryDao.getAllPhotoMemories()
    ) { diaries, weights, vaccines, photos ->
        Log.d("PuppyDiary", "Combining: diaries=${diaries.size}, weights=${weights.size}, vaccines=${vaccines.size}, photos=${photos.size}")

        // Pair: (데이터, createdAt) - createdAt으로 정렬
        val activities = mutableListOf<Pair<Any, Long>>()

        diaries.forEach { entry ->
            activities.add(Pair(DiaryEntry(entry.id, entry.date, entry.title, entry.content, entry.photo), entry.createdAt))
        }

        weights.forEach { record ->
            activities.add(Pair(WeightRecord(record.date, record.weight), record.createdAt))
        }

        vaccines.forEach { vaccine ->
            activities.add(Pair(Vaccination(vaccine.date, vaccine.vaccine, vaccine.nextDate, vaccine.completed), vaccine.createdAt))
        }

        photos.forEach { photo ->
            activities.add(Pair(PhotoMemory(photo.id, photo.photo, photo.date, photo.weight, photo.description, photo.diaryEntryId), photo.createdAt))
        }

        // createdAt 내림차순 (최신이 위로)
        activities.sortedByDescending { it.second }.take(5).map { it.first }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    var selectedDateRange = mutableStateOf(DateRange.MONTH)
        private set

    // 강아지 등록
    fun registerPuppy(name: String, breed: String, birthDate: String, profileImageUri: String?) {
        viewModelScope.launch {
            val puppy = PuppyEntity(
                id = 1,
                name = name,
                breed = breed,
                birthDate = birthDate,
                profileImage = profileImageUri
            )
            puppyDao.insertOrUpdate(puppy)
        }
    }

    // 강아지 프로필 이미지 업데이트
    fun updateProfileImage(imageUri: String) {
        viewModelScope.launch {
            val current = puppyDao.getPuppyOnce()
            current?.let {
                puppyDao.insertOrUpdate(it.copy(profileImage = imageUri))
            }
        }
    }

    // 강아지 프로필 전체 업데이트
    fun updatePuppy(name: String, breed: String, birthDate: String) {
        viewModelScope.launch {
            val current = puppyDao.getPuppyOnce()
            current?.let {
                puppyDao.insertOrUpdate(
                    it.copy(
                        name = name,
                        breed = breed,
                        birthDate = birthDate
                    )
                )
            }
        }
    }

    // 체중 기록 추가
    fun addWeightRecord(weight: Float) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            weightRecordDao.insert(WeightRecordEntity(date = today, weight = weight))
        }
    }

    // 예방접종 추가
    fun addVaccination(vaccine: String, nextDate: String) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val entity = VaccinationEntity(
                date = today,
                vaccine = vaccine,
                nextDate = nextDate,
                completed = false  // 아직 완료되지 않은 예정된 접종
            )
            val insertedId = vaccinationDao.insert(entity)
            
            // 3일 전 알람 예약
            AlarmScheduler.scheduleVaccinationAlarm(
                context = context,
                vaccineName = vaccine,
                nextDate = nextDate,
                notificationId = insertedId.toInt()
            )
        }
    }

    // 일기 추가
    fun addDiaryEntry(title: String, content: String, photo: String? = null) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            diaryEntryDao.insert(
                DiaryEntryEntity(
                    date = today,
                    title = title,
                    content = content,
                    photo = photo
                )
            )
        }
    }

    // 일기 삭제
    fun deleteDiaryEntry(id: Long) {
        viewModelScope.launch {
            diaryEntryDao.deleteById(id)
        }
    }

    // 사진 추가
    fun addPhoto(photoPath: String, description: String = "") {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            Log.d("PuppyDiary", "Adding photo: $photoPath, date: $today")
            photoMemoryDao.insert(
                PhotoMemoryEntity(
                    photo = photoPath,
                    date = today,
                    description = description
                )
            )
            Log.d("PuppyDiary", "Photo inserted successfully")
        }
    }

    // 사진 삭제
    fun deletePhoto(photoMemory: PhotoMemory) {
        viewModelScope.launch {
            photoMemoryDao.delete(
                PhotoMemoryEntity(
                    id = photoMemory.id,
                    photo = photoMemory.photo,
                    date = photoMemory.date,
                    weight = photoMemory.weight,
                    description = photoMemory.description,
                    diaryEntryId = photoMemory.diaryEntryId
                )
            )
        }
    }

    fun setDateRange(range: DateRange) {
        selectedDateRange.value = range
    }

    // 통계 계산
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
                daysUntilBirthday == 0 -> "오늘 생일! 🎉"
                daysUntilBirthday <= 30 -> "D-$daysUntilBirthday"
                else -> null // 30일 이상 남으면 표시 안함
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
}
