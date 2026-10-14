# AI Creator Studio - Authentication & Google Play Compliance Plan

This plan introduces an Authentication gate (Login, Sign Up, and Forgot Password) and a Google Play-compliant Account & Profile management section in Settings, while maintaining the dark purple/blue theme, local SharedPreferences persistence, vertical Tools list, and fixed bottom Banner Ad.

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Gated Access**: The application will require Login or Sign Up before granting access to the creator workspace.
> - **Local Data Durability**: Sessions, user profiles, and passwords/credentials persist via `SharedPreferences`.
> - **Google Play Policy Compliance**: Full Account Deletion and an in-app Privacy Policy modal with complete Data Safety disclosures are provided.

- **Confirmed Decision (Auth Gate)**: Strict login/sign-up required; users will not enter the main workspace until authenticated.
- **Confirmed Decision (Privacy Policy)**: A complete, scrollable in-app compliance modal covering Google Play Data Safety, API key privacy, and user deletion rights.
- **Dynamic Personalization**: User's actual name entered during sign-up or edited in Settings dynamically updates the Home top header ("Welcome back, [Name] 👋").

---

## 1. Overview of Proposed Changes

1. **Authentication Flow (`AuthScreen.kt`)**:
   - Sleek dark purple/blue branding with `ic_creator_logo`.
   - **Login Screen**:
     - Email & Password fields with password visibility toggle.
     - "Forgot Password?" button opening a reset email dialog.
     - Primary "Log In" button and "Continue with Google" option.
     - Bottom toggle switching to Sign Up.
   - **Sign Up Screen**:
     - Full Name, Email, Password, and Confirm Password fields with validation.
     - Saves session and profile to `CreatorPreferences`.
   - **Forgot Password Dialog**:
     - User enters email -> sends reset instructions simulation with success banner.

2. **Personalized Home Header**:
   - Replaces static greeting with "Welcome back, {userName} 👋".
   - Updates immediately if the user renames their profile in Settings.

3. **Google Play Compliant Profile & Account Management (`SettingsTab.kt`)**:
   - Placed at the top of the Settings tab before Gemini API configurations.
   - **Profile Card**: Displays user avatar initials, full name, and registered email.
   - **Edit Name Dialog**: Allows changing display name with instant state update.
   - **In-App Privacy Policy & Terms Modal**: Full policy explaining local key storage, direct Google Gemini REST transmission, ad tracking disclosures, and user rights.
   - **Logout**: Safe confirmation dialog that clears the session and navigates back to the Login screen.
   - **Delete Account (Google Play Policy)**: Red-accented button with strict confirmation dialog that permanently wipes user credentials, generation history, daily quota, and preferences, redirecting to Login.

---

## 2. Technical Architecture & Component Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (Compose Root)                     │
│                                                                        │
│   if (!state.isLoggedIn) ─────────────┐                                │
│                                       ▼                                │
│                       ┌──────────────────────────────┐                 │
│                       │          AuthScreen          │                 │
│                       │   • Login Mode               │                 │
│                       │   • Sign Up Mode             │                 │
│                       │   • Forgot Password Dialog   │                 │
│                       │   • Google Sign-In Mock UI   │                 │
│                       └───────────────┬──────────────┘                 │
│                                       │ (Successful Auth)              │
│   if (state.isLoggedIn)  ◄────────────┘                                │
│                                                                        │
│                       ┌──────────────────────────────┐                 │
│                       │          MainScreen          │                 │
│                       │  • HomeTab (Personalized)    │                 │
│                       │  • ToolsTab (Vertical List)  │                 │
│                       │  • HistoryTab                │                 │
│                       │  • SettingsTab:              │                 │
│                       │     - Profile & Edit Name    │                 │
│                       │     - Privacy Policy Sheet   │                 │
│                       │     - Log Out Action         │                 │
│                       │     - Delete Account Action  │                 │
│                       │  • Fixed Bottom Banner Ad    │                 │
│                       │  • Bottom Navigation Bar     │                 │
│                       └──────────────────────────────┘                 │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Verification Plan

1. **Gradle Build Verification**: Execute `compile_applet` to confirm clean compilation.
2. **Authentication Flow Verification**:
   - Test Sign Up: Enter name, email, password -> verify navigation to Home and dynamic greeting with entered name.
   - Test Forgot Password: Click "Forgot Password?" -> enter email -> verify confirmation banner.
   - Test Logout: Open Settings -> tap Log Out -> confirm -> verify return to Login screen.
   - Test Login: Enter saved credentials -> verify instant login.
3. **Google Play Policy Verification**:
   - Open Settings -> tap "Privacy Policy & Terms" -> verify scrollable in-app dialog appears with complete data disclosures.
   - Edit Name -> change name -> verify immediate update in Settings and on Home header.
   - Delete Account -> tap "Delete Account & Data" -> confirm in dialog -> verify all history, quota, and credentials are wiped, returning to Login screen.
4. **Unit Tests**: Run `:app:testDebugUnitTest` to ensure all tests pass.
