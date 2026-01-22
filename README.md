# ChonkCheck Android

Native Android calorie tracking app built with Kotlin and Jetpack Compose.

## Tech Stack

- **Language**: Kotlin 2.x
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp
- **Serialization**: Kotlinx Serialization
- **Database**: Room (offline support)
- **Auth**: Auth0 Android SDK
- **Barcode**: ML Kit Barcode Scanning
- **Billing**: Google Play Billing 7.x
- **Analytics**: Sentry Android SDK

## Features

- Track daily food intake with macro calculations
- Scan barcodes to lookup foods
- Photograph nutrition labels to auto-create food items
- Create and save recipes
- Save meal combinations for quick diary entry
- Track weight over time with graphs
- Offline-first with cloud sync

## Setup

1. Clone this repository
2. Open in Android Studio (Koala or later)
3. Sync Gradle
4. Create `local.properties`:
   ```properties
   AUTH0_DOMAIN=pixelharmony.eu.auth0.com
   AUTH0_CLIENT_ID=<your-client-id>
   API_URL=https://app.chonkcheck.com/api/
   ```
5. Run on device/emulator

## Claude Code

This project uses Claude Code for AI-assisted development. See `CLAUDE.md` for development guidelines and context.

### Shared Skills

UX design and copywriter skills are in a separate repository:
```bash
git clone https://github.com/Pixel-Harmony/chonkcheck-claude-skills.git
```

## API

The backend API documentation is available at:
- OpenAPI spec: `apps/backend/openapi.yaml` (in main ChonkCheck repo)
- Swagger UI: https://app.chonkcheck.com/api/docs

## License

Proprietary - Pixel Harmony
