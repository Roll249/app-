# Build Guide

## Backend

### Prerequisites
- Java 18+
- Gradle 8.6+
- PostgreSQL 15+
- Redis 7+

### Setup Database

```bash
# Create database
psql -U postgres -c "CREATE DATABASE fintech_db;"

# Run migrations
psql -U postgres -d fintech_db -f src/main/resources/db/migration/V1_initial_schema.sql
```

### Build & Run

```bash
cd backend

# Build
./gradlew build

# Run
./gradlew run

# Or run directly with Java
java -jar build/libs/fintech-backend-1.0.0.jar
```

### Ngrok Setup

```bash
# Install ngrok if needed
# Then run:
ngrok http 3000 --domain=acrobat-equate-emphasize.ngrok-free.dev
```

## Android Client

### Prerequisites
- Android Studio Hedgehog+
- JDK 17+
- Android SDK 34

### Build

```bash
cd android-client

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

### Install on Device

```bash
# Via ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Via Android Studio
# Run > Run 'app'
```

### Connect to Backend

Update `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "BASE_URL", "\"https://your-ngrok-url.ngrok-free.dev/api/v1/\"")
```

## Environment Variables

### Backend (.env)
```
DATABASE_URL=jdbc:postgresql://localhost:5432/fintech_db
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET=your-secret-key
REDIS_URL=redis://localhost:6379
```

## Testing

### Backend
```bash
./gradlew test
```

### Android
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Troubleshooting

### Backend
- Database connection error: Kiểm tra PostgreSQL đã chạy chưa
- Port 3000 đã sử dụng: `lsof -i :3000`

### Android
- Build failed: Clean project (`./gradlew clean`)
- Network error: Kiểm tra ngrok URL và network security config
