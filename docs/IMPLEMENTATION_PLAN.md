# ChonkCheck Android - Implementation Plan

## Overview

Build the ChonkCheck Android app from scratch to achieve full feature parity with the web application.

**Tech Stack** (from CLAUDE.md):
- Kotlin 2.x, Jetpack Compose, MVVM + Clean Architecture
- Hilt (DI), Retrofit + OkHttp, Kotlinx Serialization, Room
- Auth0 Android SDK, ML Kit (Barcode + OCR), Google Play Billing, Sentry

---

## Progress Tracker

| Phase | Description | Status |
|-------|-------------|--------|
| 1 | Project Scaffolding & Gradle Setup | ✅ Complete |
| 2 | Core Architecture Foundation | ✅ Complete |
| 3 | Room Database Schema & Entities | ✅ Complete |
| 4 | Authentication with Auth0 | ✅ Complete |
| 5 | Onboarding Flow | ⏳ Pending |
| 6 | Foods Core - Data Layer | ⏳ Pending |
| 7 | Foods Core - UI Layer | ⏳ Pending |
| 8 | Barcode Scanning | ⏳ Pending |
| 9 | Diary Core | ⏳ Pending |
| 10 | Weight Tracking | ⏳ Pending |
| 11 | Recipes Feature | ⏳ Pending |
| 12 | Saved Meals | ⏳ Pending |
| 13 | Exercise Tracking | ⏳ Pending |
| 14 | Nutrition Label Scanner | ⏳ Pending |
| 15 | Settings & Profile | ⏳ Pending |
| 16 | Dashboard / Home Screen | ⏳ Pending |
| 17 | Sync Engine & Offline Support | ⏳ Pending |
| 18 | Polish, Analytics & Release Prep | ⏳ Pending |

---

## Phase Details

### Phase 1: Project Scaffolding & Gradle Setup ✅

**Goal**: Initialize Android project structure with all dependencies configured.

**Deliverables**:
- Working Android project that compiles and runs empty MainActivity
- All dependencies declared in version catalog
- Build variants: debug, release
- ktlint configured

**Files Created**:
```
settings.gradle.kts
build.gradle.kts
gradle.properties
gradle/libs.versions.toml
app/build.gradle.kts
app/proguard-rules.pro
app/src/main/AndroidManifest.xml
app/src/main/java/com/chonkcheck/android/ChonkCheckApplication.kt
app/src/main/java/com/chonkcheck/android/MainActivity.kt
app/src/main/res/values/strings.xml, colors.xml, themes.xml
```

---

### Phase 2: Core Architecture Foundation ✅

**Goal**: Establish Clean Architecture layers, base classes, and Hilt DI modules.

**Deliverables**:
- Complete package structure (data/domain/presentation)
- Hilt modules (Network, Database, Repository)
- Base patterns: Result sealed class, UiState
- Shared Compose components (LoadingIndicator, ErrorMessage, ChonkButton)
- Navigation shell with empty screens

**Files Created**:
```
core/util/Result.kt, Extensions.kt, UiState.kt
di/NetworkModule.kt, DatabaseModule.kt
data/api/interceptor/AuthInterceptor.kt
data/auth/TokenStorage.kt
presentation/ui/theme/Theme.kt, Color.kt, Type.kt
presentation/ui/components/LoadingIndicator.kt, ChonkButton.kt, ErrorMessage.kt
presentation/navigation/ChonkCheckNavHost.kt, Screen.kt, BottomNavItem.kt
```

---

### Phase 3: Room Database Schema & Entities ✅

**Goal**: Define all Room entities upfront for offline-first architecture.

**Deliverables**:
- All entity classes (User, Food, DiaryEntry, Recipe, SavedMeal, Weight, Exercise, SyncQueue)
- All DAO interfaces with basic CRUD
- Type converters for dates, enums, JSON

**Files Created**:
```
data/db/entity/UserEntity.kt, FoodEntity.kt, DiaryEntryEntity.kt, RecipeEntity.kt,
               SavedMealEntity.kt, WeightEntryEntity.kt, ExerciseEntryEntity.kt, SyncQueueEntity.kt
data/db/dao/UserDao.kt, FoodDao.kt, DiaryDao.kt, RecipeDao.kt, WeightDao.kt,
            SavedMealDao.kt, ExerciseDao.kt, SyncQueueDao.kt
data/db/converters/Converters.kt
data/db/ChonkCheckDatabase.kt
```

---

### Phase 4: Authentication with Auth0 ✅

**Goal**: Implement complete authentication flow.

**Deliverables**:
- Auth0 SDK integration with Universal Login
- Secure token storage (EncryptedSharedPreferences)
- AuthRepository with auth state as Flow
- AuthInterceptor attaches tokens to requests
- Login screen

**Files Created**:
```
data/auth/AuthManager.kt
data/repository/AuthRepositoryImpl.kt
data/mappers/UserMappers.kt
domain/repository/AuthRepository.kt
domain/model/User.kt, AuthState.kt
presentation/ui/auth/LoginScreen.kt, LoginViewModel.kt
di/AuthModule.kt
```

---

### Phase 5: Onboarding Flow ⏳

**Goal**: Implement 3-step onboarding for new users.

**Deliverables**:
- Step 1: Unit preferences (weight/height units)
- Step 2: Body profile (height, weight, age, sex, activity level)
- Step 3: Daily goals (weight goal, diet preset, macro targets)
- TDEE calculation (Mifflin-St Jeor)
- Navigation guards redirect incomplete users

**Files to Create**:
```
domain/model/UserProfile.kt, DailyGoals.kt, UnitPreferences.kt
domain/usecase/CalculateTdeeUseCase.kt, CompleteOnboardingUseCase.kt
presentation/ui/onboarding/
  OnboardingScreen.kt, UnitsStepScreen.kt, ProfileStepScreen.kt, GoalsStepScreen.kt
  OnboardingViewModel.kt
```

---

### Phase 6: Foods Core - Data Layer ⏳

**Goal**: Implement Foods feature data layer with offline-first pattern.

**Deliverables**:
- Foods API endpoints (list, create, get, update, delete, barcode lookup)
- FoodRepository with Room-first, network-refresh pattern
- Domain Food model and mappers
- Search with local data, background network refresh

**Files to Create**:
```
data/api/FoodApi.kt, dto/FoodDto.kt, FoodsResponse.kt
data/repository/FoodRepositoryImpl.kt
data/mappers/FoodMappers.kt
domain/model/Food.kt, FoodType.kt, FoodFilter.kt
domain/repository/FoodRepository.kt
domain/usecase/SearchFoodsUseCase.kt, GetFoodByIdUseCase.kt, CreateUserFoodUseCase.kt
```

---

### Phase 7: Foods Core - UI Layer ⏳

**Goal**: Build Foods screens with search, filtering, and CRUD.

**Deliverables**:
- Foods list screen with search bar
- Filter chips (All, Platform, My Foods)
- Food detail bottom sheet
- Create/Edit food forms
- Reusable FoodCard component

**Files to Create**:
```
presentation/ui/foods/
  FoodsScreen.kt, FoodsViewModel.kt
  FoodDetailSheet.kt
  CreateFoodScreen.kt, CreateFoodViewModel.kt
  components/FoodCard.kt, FoodSearchBar.kt, FoodFilterChips.kt, NutritionFactsCard.kt
```

---

### Phase 8: Barcode Scanning ⏳

**Goal**: Integrate ML Kit for barcode scanning.

**Deliverables**:
- Camera permission handling
- Barcode scanner with live preview
- API lookup for scanned barcode
- "Not found" flow with option to create food

**Files to Create**:
```
domain/usecase/LookupBarcodeUseCase.kt
presentation/ui/scanner/
  BarcodeScannerScreen.kt, BarcodeScannerViewModel.kt
  components/CameraPreview.kt, ScannerOverlay.kt
presentation/util/PermissionHandler.kt
```

---

### Phase 9: Diary Core ⏳

**Goal**: Build the diary feature - the heart of the app.

**Deliverables**:
- Diary screen with date navigation
- Meal sections (Breakfast, Lunch, Dinner, Snacks)
- Add food to diary flow with serving selection
- Daily totals calculation
- Edit/delete entries

**Files to Create**:
```
domain/model/DiaryEntry.kt, DiarySummary.kt, MealType.kt
domain/repository/DiaryRepository.kt
domain/usecase/GetDiaryEntriesUseCase.kt, AddDiaryEntryUseCase.kt, GetDailySummaryUseCase.kt
data/api/DiaryApi.kt, repository/DiaryRepositoryImpl.kt
presentation/ui/diary/
  DiaryScreen.kt, DiaryViewModel.kt
  AddToDiarySheet.kt, EditEntrySheet.kt
  components/DateNavigator.kt, MealSection.kt, DiaryEntryCard.kt, DailySummaryCard.kt
```

---

### Phase 10: Weight Tracking ⏳

**Goal**: Implement weight logging with history and charts.

**Deliverables**:
- Weight log screen with chart (Vico library)
- Add weight entry with optional notes
- Weight history list
- Statistics (current, change, average, trend)
- Milestones

**Files to Create**:
```
domain/model/WeightEntry.kt, WeightStats.kt, WeightMilestone.kt
domain/usecase/GetWeightHistoryUseCase.kt, LogWeightUseCase.kt, GetWeightStatsUseCase.kt
data/api/WeightApi.kt, repository/WeightRepositoryImpl.kt
presentation/ui/weight/
  WeightScreen.kt, WeightViewModel.kt, LogWeightSheet.kt
  components/WeightChart.kt, WeightStatsCard.kt, WeightMilestoneCard.kt
```

---

### Phase 11: Recipes Feature ⏳

**Goal**: Build recipe management with nutrition calculation.

**Deliverables**:
- Recipes list screen
- Create recipe with ingredient search
- Auto-calculate nutrition per serving
- Use recipe in diary (treated like a food)

**Files to Create**:
```
domain/model/Recipe.kt, RecipeIngredient.kt
domain/usecase/GetRecipesUseCase.kt, CreateRecipeUseCase.kt, CalculateRecipeNutritionUseCase.kt
data/api/RecipeApi.kt, repository/RecipeRepositoryImpl.kt
presentation/ui/recipes/
  RecipesScreen.kt, RecipeDetailScreen.kt, CreateRecipeScreen.kt
  components/RecipeCard.kt, IngredientRow.kt, AddIngredientSheet.kt
```

---

### Phase 12: Saved Meals ⏳

**Goal**: Implement saved meal groups for quick logging.

**Deliverables**:
- Saved meals list
- Create saved meal from foods/recipes
- Quick-add saved meal to diary (adds all items)
- Edit/delete saved meals

**Files to Create**:
```
domain/model/SavedMeal.kt, SavedMealItem.kt
domain/usecase/GetSavedMealsUseCase.kt, CreateSavedMealUseCase.kt, AddSavedMealToDiaryUseCase.kt
data/api/SavedMealApi.kt, repository/SavedMealRepositoryImpl.kt
presentation/ui/savedmeals/
  SavedMealsScreen.kt, SavedMealDetailSheet.kt, CreateSavedMealSheet.kt
```

---

### Phase 13: Exercise Tracking ⏳

**Goal**: Add exercise logging for calories burned.

**Deliverables**:
- Exercise section in diary
- Log exercise with calories burned
- Net calories calculation (food - exercise)

**Files to Create**:
```
domain/model/Exercise.kt, ExerciseEntry.kt
domain/usecase/LogExerciseUseCase.kt, GetExerciseEntriesUseCase.kt
data/api/ExerciseApi.kt, repository/ExerciseRepositoryImpl.kt
presentation/ui/exercise/LogExerciseSheet.kt
```

---

### Phase 14: Nutrition Label Scanner ⏳

**Goal**: Camera capture with backend AI parsing (Claude Haiku via API).

**Deliverables**:
- Camera capture for nutrition labels (CameraX)
- Image encoding (base64 or multipart)
- POST to `/nutrition-labels` API endpoint (backend uses Claude Haiku)
- Receive parsed nutrition data
- Pre-fill create food form with results

**Files to Create**:
```
data/api/NutritionLabelApi.kt
data/api/dto/NutritionLabelRequest.kt, NutritionLabelResponse.kt
domain/usecase/ScanNutritionLabelUseCase.kt
presentation/ui/scanner/
  NutritionLabelScannerScreen.kt, NutritionLabelViewModel.kt
  components/CapturePreview.kt, ImageCropper.kt
```

**Note**: No on-device ML Kit text recognition needed. The backend API handles AI parsing with Claude Haiku, returning structured nutrition data.

---

### Phase 15: Settings & Profile ⏳

**Goal**: Build settings screens with all user preferences.

**Deliverables**:
- Profile view/edit
- Goals editor with TDEE recalculation
- Unit preferences (metric/imperial)
- Theme selection (light/dark/system)
- Privacy settings, data export, account deletion

**Files to Create**:
```
domain/usecase/UpdateProfileUseCase.kt, UpdateGoalsUseCase.kt, ExportUserDataUseCase.kt
presentation/ui/settings/
  SettingsScreen.kt, ProfileScreen.kt, GoalsScreen.kt, PrivacyScreen.kt
  components/SettingsItem.kt, SettingsSection.kt
```

---

### Phase 16: Dashboard / Home Screen ⏳

**Goal**: Build main dashboard with summary and quick actions.

**Deliverables**:
- Today's summary card (calories/macros vs goals)
- Quick action buttons (add food, scan, log weight)
- Recent weight widget
- Milestone celebrations
- Pull-to-refresh

**Files to Create**:
```
domain/model/DashboardData.kt
domain/usecase/GetDashboardDataUseCase.kt
presentation/ui/dashboard/
  DashboardScreen.kt, DashboardViewModel.kt
  components/TodaySummaryCard.kt, QuickActionsCard.kt, WeightWidgetCard.kt, MilestoneCard.kt
```

---

### Phase 17: Sync Engine & Offline Support ⏳

**Goal**: Build robust background sync with conflict resolution.

**Deliverables**:
- SyncManager with WorkManager
- Connectivity-aware sync
- Conflict resolution (server wins with notification)
- Retry with exponential backoff
- Sync status indicator in UI

**Files to Create**:
```
data/sync/SyncManager.kt, SyncWorker.kt, ConflictResolver.kt, SyncStatus.kt
data/repository/SyncQueueRepositoryImpl.kt
di/WorkerModule.kt
presentation/ui/components/SyncStatusIndicator.kt
```

---

### Phase 18: Polish, Analytics & Release Prep ⏳

**Goal**: Final polish and release preparation.

**Deliverables**:
- Sentry error tracking
- Google Play Billing (if premium features needed)
- Loading skeletons, empty states
- Performance optimization, Baseline Profile
- Release signing config
- Full regression testing

**Files to Create**:
```
data/billing/BillingManager.kt (if needed)
core/analytics/AnalyticsManager.kt
presentation/ui/components/SkeletonLoading.kt, EmptyState.kt
app/src/main/baseline-prof.txt
```

---

## Phase Dependencies

```
Phase 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 16 → 17 → 18
                              ↓
                         11 (Recipes)
                              ↓
                         12 (Saved Meals)
                              ↓
                         13 (Exercises)
                              ↓
                         14 (Label Scanner)
                              ↓
                         15 (Settings)
```

---

## Key Reference Files

| File | Purpose |
|------|---------|
| `CLAUDE.md` | Architecture patterns to follow |
| Backend `openapi.yaml` | API specification |
| Web app `apps/web/src/` | Implementation reference |

---

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Auth0 SDK compatibility | Pin specific version, test early in Phase 4 |
| ML Kit camera issues | Test on multiple devices, provide manual fallback |
| Offline sync conflicts | Server-wins strategy with clear user notification |
| Room migration complexity | Define full schema upfront in Phase 3 |
