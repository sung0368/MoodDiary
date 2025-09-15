# CapstoneProject - MooDiary

<img width="509" alt="pi" src="https://github.com/user-attachments/assets/ad70e5b5-ef3f-4a8c-9c52-d34936b19ff4">

<br>

## 프로젝트 소개

- MooDiary는 사용자의 일기 데이터를 기반으로 감정을 분석하고, 이를 시각화하여 제공하는 온디바이스 AI 기반 일기 애플리케이션입니다.
- 사용자의 편의를 위해 음성 인식 모델을 활용하여 텍스트 타이핑 및 음성 일기 작성 모두를 지원합니다.
- 사용자는 일기를 작성하며 하루를 되돌아보고, 감정 상태를 시각적으로 확인할 수 있으며, 앱이 제공하는 피드백을 통해 더 나은 하루를 설계할 수 있습니다.

<br>

## 팀원 구성

<div align="center">

| **서연수** | **성시우** | **박영빈** | **유두연** |
| :------: |  :------: | :------: | :------: |
| [<img src="https://github.com/user-attachments/assets/b8ee6c21-e044-4dcf-ade4-7ea26aa8c78c" height=150 width=150> <br/> @brynn00](https://github.com/brynn00) | [<img src="https://github.com/user-attachments/assets/05a676d3-31ae-4dc5-ba33-2bb342d952cf" height=150 width=150> <br/> @sung0368](https://github.com/sung0368) | [<img src="https://github.com/user-attachments/assets/d9ec4cf1-2319-4320-bf95-b074ad6d5bd7" height=150 width=150> <br/> @dudqls1106](https://github.com/dudqls1106) | [<img src="https://github.com/user-attachments/assets/57142a10-8c2b-43e5-b308-928823a941c4" height=150 width=150> <br/> @duyeonyoo99](https://github.com/duyeonyoo99) |

</div>

<br>

## 1. 개발 환경

- Front-end : Android XML, Java
- Back-end : Java, SQLite, 로컬 AI 처리 모듈(Llama v3), Android STT, Google Drive API
- 버전 및 이슈관리 : Github, Github Issues, Github Project
- 협업 툴 : Discord, Notion, Slack, Figma

---



## 2. 채택한 개발 기술

### Android(Java) 기반 네이티브 앱 개발

### SQLite
- 일기 데이터, 통계 및 피드백 등의 로컬 저장소
 
### Google Drive API

- 데이터의 선택적 클라우드 백업 및 복원 기능 제공

### Llama v3

- 온디바이스에 최적화 된 LLM 기반 로컬 AI 추론 기술 제공

### Android STT

- 온디바이스 음성 인식 기능 구현으로 인터넷 연결 없이 음성 일기 작성 제공

--- 


## 3. 프로젝트 구조

```
├── .cxx
├── .gradle
├── .idea
├── gradle
├── .gitignore
├── .gitmodules
├── build.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
├── settings.gradle
│
└── app
     ├── .cxx
     ├── build
     ├── java-llama.cpp
          ├── .github
          ├── models
          ├── src
          ├── target
          ├── .clang-format
          ├── .clang-tidy
          ├── .gitignore
          ├── CMakeLists.txt
          ├── LICENSE.md
          ├── pom.xml
          └── README.md
      
     └── src
          └── main
                ├── AndroidManifest.xml
                ├── java
                      ├── de.kherud.llama
                      ├── App
                      ├── AppLifecycleObserver
                      ├── CalendarActivity
                      ├── CalendarAdapter
                      ├── CalendarDay
                      ├── ColorBottomSheet
                      ├── ColorUtils
                      ├── DiaryAdapter
                      ├── DiaryDraftManager
                      ├── DiaryEntry
                      ├── DriveBackupManager
                      ├── HomeActivity
                      ├── HomeSearchActivity
                      ├── MiniCalendarAdapter
                      ├── MiniCalendarAdapterAtWrite
                      ├── MiniCalendarAtWriteDiary
                      ├── MiniCalendarDialog
                      ├── ModelManager
                      ├── MyDatabaseHelper
                      ├── SettingsActivity
                      ├── SpeechRecognizerHelper
                      ├── StatisticsActivity
                      ├── VoiceRecordActivity
                      ├── WriteDiaryHomeActivity
                      └── WritingButtonActivity

                └── res
                      ├── drawable
                              ├── bg_calendar_bubble.xml
                              ├── bg_card.xml
                              ├── bg_card2.xml
                              ├── bg_circle_green.xml
                              ├── bg_diary_card.xml
                              ├── bg_half_circle.xml
                              ├── bg_info_bubble.xml
                              ├── bg_popup.xml
                              ├── blue_button.xml
                              ├── dots_horizontal.xml
                              ├── green_button.xml
                              ├── handle_background.xml
                              ├── ic_add.xml
                              ├── ic_arrow_left.xml
                              ├── ic_arrow_right.xml
                              ├── ic_calendar.xml
                              ├── ic_calendar2.xml
                              ├── ic_calendar3.xml
                              ├── ic_chart.xml
                              ├── ic_close.xml
                              ├── ic_close2.xml
                              ├── ic_emotion_angry.xml
                              ├── ic_emotion_anxious.xml
                              ├── ic_emotion_confused.xml
                              ├── ic_emotion_group.png
                              ├── ic_emotion_happy.xml
                              ├── ic_emotion_hurt.xml
                              ├── ic_emotion_none.xml
                              ├── ic_emotion_sad.xml
                              ├── ic_heart.xml
                              ├── ic_info.xml
                              ├── ic_launcher_background.xml
                              ├── ic_launcher_foreground.xml
                              ├── ic_list.xml
                              ├── ic_logo.xml
                              ├── ic_microphone.xml
                              ├── ic_more.xml
                              ├── ic_palette.xml
                              ├── ic_pen.xml
                              ├── ic_photo.xml
                              ├── ic_recordmic.xml
                              ├── ic_search.xml
                              ├── ic_settings.xml
                              ├── ic_small_calendar.xml
                              ├── ic_status_done.xml
                              ├── ic_status_in_progress.xml
                              ├── ic_toggle_day_selected.xml
                              ├── ic_toggle_week_selected.xml
                              ├── ic_user.xml
                              ├── ic_wave_gray.xml
                              ├── ic_wave_green.xml
                              ├── main_button_selector.xml
                              ├── pencil_f.xml
                              ├── popup_background.xml
                              ├── purple_button.xml
                              ├── red_button.xml
                              ├── searchview_background.xml
                              ├── trash_f.xml
                              ├── white_button.xml
                              └── yellow_button.xml

                      └── layout
                              ├── activity_calendar.xml
                              ├── activity_calendar_item.xml
                              ├── activity_home.xml
                              ├── activity_home_popup.xml
                              ├── activity_home_search.xml
                              ├── activity_mini_calendar.xml
                              ├── activity_mini_calendar_item.xml
                              ├── activity_settings.xml
                              ├── activity_statistics.xml
                              ├── activity_voice_record.xml
                              ├── activity_writediary_home.xml
                              ├── activity_writingbutton.xml
                              ├── bottom_sheet_color.xml
                              ├── diary_item_popup.xml
                              └── item_diary_entry.xml

      
```

--- 

## 4. 역할 분담

### 🍊서연수

- **UI 설계 및 구현**
- **Google Cloud 연동**
- **AI 연결**

<br>
    
### 👻성시우

- **DB 설계 및 구축**
- **UI 구현**
- **AI 테스팅**


<br>

### 🐬박영빈

- **UI 개발 및 동적 구현**
- **DB 설계**
- **AI 모델 연결**


<br>

### 😎유두연

- **AI 테스팅 및 모듈 개발**
- **DB 설계**
    
--- 

## 5. 개발 기간 및 작업 관리

### 개발 기간

- 전체 개발 기간 : 2025-03-04 ~ 2025-05-30

<br>

### 작업 관리

- 주간회의를 진행하며 작업 순서와 방향성에 대한 고민을 나누었습니다.
- 일정이 지연되는 작업은 회의에서 원인을 공유하고 유동적으로 일정을 조정하였습니다.
- PR 리뷰를 통해 코드 품질을 상호 점검하고, 주요 로직 변경 사항은 회의에서 공유하였습니다.
- GitHub Issues 및 Projects 기능을 활용하여 작업 현황을 시각적으로 관리하였습니다.

--- 

## 6. 페이지별 기능

### [메인 화면 / 일기 화면]
- 해시태그 및 내용으로 작성한 일기를 확인합니다.
- 일기 확인을 통해 감정 분석의 결과를 확인할 수 있습니다.

<br>

| 메인 화면 |
|----------|
|<img src="https://github.com/user-attachments/assets/1ee0c4ef-2d30-445f-9a65-b3a7e261b855">|

<br>

### [감정분석 / 통계 및 피드백]
- 작성된 일기는 Llama를 통해 분석됩니다.
- 6가지 감정으로 통계를 제공하고, 그에 맞는 피드백을 함께 제공합니다.
- 감정 분류는 총 6가지(당황, 기쁨, 분노, 불안, 슬픔, 상처)으로 구성됩니다.

<br>

| 통계 및 피드백 화면 |
|----------|
|<img src="https://github.com/user-attachments/assets/9e2bbef1-eba4-4d41-bd23-bfad81c88127">|

<br>

### [일기 작성]
- 음성 및 텍스트 타이핑 원하는 방식으로 일기를 작성할 수 있습니다.
- 하루를 대표하는 사진 1장을 입력할 수 있습니다.
- 일기의 분위기에 맞춰 원하는 배경색을 선택할 수 있습니다.

<br>

| 일기 작성 화면 |
|----------|
|<img src="https://github.com/user-attachments/assets/2d359999-63c0-4a60-b8a3-e0ba6ea5ba2a">|

<br>

### [캘린더]
- 캘린더에서 감정을 한 눈에 확인할 수 있습니다.
- 원하는 날짜를 선택하면, 그 날의 하루를 되돌아 볼 수 있습니다.

<br>

| 캘린더 화면 |
|----------|
|<img src="https://github.com/user-attachments/assets/b5343058-8db4-447b-899a-db8854326fef">|

<br>

### [Google Drive에 백업]
- 온디바이스의 이점을 살리고 사용자의 프라이버시 보장을 위해, 백업을 선택적으로 제공합니다.
- 네트워크 연결 후 사용자가 동기화를 활성화한다면, Google로 로그인 후 Google Drive에 백업합니다.

<br>

| 백업 화면 |
|----------|
|<img src="https://github.com/user-attachments/assets/ed9289ae-bd84-4eb4-bf9d-322f8fc39a1e">|

--- 

## 7. 프로젝트 후기

### 🍊 서연수

이번 프로젝트를 통해 UI/UX 기획부터 실제 구현까지 전 과정을 경험해볼 수 있었고, 특히 Google Drive API와 같은 외부 연동 기술을 적용해보며 사용자 편의성과 데이터 보안 측면을 동시에 고려할 수 있었습니다. 디자인을 코드로 구현하는 과정에서 발생한 충돌을 해결하며, 협업 과정에서의 소통의 중요성을 크게 느꼈습니다.

<br>

### 👻 성시우

로컬 데이터베이스(SQLite) 설계 및 구조화에 집중하면서 실제 사용자 데이터를 안정적으로 처리하는 구조를 고민할 수 있었던 경험이었습니다. 단순한 기능 구현이 아닌, 유지보수 가능한 구조로 만드는 데 집중했고, AI 기능과의 연동 테스트에서도 여러 시행착오를 통해 디버깅 역량과 문제 해결력을 키울 수 있었습니다.

<br>

### 🐬 박영빈

UI 동작을 동적으로 연결하고, 사용자 경험을 매끄럽게 만들기 위해 Activity 간 데이터 흐름과 상태 관리에 많은 고민을 했습니다. 또한 AI 모델을 실제 앱에 적용하며 모델 경량화, 리소스 관리 등 현실적인 제약 조건 속에서 모바일 최적화의 중요성을 체감했습니다. 결과적으로 앱 완성도를 높이는 데 큰 보람을 느꼈습니다.

<br>

### 😎 유두연

AI 테스트와 LLM 모듈 연동 과정에서 모델 로딩, 추론 속도, 메모리 이슈 등 온디바이스 환경 특유의 문제들을 직접 다뤄본 것이 큰 자산이 되었습니다. 초기에는 생소했던 JNI와 TFLite 모델 구조를 점차 익혀가며 실제 서비스에 적합한 형태로 연결하는 경험을 쌓을 수 있었습니다. 프로젝트를 통해 기술을 제품에 녹여내는 전체 흐름을 실무처럼 경험할 수 있어 매우 의미 있었습니다.

