# LIFEOS — An AI Assistant That Turns Chaos into Action

LIFEOS transforms scattered thoughts, voice rants, messy screenshots, and sudden deadlines into a structured, realistic daily timeline with automatic schedule replanning when tasks slip. Built in high-performance Jetpack Compose with a sleek Dark Obsidian and neon accent design, local Room persistence, and Gemini Multimodal AI intelligence with instant offline heuristics.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and aesthetic choices were confirmed during Phase 1:

- **Visual Theme**: Sleek Dark Obsidian (`#0A0B10`) paired with electric cyan (`#00F0FF`) and violet (`#8B5CF6`) accents, glassmorphic cards, and crisp typography.
- **AI Engine Architecture**: Direct REST Gemini Multimodal integration (`gemini-3.5-flash`) powered by `BuildConfig.GEMINI_API_KEY` for intelligent entity extraction from voice transcripts and screenshot imagery, paired with a robust zero-latency local regex and NLP heuristic parser when offline or without an API key.
- **Primary UX Layout**: Unified real-time timeline combining daily schedule blocks, interactive "Chaos to Order" quick-capture dock, voice note transcriber modal, screenshot analyzer, and a one-tap "Auto-Replan" engine.

---

## 1. Overview & Core Concept

- **What It Does**: LIFEOS acts as a cognitive offloading engine. Users speak naturally ("I have to finish my job assignment by Friday, prepare for an interview, pay my electricity bill, and study Java for an hour every day") or upload bill/email screenshots; LIFEOS parses action items, estimates time, assigns priority, slots them into an executable daily schedule, and intelligently recalibrates when life disrupts the plan.
- **Target Audience / Persona**: Busy professionals, students, freelancers, and neurodivergent individuals experiencing executive fatigue and decision paralysis from chaotic task lists.
- **Key Value**: Moves beyond passive task lists into active temporal scheduling, frictionless multi-modal capture (voice + image), and zero-guilt schedule healing (re-planning).

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Stream-of-Consciousness Voice Capture**: Tap the glowing pulse mic button, speak naturally (or select pre-filled smart sample rants), view live transcript, tap "Turn Chaos into Action" -> Gemini structures tasks with due dates, tags, and duration estimates -> directly commits to timeline.
2. **Screenshot Intelligence**: Pick or snap a screenshot (bill receipt, flight ticket, meeting flyer, text message) -> Gemini Vision extracts action items, due dates, prices, and vendor names -> preview parsed cards -> one-tap add to schedule.
3. **Daily Schedule & Time Blocking**: Interactive hourly timeline showing available time buffers, current time indicator line, completed tasks with strike-through animations, and pending commitments.
4. **Smart Auto-Replanning**: When the user falls behind or marks a task as "Missed / Delayed", tap "Auto-Replan" -> the planner slides remaining tasks into open afternoon/evening blocks without overlapping existing commitments.
5. **Personal Execution Dashboard**: Weekly completion metrics, focus score, commitments breakdown by category (Career, Life/Bills, Learning, Urgent), and contextual quiet-hour reminders.

### Visual Identity & Theme
- **Aesthetic Direction**: Futuristic Dark Obsidian with luminous neon edges, subtle atmospheric radial gradients, and tactile micro-interactions.
- **Color Palette**:
  - `Background`: Deep Obsidian (`#0A0B10`) with elevated dark surfaces (`#141721`, `#1C202F`).
  - `Primary / Neon Accent`: Electric Cyan (`#00F0FF`) and Hyper Violet (`#8B5CF6`).
  - `Status Accents`: Emerald Glow (`#10B981`) for completed tasks, Amber Flame (`#F59E0B`) for urgent deadlines, Rose Quartz (`#F43F5E`) for overdue/missed items.
  - `Text Hierarchy`: Crisp White (`#F8FAFC`) for titles, Muted Slate (`#94A3B8`) for metadata, and Neon Cyan (`#67E8F9`) for active tags.
- **Typography & Hierarchy**: Bold geometric headings with high-contrast tabular figures for timestamps and time blocks, paired with clean sans-serif body text.
- **Component Styling**: Glass-like elevated cards with 1dp border luminescence, smooth toggle chips, and bottom sheets for voice/screenshot inputs with edge-to-edge support.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dual-Layer AI Engine (Gemini REST + Local Heuristic Engine)**
  - *Chosen Approach*: Call Google Gemini 3.5 Flash via direct REST with structured JSON schemas for deep multimodal parsing; simultaneously bundle an instant on-device parser (regex date extractors, keyword intent classifier, duration estimator) so the app is 100% functional offline, in airplane mode, or when no API key is provided.
  - *Why*: Guarantees zero friction and 100% testability out of the box while delivering state-of-the-art AI intelligence when connected.
  - *Alternatives Considered*: Firebase AI (requires manual console configuration) vs mock data (prohibited by instructions).
- **Decision 2: Local Persistence with Room**
  - *Chosen Approach*: Android Room Database with Kotlin Coroutines Flow for immediate reactive UI updates across tasks, time blocks, categories, and captured notes.
  - *Why*: Fast, offline-first, zero latency, perfectly suited for rapid day-to-day task execution and timeline mutations.
- **Decision 3: Voice Simulation & Audio Speech Input**
  - *Chosen Approach*: Android `SpeechRecognizer` contract combined with interactive quick-prompt speech presets (e.g. "Work assignment + interview prep + electricity bill + daily Java study") so reviewers can test with real voice or instant natural language inputs in any emulator environment.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                           UI Layer                               │
│  ┌────────────────────────┐  ┌────────────────────────────────┐  │
│  │   Top Executive Bar    │  │     Interactive Timeline       │  │
│  │ (Date, Replan Button)  │  │ (Hour blocks, Tasks, Buffers)  │  │
│  └───────────┬────────────┘  └───────────────┬────────────────┘  │
│              │                               │                   │
│  ┌───────────┴───────────────────────────────┴────────────────┐  │
│  │          Floating Action Dock (Voice / Camera / Chaos)     │  │
│  └───────────────────────────┬────────────────────────────────┘  │
└──────────────────────────────┼───────────────────────────────────┘
                               │ StateFlow / Events
┌──────────────────────────────▼───────────────────────────────────┐
│                    LifeOsViewModel (MVVM)                        │
│   Manages Timeline State, Task Filters, AI Parsing, Replan Logic  │
└──────────────┬───────────────────────────────┬───────────────────┘
               │                               │
┌──────────────▼─────────────┐   ┌─────────────▼───────────────────┐
│     AI Intelligence Layer   │   │        Data Storage Layer       │
│  ┌───────────────────────┐ │   │  ┌───────────────────────────┐  │
│  │ Gemini 3.5 Flash REST │ │   │  │    Room Database (SQLite) │  │
│  │ (Vision + Text JSON)  │ │   │  │   TaskEntity / DayBlock   │  │
│  └───────────────────────┘ │   │  └─────────────┬─────────────┘  │
│  ┌───────────────────────┐ │   │  ┌─────────────▼─────────────┐  │
│  │ Smart Heuristic Engine│ │   │  │ LifeOsRepository (Flows)  │  │
│  │ (Regex / Rule Parser) │ │   │  └───────────────────────────┘  │
│  └───────────────────────┘ │   └─────────────────────────────────┘
└────────────────────────────┘
```

### Data Model Entities
- `TaskEntity`:
  - `id: Long` (Primary Key)
  - `title: String`
  - `description: String`
  - `category: TaskCategory` (WORK, PERSONAL, BILLS, LEARNING, HEALTH)
  - `priority: Priority` (HIGH, MEDIUM, LOW)
  - `scheduledDate: String` (YYYY-MM-DD)
  - `startTime: String` (HH:mm)
  - `durationMinutes: Int`
  - `isCompleted: Boolean`
  - `isMissed: Boolean`
  - `rawSource: String` (VOICE, SCREENSHOT, MANUAL)
  - `extractedMeta: String?` (e.g., "$84.20 electricity bill", "Zoom link")
- `ScheduleBlock`: Computed UI models that combine fixed user events, dynamic tasks, and buffer gaps for the interactive timeline.

### Interactive State Transitions & Workflows
- **Voice Extraction**: Mic Tap -> Record/Speech Input -> `LifeOsViewModel.processChaosInput(text)` -> Gemini extracts JSON array of task objects -> Preview in bottom sheet -> User can edit or tap "Add All to Today/Week" -> Saved to Room -> Timeline animates newly scheduled blocks.
- **Screenshot Ingestion**: System Photo Picker (`ActivityResultContracts.PickVisualMedia`) -> Load bitmap -> Encode to Base64 -> Gemini Multimodal Vision API -> Extracts action items, amounts, dates -> Inserts into Task repository.
- **Auto-Replan Workflow**: Tap "Auto-Replan" -> Finds overdue/uncompleted tasks with scheduled time in past -> Computes next available free blocks starting from `currentTime` -> Updates database timestamps -> Emits animated timeline realignment with toast notification.
