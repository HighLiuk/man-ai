#!/bin/bash
set -euo pipefail

_worktree_add() {
  local SCRIPT_DIR
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  local PROJECT_DIR
  PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

  # Parse arguments
  local BRANCH=""
  local BASE_REF="HEAD"

  while [[ $# -gt 0 ]]; do
    case "$1" in
      --base)
        BASE_REF="${2:?'--base requires a ref argument'}"
        shift 2
        ;;
      -*)
        echo "Error: unknown option '$1'" >&2
        echo "Usage: source scripts/worktree-add.sh <branch-name> [--base <ref>]" >&2
        return 1
        ;;
      *)
        if [[ -z "$BRANCH" ]]; then
          BRANCH="$1"
          shift
        else
          echo "Error: unexpected argument '$1'" >&2
          echo "Usage: source scripts/worktree-add.sh <branch-name> [--base <ref>]" >&2
          return 1
        fi
        ;;
    esac
  done

  if [[ -z "$BRANCH" ]]; then
    echo "Error: branch name is required" >&2
    echo "Usage: source scripts/worktree-add.sh <branch-name> [--base <ref>]" >&2
    return 1
  fi

  # Sanitize branch name for directory: / → -
  local SAFE_BRANCH="${BRANCH//\//-}"
  local WORKTREE_DIR
  WORKTREE_DIR="$(cd "$PROJECT_DIR/.." && pwd)/man-ai--${SAFE_BRANCH}"

  # Guard: directory already exists
  if [[ -d "$WORKTREE_DIR" ]]; then
    echo "Error: directory already exists: $WORKTREE_DIR" >&2
    echo "Remove it first or choose a different branch name." >&2
    return 1
  fi

  # Guard: branch already checked out in another worktree
  if git -C "$PROJECT_DIR" worktree list --porcelain | grep -q "branch refs/heads/${BRANCH}$"; then
    echo "Error: branch '$BRANCH' is already checked out in another worktree:" >&2
    git -C "$PROJECT_DIR" worktree list | grep -v "bare" >&2
    return 1
  fi

  # Determine branch strategy
  local LOCAL_EXISTS=false
  local REMOTE_EXISTS=false

  if git -C "$PROJECT_DIR" show-ref --verify --quiet "refs/heads/${BRANCH}" 2>/dev/null; then
    LOCAL_EXISTS=true
  fi
  if git -C "$PROJECT_DIR" show-ref --verify --quiet "refs/remotes/origin/${BRANCH}" 2>/dev/null; then
    REMOTE_EXISTS=true
  fi

  echo "Creating worktree at $WORKTREE_DIR ..."

  if [[ "$LOCAL_EXISTS" == true ]] || [[ "$REMOTE_EXISTS" == true ]]; then
    # Branch exists (locally or remotely) — git handles tracking automatically
    git -C "$PROJECT_DIR" worktree add "$WORKTREE_DIR" "$BRANCH"
  else
    # New branch from BASE_REF
    echo "Branch '$BRANCH' does not exist, creating from $BASE_REF ..."
    git -C "$PROJECT_DIR" worktree add -b "$BRANCH" "$WORKTREE_DIR" "$BASE_REF"
  fi

  # Copy expensive non-versioned files
  echo ""
  echo "Copying cached files ..."

  local COPY_ITEMS=(
    ".claude/hooks/detekt-cli-*.jar"
    ".claude/hooks/detekt-classpath.txt"
    "android/.gradle/"
    "android/.kotlin/"
    "local.properties"
    "docs/plans/"
  )

  local TOTAL_SIZE=0

  for item in "${COPY_ITEMS[@]}"; do
    local SRC="$PROJECT_DIR/$item"

    # Handle glob patterns
    local EXPANDED
    EXPANDED=$(compgen -G "$SRC" 2>/dev/null || true)

    if [[ -z "$EXPANDED" ]]; then
      echo "  skip: $item (not found)"
      continue
    fi

    for src_path in $EXPANDED; do
      local REL_PATH="${src_path#"$PROJECT_DIR"/}"
      local DEST="$WORKTREE_DIR/$REL_PATH"
      local DEST_DIR
      DEST_DIR="$(dirname "$DEST")"

      mkdir -p "$DEST_DIR"

      if [[ -d "$src_path" ]]; then
        rsync -a "$src_path/" "$DEST/"
      else
        rsync -a "$src_path" "$DEST"
      fi

      # Calculate size
      local SIZE
      SIZE=$(du -sh "$src_path" 2>/dev/null | cut -f1)
      echo "  copy: $REL_PATH ($SIZE)"
    done
  done

  # Summary
  echo ""
  echo "=== Worktree ready ==="
  echo "  Branch:   $BRANCH"
  echo "  Path:     $WORKTREE_DIR"

  # Check detekt status
  if compgen -G "$WORKTREE_DIR/.claude/hooks/detekt-cli-*.jar" > /dev/null 2>&1; then
    echo "  detekt:   ready"
  else
    echo "  detekt:   NOT found (run scripts/setup-detekt.sh)"
  fi

  # Check gradle cache
  if [[ -d "$WORKTREE_DIR/android/.gradle" ]]; then
    local GRADLE_SIZE
    GRADLE_SIZE=$(du -sh "$WORKTREE_DIR/android/.gradle" 2>/dev/null | cut -f1)
    echo "  gradle:   cached ($GRADLE_SIZE)"
  else
    echo "  gradle:   no cache"
  fi

  echo ""

  # cd into the worktree (works because script is sourced)
  cd "$WORKTREE_DIR"
  echo "Now in: $(pwd)"
}

_worktree_add "$@"
