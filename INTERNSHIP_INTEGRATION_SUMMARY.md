# Internship Integration with hirewithfinder-backend

## Overview
Successfully integrated finder-mobile app with hirewithfinder-backend to fetch internships and jobs from the `/api/jobs` endpoint.

## Changes Made

### 1. Data Model Updates
**File**: `OpportunityResponse.kt`

Updated the model to match the backend `OpportunityResponseDto` structure:
- Added required fields: `recruiterUid`, `posted`, `applicants`
- Changed `skills` from `String?` to `List<String>`
- Made `stipend`, `status`, `description` non-nullable to match backend
- Kept legacy fields for backward compatibility

### 2. Repository Layer
**File**: `OpportunitiesRepositoryImpl.kt`

- Removed `type` parameter from `getOpportunities()` method
- Now fetches all opportunities from `/api/jobs` endpoint without server-side filtering
- Backend returns both jobs and internships in a single call

### 3. Domain Layer
**Files**: 
- `OpportunitiesRepository.kt` (interface)
- `GetOpportunitiesUseCase.kt`

- Updated interface to remove `type` parameter
- Use case now returns all opportunities without filtering

### 4. Presentation Layer
**File**: `OpportunitiesViewModel.kt`

Implemented client-side filtering:
- Added `_allOpportunities` cache to store all fetched opportunities
- Created `filterOpportunitiesByType()` method for efficient filtering
- `fetchOpportunities(type)` now:
  1. Fetches all opportunities once from backend
  2. Caches them in `_allOpportunities`
  3. Filters by type ("Internship" or "Job") on the client side
  4. Subsequent type switches use cached data without re-fetching

## How It Works

1. **Initial Load**: When the app opens the Opportunities screen, it calls `fetchOpportunities("Internship")`
2. **API Call**: Makes a GET request to `/api/jobs` (via `OPPORTUNITIES_BASE_URL`)
3. **Caching**: Stores all opportunities (both jobs and internships) in `_allOpportunities`
4. **Filtering**: Filters the cached list to show only "Internship" type
5. **Tab Switching**: When switching between tabs, it filters the cached data without making new API calls

## Backend Endpoint
- **URL**: `http://192.168.0.2:8081/api/jobs` (development)
- **Method**: GET
- **Returns**: List of `OpportunityResponseDto` containing both jobs and internships
- **Type Filter**: Removed (backend returns all, filtering done on mobile app)

## Testing Instructions

### Prerequisites
1. Ensure `hirewithfinder-backend` is running on `http://192.168.0.2:8081`
2. Update IP address in `AppConstants.kt` if needed
3. Backend should have some internship/job data

### Test Steps
1. **Build the App**:
   ```bash
   cd finder-mobile
   ./gradlew clean build
   ```

2. **Install on Device/Emulator**:
   ```bash
   ./gradlew installDebug
   ```

3. **Test Scenarios**:
   - ✅ Open "Explore" screen → Should show "Internships" tab by default
   - ✅ Verify internships are loaded from backend
   - ✅ Check that opportunity cards display: title, company, location, stipend
   - ✅ Switch to "Courses" tab → Should load courses
   - ✅ Switch back to "Internships" → Should use cached data (no loading state)
   - ✅ Pull to refresh → Should fetch fresh data from backend
   - ✅ Click on an internship card → Should navigate to detail screen

4. **Verify Network Call**:
   - Use Android Studio's Network Inspector
   - Confirm GET request to `/api/jobs`
   - Verify response contains internship data

5. **Error Handling**:
   - Turn off backend → Should show error message
   - Tap retry → Should attempt to fetch again

## API Response Example
```json
[
  {
    "id": "1",
    "title": "Software Engineering Intern",
    "company": "Tech Corp",
    "location": "Remote",
    "type": "Internship",
    "stipend": "₹20,000/month",
    "posted": "2 days ago",
    "applicants": 15,
    "status": "Open",
    "description": "Looking for passionate developers...",
    "skills": ["Kotlin", "Android", "REST APIs"],
    "recruiterUid": "firebase-uid-123",
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

## Benefits of Current Implementation

1. **Reduced Network Calls**: Fetch once, filter multiple times
2. **Faster Tab Switching**: No loading state when switching between opportunities
3. **Better UX**: Instant filtering without waiting for API
4. **Cache Management**: Force refresh option available via pull-to-refresh
5. **Backend Agnostic**: Backend doesn't need to support type filtering

## Future Enhancements

1. **Add "Jobs" Tab**: Currently only shows "Internships" and "Courses"
   - Update `categories` list in `OpportunitiesScreen.kt`
   - Add another tab index case in `LaunchedEffect`

2. **Search Functionality**: Filter by title, company, or skills

3. **Advanced Filters**: Location, stipend range, status

4. **Pagination**: If backend supports it, implement infinite scrolling

5. **Offline Support**: Cache opportunities in local database (Room)

## Notes

- The backend endpoint `/api/jobs` serves both jobs and internships
- Filtering by type happens on the mobile app side for better performance
- The `skills` field changed from `String?` to `List<String>` to match backend
- Legacy fields are maintained for backward compatibility if needed
- Current IP (`192.168.0.2:8081`) needs to be updated to match your local network or production URL

## Related Files Modified

1. `OpportunityResponse.kt` - Data model
2. `OpportunitiesRepositoryImpl.kt` - Data source implementation
3. `OpportunitiesRepository.kt` - Repository interface
4. `GetOpportunitiesUseCase.kt` - Use case
5. `OpportunitiesViewModel.kt` - Presentation logic

---

**Status**: ✅ Implementation Complete
**Date**: 2026-08-20
**Developer**: Kiro AI Assistant
