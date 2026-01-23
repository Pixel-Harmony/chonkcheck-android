# Claude Code Context - ChonkCheck Android

> **MANDATORY INSTRUCTIONS - READ CAREFULLY**
>
> Every rule, guideline, and instruction in this file is **MANDATORY** and **BINDING**. These are not suggestions or preferences - they are absolute requirements that **MUST** be followed without exception.

---

This file provides context for Claude Code when working on the ChonkCheck Android app.

## Project Overview

ChonkCheck Android is a native Kotlin calorie tracking app. It's the mobile companion to the ChonkCheck web application (https://app.chonkcheck.com). Features include:

- Track daily food intake with macro calculations
- Scan barcodes to lookup foods (ML Kit)
- Photograph nutrition labels to auto-create food items
- Create and save recipes
- Save meal combinations for quick diary entry
- Track weight over time with graphs
- Offline-first with sync to cloud

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.x |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Networking | Retrofit + OkHttp |
| Serialization | Kotlinx Serialization |
| Database | Room (offline support) |
| Auth | Auth0 Android SDK |
| Barcode | ML Kit Barcode Scanning |
| Image Analysis | ML Kit Text Recognition |
| Billing | Google Play Billing 7.x |
| Analytics | Sentry Android SDK |

## Architecture

### Clean Architecture Layers

```
app/
├── data/                    # Data layer
│   ├── api/                 # Retrofit interfaces, generated from OpenAPI
│   ├── db/                  # Room database, DAOs, entities
│   ├── repository/          # Repository implementations
│   └── mappers/             # Data <-> Domain mappers
├── domain/                  # Domain layer
│   ├── model/               # Domain models
│   ├── repository/          # Repository interfaces
│   └── usecase/             # Use cases
├── presentation/            # Presentation layer
│   ├── ui/                  # Compose screens and components
│   │   ├── diary/           # Diary feature
│   │   ├── foods/           # Food management
│   │   ├── recipes/         # Recipe management
│   │   ├── weight/          # Weight tracking
│   │   ├── profile/         # User profile
│   │   ├── onboarding/      # Onboarding flow
│   │   └── components/      # Shared components
│   ├── viewmodel/           # ViewModels
│   └── navigation/          # Navigation graph
└── di/                      # Hilt modules
```

### MVVM Pattern

```kotlin
// ViewModel exposes state as Flow
class DiaryViewModel @Inject constructor(
    private val getDiaryEntriesUseCase: GetDiaryEntriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiaryUiState>(DiaryUiState.Loading)
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    fun loadEntries(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = DiaryUiState.Loading
            getDiaryEntriesUseCase(date)
                .catch { _uiState.value = DiaryUiState.Error(it.message) }
                .collect { entries ->
                    _uiState.value = DiaryUiState.Success(entries)
                }
        }
    }
}

// Composable observes state
@Composable
fun DiaryScreen(viewModel: DiaryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is DiaryUiState.Loading -> LoadingIndicator()
        is DiaryUiState.Success -> DiaryContent((uiState as DiaryUiState.Success).entries)
        is DiaryUiState.Error -> ErrorMessage((uiState as DiaryUiState.Error).message)
    }
}
```

## API Integration

> **MANDATORY: OpenAPI Specification Reference**
>
> The backend API is fully documented in OpenAPI format at:
> ```
> ../chonkcheck/apps/backend/openapi.yaml
> ```
>
> **Before implementing ANY API integration, you MUST:**
> 1. Read the OpenAPI spec to verify endpoint paths (e.g., `/user/profile` not `/user`)
> 2. Check request body schemas for correct field names and types
> 3. Verify response schemas for proper DTO mapping
> 4. Check enum values (e.g., activity levels: `sedentary`, `light`, `moderate`, `active`, `very_active`)
>
> **DO NOT assume endpoint paths or schemas. The web app at `../chonkcheck` uses the same API and can serve as a reference for correct usage.**

### Key Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /foods` | List foods (search, filter) |
| `POST /foods` | Create user food |
| `GET /foods/barcode/{code}` | Barcode lookup |
| `POST /nutrition-labels` | AI nutrition label scan |
| `GET /diary/{date}` | Get diary entries |
| `POST /diary` | Add diary entry |
| `GET /weight` | Weight history |
| `POST /weight` | Log weight |
| `PATCH /user/onboarding` | Complete onboarding |
| `GET /user/profile` | Get user profile |
| `PUT /user/profile` | Update user profile and goals |
| `GET /user/data-export` | Export user data (GDPR) |
| `DELETE /user/account` | Delete account (GDPR) |

### API Contract: Diary Entry Quantity

> **CRITICAL**: The `quantity` field in diary entry requests represents **NUMBER OF SERVINGS**, not total grams.

**Correct calculation:**
```kotlin
quantity = (userEnteredAmount / food.servingSize) * numberOfServings
```

**Examples:**
- Log 150g of food with 100g serving: `quantity = 1.5`
- Log 2 servings of the base serving size: `quantity = 2`

**NEVER:**
- Send raw grams as quantity
- Use `servingSize * numberOfServings`

The backend calculates nutrition as: `calories = food.calories * quantity`. Sending wrong quantity values will corrupt nutrition data.

### Authentication

Auth0 Android SDK handles login. JWT tokens are automatically attached to API requests via OkHttp interceptor:

```kotlin
class AuthInterceptor @Inject constructor(
    private val authRepository: AuthRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = authRepository.getAccessToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
```

### Offline-First Strategy

1. Room database is source of truth for UI
2. Network requests update local database
3. UI observes Room via Flow
4. Background sync when connectivity available
5. Conflict resolution: server wins (with user notification)

## Compose Patterns

### State Hoisting

```kotlin
// BAD: State inside composable
@Composable
fun BadTextField() {
    var text by remember { mutableStateOf("") }
    TextField(value = text, onValueChange = { text = it })
}

// GOOD: State hoisted to caller
@Composable
fun GoodTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
    )
}
```

### Small Composables

Keep composables small and focused. Extract reusable components:

```kotlin
// Reusable macro display
@Composable
fun MacroRow(
    protein: Int,
    carbs: Int,
    fat: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MacroItem("P", protein)
        MacroItem("C", carbs)
        MacroItem("F", fat)
    }
}

@Composable
private fun MacroItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text("${value}g", style = MaterialTheme.typography.bodyMedium)
    }
}
```

### Preview Annotations

Always add preview for composables:

```kotlin
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MacroRowPreview() {
    ChonkCheckTheme {
        MacroRow(protein = 150, carbs = 200, fat = 65)
    }
}
```

## Hilt DI

### Module Organization

```kotlin
// Network module
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}

// Database module
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChonkCheckDatabase {
        return Room.databaseBuilder(
            context,
            ChonkCheckDatabase::class.java,
            "chonkcheck.db"
        ).build()
    }
}
```

## Room Database

### Entity Example

```kotlin
@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val brand: String?,
    val servingSize: Double,
    val servingUnit: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val type: String, // "platform" or "user"
    val syncedAt: Long? = null
)
```

### DAO Pattern

```kotlin
@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%'")
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FoodEntity>)

    @Query("DELETE FROM foods WHERE syncedAt < :threshold")
    suspend fun deleteStale(threshold: Long)
}
```

## Error Handling

### Result Pattern

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}

// Usage in repository
suspend fun getFoods(query: String): Result<List<Food>> {
    return try {
        val foods = api.listFoods(query)
        Result.Success(foods.map { it.toDomain() })
    } catch (e: Exception) {
        Sentry.captureException(e)
        Result.Error(e)
    }
}
```

## Design System

### Brand Colors

| Color | Hex | Usage |
|-------|-----|-------|
| Vivid Green 500 | `#22c55e` | Primary brand color |
| Vivid Green 600 | `#16a34a` | Primary dark/pressed |
| Vivid Green 400 | `#4ade80` | Primary light |
| Coral | `#f97316` | Accent, warnings |
| Amber | `#f59e0b` | Carbs macro color |
| Blue | `#3b82f6` | Protein macro color |
| Red | `#ef4444` | Fat macro color, errors |

### Semantic Colors (Light Mode)

| Token | Value | Usage |
|-------|-------|-------|
| Background | `#fafafa` | Page background |
| Surface | `#ffffff` | Card background |
| Surface Muted | `#f4f4f5` | Secondary surfaces |
| Border | `#e4e4e7` | Card/input borders |
| Foreground | `#18181b` | Primary text |
| Foreground Muted | `#71717a` | Secondary text |

### Semantic Colors (Dark Mode)

| Token | Value | Usage |
|-------|-------|-------|
| Background | `#09090b` | Page background |
| Surface | `#18181b` | Card background |
| Surface Muted | `#27272a` | Secondary surfaces |
| Border | `#3f3f46` | Card/input borders |
| Foreground | `#fafafa` | Primary text |
| Foreground Muted | `#a1a1aa` | Secondary text |

### Typography

- **Font Family**: Inter (Google Fonts)
- **Display**: Inter SemiBold, 36sp
- **Headline**: Inter SemiBold, 24-28sp
- **Title**: Inter Medium, 16-22sp
- **Body**: Inter Regular, 14-16sp
- **Label**: Inter Medium, 12-14sp

### Sizing & Touch Targets

- **Minimum touch target**: 44x44 dp
- **Button height**: 52dp
- **Input height**: 56dp
- **Card padding**: 16-20dp
- **Screen padding**: 24dp

### Border Radius

| Element | Radius |
|---------|--------|
| Buttons | 8dp (medium) |
| Cards | 12dp (large) |
| Inputs | 8dp (medium) |
| Chips | 8dp (medium) |

### Copy Guidelines

- **Button text with arrows**: Use arrow suffix for forward actions (e.g., "Start →", "Continue →", "Start Tracking →")
- **Macro display format**: "150g P • 200g C • 65g F"
- **Calorie display**: "2,400 cal"
- **Weight display**: "75.5 kg" or "166.5 lb" or "11 st 12 lb"

## Code Style

- **Kotlin conventions** - Follow Kotlin coding conventions
- **ktlint** - Zero lint warnings
- **Explicit types** - Prefer explicit return types for public functions
- **No `!!`** - Use safe calls and elvis operator instead of force unwrap
- **DRY** - Extract shared logic into extension functions or utilities
- **No unused code** - Delete dead code, don't comment it out
- **Small functions** - Keep functions focused, extract helpers

## Dependency Management

- **Always use latest versions** - When adding or updating dependencies, always check for the most recent stable version. Never use outdated dependencies.
- **Check before implementing** - Before implementing features that rely on a specific library, verify you're using the latest version and check for any API changes.
- **Version catalog** - All dependencies are managed in `gradle/libs.versions.toml`
- **Web search for versions** - Use web search to find the latest version of any dependency before adding or troubleshooting issues.

## Testing

### Unit Tests

```kotlin
@Test
fun `calculateMacros returns correct totals`() {
    val entries = listOf(
        DiaryEntry(calories = 200, protein = 20, carbs = 10, fat = 5),
        DiaryEntry(calories = 300, protein = 30, carbs = 20, fat = 10)
    )

    val result = calculateMacros(entries)

    assertEquals(500, result.calories)
    assertEquals(50, result.protein)
    assertEquals(30, result.carbs)
    assertEquals(15, result.fat)
}
```

### UI Tests

```kotlin
@Test
fun diaryScreen_showsEntries_whenLoaded() {
    composeTestRule.setContent {
        DiaryScreen(entries = testEntries)
    }

    composeTestRule.onNodeWithText("Breakfast").assertIsDisplayed()
    composeTestRule.onNodeWithText("Chicken Breast").assertIsDisplayed()
}
```

## Git Commit Messages

- **Keep commits concise** - Short, focused commit messages
- **NEVER mention Claude** - Do not include "Claude", "AI", or related references
- **NO Co-Authored-By lines** - Never add trailer lines
- **Use conventional format** - `feat:`, `fix:`, `refactor:`, `docs:`, `chore:`

## Shared Skills Plugin

ChonkCheck shared skills (UX design, copywriter) are installed as a Claude Code plugin:
- **Repository**: https://github.com/Pixel-Harmony/chonkcheck-claude-skills

The plugin is auto-installed via `.claude/settings.json` - no manual setup required.

**Available skills:**
- `/chonkcheck-skills:ux-design` - UX design guidance
- `/chonkcheck-skills:copywriter` - Copy refinement with brand voice
- `/developer` - Android development (local skill)

## Project Setup

1. Clone repository
2. Open in Android Studio (Koala or later)
3. Sync Gradle
4. Create `local.properties` with:
   ```
   AUTH0_DOMAIN=pixelharmony.eu.auth0.com
   AUTH0_CLIENT_ID=<your-client-id>
   AUTH0_AUDIENCE=<your-api-identifier>
   API_URL=https://app.chonkcheck.com/api/
   ```
5. Run on device/emulator

## Running Gradle Commands

When running Gradle from the command line, use `./gradlew.bat` (not `gradlew.bat` or `./gradlew`):

```bash
# Build debug APK
./gradlew.bat assembleDebug

# Run tests
./gradlew.bat test

# Run lint
./gradlew.bat lint
```

## Files to Know

- `app/build.gradle.kts` - App-level Gradle config
- `app/src/main/java/.../di/` - Hilt modules
- `app/src/main/java/.../data/api/` - Retrofit interfaces
- `app/src/main/java/.../data/db/` - Room database
- `app/src/main/java/.../presentation/ui/` - Compose screens
- `app/src/main/java/.../presentation/navigation/` - Navigation graph
