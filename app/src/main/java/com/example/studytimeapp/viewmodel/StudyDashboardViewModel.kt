package com.example.studytimeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studytimeapp.core.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyDashboardViewModel : ViewModel() {

    private val useCase = StudySessionUseCase()
    private var timerJob: Job? = null

    data class SubjectStat(
        val subject: Subject,
        val currentDurationSec: Long,
        val actualPercent: Int
    )

    data class DashboardState(
        val subjects: List<Subject> = emptyList(),
        val sessions: List<Session> = emptyList(),
        val stats: List<SubjectStat> = emptyList(),
        val recommendedSubject: Subject? = null,
        val isSyncing: Boolean = false
    )

    data class TimerState(
        val isRunning: Boolean = false,
        val subjectId: Int? = null,
        val subjectName: String = "",
        val elapsedSec: Long = 0L
    )

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val initialSubjects = listOf(
                Subject(id = 101, name = "국어", colorHex = "#EF4444", targetPercent = 30),
                Subject(id = 102, name = "수학", colorHex = "#3B82F6", targetPercent = 40),
                Subject(id = 103, name = "영어", colorHex = "#F59E0B", targetPercent = 30)
            )
            _state.update { it.copy(subjects = initialSubjects) }
            updateDashboardStats()
        }
    }

    fun startTimer(subjectId: Int) {
        if (_timerState.value.isRunning) stopTimer()

        val subject = _state.value.subjects.find { it.id == subjectId }
        val name = subject?.name ?: "알 수 없는 과목"

        _timerState.update { 
            TimerState(isRunning = true, subjectId = subjectId, subjectName = name) 
        }
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timerState.update { s ->
                    s.copy(elapsedSec = s.elapsedSec + 1)
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        val currentTimer = _timerState.value
        
        if (currentTimer.subjectId != null && currentTimer.elapsedSec > 0) {
            val newSession = useCase.createNewSessionRecord(currentTimer.subjectId, currentTimer.elapsedSec)
            _state.update { it.copy(sessions = it.sessions + newSession) }
            updateDashboardStats()
        }

        _timerState.update { TimerState() }
    }

    private fun updateDashboardStats() {
        val currentState = _state.value
        val summary = useCase.summarizeSessions(currentState.sessions)
        val totalTime = useCase.calculateTotalStudyTimeFromSummary(summary)

        val newStats = currentState.subjects.map { subject ->
            val duration = summary[subject.id] ?: 0L
            val percent = useCase.calculatePercentage(totalTime, duration)
            SubjectStat(subject, duration, percent)
        }

        val recommended = useCase.recommendSubject(currentState.subjects, summary)

        _state.update { it.copy(stats = newStats, recommendedSubject = recommended) }
    }

    fun syncData() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            delay(1000)
            _state.update { it.copy(isSyncing = false) }
            updateDashboardStats()
        }
    }
}
