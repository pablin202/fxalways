#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

DEVELOPER_DIR="${DEVELOPER_DIR:-/Applications/Xcode.app/Contents/Developer}"
SIMULATOR_NAME="${SIMULATOR_NAME:-FXAlways-iPhone17}"
CONFIGURATION="${CONFIGURATION:-Debug}"
SCHEME="${SCHEME:-iosApp}"
DERIVED_DATA="${DERIVED_DATA:-$ROOT_DIR/build/xcode/DerivedData}"
APP_PATH="$DERIVED_DATA/Build/Products/$CONFIGURATION-iphonesimulator/FX Always.app"

if command -v xcodegen >/dev/null 2>&1; then
  xcodegen generate
fi

DEVELOPER_DIR="$DEVELOPER_DIR" xcodebuild \
  -project FXAlways.xcodeproj \
  -scheme "$SCHEME" \
  -configuration "$CONFIGURATION" \
  -derivedDataPath "$DERIVED_DATA" \
  -destination "platform=iOS Simulator,name=$SIMULATOR_NAME" \
  build

SIMULATOR_ID="$(
  DEVELOPER_DIR="$DEVELOPER_DIR" xcrun simctl list devices available |
    sed -nE "s/^[[:space:]]*$SIMULATOR_NAME \\(([A-F0-9-]+)\\).*/\\1/p" |
    head -1
)"

if [ -z "$SIMULATOR_ID" ]; then
  echo "Simulator not found: $SIMULATOR_NAME" >&2
  exit 1
fi

DEVELOPER_DIR="$DEVELOPER_DIR" xcrun simctl boot "$SIMULATOR_ID" >/dev/null 2>&1 || true
DEVELOPER_DIR="$DEVELOPER_DIR" xcrun simctl install "$SIMULATOR_ID" "$APP_PATH"
DEVELOPER_DIR="$DEVELOPER_DIR" xcrun simctl launch "$SIMULATOR_ID" com.fxalways.app.ios
