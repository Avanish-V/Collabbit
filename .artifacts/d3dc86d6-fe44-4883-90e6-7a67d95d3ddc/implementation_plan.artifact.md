# Show Daily Aura Transactions implementation plan

The goal is to allow users to view their daily aura transactions by clicking on the aura points in their profile header. The transactions will be fetched from the local Room database.

## Proposed Changes

### Domain Layer

#### [MODIFY] [AuraRepository.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/UserProfile/domain/repository/AuraRepository.kt)
- Add `observeTransactions(userId: String): Flow<List<AuraTransactionEntity>>`.

#### [NEW] [ObserveAuraTransactionsUseCase.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/UserProfile/domain/usecase/ObserveAuraTransactionsUseCase.kt)
- Create a use case to observe transactions for the current user.

### Data Layer

#### [MODIFY] [AuraRepositoryImpl.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/UserProfile/data/remote/repository/AuraRepositoryImpl.kt)
- Implement `observeTransactions` using `AuraTransactionDao`.

### Presentation Layer

#### [MODIFY] [AuraViewModel.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/UserProfile/presentation/AuraViewModel.kt)
- Add `transactions` Flow by using `ObserveAuraTransactionsUseCase`.

#### [NEW] [AuraTransactionsDialog.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/UserProfile/ui/Components/AuraTransactionsDialog.kt)
- Create a dialog to display the list of aura transactions.

#### [MODIFY] [AuraStreakCard.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/UserProfile/ui/Components/AuraStreakCard.kt)
- Add `onClick: () -> Unit` parameter.
- Make the card clickable.

#### [MODIFY] [RedesignedProfileComponents.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/ui/UIComponents/RedesignedProfileComponents.kt)
- Add `onAuraClick: () -> Unit` to `RedesignedProfileHeader`.
- Pass `onAuraClick` to `AuraStreakCard`.

### DI

#### [MODIFY] [AppModule.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/di/AppModule.kt)
- Register `ObserveAuraTransactionsUseCase`.

## Verification Plan

### Manual Verification
- Deploy the app to a device.
- Go to the profile screen.
- Click on the Aura points section in the header.
- Verify that a dialog opens showing the list of aura transactions.
- Perform an action that awards aura (like daily check-in) and verify it appears in the list.
