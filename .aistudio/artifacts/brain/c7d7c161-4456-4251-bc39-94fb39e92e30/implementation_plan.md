# AI Creator Studio - Modern Creator Toolkit

AI Creator Studio is an all-in-one Android application tailored for content creators, video producers, marketers, and storytellers. It bundles 8 specialized AI generation tools, direct quick-generation, a daily quota system with rewarded credit replenishment, and persistent local history and settings.

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural direction follows a crash-resilient, lightweight engine: no Firebase, Room, KSP, or Retrofit. Direct OkHttp with `org.json` is used for Gemini REST communication, and Android `SharedPreferences` manages quota and history.

- **Confirmed Decision (Formatting Style)**: AI responses will use a structured rich layout with clear bold section headings, bulleted breakdowns, and copy-ready tags.
- **Confirmed Choice (Ad Reward Card Visibility)**: The "+1 Credit" card strictly renders when `remainingQuota < dailyFreeQuota`, giving creators a quick simulated rewarded ad dialog to replenish credits.
- **Confirmed Choice (Sample Prompts)**: Interactive 1-tap example chips will be included on each tool screen to make immediate testing effortless.

---

## 1. Overview & Core Concept

- **What It Does**: Empowers creators to generate YouTube viral titles & SEO tags, long-form video scripts, cinematic image generation prompts, viral Reels/Shorts hooks, e-commerce product ad copy, multilingual polished translations, brand sponsorship pitches, and narrative stories.
- **Target Audience**: YouTubers, Instagram & TikTok creators, e-commerce shop owners, copywriters, and creative storytellers.
- **Key Value**: Delivers hyper-tailored prompts to Gemini models without setup friction, tracks daily usage quotas, and allows instantaneous copying of specialized segments (such as separate Title, Script, or Tag copy actions).

---

## 2. User Experience & Visual Design

### Navigation & Layout Structure
A modern Material 3 `Scaffold` with edge-to-edge system insets and a 4-item bottom navigation bar:
1. **Home**: Header badge, Daily Quota status card, conditional "+1 Credit Ad" card, Direct Quick AI Studio Generator, and Popular Tools grid.
2. **Tools**: Category selector and detailed cards for all 8 creator tools with custom inputs, quick presets, generation states, and tailored multi-copy action bars.
3. **History**: Filterable reverse-chronological list of saved generations with individual copy, delete, and clear-all actions.
4. **Settings**: Custom Gemini API Key entry, Model selector (`gemini-2.5-flash` default), API Key verification tester, and developer quota reset tools.

### Visual Identity & Theme
- **Theme Direction**: Deep creator dark theme with obsidian background (`#0B0F19`), surface layers (`#131B2E` and `#1E293B`), and vibrant violet-to-blue gradient accents (`0xFF8B5CF6` to `0xFF3B82F6`).
- **Card Geometry**: Consistent `20.dp` rounded corners across all cards, dialogs, and text fields.
- **Accents**: Distinct vibrant signature color accents for each tool (Amber for SEO, Red for Scripts, Purple for Image Prompts, Pink for Shorts Hooks, Emerald for Ad Copy, Blue for Translator, Cyan for Sponsorships, Orange for Stories).
- **Motion & Feedback**: Animated visibility transitions for generated output cards, progress indicators during generation and rewarded ad simulation, and clear snackbar notifications for copy actions.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Direct OkHttp & org.json over Retrofit/SDKs**
  - *Chosen Approach*: Lightweight `GeminiApiClient` using OkHttp with 60-second timeouts and native `org.json` parsing. Model names are automatically cleaned with `removePrefix("models/")` to prevent 404 errors.
  - *Why*: Eliminates boilerplate, avoids KSP/Room annotation processing bugs, and guarantees 100% build stability on first compile.
- **Decision 2: SharedPreferences Local Store for Persistence**
  - *Chosen Approach*: `CreatorPreferences` repository managing daily quota, date-based midnight resets, generation history entries serialized as JSON arrays, and custom API key overrides.
  - *Why*: Immediate startup performance, zero SQL migrations, and total offline resilience.
- **Decision 3: Specialized Multi-Action Copy Buttons**
  - *Chosen Approach*: On tools like YouTube SEO Tags and Reels/Shorts Hooks, provide both "Copy All" and granular segment copying (e.g., "Copy Titles", "Copy Description", "Copy Tags & Hashtags").
  - *Why*: Creators need specific segments directly in their YouTube Studio or Instagram paste buffers without tedious manual text selection.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (Compose Edge-to-Edge)             │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                     MainScreen (BottomBar)                     │   │
│   │                                                                │   │
│   │  [HomeTab]        [ToolsTab]        [HistoryTab]  [SettingsTab]│   │
│   │   • Quota Card     • 8 Specialized   • Saved List  • Custom Key│   │
│   │   • Ad Card (+1)     Tool Sheets     • Copy/Delete • Model Pick│   │
│   │   • Quick Studio   • Multi-Copy      • Clear All   • Quota Dev │   │
│   │   • Tool Grid      • Sample Chips                  • Key Test  │   │
│   └────────────────────────────────┬───────────────────────────────┘   │
└────────────────────────────────────┼───────────────────────────────────┘
                                     │
                     ┌───────────────┴───────────────┐
                     ▼                               ▼
       ┌───────────────────────────┐   ┌───────────────────────────┐
       │     CreatorViewModel      │   │     CreatorPreferences    │
       │  • Quota State & Ad Flow  │   │  • SharedPreferences      │
       │  • Generation Execution   │   │  • Quota & Date Tracking  │
       │  • History State Store    │   │  • History (JSON Array)   │
       │  • Tool State Management  │   │  • Custom API Key Store   │
       └─────────────┬─────────────┘   └───────────────────────────┘
                     │
                     ▼
       ┌───────────────────────────┐
       │      GeminiApiClient      │
       │  • OkHttp Client (60s)    │
       │  • URL: generateContent   │
       │  • Clean Model Prefix     │
       │  • org.json Payload Parse │
       └───────────────────────────┘
```

### Data Entities & State Models
- `AiTool`: Enum of all 8 tools with titles, subtitles, icons, colors, prompts, and sample presets.
- `GenerationHistoryItem`: Unique ID, tool id, prompt, result text, formatted timestamp.
- `CreatorStudioState`: Quota counts, remaining quota, active tool, current input, generation loading status, error message, and ad watching status.
- `GeminiApiClient`: Pure Kotlin class executing `POST https://generativelanguage.googleapis.com/v1beta/models/{cleanModel}:generateContent?key={apiKey}` with standard error handling.

---

## 5. Verification Plan

1. **Gradle Build Verification**: Execute `compile_applet` to confirm zero compilation errors.
2. **Quota & Ad Testing**:
   - Check default 5/5 credits display.
   - Use direct generator to consume 1 credit -> verify remaining becomes 4/5.
   - Verify the "+1 Credit Ad" card immediately appears when `remainingQuota < 5`.
   - Tap "Watch Ad (+1 Credit)" -> verify ad simulation dialog triggers and increments quota back to 5.
   - Verify card disappears once quota hits 5/5.
3. **Tool Features & Copy Verification**:
   - Test YouTube SEO Tags tool generation and verify dedicated "Copy Titles", "Copy Description", and "Copy Tags" buttons work.
   - Test Language Selector in AI Translator & Polisher tool (default English).
4. **History & Settings Verification**:
   - Verify generated outputs appear immediately in History.
   - Test item deletion and "Clear All".
   - Test Settings API key saving and Quota Reset / Simulate Credit consumption actions.
