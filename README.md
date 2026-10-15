# Parle French

A French-learning Android app. The **interface is in English** (the learner's
native language) while everything the learner studies — vocabulary, dialogues,
example sentences and writing tasks — stays in **French**.

Built with Kotlin and Jetpack Compose (Material 3), offline-first with Room,
and backed by Firebase (Auth, Firestore, App Check, Firebase AI / Gemini).

## Features

| Module | What it does |
| --- | --- |
| **Home** | Fluency dashboard, streak, due-card count and quick access to every module. |
| **Roleplay** | Six AI conversation scenarios (café, train station, job interview, catching up, pharmacy, debate) with live replies, English translations, suggested replies and tutor grammar feedback. |
| **Spaced Repetition** | SM-2 flashcards with a 3D flip, Again/Hard/Good/Easy grading, category filters, sample deck and a "add your own word" dialog. |
| **Vision & Real Objects** | Point the camera or pick a photo; the AI names the objects in French with gender, IPA, CEFR level and a contextual sentence, then saves them as flashcards. |
| **Exams & DELF** | DELF/DALF-style writing prompts graded against the official 25-point rubric, plus a grammar quiz sprint. |
| **Profile & Sync** | Google Sign-In, manual cloud sync, target CEFR level, and an explanation of the offline/cloud data model. |

## Project layout

```
app/src/main/java/com/example/
├── MainActivity.kt              # Navigation shell (bottom nav)
├── data/
│   ├── ai/FrenchAiService.kt    # Gemini prompts + parsing (roleplay, vision, grading)
│   ├── local/                   # Room entities, DAOs, database + seed deck
│   └── repository/              # Offline-first repository + Firestore sync
├── domain/                      # SM-2 scheduler, French TTS manager
├── firebase/                    # App Check + Firestore error reporting
└── ui/
    ├── theme/                   # Colour, typography, Material theme
    ├── components/              # Shared badges, audio button, tricolour stripe
    ├── screens/                 # One composable screen per module
    └── <feature>/               # ViewModels
```

## Building

The repo builds a debug APK on every push via
`.github/workflows/build-apk.yml` (Actions → run → Artifacts).

Locally, with a JDK 17 and the Android SDK installed:

```bash
gradle assembleDebug
```

`app/google-services.json` is committed, so Firebase initialises out of the box.
App Check runs in debug mode for debug builds and uses reCAPTCHA for release.
