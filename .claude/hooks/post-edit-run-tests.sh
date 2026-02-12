#!/usr/bin/env bash
set -euo pipefail

# Only run tests when Kotlin/Java source files are edited
INPUT=$(cat /dev/stdin)
FILE=$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // ""')

# Skip non-source files (docs, configs, skills, etc.)
if [[ ! "$FILE" =~ \.(kt|java)$ ]]; then
  exit 0
fi

# Skip files outside the Android project
if [[ "$FILE" != *"android/"* ]]; then
  exit 0
fi

cd android
if ! ./gradlew test --quiet 2>/dev/null; then
  echo "⚠️ Unit tests are failing after this edit."
fi

exit 0
