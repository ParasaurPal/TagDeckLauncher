#!/usr/bin/env bash
set -euo pipefail

ROOT="${PWD}"
WORKSPACE_ROOT="$(dirname "$ROOT")"

# Keep each original repository separate so changes remain attributable.
# TagDeckLauncher is the host application; the others are side-by-side references.
REPOS=(
  "tagdecktomerge"
  "tagdecktomerge2"
  "tagdecktomerge3"
  "my-niagara-clone"
)

for repo in "${REPOS[@]}"; do
  target="$WORKSPACE_ROOT/$repo"
  if [ -d "$target/.git" ]; then
    echo "Already present: $repo"
  else
    echo "Cloning ParasaurPal/$repo ..."
    gh repo clone "ParasaurPal/$repo" "$target"
  fi
done

cat > "$WORKSPACE_ROOT/TagDeck-Unified.code-workspace" <<'WORKSPACE'
{
  "folders": [
    { "name": "TagDeckLauncher (active unified app)", "path": "TagDeckLauncher" },
    { "name": "tagdecktomerge", "path": "tagdecktomerge" },
    { "name": "tagdecktomerge2", "path": "tagdecktomerge2" },
    { "name": "tagdecktomerge3 (Cascade reference)", "path": "tagdecktomerge3" },
    { "name": "my-niagara-clone", "path": "my-niagara-clone" },
  ],
  "settings": {
    "search.exclude": {
      "**/.git": true,
      "**/.gradle": true,
      "**/build": true
    }
  }
}
WORKSPACE

echo
echo "Workspace prepared: $WORKSPACE_ROOT/TagDeck-Unified.code-workspace"
echo "Open that workspace file in VS Code to work across all five repositories."
echo "The TagDeckLauncher folder is the active unified application."
