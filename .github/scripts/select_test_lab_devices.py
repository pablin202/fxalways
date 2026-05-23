#!/usr/bin/env python3
"""Select a small Firebase Test Lab device matrix from the current catalog.

The catalog changes over time, so CI reads it from gcloud and this script picks
preferred models when present, with a deterministic fallback when they are not.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path
from typing import Any


TARGET_DEVICE_COUNT = 3
MIN_API_LEVEL = 26
LOCALE = "en"
ORIENTATION = "portrait"

PREFERRED_DEVICES: list[tuple[str, list[int]]] = [
    ("NexusLowRes", [30, 29, 28, 27, 26]),
    ("Pixel2", [30, 29, 28, 27, 26]),
    ("Pixel5", [31, 30]),
    ("Pixel6", [35, 34, 33, 32, 31]),
    ("Pixel7", [35, 34, 33]),
    ("Pixel8", [35, 34]),
    ("PixelTablet", [35, 34, 33]),
    ("MediumPhone.arm", [35, 34, 33]),
    ("SmallPhone.arm", [35, 34, 33]),
]


def fail(message: str) -> None:
    print(message, file=sys.stderr)
    raise SystemExit(1)


def model_id(model: dict[str, Any]) -> str | None:
    for key in ("id", "modelId", "name"):
        value = model.get(key)
        if isinstance(value, str) and value.strip():
            return value.strip()
    return None


def supported_versions(model: dict[str, Any]) -> list[int]:
    raw_versions = (
        model.get("supportedVersionIds")
        or model.get("supportedVersions")
        or model.get("versionIds")
        or []
    )
    if isinstance(raw_versions, str):
        raw_versions = [raw_versions]

    versions: set[int] = set()
    for raw_version in raw_versions:
        if isinstance(raw_version, dict):
            raw_version = (
                raw_version.get("id")
                or raw_version.get("versionId")
                or raw_version.get("name")
            )
        try:
            versions.add(int(str(raw_version)))
        except (TypeError, ValueError):
            continue
    return sorted(versions, reverse=True)


def choose_version(
    available_versions: list[int],
    preferred_versions: list[int] | None = None,
) -> int | None:
    if preferred_versions:
        for version in preferred_versions:
            if version in available_versions and version >= MIN_API_LEVEL:
                return version

    for version in available_versions:
        if version >= MIN_API_LEVEL:
            return version
    return None


def is_virtual(model: dict[str, Any]) -> bool:
    searchable = " ".join(
        str(model.get(key, "")) for key in ("form", "formFactor", "tags", "type")
    ).lower()
    return "virtual" in searchable


def read_catalog(path: Path) -> list[dict[str, Any]]:
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as error:
        fail(f"Could not parse Firebase Test Lab model catalog JSON: {error}")

    if not isinstance(data, list):
        fail("Firebase Test Lab model catalog JSON must be a list.")

    models = [item for item in data if isinstance(item, dict) and model_id(item)]
    if not models:
        fail("Firebase Test Lab model catalog did not contain usable models.")
    return models


def select_devices(models: list[dict[str, Any]]) -> list[tuple[str, int]]:
    selected: list[tuple[str, int]] = []
    selected_ids: set[str] = set()
    by_id = {model_id(model): model for model in models}

    for preferred_id, preferred_versions in PREFERRED_DEVICES:
        model = by_id.get(preferred_id)
        if model is None:
            continue

        version = choose_version(supported_versions(model), preferred_versions)
        if version is None:
            continue

        selected.append((preferred_id, version))
        selected_ids.add(preferred_id)
        if len(selected) == TARGET_DEVICE_COUNT:
            return selected

    fallback_models = sorted(
        models,
        key=lambda model: (
            not is_virtual(model),
            model_id(model) or "",
        ),
    )
    for model in fallback_models:
        identifier = model_id(model)
        if identifier is None or identifier in selected_ids:
            continue

        version = choose_version(supported_versions(model))
        if version is None:
            continue

        selected.append((identifier, version))
        selected_ids.add(identifier)
        if len(selected) == TARGET_DEVICE_COUNT:
            return selected

    fail(
        "Firebase Test Lab catalog did not contain enough models with API "
        f"{MIN_API_LEVEL}+ to build a {TARGET_DEVICE_COUNT}-device matrix."
    )


def main() -> None:
    if len(sys.argv) != 2:
        fail("Usage: select_test_lab_devices.py <test-lab-models.json>")

    selected_devices = select_devices(read_catalog(Path(sys.argv[1])))
    for identifier, version in selected_devices:
        print(
            "--device="
            f"model={identifier},version={version},locale={LOCALE},orientation={ORIENTATION}"
        )


if __name__ == "__main__":
    main()
