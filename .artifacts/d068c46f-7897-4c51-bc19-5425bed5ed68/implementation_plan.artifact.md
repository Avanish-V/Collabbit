# Implementation Plan - Collaboration Search and Filter Fix

Fix the collaboration tab filter type list and implement search functionality in the collaboration explore screen.

## User Review Required

> [!IMPORTANT]
> This plan assumes that the backend `GET /collabs` endpoint supports a `query` parameter for searching. If the backend does not support it, the search will not return filtered results from the server.

## Proposed Changes

### Collaboration Feature - Data Layer

#### [MODIFY] [CollabRepository.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/Collab/domain/repository/CollabRepository.kt)
- Update `getCollabs` and `getCollabsPaging` to include a `query: String` parameter.

#### [MODIFY] [CollabRepoImpl.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/Collab/data/remote/repository/CollabRepoImpl.kt)
- Implement search query support by passing the `query` parameter to the API request.

#### [MODIFY] [CollabPagingSource.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/Collab/data/paging/CollabPagingSource.kt)
- Add `query: String` to the constructor and pass it to `repository.getCollabs`.

---

### Collaboration Feature - Presentation Layer

#### [MODIFY] [CollabViewModel.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/Collab/presentation/CollabViewModel.kt)
- Update `CollabFilter` data class to include `query`.
- Update `fetchCollabs` to accept an optional `query`.
- Ensure search query updates trigger a new paging data flow.

#### [MODIFY] [CollabExploreScreen.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/Collab/presentation/CollabExploreScreen.kt)
- Add "Hackathon" to `filterTypes`.
- Fix bug where `tabList` was passed an undefined variable `filterType`.
- Fix bug in header title string concatenation.
- Add a Search Bar (OutlinedTextField) above the tabs to allow users to search collaborations.
- Implement debounce or immediate search update via ViewModel.

## Verification Plan

### Automated Tests
- Build the project using `:app:assembleDebug` to ensure no compilation errors.

### Manual Verification
- Open the Collaboration tab.
- Verify "Hackathon" appears in the filter tabs.
- Verify selecting different tabs correctly filters collaborations (via logs or UI update).
- Enter text in the search bar and verify that it triggers a refresh of the collaboration list.
- Verify that clearing the search bar returns all collaborations for the selected type.
