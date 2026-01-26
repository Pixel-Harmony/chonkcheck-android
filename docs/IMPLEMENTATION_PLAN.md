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
| 5 | Onboarding Flow | ✅ Complete |
| 6 | Foods Core - Data Layer | ✅ Complete |
| 7 | Foods Core - UI Layer | ✅ Complete |
| 8 | Barcode Scanning | ✅ Complete |
| 9 | Diary Core | ✅ Complete |
| 10 | Weight Tracking | ✅ Complete |
| 11 | Recipes Feature | ✅ Complete |
| 12 | Saved Meals | ✅ Complete |
| 13 | Exercise Tracking | ✅ Complete |
| 14 | Nutrition Label Scanner | ✅ Complete |
| 15 | Settings & Profile | ✅ Complete |
| 16 | Dashboard / Home Screen | ✅ Complete |
| 17 | Sync Engine & Offline Support | ✅ Complete |
| 18 | Polish, Analytics & Release Prep | ✅ Complete |

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

### Phase 5: Onboarding Flow ✅

**Goal**: Implement 3-step onboarding for new users.

**Deliverables**:
- Step 1: Unit preferences (weight/height units)
- Step 2: Body profile (height, weight, age, sex, activity level)
- Step 3: Daily goals (weight goal, diet preset, macro targets)
- TDEE calculation (Mifflin-St Jeor)
- Navigation guards redirect incomplete users

**Files Created**:
```
domain/usecase/CalculateTdeeUseCase.kt, CompleteOnboardingUseCase.kt
presentation/MainViewModel.kt
presentation/ui/onboarding/
  OnboardingScreen.kt, UnitsStepScreen.kt, ProfileStepScreen.kt, GoalsStepScreen.kt
  OnboardingViewModel.kt
```

**Note**: Domain models (UserProfile, DailyGoals, UnitPreferences) were already defined in User.kt during Phase 2.

---

### Phase 6: Foods Core - Data Layer ✅

**Goal**: Implement Foods feature data layer with offline-first pattern.

**Deliverables**:
- Foods API endpoints (list, create, get, update, delete, barcode lookup)
- FoodRepository with Room-first, network-refresh pattern
- Domain Food model and mappers
- Search with local data, background network refresh

**API Endpoints**:
```
GET /foods?search={query}&type={all|user}&includeRecipes={bool}&includeMeals={bool}
GET /foods/:id
POST /foods
PUT /foods/:id
DELETE /foods/:id
POST /foods/:id/promote
GET /foods/barcode/:code
POST /nutrition-labels (body: { image: base64, mediaType: string })
```

**Food Data Model**:
```kotlin
data class Food(
    val id: String,
    val name: String,
    val brand: String?,
    val barcode: String?,
    val servingSize: Double,
    val servingUnit: String,  // g, ml, oz, cup, tablespoon, teaspoon, piece, slice
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double?,       // Optional
    val sugar: Double?,       // Optional
    val sodium: Double?,      // Optional (in mg)
    val type: FoodType,       // PLATFORM or USER
    val overrideOf: String?,  // If overriding platform food
    val promotionRequested: Boolean
)
```

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

### Phase 7: Foods Core - UI Layer ✅

**Goal**: Build Foods screens with search, filtering, and CRUD.

**Deliverables**:
- Foods list screen with search bar
- Filter chips (All, Platform, My Foods)
- Food detail bottom sheet
- Create/Edit food forms
- Reusable FoodCard component

#### Foods List Screen

**Page Header:**
- Title: "Foods"
- Add button: "+ Add" (orange color `#f97316`)

**Sub-tabs:**
- "All Foods" | "My Foods"
- Active state: orange background with shadow

**Search Bar:**
- Placeholder: "Search foods..."
- Icon: Magnifying glass (orange when active)
- Debounce: 300ms, minimum 1 character

**Empty States:**
| Scenario | Message | CTA |
|----------|---------|-----|
| No foods (all tab) | "No foods in the database yet" | "Add First Food" |
| No search results | "No foods found" | "Add First Food" |
| No user foods | "You haven't created any foods yet" | "Add First Food" |

**Attribution Footer:**
- Text: "Some food data sourced from Open Food Facts"
- Link: https://world.openfoodfacts.org

#### Food Card Component
```
┌─────────────────────────────────────────────────┐
│ [Name] [Platform Badge] [Revised Badge]    [X]  │
│ [Brand]                                         │
│ [Serving: 100g]                                 │
│ P: 25g · C: 30g · F: 10g          [Calories]   │
│                                      cal        │
└─────────────────────────────────────────────────┘
```

**Badges:**
- "Platform" - Teal (`#14b8a6`)
- "Revised" - Cyan (`#22d3ee`)
- "Review" - Amber (`#f59e0b`)

**Macro Format:** `P: {protein}g · C: {carbs}g · F: {fat}g`

#### Add/Edit Food Form

**Quick Actions (Add only):**
- "Scan Barcode" - Barcode icon, orange
- "Scan Label" - Camera icon, orange

**Form Fields:**
| Field | Type | Required | Placeholder |
|-------|------|----------|-------------|
| Name | text | Yes | "e.g., Chicken Breast" |
| Brand | text | No | "Optional" |
| Barcode | text | No | "Optional" |
| Serving Size | number | Yes | "100" |
| Unit | select | Yes | Options: g, ml, oz, cup, tablespoon, teaspoon, piece, slice |

**Nutrition Section (per serving):**
| Field | Type | Required | Step |
|-------|------|----------|------|
| Calories | number | Yes | 1 |
| Protein (g) | number | Yes | 0.1 |
| Carbs (g) | number | Yes | 0.1 |
| Fat (g) | number | Yes | 0.1 |
| Fiber (g) | number | No | 0.1 |
| Sugar (g) | number | No | 0.1 |
| Sodium (mg) | number | No | 1 |

**Buttons:**
- Save: "Save Food" (add) / "Save Changes" (edit)
- Loading: "Saving..."

**Platform Food Notice:**
- Message: "This is a platform food and cannot be edited."
- Override CTA: "Disagree with these nutrition facts? Create your own version."
- Button: "Create My Version"

**Submit to ChonkCheck:**
- Regular: "Want to share this food with all ChonkCheck users? Submit it for review."
- Override: "Think your corrections should replace the platform version? Submit as a revision for review."
- Button: "Submit to ChonkCheck" / "Submit Revision"

**Files Created**:
```
presentation/ui/foods/
  FoodsScreen.kt, FoodsViewModel.kt
  FoodFormScreen.kt, FoodFormViewModel.kt (combined create/edit form)
  components/FoodCard.kt, FoodSearchBar.kt, FoodFilterChips.kt, FoodsEmptyState.kt
presentation/navigation/ChonkCheckNavHost.kt (updated with Foods routes)
```

---

### Phase 8: Barcode Scanning ✅

**Goal**: Integrate ML Kit for barcode scanning.

**Deliverables**:
- Camera permission handling
- Barcode scanner with live preview
- API lookup for scanned barcode
- "Not found" flow with option to create food

**UI States:**

**Barcode Lookup Loading:**
- Message: "Looking up barcode in database..."
- Spinner: Small circular loader

**Success (from Open Food Facts):**
- Notice: "Product details loaded from Open Food Facts. Review and edit as needed before saving."
- Background: Teal

**Not Found:**
- Message: "Hmm, can't find that barcode. Try adding the food manually."

**Files to Create**:
```
domain/usecase/LookupBarcodeUseCase.kt
presentation/ui/scanner/
  BarcodeScannerScreen.kt, BarcodeScannerViewModel.kt
  components/CameraPreview.kt, ScannerOverlay.kt
presentation/util/PermissionHandler.kt
```

---

### Phase 9: Diary Core ✅

**Goal**: Build the diary feature - the heart of the app.

**Deliverables**:
- Diary screen with date navigation
- Meal sections (Breakfast, Lunch, Dinner, Snacks)
- Add food to diary flow with serving selection
- Daily totals calculation
- Edit/delete entries

#### Diary Screen Layout
```
┌─────────────────────────────────┐
│ [Logo] [Date Selector]          │
├─────────────────────────────────┤
│ [MacroSummary - 4 rings]        │
├─────────────────────────────────┤
│ [Weight Projection] (if shown)  │
├─────────────────────────────────┤
│ [Complete Day] (if today)       │
├─────────────────────────────────┤
│ Breakfast              + Add    │
│ [entries or "No items"]         │
├─────────────────────────────────┤
│ Lunch                  + Add    │
│ [entries or "No items"]         │
├─────────────────────────────────┤
│ Dinner                 + Add    │
│ [entries or "No items"]         │
├─────────────────────────────────┤
│ Snack                  + Add    │
│ [entries or "No items"]         │
├─────────────────────────────────┤
│ Exercise         + Add Exercise │
│ [entries or empty state]        │
│ [Net calories summary]          │
└─────────────────────────────────┘
```

#### Date Selector
- Format: Day of week ("EEEE") + Full date ("MMMM d, yyyy")
- Example: "Friday\nJanuary 17, 2025"
- Navigation: Left/right arrows

#### Meal Section Colors
| Meal | Color |
|------|-------|
| Breakfast | Coral `#ff6347` |
| Lunch | Amber `#f59e0b` |
| Dinner | Vivid `#22c55e` |
| Snack | Purple `#a855f7` |

#### Macro Summary (4 circular rings)
| Macro | Color | Background | Unit |
|-------|-------|------------|------|
| Cal | Coral `#ff6347` | `#ffe8e3` | - |
| Pro | Teal `#14b8a6` | `#ccfbf1` | g |
| Carb | Vivid `#22c55e` | `#dcfce8` | g |
| Fat | Amber `#f59e0b` | `#fef3c7` | g |

**Display:** Percentage in center, label below, current/goal values

#### Diary Entry Card
```
┌─────────────────────────────────────────┐
│ [●] [Food Name]           [Delete]      │
│     [Brand]                             │
│     [1.5 x 100g]                        │
│     P: 25g · C: 30g · F: 10g   350 cal  │
└─────────────────────────────────────────┘
```

**Indicator Colors:**
- Food: Orange `#f97316`
- Recipe: Vivid `#22c55e`
- Meal: Purple `#a855f7`

#### Add to Diary Flow
**Header:** "Add to {Meal}" or "Edit {Meal}"

**Meal Selector:** Breakfast | Lunch | Dinner | Snack (horizontal buttons)

**Search Placeholder:** "Find your scran..."

**Input Mode Toggle:**
- "Servings" | "Amount"
- Active: Primary background

**Serving Info:** "1 serving = {servingSize}{unit}"

**Nutrition Display:**
```
Total nutrition:
350 cal
P: 25g · C: 30g · F: 10g
```

**Buttons:**
- Primary: "Log It" (new) / "Update" (edit)
- Secondary: "Back" (new) / "Cancel" (edit)
- Loading: "Logging..." / "Updating..."

#### Complete Day Button
- Incomplete: "Complete Day" (green gradient)
- Completed: "Reopen" (orange)
- Loading: "Saving..."

#### Exercise Section
**Empty State:** "No exercise logged yet. Add one to get started!"

**Net Calories Summary:**
```
Total exercise:    300 cal
Net calories:     1700 cal
```

**Files to Create**:
```
domain/model/DiaryEntry.kt, DiarySummary.kt, MealType.kt
domain/repository/DiaryRepository.kt
domain/usecase/GetDiaryEntriesUseCase.kt, AddDiaryEntryUseCase.kt, GetDailySummaryUseCase.kt
data/api/DiaryApi.kt, repository/DiaryRepositoryImpl.kt
presentation/ui/diary/
  DiaryScreen.kt, DiaryViewModel.kt
  AddToDiarySheet.kt, EditEntrySheet.kt
  components/DateNavigator.kt, MealSection.kt, DiaryEntryCard.kt, DailySummaryCard.kt, MacroSummary.kt
```

---

### Phase 10: Weight Tracking ✅

**Goal**: Implement weight logging with history and charts.

**Deliverables**:
- Weight log screen with chart (Vico library)
- Add weight entry with optional notes
- Weight history list
- Statistics (current, change, average, trend)
- Milestones

#### Stats Cards (3-column grid)
| Card | Background | Label |
|------|------------|-------|
| Starting Weight | Purple `#a855f7` | "Starting" |
| Current Weight | Vivid `#22c55e` | "Current" |
| Total Change | Amber `#f59e0b` | "Total Change" |

#### Weight Chart
- Library: Use Vico (Android charting library)
- Actual line: Solid green `#22c55e` with dots
- Trend line: Dashed teal `#14b8a6` (7+ entries)
- X-axis: "MMM d" format
- Y-axis: Auto-scaled weight values

#### Log Weight Form
**Fields:**
| Field | Type | Placeholder |
|-------|------|-------------|
| Weight (kg/lb) | number | "0.0" |
| Stones (if st) | number | "0" |
| Pounds (if st) | number | "0" |
| Unit | select | kg, lb, st/lb |
| Date | date | Today |
| Notes | text | "e.g., After workout" |

**Button:** "Log Weight" / "Saving..."

#### History Section
**Empty State:** "No weight entries yet. Start tracking above!"

**Entry Format:**
- Date: "EEEE, MMM d" (e.g., "Monday, Jan 15")
- Weight: Formatted with unit
- Notes: Below date if present
- Delete: Trash icon

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

### Phase 11: Recipes Feature ✅

**Goal**: Build recipe management with nutrition calculation.

**Deliverables**:
- Recipes list screen
- Create recipe with ingredient search
- Auto-calculate nutrition per serving
- Use recipe in diary (treated like a food)

#### Recipe List
**Header:** "My Recipes"
**Add Button:** "+ Add" (vivid `#22c55e`)
**Search Placeholder:** "Search recipes..."

**Recipe Card:**
```
┌─────────────────────────────────────────┐
│ [●] [Recipe Name]              [Delete] │
│     3 ingredient(s) · 4 serving(s)      │
│     P: 25g · C: 30g · F: 10g   450 cal  │
└─────────────────────────────────────────┘
```
- Indicator: Vivid dot

**Empty State:**
- Message: "You haven't created any recipes yet"
- Subtext: "Save your favorite food combinations as recipes for easy logging"
- CTA: "Create First Recipe"

#### Create/Edit Recipe Form
**Basic Info:**
| Field | Type | Required | Placeholder |
|-------|------|----------|-------------|
| Recipe Name | text | Yes | "e.g., Chicken Stir Fry" |
| Description | textarea | No | "Optional notes about this recipe" |
| Total Servings | number | Yes | min=1 |
| Serving Unit | select | Yes | serving, bowl, plate, portion, cup, piece |

**Ingredients Section:**
- Search: "Search foods..."
- Empty: "No ingredients added yet. Search for foods or recipes above."

**Nutrition Summary:**
```
┌─────────────────────┬─────────────────────┐
│ Total Recipe        │ Per {servingUnit}   │
│ 1800 calories       │ 450 calories        │
│ P: 100g C: 120g F:40│ P: 25g C: 30g F: 10g│
└─────────────────────┴─────────────────────┘
```

**Button:** "Save Recipe" / "Update Recipe" / "Saving..."

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

### Phase 12: Saved Meals ✅

**Goal**: Implement saved meal groups for quick logging.

**Deliverables**:
- Saved meals list
- Create saved meal from foods/recipes
- Quick-add saved meal to diary (adds all items)
- Edit/delete saved meals

#### Saved Meals List
**Header:** "My Meals"
**Add Button:** "+ Add" (purple `#a855f7`)
**Search Placeholder:** "Search meals..."

**Meal Card:**
```
┌─────────────────────────────────────────┐
│ [●] [Meal Name]                [Delete] │
│     4 item(s)                           │
│     P: 50g · C: 60g · F: 20g   650 cal  │
└─────────────────────────────────────────┘
```
- Indicator: Purple dot

**Empty State:**
- Message: "You haven't saved any meals yet"
- Subtext: "Save food combinations to log your usual meals with one tap"
- CTA: "Create First Meal"

#### Create/Edit Meal Form
**Basic Info:**
| Field | Type | Required | Placeholder |
|-------|------|----------|-------------|
| Meal Name | text | Yes | "e.g., My Usual Breakfast" |

**Helper Text:** "Save a combination of foods and recipes to log together"

**Items Section:**
- Search: "Search foods or recipes to add..."
- Empty: "No items added yet. Search for foods or recipes above."

**Total Nutrition:**
```
650 calories
P: 50g · C: 60g · F: 20g
```

**Button:** "Save Meal" / "Update Meal" / "Saving..."

**Files to Create**:
```
domain/model/SavedMeal.kt, SavedMealItem.kt
domain/usecase/GetSavedMealsUseCase.kt, CreateSavedMealUseCase.kt, AddSavedMealToDiaryUseCase.kt
data/api/SavedMealApi.kt, repository/SavedMealRepositoryImpl.kt
presentation/ui/savedmeals/
  SavedMealsScreen.kt, SavedMealDetailSheet.kt, CreateSavedMealSheet.kt
```

---

### Phase 13: Exercise Tracking ✅

**Goal**: Add exercise logging for calories burned.

**Deliverables**:
- Exercise section in diary
- Log exercise with calories burned
- Net calories calculation (food - exercise)

**Files Created**:
```
domain/model/Exercise.kt, ExerciseEntry.kt
domain/repository/ExerciseRepository.kt
domain/usecase/CreateExerciseUseCase.kt, GetExercisesUseCase.kt, UpdateExerciseUseCase.kt, DeleteExerciseUseCase.kt
data/api/ExerciseApi.kt, dto/ExerciseDtos.kt
data/db/entity/ExerciseEntity.kt, dao/ExerciseDao.kt
data/repository/ExerciseRepositoryImpl.kt
data/mappers/ExerciseMappers.kt
presentation/ui/exercise/ExerciseFormScreen.kt, ExerciseFormViewModel.kt
presentation/ui/diary/ (updated with exercise section and net calories)
```

---

### Phase 14: Nutrition Label Scanner ✅

**Goal**: Camera capture with backend AI parsing (Claude Haiku via API).

**Deliverables**:
- Camera capture for nutrition labels (CameraX)
- Image encoding (base64)
- POST to `/nutrition-labels` API endpoint (backend uses Claude Haiku)
- Receive parsed nutrition data
- Pre-fill create food form with results

**Files Created**:
```
data/api/NutritionLabelApi.kt
data/api/dto/NutritionLabelDtos.kt
domain/model/NutritionLabelResult.kt
domain/usecase/ScanNutritionLabelUseCase.kt
data/mappers/NutritionLabelMappers.kt
core/util/ImageUtils.kt
presentation/ui/scanner/
  NutritionLabelScannerScreen.kt, NutritionLabelScannerViewModel.kt
```

**Note**: No on-device ML Kit text recognition needed. The backend API handles AI parsing with Claude Haiku, returning structured nutrition data.

---

### Phase 15: Settings & Profile ✅

**Goal**: Build settings screens with all user preferences.

**Deliverables**:
- Profile view/edit
- Goals editor with TDEE recalculation
- Unit preferences (metric/imperial)
- Theme selection (light/dark/system)
- Privacy settings, data export, account deletion

#### Settings Sections

**1. Account Section**
- User avatar + email
- "Logged in with Auth0"
- Button: "Sign Out" (red)

**2. Preferences Section**
- Weight Unit: "Kilograms (kg)", "Pounds (lb)", "Stones & Pounds (st/lb)"
- Height Unit: "Centimetres (cm)", "Feet & Inches (ft/in)"
- Appearance: "Light" | "Dark" | "System"

**3. Body & Goals Section**

*Body Measurements:*
- Height (cm or ft/in dual input)
- Age (number, min=13, max=120)
- Sex: "Male" | "Female"
- Activity Level (dropdown with descriptions)
- Starting/Current Weight (readonly if logged, editable if not)

*TDEE Display:*
- Card: Primary green background
- Text: "What you burn each day"
- Value: "{tdee} cal/day"

*Weight Goals (if TDEE exists):*
- "Lose Weight" | "Maintain" | "Gain Weight"
- Rate options with weekly deficit/surplus
- Warning icon if <1200 cal

*Diet Preset:*
- Dropdown: Balanced, Low Carb, High Protein, Keto, GLP-1 Friendly, Mediterranean, Custom
- Shows ratio: "(30/40/30)"
- Custom: Three sliders totaling 100%

*Daily Macro Goals:*
- Calories, Protein (g), Carbs (g), Fat (g)

**4. Privacy & Data Section**
- "Data Processing Consent: Given"
- "Export My Data" → downloads JSON
- "Privacy Policy" → link

**5. Danger Zone Section**
- Warning: "Permanently delete your account and all associated data. This can't be undone."
- Button: "Delete My Account"
- Confirmation: Type "DELETE" to confirm

**Files Created**:
```
domain/repository/SettingsRepository.kt
domain/usecase/CalculateTdeeUseCase.kt
data/repository/SettingsRepositoryImpl.kt
data/mappers/WeightMappers.kt (updated with unit conversion)
presentation/ui/settings/
  SettingsScreen.kt, SettingsViewModel.kt
  components/AccountSection.kt, PreferencesSection.kt, BodyGoalsSection.kt,
            PrivacyDataSection.kt, DangerZoneSection.kt, CollapsibleSection.kt,
            DeleteAccountDialog.kt
presentation/ui/weight/components/WeightUnitConverter.kt (updated with rounding fix)
```

**Key Features Implemented**:
- 5 collapsible sections: Account, Preferences, Body & Goals, Privacy & Data, Danger Zone
- Theme toggle (System/Light/Dark) with DataStore persistence
- Weight/Height unit preferences with proper conversions
- Birth date picker with age calculation
- Sex and Activity level selection
- TDEE preview card with real-time recalculation
- Weight goal selection with weekly rate options
- Diet preset selection (synced to API)
- Daily macro goals inputs with change detection
- Data export via FileProvider share intent
- Weight saving from settings (creates weight entry)
- Sign out and delete account functionality
- Weight unit conversion fix (API returns total lbs for "st" unit)
- Stone/pound rounding fix (round instead of truncate)

---

### Phase 16: Dashboard / Home Screen ✅

**Goal**: Build main dashboard with summary and quick actions.

**Deliverables**:
- Today's summary card (calories/macros vs goals)
- Quick action buttons (add food, scan, log weight)
- Recent weight widget
- Milestone celebrations
- Pull-to-refresh

#### Today's Summary
- Same MacroSummary component as Diary
- 4 circular progress rings

#### Quick Actions
- Add Food (orange)
- Scan Barcode (orange)
- Log Weight (green)

#### Recent Weight Widget
- Shows latest weight + trend
- Link to weight page

**Files to Create**:
```
domain/model/DashboardData.kt
domain/usecase/GetDashboardDataUseCase.kt
presentation/ui/dashboard/
  DashboardScreen.kt, DashboardViewModel.kt
  components/TodaySummaryCard.kt, QuickActionsCard.kt, WeightWidgetCard.kt, MilestoneCard.kt
```

---

### Phase 17: Sync Engine & Offline Support ✅

**Goal**: Build robust background sync with conflict resolution.

**Deliverables**:
- SyncManager with WorkManager
- Connectivity-aware sync
- Conflict resolution (server wins with notification)
- Retry with exponential backoff
- Sync status indicator in UI

**Files Created**:
```
domain/model/SyncStatus.kt
data/sync/ConnectivityMonitor.kt
data/sync/SyncOperationProcessor.kt
data/sync/SyncManager.kt
data/sync/SyncWorker.kt
data/sync/SyncScheduler.kt
data/sync/SyncQueueHelper.kt
di/SyncModule.kt
presentation/ui/components/SyncStatusIndicator.kt
```

**Files Modified**:
```
ChonkCheckApplication.kt (WorkManager & sync initialization)
data/repository/WeightRepositoryImpl.kt (sync queue integration)
data/repository/FoodRepositoryImpl.kt (sync queue integration)
data/repository/DiaryRepositoryImpl.kt (sync queue integration)
data/repository/RecipeRepositoryImpl.kt (sync queue integration)
data/repository/SavedMealRepositoryImpl.kt (sync queue integration)
data/repository/ExerciseRepositoryImpl.kt (sync queue integration)
```

---

### Phase 18: Polish, Analytics & Release Prep ✅

**Goal**: Final polish and release preparation.

**Deliverables**:
- Sentry error tracking
- Milestone celebrations with confetti
- Performance optimization, Baseline Profile
- Release signing config

**Files Created**:
```
SentryInitializer.kt
domain/model/Milestone.kt
domain/repository/MilestoneRepository.kt
domain/usecase/GetPendingMilestoneUseCase.kt
domain/usecase/MarkMilestoneViewedUseCase.kt
data/api/MilestoneApi.kt
data/api/dto/MilestoneDtos.kt
data/api/dto/SuccessResponse.kt
data/repository/MilestoneRepositoryImpl.kt
data/mappers/MilestoneMappers.kt
di/MilestoneModule.kt
presentation/ui/milestones/ConfettiEffect.kt
presentation/ui/milestones/MilestoneModal.kt
presentation/ui/milestones/MilestoneCopy.kt
presentation/viewmodel/MilestoneViewModel.kt
app/src/main/baseline-prof.txt
```

**Files Modified**:
```
ChonkCheckApplication.kt (Sentry initialization)
MainActivity.kt (Milestone modal integration)
app/build.gradle.kts (SENTRY_DSN, release signing)
app/proguard-rules.pro (sync & milestone rules)
```

---

## Global UI Patterns

### Feature Color Mapping
| Feature | Primary Color | Hex |
|---------|---------------|-----|
| Foods | Orange | `#f97316` |
| Recipes | Vivid/Cyan | `#22c55e` |
| Meals | Purple | `#a855f7` |
| Weight | Green | `#22c55e` |
| Exercise | Coral | `#ff6347` |

### Common Components

**Search Bar:**
- Icon on left (feature color)
- Placeholder text
- 300ms debounce

**Empty State:**
- Icon (feature color, 64dp)
- Primary message (medium weight)
- Secondary message (muted)
- CTA button (feature color)

**Card Styling:**
- Background: Surface
- Rounded: 12dp
- Shadow: Small
- Padding: 16dp

**Unsaved Changes Modal:**
- Title: "Unsaved changes"
- Message: "You have unsaved changes. Are you sure you want to go back?"
- Buttons: "Stay" | "Discard" (red)

---

## Verification Checklist

1. Build: `./gradlew.bat assembleDebug`
2. Compare each screen with web app visually
3. Verify all copy text matches exactly
4. Test all unit options work correctly
5. Test light/dark mode

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
