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
    // 1. 상태 분리 구독
    val dashboardState by viewModel.state.collectAsState()
    val timerState by viewModel.timerState.collectAsState()

    if (timerState.isRunning) {
        // 타이머 화면: timerState만 사용함
        StudyTimerOverlay(
            timerState = timerState,
            onStop = { viewModel.stopTimer() }
        )
    } else {
        // 대시보드 화면: dashboardState만 사용함
        // 타이머가 업데이트되어도 DashboardContent 내부의 정적 요소들은 리컴포지션되지 않음
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
        // 인사이트 섹션 (타이머 업데이트 시에도 다시 그려지지 않음)
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

        // 과목 리스트
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
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stat.subject.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "오늘 공부: ${formatTime(stat.currentDurationSec)} (${stat.actualPercent}%)",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = stat.actualPercent / 100f,
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = Color(android.graphics.Color.parseColor(stat.subject.colorHex)),
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }
            
            IconButton(onClick = onStart) {
                Text("▶", fontSize = 24.sp)
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
