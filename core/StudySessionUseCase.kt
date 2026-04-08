// File: core/StudySessionUseCase.kt (Kotlin)
package com.example.studytimeapp.core

import java.util.Date

/**
 * @brief 학습 세션의 핵심 비즈니스 로직을 처리하는 UseCase 클래스.
 *
 * 이 클래스는 플랫폼(Android, Web 등)에 독립적인 순수 비즈니스 규칙과 상태 전이 로직만을 포함합니다.
 * 웹에서 추출된 시간 계산 및 목표 설정 로직을 Kotlin 네이티브 패턴으로 재구현했습니다.
 */
class StudySessionUseCase {

    /**
     * @brief 주어진 세션 목록으로부터 총 학습 시간을 초 단위로 집계합니다.
     * @param sessions 학습 세션 데이터 리스트 (각 항목은 duration_sec 필드를 가짐)
     * @return 총 학습 시간 (초)
     */
    fun calculateTotalStudyTime(sessions: List<Session>): Long {
        return sessions.sumOf { it.durationSec }
    }

    /**
     * @brief 특정 과목의 누적 학습 시간을 계산합니다.
     * @param sessions 전체 세션 목록
     * @param subjectId 분석할 과목의 ID
     * @return 해당 과목의 총 학습 시간 (초)
     */
    fun calculateSubjectDuration(sessions: List<Session>, subjectId: Int): Long {
        return sessions.filter { it.subjectId == subjectId }.sumOf { it.durationSec }
    }

    /**
     * @brief 목표 대비 현재 학습 비중을 백분율로 계산합니다.
     * @param totalTime 전체 총 학습 시간 (초)
     * @param subjectDuration 특정 과목의 누적 학습 시간 (초)
     * @return 백분율 (0-100)
     */
    fun calculatePercentage(totalTime: Long, subjectDuration: Long): Int {
        if (totalTime == 0L) return 0
        // Math.round((myDuration / totalStudyTime) * 100) 로직 재현
        return ((subjectDuration.toDouble() / totalTime.toDouble()) * 100).toInt()
    }

    /**
     * @brief 학습 타이머의 상태를 관리하고, 세션 종료 시 새로운 기록을 생성합니다.
     * @param currentSubjectId 현재 학습 중인 과목 ID
     * @param elapsedSec 경과 시간 (초)
     * @return 새로 생성된 Session 객체
     */
    fun createNewSessionRecord(currentSubjectId: Int, elapsedSec: Long): Session {
        // 로컬 DB에 저장되는 기본 구조를 모사합니다.
        return Session(
            id = System.currentTimeMillis(),
            subjectId = currentSubjectId,
            durationSec = elapsedSec
        )
    }

    /**
     * @brief 학습 목표 달성도를 분석하고 가장 부족한 과목을 추천합니다. (가장 복잡한 비즈니스 로직)
     * @param subjects 모든 과목 목록 (ID와 TargetPercent 포함)
     * @param sessions 현재까지의 전체 세션 기록
     * @param peerData 피어 그룹 평균 데이터 (선택 사항)
     * @return 가장 부족하거나 집중해야 할 과목 ID
     */
    fun recommendSubject(subjects: List<Subject>, sessions: List<Session>, peerData: Map<Int, PeerData>?): Int? {
        var worstDeficit = 0;
        var recommendedId: Int? = null;

        val totalStudyTime = calculateTotalStudyTime(sessions);

        for (subject in subjects) {
            val myDuration = calculateSubjectDuration(sessions, subject.id);
            val actualPercent = calculatePercentage(totalStudyTime, myDuration);

            var deficit = subject.targetPercent - actualPercent;

            // 피어 데이터가 있고, 현재 부족도가 가장 크면 추천
            if (deficit > worstDeficit) {
                worstDeficit = deficit;
                recommendedId = subject.id;
            }
            // 만약 동기화된 피어 데이터가 있다면, 목표치보다 '피어 평균'에 더 근접한 과목을 우선 고려할 수 있습니다. (추가 로직 필요)
        }
        return recommendedId;
    }
}

/**
 * @brief 학습 세션의 기본 모델 구조체.
 */
data class Session(
    val id: Long,
    val subjectId: Int,
    val durationSec: Long // 초 단위로 저장하는 것이 가장 정확함
)

/**
 * @brief 과목 정보를 담는 데이터 클래스.
 */
data class Subject(
    val id: Int,
    val name: String,
    val colorHex: String, // 웹에서 가져온 색상 코드
    val targetPercent: Int // 목표 비율 (%)
)

/**
 * @brief 피어 그룹 평균 데이터를 담는 데이터 클래스.
 */
data class PeerData(
    val avgDurationSec: Long // 초 단위
)