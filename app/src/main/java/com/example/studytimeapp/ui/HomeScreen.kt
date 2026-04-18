package com.example.studytimeapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studytimeapp.viewmodel.StudyDashboardViewModel

@Composable
fun HomeScreen(viewModel: StudyDashboardViewModel = viewModel()) {
    val dashboardState by viewModel.state.collectAsState()
    val timerState by viewModel.timerState.collectAsState()

    if (timerState.isRunning) {
        StudyTimerOverlay(
            timerState = timerState,
            onStop = { viewModel.stopTimer() }
        )
    } else {
        DashboardContent(
            state = dashboardState,
            onStartTimer = { subjectId -> viewModel.startTimer(subjectId) },
            onSync = { viewModel.syncData() }
        )
    }
}

@Composable
fun StudyTimerOverlay(
    timerState: StudyDashboardViewModel.TimerState,
    onStop: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timerState.subjectName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = formatTime(timerState.elapsedSec),
                color = Color.White,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("공부 중단", color = Color.White)
            }
        }
    }
}

@Composable
fun DashboardContent(
    state: StudyDashboardViewModel.DashboardState,
    onStartTimer: (Int) -> Unit,
    onSync: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("오늘의 추천", style = MaterialTheme.typography.titleMedium)
                val recommended = state.recommendedSubject
                Text(
                    text = if (recommended != null) "${recommended.name} 과목이 가장 부족해요!" else "학습 데이터를 쌓아보세요.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }

        Text("과목별 학습 현황", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        state.stats.forEach { stat ->
            SubjectCard(stat = stat, onStart = { onStartTimer(stat.subject.id) })
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSync,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSyncing
        ) {
            Text(if (state.isSyncing) "동기화 중..." else "데이터 동기화")
        }
    }
}

@Composable
fun SubjectCard(
    stat: StudyDashboardViewModel.SubjectStat,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally // 모든 요소를 중앙 정렬
        ) {
            // 1. 과목별 통계 (중앙 배치)
            Text(
                text = "오늘 공부: ${formatTime(stat.currentDurationSec)} (${stat.actualPercent}%)",
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = stat.actualPercent / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(android.graphics.Color.parseColor(stat.subject.colorHex)),
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. 과목 이름 버튼 (크고 단순하게)
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(android.graphics.Color.parseColor(stat.subject.colorHex))
                )
            ) {
                Text(
                    text = stat.subject.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}
