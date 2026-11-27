# Unified launcher integration

## Product rule

There is one launcher, one Android application ID (`com.tagdeck.launcher`), one settings system, one app database, and one build pipeline. TagDeck is the host architecture. Features from the other launcher projects should be adapted to TagDeck's Compose UI, Room database, DataStore preferences, and existing app-discovery services rather than introducing a second launcher activity or a second app database.

## Source inventory

| Source | Role in the unified launcher | Integration approach |
|---|---|---|
| TagDeckLauncher | Primary product and architecture | Keep as the base; preserve its tag graph, freeform home studio, sidebars, widgets, icon tooling, backup, and settings. |
| tagdecktomerge3 | User-owned Cascade/Niagara-style launcher prototype | Source and assets may be directly ported and adapted: wave alphabet navigation, compact app rows, category icon suggestions, notification center, favorites editor, quick math, wallpaper palette extraction, and icon catalog. Consolidate into TagDeck's architecture. |
| Stario | Excluded by user request | Do not merge Stario source, assets, or feature set unless the user later asks. |
| tagdecktomerge, tagdecktomerge2, my-niagara-clone | User-owned repositories with README-only content on their current default branches | Keep as part of the project inventory; there is no implementation content to import from their current default branches. |

## Feature inventory

### Already present in TagDeck

- Freeform home pages and drag/drop element editing.
- Folders, widgets, page management, element layering, duplicate/copy/paste, undo/redo, and snapshots.
- Tags, categories, smart rules, tag-driven sidebars, configured sidebars, floating windows, and app assignment.
- Niagara-style alphabet app drawer and universal search.
- Custom app names/icons, icon packs, line icons, Anycons, wallpaper and typography controls.
- App locks, notification badge counts, local backup, and optional cloud backup infrastructure.
- Settings catalog and global search across launcher configuration and content.

### Added on the unified branch

- Reusable T9 keypad encoder with numeric matching in both the universal search overlay and app drawer.
- Opt-in contacts search, including matching contact names and phone numbers with T9; permission is requested only when the user enables it.
- User-selected file search using Android's document picker and persisted read grants; TagDeck does not scan shared storage.
- Unit tests for keypad encoding, separator handling, and matching.
- In-search calculator with operator precedence, parentheses, division/modulo, and Unicode math operators; expression evaluation is covered by unit tests.
- Notification center overlay with user-controlled Android notification access, live in-memory notification summaries, open/dismiss actions, clear-all, and a privacy note. Notification contents are not written to launcher storage or backups.

### Planned native integrations

- Full notification center with per-app grouping, notification opening and dismissal, and a clear permission/retention explanation.
- Expand file search with optional user-selected folders and removable saved sources.
- Optional compact Niagara home mode alongside the existing freeform home studio.
- Pinned/favorite app groups and configurable app collections.
- RSS/news briefing panel with user-managed feeds.
- Media session panel and playback controls.
- Weather and calendar glance panels.
- More comprehensive category-based icon suggestions and editable icon assignments.
- Search quick calculator and command/action results.
- Gesture/action editor with consistent global shortcuts.

## Integration constraints

1. Preserve TagDeck's existing Room schema and migrate it only when necessary.
2. Do not create parallel settings stores or duplicate app discovery.
3. Keep optional permissions opt-in: contacts, files, notifications, calendar, and location are not requested until the related feature is enabled.
4. Avoid reading or retaining notification contents unless the user explicitly enables the notification center.
5. Keep all core launcher functionality usable without accounts, API keys, or subscriptions.
6. Test on the existing minimum SDK and the current Pixel/Android target before merging to `main`.

## Licensing and provenance

Stario's repository declares GNU GPL-3.0. This branch uses Stario as a feature/UX reference and does not copy its implementation or assets. Any future decision to port Stario source code must first preserve its license notices and satisfy GPL-3.0 obligations for the resulting distribution. The Cascade prototype's repository did not expose a clear license file during inspection; do not copy its source or assets unless its license is clarified. Implement the desired behavior independently or obtain permission before source reuse.
