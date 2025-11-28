package com.example.puppydiary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puppydiary.data.model.Vaccination
import com.example.puppydiary.data.model.WeightRecord
import com.example.puppydiary.viewmodel.PuppyViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: PuppyViewModel) {
    val weightRecords by viewModel.weightRecords.collectAsState()
    val vaccinations by viewModel.vaccinations.collectAsState()

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 접종 수정/삭제 다이얼로그
    var selectedVaccination by remember { mutableStateOf<Vaccination?>(null) }
    var showVaccinationDialog by remember { mutableStateOf(false) }
    var vaccineInput by remember { mutableStateOf("") }
    var nextDateInput by remember { mutableStateOf("") }
    var completedInput by remember { mutableStateOf(false) }

    // 몸무게 수정/삭제 다이얼로그
    var selectedWeight by remember { mutableStateOf<WeightRecord?>(null) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("") }

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
                Text(
                    text = "📊 성장 기록",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 요약 카드
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsSummaryCard(
                        title = "현재 체중",
                        value = "${viewModel.getCurrentWeight()}kg",
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    StatsSummaryCard(
                        title = "평균 체중",
                        value = "${String.format("%.1f", viewModel.getAverageWeight())}kg",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsSummaryCard(
                        title = "건강 점수",
                        value = "${viewModel.getHealthScore()}점",
                        color = Color(0xFFE91E63),
                        modifier = Modifier.weight(1f)
                    )
                    StatsSummaryCard(
                        title = "주간 성장",
                        value = "${if (viewModel.getWeeklyGrowth() >= 0) "+" else ""}${String.format("%.1f", viewModel.getWeeklyGrowth())}kg",
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 몸무게 라인 차트
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📈 몸무게 변화",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (weightRecords.isEmpty()) {
                            Text(
                                text = "아직 기록된 몸무게가 없어요",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        } else if (weightRecords.size == 1) {
                            Text(
                                text = "첫 번째 기록: ${weightRecords.first().weight}kg",
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        } else {
                            val displayRecords = weightRecords.takeLast(10)
                            val chartEntryModel = remember(displayRecords) {
                                entryModelOf(*displayRecords.mapIndexed { index, record ->
                                    index.toFloat() to record.weight
                                }.toTypedArray())
                            }

                            val dateLabels = remember(displayRecords) {
                                displayRecords.map { it.date.substring(5) }
                            }

                            val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                                dateLabels.getOrElse(value.toInt()) { "" }
                            }

                            Chart(
                                chart = lineChart(
                                    lines = listOf(
                                        lineSpec(
                                            lineColor = Color(0xFF2196F3),
                                            lineBackgroundShader = null
                                        )
                                    )
                                ),
                                model = chartEntryModel,
                                startAxis = rememberStartAxis(
                                    title = "kg",
                                    titleComponent = textComponent(
                                        color = Color.Gray,
                                        padding = dimensionsOf(end = 8.dp)
                                    )
                                ),
                                bottomAxis = rememberBottomAxis(
                                    valueFormatter = bottomAxisValueFormatter,
                                    titleComponent = textComponent(
                                        color = Color.Gray
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                }
            }

            // 몸무게 기록 목록
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚖️ 몸무게 기록",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "클릭하여 수정/삭제",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            items(weightRecords.reversed().take(10)) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedWeight = record
                            weightInput = record.weight.toString()
                            showWeightDialog = true
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${record.weight}kg",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF2196F3)
                            )
                            Text(
                                text = record.date,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "수정",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "💉 예방접종 현황",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val completedCount = vaccinations.count { it.completed }
                        Text(
                            text = "완료: $completedCount / ${vaccinations.size}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "클릭하여 수정/삭제",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (vaccinations.isEmpty()) {
                item {
                    Text(
                        text = "아직 기록된 예방접종이 없어요",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(vaccinations) { vaccination ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedVaccination = vaccination
                            vaccineInput = vaccination.vaccine
                            nextDateInput = vaccination.nextDate
                            completedInput = vaccination.completed
                            showVaccinationDialog = true
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = vaccination.vaccine,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "접종일: ${vaccination.date}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = if (vaccination.completed)
                                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                                else
                                    Color(0xFFFFC107).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (vaccination.completed) "✓ 완료" else "예정",
                                    color = if (vaccination.completed)
                                        Color(0xFF4CAF50)
                                    else
                                        Color(0xFFFFC107),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "다음: ${vaccination.nextDate}",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // 접종 수정/삭제 다이얼로그
    if (showVaccinationDialog && selectedVaccination != null) {
        AlertDialog(
            onDismissRequest = { showVaccinationDialog = false },
            title = { Text("예방접종 수정") },
            text = {
                Column {
                    OutlinedTextField(
                        value = vaccineInput,
                        onValueChange = { vaccineInput = it },
                        label = { Text("백신명") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nextDateInput,
                        onValueChange = { nextDateInput = it },
                        label = { Text("다음 접종일 (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = completedInput,
                            onCheckedChange = { completedInput = it }
                        )
                        Text("접종 완료")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateVaccination(
                            selectedVaccination!!.id,
                            vaccineInput,
                            nextDateInput,
                            completedInput
                        )
                        showVaccinationDialog = false
                        scope.launch { snackbarHostState.showSnackbar("접종 정보가 수정되었습니다") }
                    },
                    enabled = vaccineInput.isNotEmpty() && nextDateInput.isNotEmpty()
                ) { Text("저장") }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.deleteVaccination(selectedVaccination!!.id)
                            showVaccinationDialog = false
                            scope.launch { snackbarHostState.showSnackbar("접종 정보가 삭제되었습니다") }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("삭제")
                    }
                    TextButton(onClick = { showVaccinationDialog = false }) {
                        Text("취소")
                    }
                }
            }
        )
    }

    // 몸무게 수정/삭제 다이얼로그
    if (showWeightDialog && selectedWeight != null) {
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text("몸무게 수정") },
            text = {
                Column {
                    Text(
                        text = "날짜: ${selectedWeight!!.date}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("몸무게 (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        weightInput.toFloatOrNull()?.let { weight ->
                            viewModel.updateWeightRecord(selectedWeight!!.id, weight)
                            showWeightDialog = false
                            scope.launch { snackbarHostState.showSnackbar("몸무게가 수정되었습니다") }
                        }
                    },
                    enabled = weightInput.toFloatOrNull() != null
                ) { Text("저장") }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.deleteWeightRecord(selectedWeight!!.id)
                            showWeightDialog = false
                            scope.launch { snackbarHostState.showSnackbar("몸무게 기록이 삭제되었습니다") }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("삭제")
                    }
                    TextButton(onClick = { showWeightDialog = false }) {
                        Text("취소")
                    }
                }
            }
        )
    }
}

@Composable
fun StatsSummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
