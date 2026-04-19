package com.example.studytimeapp.core

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @brief 학습 세션의 핵심 비즈니스 로직을 처리하는 UseCase 클래스.
 */
class StudySessionUseCase {

    /**
     * @brief 세션 리스트를 과목별로 그룹화하여 합산된 맵을 반환합니다. (O(N) 최적화의 핵심)
     */
    fun summarizeSessions(sessions: List<Session>): Map<Int, Long> {
        return sessions.groupBy { it.subjectId }
            .mapValues { (_, subjectSessions) -> subjectSessions.sumOf { it.durationSec } }
    }

    fun calculateTotalStudyTimeFromSummary(summary: Map<Int, Long>): Long = 
        summary.values.sum()

    fun calculatePercentage(totalTime: Long, subjectDuration: Long): Int {
        if (totalTime == 0L) return 0
        return ((subjectDuration.toDouble() / totalTime.toDouble()) * 100).toInt()
    }

    fun createNewSessionRecord(currentSubjectId: Int, elapsedSec: Long): Session =
        Session(
            id = System.currentTimeMillis(),
            subjectId = currentSubjectId,
            durationSec = elapsedSec
        )

    /**
     * @brief 요약된 데이터를 사용하여 가장 부족한 과목을 추천합니다.
     */
    fun recommendSubject(subjects: List<Subject>, summary: Map<Int, Long>): Subject? {
        val totalStudyTime = calculateTotalStudyTimeFromSummary(summary)
        
        return subjects.maxByOrNull { subject ->
            val myDuration = summary[subject.id] ?: 0L
            val actualPercent = calculatePercentage(totalStudyTime, myDuration)
            subject.targetPercent - actualPercent
        }
    }

    // 하위 호환성을 위한 기존 함수 유지 (필요 시)
    fun calculateTotalStudyTime(sessions: List<Session>): Long = sessions.sumOf { it.durationSec }
}

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey val id: Long,
    val subjectId: Int,
    val durationSec: Long
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey val id: Int,
    val name: String,
    val colorHex: String,
    val targetPercent: Int
)
