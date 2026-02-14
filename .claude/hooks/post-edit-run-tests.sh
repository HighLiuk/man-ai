#!/usr/bin/env bash

exec 2>/dev/null

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')

if [[ "$FILE_PATH" != *.kt && "$FILE_PATH" != *.java ]]; then
  echo '{"systemMessage": "⚠️ Not a Kotlin/Java file. Skipping tests."}'
  exit 0
fi

cd "$CLAUDE_PROJECT_DIR/android" && ./gradlew test --quiet >/dev/null 2>&1
EXIT_CODE=$?

if [[ "$FILE_PATH" == */test/* || "$FILE_PATH" == */androidTest/* ]]; then
  if [ $EXIT_CODE -ne 0 ]; then
    echo '{"systemMessage": "🔴 RED confirmed. Test fails as expected."}'
  else
    echo '{"systemMessage": "⚠️ Test PASSES already — rewrite it."}'
  fi
else
  if [ $EXIT_CODE -eq 0 ]; then
    echo '{"systemMessage": "🟢 GREEN confirmed. Tests pass."}'
  else
    echo '{"systemMessage": "❌ Tests FAILED. Fix before proceeding."}'
  fi
fi

exit 0
