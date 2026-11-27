# TagDeck Launcher

A highly configurable Android launcher combining TagDeck's freeform home studio and tag-driven organization with a fast Niagara-inspired app-browsing experience.

## Current foundation

- Freeform, multi-page home screen with draggable elements, folders, widgets, layer ordering, undo/redo, and layout snapshots.
- Many-to-many app tags, categories, smart rules, custom sidebars, and floating windows.
- App discovery, app visibility controls, custom icons, icon packs, line-icon studio, and Anycons glyph rendering.
- Niagara-style A–Z app browsing and a universal search overlay for apps, tags, categories, settings, pages, sidebars, folders, and widgets.
- Theme, wallpaper, typography, icon, gesture, status-bar, and navigation-bar customization.
- Local backup and optional cloud-backup infrastructure.
- Notification badges without collecting notification message content.
- T9 keypad matching in universal app search: numeric queries such as `843` can match names such as “The…”.

## Build

Open the repository in Android Studio or run the included Gradle wrapper:

```bash
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Integration direction

TagDeck remains the single application and data model. Niagara/Cascade-style browsing and Stario-inspired panels are integrated as features of this launcher, not as competing launcher applications. See [docs/UNIFIED_LAUNCHER.md](docs/UNIFIED_LAUNCHER.md) for the feature inventory, integration boundaries, and next steps.

## Repository

The unified development branch is `merge/unified-launcher`. The original repositories and `main` branch are left unchanged while the combined build is developed and tested.
