# StudyNest - Academic Book, Chapter & Study Organizer

StudyNest is an academic study companion and organizer designed to help students and lifelong learners manage daily books, curriculum chapters, and detailed notes. It features chapter-by-chapter progress tracking, daily study goal reminders, exam countdowns, an interactive analytics dashboard, and seamless multi-device cloud synchronization with offline persistence.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following key decisions have been confirmed and incorporated into this architecture:

- **Application Name**: **StudyNest** (with matching Android app title and platform metadata).
- **Cloud & Offline Sync**: **Firebase Firestore with offline caching** and **Google Sign-In via Jetpack Credential Manager**. Data is cached locally on device for zero-latency offline use during commutes and automatically synced with Firestore whenever connectivity is available.
- **Visual Aesthetic & Theme**: **Modern Minimalist Slate** featuring crisp slate and dark charcoal surfaces, high-contrast typography, and vivid color-coded category badges (e.g., Mathematics, Computer Science, Literature, Sciences). Full toggleable Dark & Light modes with system default support.
- **Tablet & Large Screen Structure**: **Adaptive dual-pane layout** utilizing a permanent Navigation Rail and side-by-side list/detail hierarchy for tablets and foldables, while maintaining a smooth bottom navigation experience on phones.

---

## 1. Overview & Core Concept

- **What It Does**: StudyNest allows learners to catalog books and study materials, divide them into structured chapters, capture rich study notes, mark chapter completion status, track daily study time, schedule automated deadline and exam reminders, and visualize learning milestones through an interactive dashboard.
- **Target Audience / Persona**: Students, researchers, and self-taught professionals who balance multiple subjects, textbooks, and upcoming exams and need a focused, distraction-free environment on both mobile phones and tablets.
- **Key Value**: Replaces scattered notebooks and physical bookmarks with a single organized system that keeps study materials, reading progress, and deadline reminders synchronized across all devices, even offline.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Authentication & Welcome Flow**:
   - Clean welcome landing with Google Sign-In (`CredentialManager`) and instant offline session resumption.
2. **Dashboard & Analytics**:
   - High-level glance showing: Total Books in progress, Completed Chapters %, Current Daily Study Streak (days), and Today's Study Time vs. Target Goal.
   - Interactive bar chart showing weekly study minutes and a breakdown by color-coded subject categories.
   - Quick-action banner for "Upcoming Exam / Deadline" countdowns with urgent tags (<3 days, this week, later).
3. **Books & Chapters Management**:
   - Category filtering pills (All, CS, Math, Biology, Law, etc.) with custom color badges.
   - Book card showing cover accent, author, chapter progress ring (`7/12 chapters done - 58%`), and last studied date.
   - Detailed Book View: List of chapters with statuses (*Not Started*, *In Progress*, *Completed*), page count, estimated reading time, and direct action to "Add / View Notes".
4. **Chapter Notes & Study Session**:
   - Rich Markdown-friendly note taker with quick formatting tags (Key Definition, Formula, Question, Summary).
   - In-app Study Timer / Stopwatch with Pomodoro option (25m / 5m) that logs completed study minutes directly into the chapter and dashboard stats.
5. **Exam & Deadline Reminders**:
   - Dedicated deadline manager: add exam title, linked book/chapter, due date & time, and notification threshold (1 day before, morning of, etc.).
   - Android AlarmManager / WorkManager notification integration with local notifications for upcoming deadlines.
6. **Adaptive Form Factor (Phone vs. Tablet)**:
   - **Phone**: Bottom navigation bar (`Dashboard`, `Books`, `Deadlines`, `Settings`) with fluid single-column drill-down views.
   - **Tablet / Large Screen**: Left Navigation Rail + Master-Detail split pane (Books catalog on the left 40%, selected Chapter details and active Notes editor on the right 60%).

### Visual Identity & Theme Tokens

```
Palette Mode: Modern Slate & Vibrant Subject Accents
- Background Light:  #F8FAFC (Slate 50)
- Surface Light:     #FFFFFF (Pure White)
- Surface Variant:   #F1F5F9 (Slate 100)
- Primary Accent:    #4F46E5 (Indigo 600)
- Secondary Accent:  #06B6D4 (Cyan 500)
- Background Dark:   #0F172A (Slate 900)
- Surface Dark:      #1E293B (Slate 800)
- Surface Variant Dark: #334155 (Slate 700)
- Category Colors:   Emerald (#10B981), Amber (#F59E0B), Rose (#F43F5E), Violet (#8B5CF6), Sky (#0284C7)
```

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Firebase Firestore with Offline Persistence**:
  - *Chosen Approach*: Configure Firebase Firestore with local disk cache enabled (`PersistentCacheIndexManager` / standard offline cache). All read queries observe real-time snapshot listeners with zero-latency local cache fallback.
  - *Why*: Satisfies the requirement for seamless offline access during transit while syncing changes to the cloud when online.
  - *Alternatives Considered*: Pure local Room DB lacks real-time multi-device cloud synchronization; pure REST API fails completely during offline commutes without complex custom synchronization queues.
- **Decision 2: Jetpack Credential Manager for Google Sign-In**:
  - *Chosen Approach*: Official Android Credential Manager using `GetSignInWithGoogleOption` with Web Client ID from `R.string.default_web_client_id`, accompanied by automatic silent credential sign-in on app launch.
  - *Why*: Secure, zero-friction sign-in adhering to modern Android standards without legacy Auth dialogs.
- **Decision 3: Android Notification Engine**:
  - *Chosen Approach*: WorkManager / NotificationManager channel (`study_reminders_channel`) for scheduled daily study goal alerts and exam deadline alerts.
  - *Why*: Reliable background execution respecting Android battery optimization and edge-to-edge guidelines.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                              UI LAYER                                  │
│  ┌────────────────────────┐  ┌──────────────────────────────────────┐  │
│  │    Phone Navigation    │  │        Tablet Adaptive Rail          │  │
│  │ (M3 BottomBar / Stack) │  │  (Side Rail + Dual Pane Master/Detail│  │
│  └───────────┬────────────┘  └──────────────────┬───────────────────┘  │
│              ▼                                  ▼                      │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  Screens: DashboardScreen | BookListScreen | BookDetailScreen    │  │
│  │           ChapterNotesScreen | DeadlineScreen | SettingsScreen   │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
└─────────────────────────────────────┼──────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼──────────────────────────────────┐
│                           VIEWMODEL LAYER                              │
│  ┌────────────────────────┐  ┌──────────────────────────────────────┐  │
│  │  AuthViewModel         │  │  StudyViewModel (Books/Chapters/     │  │
│  │  (CredentialManager)   │  │   Notes/Stats/Deadlines)             │  │
│  └───────────┬────────────┘  └──────────────────┬───────────────────┘  │
└──────────────┼──────────────────────────────────┼──────────────────────┘
               │                                  │
┌──────────────▼──────────────────────────────────▼──────────────────────┐
│                          DATA & SYNC LAYER                             │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ StudyRepository (Offline-enabled Firebase Firestore)             │  │
│  │ Collections:                                                     │  │
│  │  - /users/{userId}/books/{bookId}                                │  │
│  │  - /users/{userId}/books/{bookId}/chapters/{chapterId}           │  │
│  │  - /users/{userId}/notes/{noteId}                                │  │
│  │  - /users/{userId}/deadlines/{deadlineId}                        │  │
│  │  - /users/{userId}/studyLogs/{logId}                             │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
│                                     │                                  │
│              ┌──────────────────────┴──────────────────────┐           │
│              ▼                                             ▼           │
│  ┌───────────────────────┐                     ┌─────────────────────┐ │
│  │ Local Firestore Cache │                     │ Cloud Firestore DB  │ │
│  │  (Zero-latency local) │                     │ (Multi-device sync) │ │
│  └───────────────────────┘                     └─────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Models

1. **BookEntity**:
   - `id: String`, `userId: String`, `title: String`, `author: String`, `category: String`, `colorTag: String`, `totalChapters: Int`, `completedChapters: Int`, `createdAt: Timestamp?`, `updatedAt: Timestamp?`
2. **ChapterEntity**:
   - `id: String`, `bookId: String`, `chapterNumber: Int`, `title: String`, `isCompleted: Boolean`, `pageCount: Int`, `estimatedMinutes: Int`, `createdAt: Timestamp?`
3. **NoteEntity**:
   - `id: String`, `bookId: String`, `chapterId: String`, `title: String`, `content: String`, `tag: String`, `createdAt: Timestamp?`, `updatedAt: Timestamp?`
4. **DeadlineEntity**:
   - `id: String`, `title: String`, `bookTitle: String`, `dueDateEpochMs: Long`, `isCompleted: Boolean`, `urgency: String`, `createdAt: Timestamp?`
5. **StudyLogEntity**:
   - `id: String`, `dateStr: String`, `minutesStudied: Int`, `category: String`, `timestamp: Timestamp?`

### Interactive Component & State Mapping

- **Book Catalog & Filters**: Real-time category selector pills dynamically filter the books list with animated transition.
- **Chapter Toggle Checkbox**: Tapping chapter completion triggers an instant optimistic UI state update and atomically recalculates the parent book's `completedChapters` counter and progress percentage in Firestore.
- **Study Timer**: Built-in stopwatch modal with play/pause/reset; on "Save Session", saves minutes studied and updates daily goal progress on the dashboard.
- **Color-Coded Tags**: Supports preset academic colors (Emerald, Amber, Rose, Violet, Cyan, Indigo) with color picker in the Add/Edit Book dialog.
- **Tablet Master-Detail Pane**: In landscape/tablet width (>600dp), selecting a book updates the right pane immediately without losing the scroll position of the book list.
