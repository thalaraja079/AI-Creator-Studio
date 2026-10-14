# AI Creator Studio - Updates & Enhancements Plan

This plan details the implementation of 4 focused updates to elevate the visual branding, UX flow, and ad monetization experience in AI Creator Studio: custom logo branding, vertical tool directory, fixed bottom banner ad above navigation, and robust rewarded ad credit replenishment.

## User Review & Critical Decisions

> [!IMPORTANT]
> The existing architecture (OkHttp, org.json, SharedPreferences) will remain completely intact. Google Mobile Ads SDK (`play-services-ads`) will be integrated with standard official test IDs, wrapped in crash-proof Compose containers with visual emulator fallbacks.

- **Confirmed Decision (Tools Tab Layout)**: The Tools tab will feature a full-width vertical card list for all 8 tools, each displaying its colored icon badge, title, subtitle, and trailing navigation chevron. Tapping opens the dedicated tool generator screen with a prominent "← Back to All Tools" button and `BackHandler`.
- **Confirmed Choice (Fixed Banner Ad)**: A persistent 50.dp container directly above the bottom navigation bar will host the Google Mobile Ads test banner (`ca-app-pub-3940256099942544/6300978111`) with a sleek dark fallback UI for web/emulator preview resilience.

---

## 1. Overview of Proposed Changes

1. **Custom App Logo & Launcher Icons**:
   - Download the user's custom brand logo from Google Drive.
   - Generate all raster densities (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) for `ic_launcher.png` and circular `ic_launcher_round.png`.
   - Update `ic_launcher_foreground.xml` and `ic_launcher_background.xml` to eliminate any clipping artifacts.
   - Add a circular logo badge in the Home top header next to "AI Creator Studio".

2. **Vertical Tool Directory (`ToolsTab.kt`)**:
   - Replace top horizontal chip bar with a master-detail flow:
     - **Tool List Screen**: Vertical `LazyColumn` presenting all 8 tools as clean, full-width cards with vibrant icon badges and navigation chevrons.
     - **Tool Detail Screen**: Opened upon card click (and from Home Popular Tools). Includes a prominent "← Back to All Tools" back button, preset chips, custom input, and specialized copy bars.

3. **Fixed Bottom Banner Ad**:
   - Add `com.google.android.gms:play-services-ads` to `app/build.gradle.kts`.
   - Add test AdMob `APPLICATION_ID` (`ca-app-pub-3940256099942544~3347511713`) in `AndroidManifest.xml`.
   - Implement `FixedBottomBannerAd` composable anchored above `NavigationBar` in `MainScreen.kt` using test unit ID `ca-app-pub-3940256099942544/6300978111` with a sleek 50.dp emulator-safe placeholder.

4. **Watch Ad to Earn +1 Credit**:
   - Maintain strict conditional rendering `if (state.remainingQuota < state.dailyFreeQuota)` on Home.
   - Refine rewarded ad trigger to smoothly award +1 credit upon completion and update `CreatorPreferences`.

---

## 2. Technical Architecture & Component Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MainScreen (Scaffold)                           │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                      Active Tab Content                        │   │
│   │  [HomeTab]        [ToolsTab]        [HistoryTab]  [SettingsTab]│   │
│   │   • Logo Badge     • Master: 8 Vertical Cards                  │   │
│   │   • Quota Card     • Detail: "← Back" + Generator Screen       │   │
│   │   • Watch Ad Card    - YouTube SEO: Dedicated Copy Buttons     │   │
│   │   • Quick Studio     - Translator: Target Language selector    │   │
│   └────────────────────────────────────────────────────────────────┘   │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │  Fixed Bottom Banner Ad (Google Mobile Ads / 50.dp Container)  │   │
│   │  Test Ad Unit: ca-app-pub-3940256099942544/6300978111          │   │
│   └────────────────────────────────────────────────────────────────┘   │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │  Bottom Navigation Bar (Home | Tools | History | Settings)     │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Verification Plan

1. **Gradle Build Verification**: Execute `compile_applet` to confirm clean dependency resolution and zero compilation errors.
2. **Branding & Icon Verification**:
   - Inspect Home top header to ensure the downloaded brand logo renders crisply.
   - Confirm adaptive launcher icons generated across all densities without clipping.
3. **Tools Tab Navigation Verification**:
   - Verify opening Tools tab shows the 8 tools in a vertical column with chevrons.
   - Tap a tool -> verify it opens the detail generator screen with "← Back to All Tools".
   - Tap "← Back to All Tools" (or hardware back) -> verify return to the vertical list.
   - Tap a tool from Home Popular Tools -> verify it opens directly to that tool's detail screen.
4. **AdMob Banner & Rewarded Flow Verification**:
   - Verify 50.dp fixed banner ad remains visible above the bottom navigation bar across all 4 tabs.
   - Verify "Watch Ad (+1 Credit)" appears on Home when credits < 5/5, plays the simulation dialog, and awards +1 credit cleanly.
