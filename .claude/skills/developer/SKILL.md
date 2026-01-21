# Android Developer Skill

Expert native Android development for ChonkCheck. Implements features using Kotlin, Jetpack Compose, and modern Android architecture.

## Activation

Use when: Implementing features, building UI, creating APIs, writing production code.

Triggers: "Implement...", "Build...", "Create...", "Add feature..."

Manual: `/developer`

## Core Principles

### DRY (Don't Repeat Yourself)
- Extract shared logic into extension functions
- Create reusable Compose components
- Use base classes for common ViewModel patterns
- Share mappers and utilities across features

### Component Extraction
- Keep composables small (<100 lines ideally)
- Extract reusable UI components
- Create feature-specific component files
- Preview all composables

### Code Quality
- Zero ktlint warnings
- No `!!` force unwraps
- Explicit return types for public functions
- No unused code or comments

## Implementation Patterns

### ViewModel
```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val useCase: FeatureUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
}
```

### Composable Screen
```kotlin
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Handle state
}
```

### Repository
```kotlin
class FeatureRepositoryImpl @Inject constructor(
    private val api: FeatureApi,
    private val dao: FeatureDao
) : FeatureRepository {
    override fun getItems(): Flow<List<Item>> = dao.getAll()
        .onStart { refreshFromNetwork() }
}
```

## Workflow

1. **Understand** - Read existing code, understand patterns
2. **Plan** - Identify files to create/modify
3. **Implement** - Write code following patterns
4. **Test** - Add unit tests for business logic
5. **Review** - Check for lint warnings, unused code

## Reference

See CLAUDE.md for:
- Full architecture documentation
- API integration details
- Compose patterns
- Hilt module organization
- Room database patterns
