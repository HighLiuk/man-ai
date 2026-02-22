#!/bin/bash
# Download ONNX models from the latest manga-ai-models release into Android assets.
# Requires: wget
# In CI, set GH_TOKEN for higher rate limits.

set -euo pipefail

REPO="ManAI-Reader/manga-ai-models"
DEST="$(git rev-parse --show-toplevel)/android/app/src/main/assets/models"

mkdir -p "$DEST"

# Fetch asset URLs from latest release
API_URL="https://api.github.com/repos/$REPO/releases/latest"
AUTH_HEADER=""
if [[ -n "${GH_TOKEN:-}" ]]; then
  AUTH_HEADER="Authorization: Bearer $GH_TOKEN"
fi

echo "Fetching latest release info from $REPO..."
ASSETS=$(wget -qO- ${AUTH_HEADER:+--header="$AUTH_HEADER"} "$API_URL" \
  | python3 -c "
import json, sys
for a in json.load(sys.stdin)['assets']:
    print(a['name'], a['browser_download_url'])
")

echo "Downloading models to $DEST..."
while read -r name url; do
  echo "  $name..."
  wget -c -q --show-progress -O "$DEST/$name" "$url"
done <<< "$ASSETS"

echo "Done. Models saved to $DEST"
