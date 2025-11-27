package com.example.puppydiary.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.puppydiary.data.model.StatsExportData
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object ExportHelper {

    enum class ExportFormat(val displayName: String, val description: String) {
        JSON("JSON", "개발자용 데이터 형식"),
        CSV("CSV", "엑셀에서 열 수 있는 형식"),
        PDF("PDF", "읽기 쉬운 보고서 형식")
    }

    fun exportData(context: Context, data: StatsExportData, format: ExportFormat) {
        try {
            val fileName = generateFileName(data.puppyInfo.name, format)
            val file = File(context.getExternalFilesDir(null), fileName)

            when (format) {
                ExportFormat.JSON -> exportAsJson(file, data)
                ExportFormat.CSV -> exportAsCsv(file, data)
                ExportFormat.PDF -> exportAsPdf(file, data)
            }

            shareFile(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateFileName(puppyName: String, format: ExportFormat): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val extension = when (format) {
            ExportFormat.JSON -> "json"
            ExportFormat.CSV -> "csv"
            ExportFormat.PDF -> "pdf"
        }
        return "${puppyName}_통계_${timestamp}.${extension}"
    }

    private fun exportAsJson(file: File, data: StatsExportData) {
        val jsonObject = JSONObject().apply {
            put("puppyInfo", JSONObject().apply {
                put("name", data.puppyInfo.name)
                put("breed", data.puppyInfo.breed)
                put("birthDate", data.puppyInfo.birthDate)
            })

            put("exportInfo", JSONObject().apply {
                put("exportDate", data.exportDate)
                put("dateRange", data.dateRange.displayName)
            })

            put("weightRecords", JSONArray().apply {
                data.weightRecords.forEach { record ->
                    put(JSONObject().apply {
                        put("date", record.date)
                        put("weight", record.weight)
                    })
                }
            })

            put("vaccinations", JSONArray().apply {
                data.vaccinations.forEach { vaccination ->
                    put(JSONObject().apply {
                        put("date", vaccination.date)
                        put("vaccine", vaccination.vaccine)
                        put("nextDate", vaccination.nextDate)
                        put("completed", vaccination.completed)
                    })
                }
            })

            put("diaryEntries", JSONArray().apply {
                data.diaryEntries.forEach { entry ->
                    put(JSONObject().apply {
                        put("id", entry.id)
                        put("date", entry.date)
                        put("title", entry.title)
                        put("content", entry.content)
                        put("hasPhoto", entry.photo != null)
                    })
                }
            })

            put("achievements", JSONArray().apply {
                data.achievements.forEach { achievement ->
                    put(JSONObject().apply {
                        put("title", achievement.title)
                        put("description", achievement.description)
                        put("unlockedDate", achievement.unlockedDate)
                        put("category", achievement.category.name)
                    })
                }
            })
        }

        FileWriter(file).use { writer ->
            writer.write(jsonObject.toString(2))
        }
    }

    private fun exportAsCsv(file: File, data: StatsExportData) {
        FileWriter(file).use { writer ->
            writer.appendLine("# ${data.puppyInfo.name} 성장 기록")
            writer.appendLine("# 품종: ${data.puppyInfo.breed}")
            writer.appendLine("# 생년월일: ${data.puppyInfo.birthDate}")
            writer.appendLine("# 내보내기 날짜: ${data.exportDate}")
            writer.appendLine("# 기간: ${data.dateRange.displayName}")
            writer.appendLine("")

            writer.appendLine("=== 몸무게 기록 ===")
            writer.appendLine("날짜,몸무게(kg)")
            data.weightRecords.forEach { record ->
                writer.appendLine("${record.date},${record.weight}")
            }
            writer.appendLine("")

            writer.appendLine("=== 예방접종 기록 ===")
            writer.appendLine("날짜,백신명,다음접종일,완료여부")
            data.vaccinations.forEach { vaccination ->
                writer.appendLine("${vaccination.date},${vaccination.vaccine},${vaccination.nextDate},${if(vaccination.completed) "완료" else "예정"}")
            }
            writer.appendLine("")

            writer.appendLine("=== 일기 기록 ===")
            writer.appendLine("날짜,제목,내용,사진첨부")
            data.diaryEntries.forEach { entry ->
                val content = entry.content.replace("\n", " ").replace(",", ".")
                writer.appendLine("${entry.date},${entry.title},${content},${if(entry.photo != null) "있음" else "없음"}")
            }
            writer.appendLine("")

            writer.appendLine("=== 달성한 성취 ===")
            writer.appendLine("제목,설명,달성일,카테고리")
            data.achievements.forEach { achievement ->
                writer.appendLine("${achievement.title},${achievement.description},${achievement.unlockedDate},${achievement.category.name}")
            }
        }
    }

    private fun exportAsPdf(file: File, data: StatsExportData) {
        val pdfFile = File(file.parent, file.nameWithoutExtension + ".txt")

        FileWriter(pdfFile).use { writer ->
            writer.appendLine("🐾 ${data.puppyInfo.name} 성장 보고서")
            writer.appendLine("=".repeat(50))
            writer.appendLine()

            writer.appendLine("📋 기본 정보")
            writer.appendLine("이름: ${data.puppyInfo.name}")
            writer.appendLine("품종: ${data.puppyInfo.breed}")
            writer.appendLine("생년월일: ${data.puppyInfo.birthDate}")
            writer.appendLine("보고서 생성일: ${data.exportDate}")
            writer.appendLine("분석 기간: ${data.dateRange.displayName}")
            writer.appendLine()

            writer.appendLine("📈 성장 통계")
            writer.appendLine("-".repeat(30))
            if (data.weightRecords.isNotEmpty()) {
                val firstWeight = data.weightRecords.first().weight
                val lastWeight = data.weightRecords.last().weight
                val averageWeight = data.weightRecords.map { it.weight }.average()

                writer.appendLine("최초 기록 몸무게: ${firstWeight}kg")
                writer.appendLine("현재 몸무게: ${lastWeight}kg")
                writer.appendLine("평균 몸무게: ${String.format("%.1f", averageWeight)}kg")
                writer.appendLine("총 증가량: ${String.format("%.1f", lastWeight - firstWeight)}kg")
            }
            writer.appendLine()

            writer.appendLine("💉 예방접종 현황")
            writer.appendLine("-".repeat(30))
            val completedVaccinations = data.vaccinations.count { it.completed }
            val totalVaccinations = data.vaccinations.size
            writer.appendLine("완료된 접종: ${completedVaccinations}/${totalVaccinations}")
            writer.appendLine("완료율: ${if(totalVaccinations > 0) (completedVaccinations * 100 / totalVaccinations) else 0}%")
            writer.appendLine()

            writer.appendLine("📖 활동 기록")
            writer.appendLine("-".repeat(30))
            writer.appendLine("총 일기 수: ${data.diaryEntries.size}개")
            writer.appendLine("사진 포함 일기: ${data.diaryEntries.count { it.photo != null }}개")
            writer.appendLine()

            writer.appendLine("🏆 달성한 성취")
            writer.appendLine("-".repeat(30))
            data.achievements.forEach { achievement ->
                writer.appendLine("• ${achievement.title}: ${achievement.description}")
                writer.appendLine("  달성일: ${achievement.unlockedDate}")
                writer.appendLine()
            }

            writer.appendLine()
            writer.appendLine("=".repeat(50))
            writer.appendLine("이 보고서는 PuppyDiary 앱에서 생성되었습니다.")
        }
    }

    private fun shareFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = when {
                    file.extension == "json" -> "application/json"
                    file.extension == "csv" -> "text/csv"
                    file.extension == "pdf" -> "application/pdf"
                    else -> "text/plain"
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "통계 데이터 공유"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}