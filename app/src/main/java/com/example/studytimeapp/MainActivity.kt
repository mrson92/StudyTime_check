package com.example.studytimeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.studytimeapp.data.AppDatabase
import com.example.studytimeapp.ui.HomeScreen
import com.example.studytimeapp.ui.theme.StudyTimeAppTheme
import com.example.studytimeapp.viewmodel.StudyDashboardViewModel

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val viewModel: StudyDashboardViewModel by viewModels {
        StudyDashboardViewModel.Factory(database.studyDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyTimeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
