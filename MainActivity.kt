// File: MainActivity.kt (Kotlin)
package com.example.studytimeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.studytimeapp.ui.HomeScreen
import com.example.studytimeapp.ui.theme.StudyTimeAppTheme // 실제 프로젝트의 테마 이름으로 변경 필요

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Theme 적용 (웹에서 네이티브로 전환 시 가장 먼저 해야 할 일)
            StudyTimeAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // 2. 메인 화면 컴포넌트 호출 (View Layer의 진입점)
                    HomeScreen()
                }
            }
        }
    }
}