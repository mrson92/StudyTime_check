// File: ui/HomeScreen.kt (Kotlin)
package com.example.studytimeapp.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studytimeapp.viewmodel.StudyDashboardViewModel
import com.example.studytimeapp.viewmodel.StudyDashboardViewModel.DashboardState

/**
 * @brief 메인 대시보드 화면 컴포넌트 (View Layer).
 *
 * ViewModel의 StateFlow를 관찰하여 UI를 렌더링합니다.
 */
@Composable
fun HomeScreen(viewModel: StudyDashboardViewModel = viewModel()) {
    // ViewModel의 상태를 관찰합니다. 이 부분이 View와 ViewModel을 연결하는 핵심입니다.
    val state by viewModel.state.collectAsState()

    // 타이머가 실행 중일 때 오버레이 모달이 뜰지 여부를 결정합니다.
    val isTimerRunning = state.timerState.isRunning

    if (isTimerRunning) {
        StudyTimerOverlay(viewModel = viewModel, state = state.timerState)
    } else {
        // 타이머가 실행 중이 아닐 때의 메인 UI 렌더링
        Column(modifier = Modifier.fillMaxSize()) {
            // Header는 상단에 고정되어 있다고 가정하고 생략하거나 별도 컴포넌트로 분리합니다.
            DashboardContent(state = state)
        }
    }
}

/**
 * @brief 타이머가 실행 중일 때 표시되는 오버레이 모달입니다.
 */
@Composable
fun StudyTimerOverlay(viewModel: StudyDashboardViewModel, state: StudyDashboardViewModel.TimerState) {
    // 이 컴포넌트는 화면 전체를 덮는 전역 오버레이 역할을 합니다.
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.95f)) // 배경 어둡게 처리
                .align(Alignment.Center)
        ) {
            // 타이머 시간 표시 (가장 중요)
            Text(
                text = "시간 계산 중...", // 실제로는 ViewModel에서 포맷된 시간을 받아와야 함
                style = androidx.compose.ui.text.TextStyle(fontSize = 72.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Light)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 제어 버튼 그룹
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // 일시정지 버튼 (Pause)
                Button(onClick = { viewModel.pauseTimer() }) {
                    Text("일시정지")
                }
                // 중지 버튼 (Stop)
                Button(onClick = { viewModel.stopTimer() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("중지 및 저장")
                }
            }
        }
    }
}

/**
 * @brief 타이머가 비활성화 상태일 때 표시되는 메인 대시보드 콘텐츠입니다.
 */
@Composable
fun DashboardContent(state: DashboardState) {
    Column(modifier = Modifier.padding(16.dp)) {
        // 1. 인사이트 카드 (상단 고정 영역)
        Card(modifier = Modifier.fillMaxWidth().shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("스마트 인사이트", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("오늘도 힘차게 공부를 시작해볼까요?", style = androidx.compose.ui.text.TextStyle(color = Color.Gray))
            }
        }

        // 2. 과목별 요약 및 퀵스타트 (가장 중요한 재사용 영역)
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Text("오늘의 학습 현황", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // 과목별 요약 컴포넌트 (재사용 가능한 패턴)
            state.subjects.forEach { subject ->
                SubjectSummaryCard(subject = subject, sessionDurationSec = useCase.calculateSubjectDuration(state.sessions, subject.id), totalTime = state.sessions.sumOf { it.durationSec })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 3. 통계 및 피어 비교 (재사용 가능한 패턴)
        if (state.isSynced && state.peerData != null) {
            Text("피어 그룹 비교", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            PeerComparisonChart(state.subjects, state.sessions, state.peerData!!)
        } else if (!state.isSyncing && !state.isSynced) {
            // 동기화 전 안내 메시지 (View에서 상태에 따라 렌더링 제어)
            Text("데이터를 로드하고 분석하려면 '데이터 동기화 및 비교하기' 버튼을 눌러주세요.", color = Color.Gray)
        }
    }
}

/**
 * @brief 개별 과목의 학습 현황을 보여주는 재사용 가능한 컴포넌트입니다.
 */
@Composable
fun SubjectSummaryCard(subject: com.example.studytimeapp.core.Subject, sessionDurationSec: Long, totalTime: Long) {
    val actualPercent = if (totalTime == 0L) 0 else ((sessionDurationSec.toDouble() / totalTime.toDouble()) * 100).toInt().coerceAtMost(100)

    Card(modifier = Modifier.fillMaxWidth().shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 과목명 및 타이머 버튼 영역
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color = subject.colorHex.toColor()) // 색상으로 표시
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${subject.name} 학습 현황", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))

                // 퀵스타트 버튼 (재사용 가능한 액션 패턴)
                Button(onClick = { /* TODO: ViewModel 호출 */ }) {
                    Text("지금 시작")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 진행률 바 영역
            Column {
                Text("진행률", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(color = Color.Gray.copy(alpha = 0.2f))
                ) {
                    // 진행률 바 (핵심 시각화 로직)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(remember { mutableStateOf(actualPercent.toFloat()) }) // 실제로는 State를 받아와야 함
                            .background(color = Color(android.graphics.Color.parseColor(subject.colorHex)))
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("목표 대비: ${actualPercent}% / 목표: ${subject.targetPercent}%", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * @brief 피어 그룹 비교 차트 컴포넌트 (재사용 가능한 시각화 패턴).
 */
@Composable
fun PeerComparisonChart(subjects: List<com.example.studytimeapp.core.Subject>, sessions: List<com.example.studytimeapp.core.Session>, peerDataMap: Map<Int, com.example.studytimeapp.core.PeerData>) {
    // 이 컴포넌트는 ViewModel에서 계산된 데이터를 받아와서 차트를 그립니다.
    // 실제 구현에서는 Charting 라이브러리 사용을 고려해야 합니다.
}

/**
 * @brief 색상 Hex 코드를 Compose Color 객체로 변환하는 확장 함수 (유틸리티)
 */
fun String.toColor(): androidx.compose.ui.graphics.Color {
    return try {
        android.graphics.Color.parseColor(this)
    } catch (e: Exception) {
        androidx.compose.ui.graphics.Color.Gray
    }
}