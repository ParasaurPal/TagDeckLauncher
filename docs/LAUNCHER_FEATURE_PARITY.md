# Launcher Feature Parity Migration

TagDeckLauncher remains the product and architectural foundation. This document is the acceptance inventory for compatible capabilities from Stario and Total Launcher. Implement native Android/Jetpack Compose equivalents; do not copy proprietary source code, branding, assets, or theme packs.

## Product invariants

- Preserve TagDeck's tagging, smart rules, category hierarchy, page editor, windows, sidebars, widgets, themes, app customization, and backup/restore.
- Preserve existing user data and backup compatibility. Every new persisted setting or object must have a versioned migration and round-trip backup coverage.
- Keep optional capabilities opt-in and request runtime/special permissions only when a user invokes the related feature.
- All user-facing features must have working UI, persistence, lifecycle handling, accessibility labels, empty/error states, and tests. A settings toggle or placeholder screen alone does not count as implementation.
- Build and validate the Android APK after each feature batch; attach the APK to the repository's main download surface/release workflow.

## Stario parity

- [ ] Full notification feed page: notification listener ingestion, grouped timeline, app filters, clear/dismiss actions where Android permits, privacy controls, glass visual treatment, unread state, and page selection.
- [ ] RSS/news briefing: multiple feed sources, add/edit/remove feeds, refresh, article list/detail, offline cache, error states, and configurable page/widget.
- [ ] Search providers: selectable providers, web suggestions/results, provider-specific settings, safe external intents, and persistence.
- [ ] Niagara-style app drawer: alphabetical wave navigation, fast scrolling, favorites, app hiding, category/tag filtering, and configurable icon/label styles.
- [ ] Keypad/T9 search across installed apps and supported system targets: contacts, settings, app shortcuts, and searchable launcher actions; handle unavailable targets gracefully.
- [ ] Expanded icon library and manual per-app assignment; monochrome, inverted monochrome, adaptive dark/light and colored variants; preserve assignments across package updates.
- [ ] App customization parity: labels, icons, sorting, categories, favorites, hidden apps, and shortcut handling.
- [ ] Full backup/restore of all launcher configuration, customizations, assets and feature settings with validation, conflict handling, and backward compatibility.
- [ ] Permission center that explains and requests each feature's required Android permission/access at point of use.

## Total Launcher parity

### Editing and object model
- [ ] True freeform object canvas with drag, resize, rotate, scale, skew/3D transform, alignment guides, snapping, margins and precise numeric positioning.
- [ ] Multi-select, marquee selection, group/ungroup, copy/cut/paste, duplicate, z-order, lock/hide, undo/redo and reusable saved objects.
- [ ] Object types: text, image, mixed graphic, shape, app shortcut, folder, widget, layout, dynamic data graphic, and interactive control.
- [ ] Per-object appearance: typography, custom fonts, gradients, strokes, shadows, masks, opacity, clipping, blend/transform options and backgrounds.
- [ ] Per-object actions: tap, long press, directional swipes, multiple tap actions, visibility and touchability rules, launch animations and optional sound.

### Pages, containers and layouts
- [ ] Add/remove/rename/reorder pages, set home page, duplicate/swap pages, hide pages and configure page indicators.
- [ ] Independent portrait/landscape layouts, responsive screen ratios, foldable/tablet layouts, horizontal and vertical scrolling.
- [ ] Grid, circle, freeform, list and mixed layouts with configurable dimensions, spacing and alignment.
- [ ] Floating windows/popups with size, position, animation, blur, transparency, stacking and gesture/shortcut triggers.
- [ ] Four-edge sliding drawers with handles, independent content, animation and gesture configuration.
- [ ] Folders/containers with custom masks, layouts, backgrounds, blur and nested organization.

### Gestures, actions and automation
- [ ] Configurable global gestures: double/triple tap, directional swipes, two-finger swipes, pinch, and supported sensor gestures.
- [ ] Landscape-specific gesture mappings, haptic/visual feedback and sensitivity settings.
- [ ] Launcher action catalog for page navigation, edit mode, menus, windows/drawers, app launch, search, screen lock and supported device settings.
- [ ] User-defined action sequences with ordering, delays, cancellation and safe failure handling.

### Widgets and dynamic content
- [ ] Native and Android AppWidgetHost support; add/configure/resize/remove third-party widgets on pages, windows and drawers.
- [ ] App drawer, app group, contacts, analog clock, media controller, page indicator, resource/circle indicators, compass, drawer handle, photo slideshow and RSS reader.
- [ ] Dynamic text/images/data sources for date/time, calendar events, notifications and supported device state.
- [ ] Widget stacking and trigger-based switching between widget windows.

### Themes, assets and sharing
- [ ] Theme/resource manager for images, shapes, shadows, sounds and typefaces; reusable user objects and theme import/export.
- [ ] Wallpaper and per-page/container backgrounds; image library with safe asset cleanup.
- [ ] Complete backup/restore including pages, object geometry, actions, resources, fonts, widgets' placeholders/configuration where Android allows, and settings.

## Quality gates

- [ ] Unit tests for data models, action sequences, migrations, backup encode/decode and search matching.
- [ ] UI/instrumentation tests for editor interactions, drawer gestures, page navigation, notification feed and widget lifecycle.
- [ ] Build debug and release APK; install/launch smoke test on Android API 35+.
- [ ] Verify fresh install, upgrade from existing TagDeck data, backup/restore round trip, rotation, process death and permission denial.
- [ ] Verify no feature claims are marked complete until the user-facing flow is implemented and tested on device.

## Implementation sequencing

1. Data model/versioned migrations and feature action registry.
2. Freeform editor/object manipulation and reusable objects.
3. Pages, layouts, windows, drawers and folders.
4. Object/global gestures and action sequences.
5. Notification feed and RSS briefing.
6. Search providers and expanded system-wide searchable targets.
7. Widgets/dynamic content and icon library parity.
8. Theme/resource manager, import/export and backup coverage.
9. End-to-end regression, APK build and install smoke tests.

Status: inventory created; feature implementation and device validation remain outstanding until each checklist item is completed and tested.
