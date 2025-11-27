package com.example.puppydiary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puppydiary.viewmodel.PuppyViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun StatsScreen(viewModel: PuppyViewModel) {
    val weightRecords by viewModel.weightRecords.collectAsState()
    val vaccinations by viewModel.vaccinations.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
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
                        // 데이터가 1개일 때는 간단히 표시
                        Text(
                            text = "첫 번째 기록: ${weightRecords.first().weight}kg",
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    } else {
                        // Vico 라인 차트
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
                modifier = Modifier.fillMaxWidth()
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
