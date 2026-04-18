# StudyTime Check

**StudyTime Check**는 과목별 공부 시간을 정교하게 측정하고 분석하여 효율적인 학습 시간 배분을 도와주는 안드로이드 애플리케이션입니다.

## 🚀 주요 기능

- **실시간 학습 타이머:** 과목별로 학습 시간을 실시간으로 측정합니다.
- **스마트 인사이트:** 설정된 목표 대비 현재 학습 비중을 분석하여 가장 부족한 과목을 자동으로 추천합니다.
- **학습 통계 대시보드:** 모든 학습 세션을 요약하여 과목별 점유율과 누적 시간을 시각적으로 제공합니다.
- **데이터 동기화:** 로컬 데이터와 서버 데이터를 동기화하여 분석 그룹(Peer Group)과의 비교 데이터를 제공합니다.

## 🛠 기술 스택

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material3)
- **Architecture:** Clean Architecture (Layered: Core, ViewModel, UI)
- **State Management:** StateFlow & ViewModel (Android Architecture Components)
- **Concurrency:** Kotlin Coroutines & Flow

## 🏗 프로젝트 구조

프로젝트는 관심사 분리를 위해 세 개의 주요 레이어로 나뉘어 있습니다.

- **`core/` (Domain Layer):** 플랫폼 의존성이 없는 순수 비즈니스 로직 및 도메인 모델.
    - `StudySessionUseCase`: 통계 계산, 세션 요약, 추천 알고리즘 담당.
- **`viewmodel/` (Presentation Layer):** UI 상태를 관리하고 비즈니스 로직을 UI에 연결.
    - `StudyDashboardViewModel`: 타이머 제어 및 대시보드 상태 관리.
- **`ui/` (View Layer):** Jetpack Compose를 이용한 선언형 UI.
    - `HomeScreen`: 메인 대시보드 및 타이머 오버레이 인터페이스.

## ⚡ 성능 최적화 포인트

본 프로젝트는 대용량 데이터 처리와 부드러운 UI 경험을 위해 다음과 같은 최적화가 적용되었습니다.

1. **타이머 상태 분리 (State Isolation):** 1초마다 업데이트되는 타이머 상태를 정적인 대시보드 상태와 분리하여, 타이머 작동 시 불필요한 전체 화면 리컴포지션(Recomposition)을 방지했습니다.
2. **조회 알고리즘 최적화 ($O(N)$):** 매번 전체 리스트를 순회하는 대신, 세션 리스트를 한 번의 순회로 요약(Map Grouping)하여 과목별 통계 조회 성능을 $O(1)$로 개선했습니다.

## 📖 시작하기

### 빌드 및 실행
본 프로젝트는 표준 안드로이드 프로젝트 구조를 따릅니다.
```bash
./gradlew assembleDebug
```
안드로이드 스튜디오를 사용하여 기기 또는 에뮬레이터에서 실행할 수 있습니다.

---
*본 프로젝트는 AI 협업을 통해 설계 및 개발되었습니다.*
