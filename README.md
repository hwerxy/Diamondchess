# Diamond Chess — Android prototype v0.1

Native Android source project (Kotlin + Jetpack Compose). No network permission, no backend.

## Included
- Local two-player chess on one device.
- Experimental offline computer opponent with 10 difficulty settings (heuristic, **not** calibrated Elo).
- Legal move generation with check, checkmate, stalemate, castling, en passant, auto-queen promotion.
- Undo and coordinate move log.
- Settings for when brilliant-move markers should appear (UI preference only, not yet calculated).
- Placeholder review view that clearly discloses absence of engine analysis.

## Not implemented yet
- Stockfish engine, accurate Elo bots, real game review / accuracy / brilliant moves, saved history, PGN, time controls, threefold repetition and 50-move rule, promotion selection, UI animations, full rules testing.
- This is not a production-ready chess app.

## Build
1. Open this folder in Android Studio with JDK 17 and Android SDK 35 installed.
2. Allow Gradle sync (requires internet to fetch dependencies).
3. Build > Build APK(s). APK will appear under `app/build/outputs/apk/debug/`.

The environment used to create this project did not have Android SDK or Gradle, so APK compilation and device tests have not been performed.

## License and identity
Original app name and UI. Not affiliated with Chess.com. Unicode chess symbols are system-font dependent.
