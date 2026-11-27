# Unified GitHub Codespace

Create the Codespace from the `merge/unified-launcher` branch of this repository.

The dev container uses Java 21 and GitHub CLI. On first creation, its setup script checks out the five companion repositories beside TagDeckLauncher and creates:

`/workspaces/TagDeck-Unified.code-workspace`

Open that workspace file in the Codespace editor to work across all six repositories at once.

## Repository roles

- **TagDeckLauncher** — active unified Android launcher. Make product changes here.
- **tagdecktomerge**, **tagdecktomerge2**, **my-niagara-clone** — placeholder/reference repositories.
- **tagdecktomerge3** — Cascade/Niagara-style implementation reference.
- **Stario** — GPL-3.0 feature reference. Do not copy its code or assets into TagDeck unless the distribution is deliberately made GPL-compliant.

The companion repositories are cloned as separate folders; their Git histories and remotes remain independent. This setup does not merge or modify their default branches.

## Build

The repository's GitHub Actions workflows run Android unit tests and build the beta APK. The Codespace is configured for editing with Java/Kotlin support; use the repository's Gradle wrapper and CI workflow for the authoritative Android SDK build.
