# Daily Aura Point System - Integration Guide

## Overview
This guide shows how to integrate the daily aura check-in system into the Finder Mobile app.

## Components Created
1. **Backend API** - REST endpoints for aura operations
2. **Use Cases** - Clean architecture business logic
3. **ViewModel** - State management
4. **UI Components** - Beautiful dialogs and animations

## Integration Steps

### Step 1: Add ViewModel to Koin

Add `AuraViewModel` to your Koin module configuration:

```kotlin
// In your Koin module file (e.g., AppModule.kt)
single { ClaimDailyAuraUseCase(get()) }
single { GetAuraInfoUseCase(get()) }
viewModel { AuraViewModel(get(), get()) }
```

### Step 2: Integrate in MainActivity

Add the daily check-in trigger in `MainActivity.kt`:

```kotlin
import com.iota.campusX.Feature.UserProfile.presentation.AuraViewModel
import com.iota.campusX.Feature.UserProfile.presentation.CheckInEvent
import com.iota.campusX.Feature.UserProfile.ui.Components.DailyAuraCheckInDialog
import com.iota.campusX.Feature.UserProfile.ui.Components.AlreadyClaimedDialog

// Inside setContent block, after authViewModel check:
setContent {
    val authViewModel = koinInject<GoogleSignInViewModel>()
    val auraViewModel: AuraViewModel = koinViewModel() // Add this
    
    // State for showing dialogs
    var showCheckInDialog by remember { mutableStateOf(false) }
    var showAlreadyClaimedDialog by remember { mutableStateOf(false) }
    var checkInData by remember { mutableStateOf<CheckInEvent.Success?>(null) }
    
    // Trigger daily check-in when user is authenticated
    LaunchedEffect(authViewModel.getCurrentUser()) {
        if (authViewModel.getCurrentUser()) {
            // Existing profile fetch code...
            profileViewModel.getUserProfile()
            
            // NEW: Automatic daily check-in
            auraViewModel.claimDailyCheckIn(showAlreadyClaimedMessage = false)
        }
    }
    
    // Collect check-in events
    LaunchedEffect(Unit) {
        auraViewModel.checkInEvent.collect { event ->
            when (event) {
                is CheckInEvent.Success -> {
                    checkInData = event
                    showCheckInDialog = true
                }
                is CheckInEvent.AlreadyClaimed -> {
                    // Optionally show already claimed message
                    // showAlreadyClaimedDialog = true
                }
                is CheckInEvent.Error -> {
                    // Handle error silently or show toast
                    Log.e("Aura", "Check-in failed: ${event.message}")
                }
            }
        }
    }
    
    AppTheme {
        // Your existing UI code...
        
        // Add dialogs at the end, before closing braces
        if (showCheckInDialog && checkInData != null) {
            DailyAuraCheckInDialog(
                pointsEarned = checkInData!!.pointsEarned,
                totalPoints = checkInData!!.totalPoints,
                level = checkInData!!.level,
                onDismiss = {
                    showCheckInDialog = false
                    checkInData = null
                }
            )
        }
        
        if (showAlreadyClaimedDialog) {
            AlreadyClaimedDialog(
                totalPoints = checkInData?.totalPoints ?: 0,
                onDismiss = { showAlreadyClaimedDialog = false }
            )
        }
    }
}
```

### Step 3: Add Manual Check-In Button (Optional)

If you want users to manually claim points from a button:

```kotlin
// In your Profile screen or anywhere
Button(
    onClick = { 
        auraViewModel.claimDailyCheckIn(showAlreadyClaimedMessage = true) 
    }
) {
    Text("Claim Daily Points")
}
```

### Step 4: Display Aura in Profile

Update your profile screen to show aura points:

```kotlin
// In your profile composable
val auraInfoState by auraViewModel.auraInfoState.collectAsState()

LaunchedEffect(Unit) {
    auraViewModel.fetchAuraInfo()
}

when (val state = auraInfoState) {
    is UiState.Success -> {
        AuraStreakCard(
            aura = state.data,
            modifier = Modifier.fillMaxWidth()
        )
    }
    is UiState.Loading -> {
        CircularProgressIndicator()
    }
    is UiState.Error -> {
        Text("Failed to load aura: ${state.message}")
    }
    else -> {}
}
```

## Behavior

### Automatic Check-In
- Triggers when user opens app while authenticated
- Shows celebration dialog if points earned (first time today)
- Silent if already claimed today
- Fails gracefully if network error

### User Experience Flow
1. User opens app
2. App authenticates user
3. Background: API call to claim daily points
4. If successful (first time today):
   - Beautiful animated dialog appears
   - Shows points earned (+5)
   - Shows total points
   - Shows current level
   - Optional: Shows streak days
5. User taps "Continue" to dismiss
6. Points are cached locally for offline viewing

## Testing

### Test Scenario 1: First Check-In of Day
1. Open app after midnight (new day)
2. Should see success dialog with +5 points
3. Check local database - aura_points should increase

### Test Scenario 2: Already Claimed
1. Open app again on same day
2. Should NOT show dialog (silent)
3. Check logs - should show "Already claimed today"

### Test Scenario 3: Network Error
1. Turn off internet
2. Open app
3. Should fail gracefully, no crash
4. Check logs for error message

### Test Scenario 4: Streak Bonus
1. Check in for 7 consecutive days
2. On day 7, should see bonus points (+10)
3. Total should be base (5) + bonus (10) = 15 points

## API Endpoints Used

- `POST /api/v1/aura/check-in` - Claim daily points
- `GET /api/v1/aura/me` - Get current aura info
- `GET /api/v1/aura/stats` - Get detailed stats with streak

## Database Tables

The backend uses these tables:
- `aura_rules` - Point award rules
- `user_aura` - User totals and streaks
- `aura_transactions` - Audit log of all point awards

## Troubleshooting

### Dialog doesn't show
- Check if user is authenticated
- Check network logs for API response
- Verify ViewModel is injected correctly

### Points not updating
- Check API response in logs
- Verify database has proper indexes
- Check for constraint violations

### Multiple dialogs appearing
- Ensure proper state management
- Use `LaunchedEffect` with correct keys
- Reset dialog state after dismiss

## Customization

### Change Daily Points
Update in `AuraService.kt`:
```kotlin
const val DEFAULT_DAILY_POINTS = 10 // Change from 5 to 10
```

### Change Streak Bonus
Update in `AuraService.kt`:
```kotlin
const val STREAK_BONUS_THRESHOLD = 5 // Change from 7 to 5
const val STREAK_BONUS_POINTS = 20 // Change from 10 to 20
```

### Customize Dialog Appearance
Edit `DailyAuraCheckInDialog.kt`:
- Colors: Change gradient colors
- Animations: Modify animation specs
- Layout: Adjust spacing and sizing

## Best Practices

1. **Don't block UI** - Check-in happens asynchronously
2. **Handle errors gracefully** - Don't show error dialogs for check-ins
3. **Cache locally** - Use Room to cache aura data
4. **Log everything** - Use proper logging for debugging
5. **Test edge cases** - Midnight transitions, timezone changes

## Future Enhancements

- [ ] Add confetti animation on check-in
- [ ] Show leaderboard in app
- [ ] Add push notification for streak reminders
- [ ] Implement streak freeze (miss 1 day without losing streak)
- [ ] Add special rewards for milestones (100 days, etc.)
- [ ] Gamification: Badges, achievements, levels

## Support

For issues or questions:
1. Check logs: `adb logcat | grep Aura`
2. Verify API is running: `curl http://localhost:8080/api/v1/aura/me`
3. Check database: Query `user_aura` and `aura_transactions` tables
