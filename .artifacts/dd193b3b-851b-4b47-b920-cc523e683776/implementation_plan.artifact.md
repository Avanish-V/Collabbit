# Add Tab Row to Home Screen

The goal is to add a tab row to the Home Screen with a "Feed" tab and a "+" icon tab that navigates to the Create Post screen.

## User Review Required

> [!IMPORTANT]
> The "+" icon is implemented as a tab in the `TabRow`. Clicking it will trigger navigation to the `CreatePost` screen rather than switching content within the current screen.

## Proposed Changes

### [Home Screen Component]

#### [MODIFY] [HomeScreen.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Screens/Home/HomeScreen.kt)

- Add state `selectedTabIndex` to manage the selected tab.
- Implement a `TabRow` with two tabs:
    - "Feed": Displays the current post feed.
    - "+": Navigates to the `CreatePost` route.
- Ensure proper styling using Material 3 `TabRow` and `Tab`.
- Add necessary imports for `TabRow`, `Tab`, and `Icons.Default.Add`.

## Verification Plan

### Manual Verification
1. Deploy the app to a device or emulator.
2. Navigate to the Home screen.
3. Verify that a tab row appears below the top app bar with "Feed" and a "+" icon.
4. Verify that the "Feed" tab is selected by default and shows the feed.
5. Tap the "+" icon and verify that it navigates to the Create Post screen.
