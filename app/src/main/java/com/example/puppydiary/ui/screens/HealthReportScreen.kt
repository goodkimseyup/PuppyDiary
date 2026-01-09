package com.example.puppydiary.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puppydiary.ui.theme.AppColors
import com.example.puppydiary.utils.HealthAnalyzer
import com.example.puppydiary.viewmodel.PuppyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthReportScreen(
    viewModel: PuppyViewModel,
    onNavigateBack: () -> Unit
) {
    val puppyData by viewModel.puppyData.collectAsState()
    val weightRecords by viewModel.weightRecords.collectAsState()
    val walkRecords by viewModel.walkRecords.collectAsState()
    val mealRecords by viewModel.mealRecords.collectAsState()
    val vaccinations by viewModel.vaccinations.collectAsState()
    val medicationRecords by viewModel.medicationRecords.collectAsState()

    val healthReport = remember(puppyData, weightRecords, walkRecords, mealRecords, vaccinations, medicationRecords) {
        puppyData?.let { puppy ->
            HealthAnalyzer.analyzeHealth(
                puppy = puppy,
                weightRecords = weightRecords,
                walkRecords = walkRecords,
                mealRecords = mealRecords,
                vaccinations = vaccinations,
                medicationRecords = medicationRecords
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 건강 리포트", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF7F9FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            healthReport?.let { report ->
                // 전체 건강 점수
                item {
                    OverallScoreCard(
                        score = report.overallScore,
                        puppyName = puppyData?.name ?: ""
                    )
                }

                // 세부 점수들
                item {
                    DetailScoresCard(
                        weightScore = report.weightScore,
                        activityScore = report.activityScore,
                        nutritionScore = report.nutritionScore,
                        weightTrend = report.weightTrend,
                        activityTrend = report.activityTrend
                    )
                }

                // 건강 알림
                if (report.alerts.isNotEmpty()) {
                    item {
                        Text(
                            text = "건강 알림",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(report.alerts) { alert ->
                        AlertCard(alert = alert)
                    }
                }

                // 추천사항
                if (report.recommendations.isNotEmpty()) {
                    item {
                        RecommendationsCard(recommendations = report.recommendations)
                    }
                }

                // 하단 여백
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun OverallScoreCard(score: Int, puppyName: String) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "score"
    )

    val grade = HealthAnalyzer.getHealthGrade(score)
    val scoreColor = Color(HealthAnalyzer.getHealthColor(score))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(scoreColor.copy(alpha = 0.1f), Color.White)
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${puppyName}의 건강 점수",
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(16.dp))

                // 원형 프로그레스
                Box(contentAlignment = Alignment.Center) {
                    CircularScoreIndicator(
                        score = animatedScore,
                        color = scoreColor,
                        modifier = Modifier.size(180.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$animatedScore",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = grade,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = scoreColor
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = getScoreMessage(score),
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CircularScoreIndicator(score: Int, color: Color, modifier: Modifier = Modifier) {
    val sweepAngle by animateFloatAsState(
        targetValue = (score / 100f) * 360f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "sweep"
    )

    Canvas(modifier = modifier) {
        // 배경 원
        drawArc(
            color = Color(0xFFE0E0E0),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
        )
        // 점수 원
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun DetailScoresCard(
    weightScore: Int,
    activityScore: Int,
    nutritionScore: Int,
    weightTrend: HealthAnalyzer.WeightTrend,
    activityTrend: HealthAnalyzer.ActivityTrend
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "세부 분석",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            ScoreRow(
                icon = "⚖️",
                label = "체중 관리",
                score = weightScore,
                subtitle = getTrendText(weightTrend)
            )

            Spacer(Modifier.height(12.dp))

            ScoreRow(
                icon = "🚶",
                label = "활동량",
                score = activityScore,
                subtitle = getTrendText(activityTrend)
            )

            Spacer(Modifier.height(12.dp))

            ScoreRow(
                icon = "🍽️",
                label = "영양 관리",
                score = nutritionScore,
                subtitle = null
            )
        }
    }
}

@Composable
fun ScoreRow(icon: String, label: String, score: Int, subtitle: String?) {
    val color = Color(HealthAnalyzer.getHealthColor(score))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 24.sp)

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            subtitle?.let {
                Text(text = it, fontSize = 12.sp, color = Color.Gray)
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${score}점",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun AlertCard(alert: HealthAnalyzer.HealthAlert) {
    val backgroundColor = when (alert.severity) {
        HealthAnalyzer.Severity.CRITICAL -> Color(0xFFFFEBEE)
        HealthAnalyzer.Severity.WARNING -> Color(0xFFFFF8E1)
        HealthAnalyzer.Severity.INFO -> Color(0xFFE8F5E9)
    }

    val borderColor = when (alert.severity) {
        HealthAnalyzer.Severity.CRITICAL -> Color(0xFFF44336)
        HealthAnalyzer.Severity.WARNING -> Color(0xFFFFC107)
        HealthAnalyzer.Severity.INFO -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = alert.icon, fontSize = 28.sp)

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = alert.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = alert.message,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
fun RecommendationsCard(recommendations: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💡", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "AI 추천사항",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            recommendations.forEach { recommendation ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        fontSize = 16.sp,
                        color = AppColors.Primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = recommendation,
                        fontSize = 14.sp,
                        color = Color(0xFF444444)
                    )
                }
            }
        }
    }
}

private fun getScoreMessage(score: Int): String {
    return when {
        score >= 90 -> "훌륭해요! 건강 관리를 아주 잘 하고 있어요 🎉"
        score >= 80 -> "좋아요! 조금만 더 신경쓰면 완벽해요 👍"
        score >= 70 -> "괜찮아요. 몇 가지 개선하면 더 좋아질 거예요"
        score >= 60 -> "관심이 필요해요. 아래 추천사항을 확인해주세요"
        else -> "주의가 필요해요. 수의사 상담을 권장합니다"
    }
}

private fun getTrendText(trend: HealthAnalyzer.WeightTrend): String {
    return when (trend) {
        HealthAnalyzer.WeightTrend.STABLE -> "안정적 ✓"
        HealthAnalyzer.WeightTrend.INCREASING -> "증가 추세 ↑"
        HealthAnalyzer.WeightTrend.DECREASING -> "감소 추세 ↓"
        HealthAnalyzer.WeightTrend.FLUCTUATING -> "불안정 ~"
        HealthAnalyzer.WeightTrend.INSUFFICIENT_DATA -> "데이터 부족"
    }
}

private fun getTrendText(trend: HealthAnalyzer.ActivityTrend): String {
    return when (trend) {
        HealthAnalyzer.ActivityTrend.VERY_ACTIVE -> "매우 활발 🏃"
        HealthAnalyzer.ActivityTrend.ACTIVE -> "활발 🚶"
        HealthAnalyzer.ActivityTrend.MODERATE -> "보통"
        HealthAnalyzer.ActivityTrend.LOW -> "부족 ⚠️"
        HealthAnalyzer.ActivityTrend.INSUFFICIENT_DATA -> "데이터 부족"
    }
}
