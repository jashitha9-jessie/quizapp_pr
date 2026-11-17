# Quiz App (Kotlin, Android)

Join a quiz by scanning a QR code (or pick category + difficulty), answer 10 questions, and see your score history.

## Features
- Firebase Authentication with Google Sign-In
- Google Code Scanner (`play-services-code-scanner`), QR text format: `category=9;difficulty=easy`
- Open Trivia DB (no key): categories + 10 multiple-choice questions
- One question at a time, 4 shuffled options, HTML entities decoded
- Selected option turns green (correct) or red (wrong, correct one is shown in green)
- Progress bar for questions 1 to 10
- Result screen with score and **Play Again**
- Score history (date, category, score) saved with **Room**, shown in a **RecyclerView**

## Setup (required: Firebase)
1. Open this folder in **Android Studio** (Koala or newer) and let Gradle sync.
2. Go to https://console.firebase.google.com and create a project.
3. Add an **Android app** with package name `com.example.quizapp`.
4. Add your **SHA-1** fingerprint (Android Studio: Gradle panel > app > Tasks > android > signingReport, or run `./gradlew signingReport`).
5. Firebase console > **Authentication > Sign-in method > Google > Enable**.
6. Download **google-services.json** and put it in the `app/` folder (next to `app/build.gradle.kts`).
7. Sync Gradle and run on a **real device or an emulator with Google Play services**.

> If you change the package name, change it in `app/build.gradle.kts` (namespace + applicationId) and in Firebase too.

## Test QR codes
Create a QR code (any online QR generator, "text" type) with this exact text:

```
category=9;difficulty=easy
```

Some category ids: 9 General Knowledge, 17 Science & Nature, 18 Computers, 21 Sports, 22 Geography, 23 History.

## Project structure
```
app/src/main/java/com/example/quizapp/
  data/api/   ApiModels.kt, TriviaApi.kt        (Retrofit + Open Trivia DB)
  data/db/    ScoreEntity, ScoreDao, AppDatabase (Room)
  model/      Question.kt
  ui/         LoginActivity, HomeActivity, QuizActivity,
              ResultActivity, HistoryActivity, HistoryAdapter
  util/       QrParser, HtmlUtils, GoogleAuthHelper, Extras
app/src/main/res/layout/   activity_*.xml, item_history.xml
```

## Notes
- Open Trivia DB allows about 1 request per 5 seconds per IP. If you see "Too many requests", wait and tap Retry.
- Some category + difficulty combinations have fewer than 10 questions; the app shows a message in that case.
