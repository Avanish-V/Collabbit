# Walkthrough - Joined Societies List

Implemented the joined societies list within the Societies tab of the `HomeScreen`.

## Changes

### [HomeViewModel](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Screens/Home/HomeViewModel.kt)
- Added `isRefreshing` state to track pull-to-refresh status.
- Added `refresh()` function that triggers `repository.syncCommunities()` to fetch the latest society data from Firestore.

### [HomeScreen](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Screens/Home/HomeScreen.kt)
- **State Collection**: Now collects `joinedCommunities` and `isRefreshing` from `HomeViewModel`.
- **Societies Tab UI**:
    - Replaced the placeholder `LazyColumn` with a production-ready implementation.
    - Integrated `RefreshBox` for pull-to-refresh functionality.
    - Added `JoinedCommunityCard` composable for consistent society display.
    - Integrated `CampusEmptyState` to handle cases where no societies are joined.
- **Navigation**:
    - The "Add" icon next to the `TabRow` now correctly navigates to the `SocietyHub`.
    - `CampusEmptyState`'s "Update" button also leads to `SocietyHub`.

## Verification

### Automated Tests
- Verified the build using `gradle build app:assembleDebug`, which passed successfully.

### Manual Verification
- Inspected the UI components (`JoinedCommunityCard`) to ensure they match the application's design system (Material 3, custom icons, typography).
- Verified that the `joinedCommunities` flow correctly populates the `LazyColumn`.
