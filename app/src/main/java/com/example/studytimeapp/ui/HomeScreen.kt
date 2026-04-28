package com.example.studytimeapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studytimeapp.viewmodel.StudyDashboardViewModel
import com.example.studytimeapp.ui.theme.*

@Composable
fun HomeScreen(viewModel: StudyDashboardViewModel = viewModel()) {
    val dashboardState by viewModel.state.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            if (!timerState.isRunning) {
                StudyBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        },
        floatingActionButton = {
            if (!timerState.isRunning && selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = BrandBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "과목 추가")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> DashboardContent(
                    state = dashboardState,
                    onStartTimer = { subjectId -> viewModel.startTimer(subjectId) }
                )
                1 -> PlaceholderScreen("통계 화면")
                2 -> PlaceholderScreen("설정 화면")
            }

            if (timerState.isRunning) {
                StudyTimerOverlay(
                    timerState = timerState,
                    onPause = { viewModel.pauseTimer() },
                    onResume = { viewModel.resumeTimer() },
                    onStop = { viewModel.stopTimer() }
                )
            }

            if (showAddDialog) {
                AddSubjectDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, color, target ->
                        viewModel.addSubject(name, color, target)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun StudyBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("홈") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandBlue,
                selectedTextColor = BrandBlue,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.Timeline, contentDescription = null) },
            label = { Text("통계") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandBlue,
                selectedTextColor = BrandBlue,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("설정") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandBlue,
                selectedTextColor = BrandBlue,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray,
                indicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = TextGray)
    }
}

@Composable
fun DashboardContent(
    state: StudyDashboardViewModel.DashboardState,
    onStartTimer: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            HeaderSection()
        }

        item {
            SmartInsightSection(
                recommendedSubject = state.recommendedSubject,
                onStart = { state.recommendedSubject?.let { onStartTimer(it.id) } }
            )
        }

        item {
            SectionTitle(title = "오늘의 학습 현황", icon = Icons.Default.Schedule)
        }

        items(state.stats) { stat ->
            SubjectListItem(
                stat = stat,
                onStart = { onStartTimer(stat.subject.id) }
            )
        }
    }
}

@Composable
fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = "StudyBench.",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "메타인지 자극 학습 타이머",
            fontSize = 14.sp,
            color = TextGray
        )
    }
}

@Composable
fun SmartInsightSection(
    recommendedSubject: com.example.studytimeapp.core.Subject?,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightBlueBG)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "스마트 인사이트",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val subjectName = recommendedSubject?.name ?: "과목"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🎯 설정한 목표 대비 [$subjectName] 비중이 가장 부족해요!\n지금 채워볼까요?",
                    fontSize = 15.sp,
                    color = TextDark,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$subjectName 지금 시작하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextDark)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}

@Composable
fun SubjectListItem(
    stat: StudyDashboardViewModel.SubjectStat,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(stat.subject.colorHex)))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = stat.subject.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTimeKorean(stat.currentDurationSec),
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = stat.actualPercent / 100f,
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(android.graphics.Color.parseColor(stat.subject.colorHex)),
                        trackColor = ProgressTrack
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${stat.actualPercent}% / ${stat.subject.targetPercent}%",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(
                onClick = onStart,
                modifier = Modifier
                    .size(40.dp)
                    .background(BackgroundGray, CircleShape)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "시작",
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun StudyTimerOverlay(
    timerState: StudyDashboardViewModel.TimerState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F2937).copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${timerState.subjectName} 학습 중",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = formatTimeDigital(timerState.elapsedSec),
                color = Color.White,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(60.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause/Resume Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { if (timerState.isPaused) onResume() else onPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (timerState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                // Stop Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .clickable { onStop() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "정지 버튼을 누르면 기록이 저장됩니다.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetPercent by remember { mutableStateOf("20") }
    var selectedColor by remember { mutableStateOf("#EF4444") }

    val colors = listOf("#EF4444", "#3B82F6", "#F59E0B", "#10B981", "#8B5CF6", "#EC4899", "#6B7280")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 과목 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("과목 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetPercent,
                    onValueChange = { if (it.all { char -> char.isDigit() }) targetPercent = it },
                    label = { Text("목표 비중 (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("색상 선택", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colors.forEach { colorStr ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(colorStr)))
                                .clickable { selectedColor = colorStr }
                        ) {
                            if (selectedColor == colorStr) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedColor, targetPercent.toIntOrNull() ?: 0)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextGray)
            }
        }
    )
}

fun formatTimeKorean(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "${h}시간 ${m}분 ${s}초" else "${m}분 ${s}초"
}

fun formatTimeDigital(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}
