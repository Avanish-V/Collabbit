# Implement Daily Aura Stats View

The goal is to allow users to view their daily aura stats (points, level, progress) by clicking on the aura section in their profile.

## User Review Required

> [!IMPORTANT]
> The "daily" stats will currently focus on total points, current level, and progress to the next level, as the current `AuraInfoResponse` does not provide a daily breakdown. If a "today's points" feature is strictly required, we may need to fetch transactions for the current day.

## Proposed Changes

### User Profile Component

#### [MODIFY] [AuraStreakCard.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/UserProfile/ui/Components/AuraStreakCard.kt)
- Add an `onClick: () -> Unit` parameter.
- Make the root `Column` clickable.

#### [MODIFY] [RedesignedProfileComponents.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/ui/UIComponents/RedesignedProfileComponents.kt)
- Update `RedesignedProfileHeader` to accept `onAuraClick: () -> Unit`.
- Pass `onAuraClick` to `AuraStreakCard`.

#### [NEW] [AuraStatsDialog.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/UserProfile/ui/Components/AuraStatsDialog.kt)
- Create a new dialog to display detailed aura statistics.
- Features:
    - Current Level with badge.
    - Total Aura Points.
    - Progress bar to the next level.
    - Remaining points for next level.
    - "How to earn" info section.

### User Profile Screen

#### [MODIFY] [AppUserProfileScreen.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/UserProfile/ui/screens/ProfileMain/AppUserProfileScreen.kt)
- Add state to track if `AuraStatsDialog` should be shown.
- Pass the click handler to `RedesignedProfileHeader`.
- Render `AuraStatsDialog` when the state is active.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to the Profile screen.
- Click on the Aura points section.
- Verify that the `AuraStatsDialog` appears with correct data.
- Verify that the progress bar correctly reflects the progress to the next level.
