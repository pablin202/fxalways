#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

DEVELOPER_DIR="${DEVELOPER_DIR:-/Applications/Xcode.app/Contents/Developer}"
DEVICE_ID="${DEVICE_ID:-00008150-00052DDA343A401C}"
CONFIGURATION="${CONFIGURATION:-Debug}"
SCHEME="${SCHEME:-iosApp}"
DERIVED_DATA="${DERIVED_DATA:-$ROOT_DIR/build/xcode/DerivedData}"
APP_PATH="$DERIVED_DATA/Build/Products/$CONFIGURATION-iphoneos/FX Always.app"

if command -v xcodegen >/dev/null 2>&1; then
  xcodegen generate
fi

DEVELOPER_DIR="$DEVELOPER_DIR" xcodebuild \
  -project FXAlways.xcodeproj \
  -scheme "$SCHEME" \
  -configuration "$CONFIGURATION" \
  -derivedDataPath "$DERIVED_DATA" \
  -destination "id=$DEVICE_ID" \
  build

DEVELOPER_DIR="$DEVELOPER_DIR" xcrun devicectl device install app \
  --device "$DEVICE_ID" \
  "$APP_PATH"

DEVELOPER_DIR="$DEVELOPER_DIR" xcrun devicectl device process launch \
  --device "$DEVICE_ID" \
  com.fxalways.app.ios
