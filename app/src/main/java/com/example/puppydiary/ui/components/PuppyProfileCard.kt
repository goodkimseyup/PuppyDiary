package com.example.puppydiary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.puppydiary.data.model.PuppyData
import com.example.puppydiary.ui.theme.AppColors
import com.example.puppydiary.utils.getBreedEmoji
import java.io.File

@Composable
fun PuppyProfileCard(
    puppyData: PuppyData,
    age: String,
    currentWeight: Float,
    birthdayDday: String? = null,
    modifier: Modifier = Modifier,
    onImageClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = AppColors.Primary.copy(alpha = 0.2f),
                spotColor = AppColors.Primary.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        AppColors.Primary,
                        AppColors.Secondary
                    )
                )
            )
    ) {
        // 배경 장식 원
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-30).dp, y = (-30).dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 20.dp)
                .background(
                    Color.White.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )
        
        // 수정 버튼 (오른쪽 상단)
        if (onEditClick != null) {
            Surface(
                onClick = onEditClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "프로필 수정",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(18.dp)
                )
            }
        }
        
        // 생일 D-day 배지 (왼쪽 상단)
        if (birthdayDday != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "🎂 $birthdayDday",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        
        Row(
            modifier = Modifier
                .padding(24.dp)
                .padding(top = if (birthdayDday != null) 24.dp else 0.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 이미지
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .then(
                        if (onImageClick != null) {
                            Modifier.clickable { onImageClick() }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (puppyData.profileImage != null) {
                    val imageFile = File(puppyData.profileImage)
                    if (imageFile.exists()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageFile)
                                .crossfade(true)
                                .build(),
                            contentDescription = "프로필 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = getBreedEmoji(puppyData.breed),
                            fontSize = 44.sp
                        )
                    }
                } else {
                    Text(
                        text = getBreedEmoji(puppyData.breed),
                        fontSize = 44.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = puppyData.name,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // 견종 배지
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = puppyData.breed,
                        fontSize = 13.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 정보 행
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            text = "나이",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = age,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "몸무게",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${currentWeight}kg",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                
                // 프로필 편집 힌트
                if (onImageClick != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📷 사진 탭하여 변경",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
