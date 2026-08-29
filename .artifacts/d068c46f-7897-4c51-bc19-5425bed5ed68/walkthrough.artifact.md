# Walkthrough - Collaboration Search and Filter Enhancement

I have successfully updated the collaboration explore screen to include a search feature and fixed the filter tab list.

## Changes Made

### Data Layer
- **[CollabRepository](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/Collab/domain/repository/CollabRepository.kt)**: Added `query` parameter to `getCollabs` and `getCollabsPaging`.
- **[CollabRepoImpl](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/Collab/data/remote/repository/CollabRepoImpl.kt)**: Implemented passing the `query` parameter to the Ktor client request.
- **[CollabPagingSource](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/Collab/data/paging/CollabPagingSource.kt)**: Added `query` to constructor and passed it to the repository.

### Presentation Layer
- **[CollabViewModel](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/Collab/presentation/CollabViewModel.kt)**: Updated `fetchCollabs` to handle both `type` and `query` updates.
- **[CollabExploreScreen](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/Collab/presentation/CollabExploreScreen.kt)**:
    - Added "Hackathon" to the filter types list.
    - Fixed the `AppTabRow` call to use the correct tab list.
    - Fixed a bug in the header title text generation.
    - Added a search bar (`OutlinedTextField`) at the top of the screen.

## Verification Results

### Automated Tests
- Ran `:app:assembleDebug` and the build finished successfully, confirming no syntax errors or broken dependencies.

### Manual Verification Details
- The collaboration screen now displays "Hackathon" as a filter option.
- The search bar is visible below the title and above the tabs.
- The header title correctly displays the plural form of the selected category (e.g., "Hackathons").
