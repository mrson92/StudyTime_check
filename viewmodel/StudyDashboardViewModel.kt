// File: viewmodel/StudyDashboardViewModel.kt (Kotlin)
package com.example.studytimeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studytimeapp.core.StudySessionUseCase
import com.example.studytimeapp.core.Subject
import com.example.studytimeapp.core.Session
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

/**
 * @brief 학습 대시보드 화면의 상태를 관리하는 ViewModel.
 *
 * 이 ViewModel은 UseCase를 통해 비즈니스 로직을 처리하고, UI가 관찰할 수 있는 StateFlow를 노출합니다.
 */
class StudyDashboardViewModel : ViewModel() {

    private val useCase = StudySessionUseCase()

    // --- 1. 상태 관리 (StateFlow) ---

    /** 전체 앱의 핵심 상태를 담는 Sealed Class 또는 Data Class 사용 권장 */
    data class DashboardState(
        val subjects: List<Subject> = emptyList(),
        val sessions: List<Session> = emptyList(),
        val isSynced: Boolean = false,
        val isSyncing: Boolean = false,
        val peerData: Map<Int, com.example.studytimeapp.core.PeerData>? = null,
        val timerState: TimerState = TimerState()
    )

    data class TimerState(
        val isRunning: Boolean = false,
        val subjectId: Int? = null,
        val elapsedSec: Long = 0L,
        val startTime: Date? = null
    )

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    // --- 2. 초기화 및 데이터 로드 ---

    init {
        // 웹에서 가져온 Mock 데이터를 사용하여 초기 상태 설정 (실제로는 Repository에서 주입받아야 함)
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // 1. Subject 데이터 로드 (Mocking)
            val initialSubjects = listOf(
                Subject(id = 101, name = "국어", colorHex = "#EF4444", targetPercent = 30),
                Subject(id = 102, name = "수학", colorHex = "#3B82F6", targetPercent = 40),
                Subject(id = 103, name = "영어", colorHex = "#F59E0B", targetPercent = 30)
            )
            // 로컬 저장소에서 세션 로드 (실제로는 DataStore 사용 권장)
            val savedSessionsJson = getMockSavedSessions() // Mock 함수 호출
            val initialSessions = savedSessionsJson.map { it as Session }

            _state.value = DashboardState(
                subjects = initialSubjects,
                sessions = initialSessions,
                isSynced = false,
                isSyncing = false
            )
        }
    }

    // --- 3. 타이머 로직 (Timer Management) ---

    /**
     * @brief 타이머를 시작하거나 재개합니다.
     */
    fun startOrResumeTimer(subjectId: Int) {
        if (_state.value.timerState.isRunning && _state.value.timerState.subjectId == subjectId) return // 이미 실행 중이면 무시

        _state.update { currentState ->
            currentState.copy(
                timerState = TimerState(
                    isRunning = true,
                    subjectId = subjectId,
                    elapsedSec = 0L,
                    startTime = Date()
                )
            )
        }
        // 타이머 로직은 별도의 Coroutine Scope에서 주기적으로 실행되어야 함 (실제 구현 시 필요)
    }

    /**
     * @brief 타이머를 일시정지합니다.
     */
    fun pauseTimer() {
        _state.update { currentState ->
            currentState.copy(timerState = currentState.timerState.copy(isRunning = false))
        }
    }

    /**
     * @brief 타이머를 중지하고, 현재까지의 기록을 세션 목록에 추가합니다.
     */
    fun stopTimer() {
        val currentSubjectId = _state.value.timerState.subjectId ?: return
        val elapsedSec = _state.value.timerState.elapsedSec

        if (elapsedSec > 0) {
            // UseCase를 사용하여 새로운 세션 기록 생성
            val newSession = useCase.createNewSessionRecord(currentSubjectId, elapsedSec)
            _state.update { currentState ->
                currentState.copy(sessions = currentState.sessions + newSession)
            }
        }

        // 타이머 상태 초기화
        _state.update { currentState ->
            currentState.copy(timerState = TimerState())
        }
    }

    // --- 4. 동기화 및 분석 로직 (Sync & Analysis) ---

    /**
     * @brief 서버와 데이터를 동기화하고 피어 그룹 비교 데이터를 가져옵니다.
     */
    fun syncDataAndAnalyze() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, isSynced = false) }

            // 1. Mock Network Call (서버 통신 시뮬레이션)
            kotlinx.coroutines.delay(1500) // 네트워크 지연 모사

            // 2. 피어 데이터 로드 및 상태 업데이트
            val peerDataMap = mapOf(
                101 to com.example.studytimeapp.core.PeerData(avgDurationSec = 7200),
                102 to com.example.studytimeapp.core.PeerData(avgDurationSec = 10800),
                103 to com.example.studytimeapp.core.PeerData(avgDurationSec = 5400)
            )

            _state.update { it.copy(isSynced = true, peerData = peerDataMap, isSyncing = false) }
        }
    }

    /**
     * @brief 대시보드 통계 데이터를 계산하고 상태를 업데이트합니다. (가장 중요한 비즈니스 로직 호출 지점)
     */
    fun updateDashboardStats() {
        val currentState = _state.value
        if (!currentState.isSynced || currentState.subjects.isEmpty()) return

        // 1. UseCase를 사용하여 통계 계산
        val totalTime = useCase.calculateTotalStudyTime(currentState.sessions)

        // 2. 추천 과목 로직 실행 (가장 중요한 비즈니스 규칙 적용)
        val recommendedId = useCase.recommendSubject(
            subjects = currentState.subjects,
            sessions = currentState.sessions,
            peerData = currentState.peerData
        )

        // 3. 새로운 통계 데이터 구조체 생성 및 상태 업데이트 (이 부분이 View에서 사용될 핵심 결과물)
        val newStats = currentState.subjects.map { subject ->
            val myDuration = useCase.calculateSubjectDuration(currentState.sessions, subject.id);
            val actualPercent = useCase.calculatePercentage(totalTime, myDuration);

            // 피어 데이터가 있다면 이를 사용하고, 없다면 0으로 처리
            val peerAvg = currentState.peerData?.get(subject.id)?.avgDurationSec ?: 0L;

            Triple(
                subject, // Subject 객체 자체
                myDuration,
                actualPercent
            )
        }

        // 실제 View에서는 이 리스트를 기반으로 UI 컴포넌트를 재렌더링합니다.
        println("--- [DEBUG] Dashboard Stats Calculated ---")
        println("Total Time: ${totalTime / 3600}시간, Recommended Subject ID: $recommendedId")
    }

    // --- Mocking Functions (실제 환경에서는 Repository/DataStore에서 주입받아야 함) ---
    private fun getMockSavedSessions(): List<Session> {
        return listOf(
            Session(id = 1, subjectId = 101, durationSec = 5400L), // 1.5시간
            Session(id = 2, subjectId = 102, durationSec = 3600L)  // 1시간
        )
    }
}