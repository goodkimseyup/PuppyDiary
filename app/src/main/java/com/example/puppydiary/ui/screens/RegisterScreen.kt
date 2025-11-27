package com.example.puppydiary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puppydiary.viewmodel.PuppyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: PuppyViewModel,
    onRegisterComplete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "🐕",
            fontSize = 80.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "반려견 등록",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "우리 아이 정보를 입력해주세요",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            placeholder = { Text("예: 꼬미") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = breed,
            onValueChange = { breed = it },
            label = { Text("품종") },
            placeholder = { Text("예: 말티즈") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "생년월일",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = birthYear,
                onValueChange = { if (it.length <= 4) birthYear = it.filter { c -> c.isDigit() } },
                label = { Text("년") },
                placeholder = { Text("2023") },
                singleLine = true,
                modifier = Modifier.weight(1.2f)
            )
            OutlinedTextField(
                value = birthMonth,
                onValueChange = { if (it.length <= 2) birthMonth = it.filter { c -> c.isDigit() } },
                label = { Text("월") },
                placeholder = { Text("03") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = birthDay,
                onValueChange = { if (it.length <= 2) birthDay = it.filter { c -> c.isDigit() } },
                label = { Text("일") },
                placeholder = { Text("15") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("현재 몸무게 (kg)") },
            placeholder = { Text("예: 3.5") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && breed.isNotBlank() && 
                    birthYear.isNotBlank() && birthMonth.isNotBlank() && birthDay.isNotBlank()) {
                    
                    val birthDate = "$birthYear-${birthMonth.padStart(2, '0')}-${birthDay.padStart(2, '0')}"
                    viewModel.registerPuppy(name.trim(), breed.trim(), birthDate, null)
                    
                    weight.toFloatOrNull()?.let { w ->
                        viewModel.addWeightRecord(w)
                    }
                    
                    onRegisterComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = name.isNotBlank() && breed.isNotBlank() && 
                      birthYear.isNotBlank() && birthMonth.isNotBlank() && birthDay.isNotBlank()
        ) {
            Text(
                text = "등록 완료",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
