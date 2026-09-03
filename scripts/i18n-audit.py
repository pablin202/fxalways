#!/usr/bin/env python3
"""Lists ui("…") keys that have no translation for the launch languages (es, pt, hi).

Usage: python3 scripts/i18n-audit.py [--languages es,pt,hi] [--main-flow] [--fail-on-missing] [--show N]

Keys are the English literals passed to ui("…") in composeApp/src/commonMain (dynamic ui(variable)
calls cannot be audited). Translations are read from every map in screens/i18n: files named
*_XX.kt belong to language XX, multi-language files use `"xx" to mapOf(` blocks.
"""
import argparse, os, re, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "composeApp", "src", "commonMain", "kotlin", "com", "fxalways", "app")
I18N = os.path.join(ROOT, "screens", "i18n")
MAIN_FLOW_DIRS = ("screens/onboarding", "screens/dashboard", "screens/converter", "screens/alerts", "screens/paywall", "screens/settings", "screens/FxAppShell", "screens/FxTab")
UI_CALL = re.compile(r'\bui\(\s*"((?:[^"\\]|\\.)*)"\s*\)')
PAIR = re.compile(r'"((?:[^"\\]|\\.)*)"\s+to\s+"((?:[^"\\]|\\.)*)"')
LANG_BLOCK = re.compile(r'"([a-z]{2})"\s+to\s+mapOf\(')
FILE_LANG = re.compile(r'_([A-Z]{2})(?:_[A-Za-z]+)?\.kt$')


def unescape(s):
    return s.encode().decode("unicode_escape") if "\\" in s else s


def collect_keys(main_flow):
    keys = {}
    for dirpath, _, files in os.walk(ROOT):
        if dirpath.startswith(I18N):
            continue
        for f in files:
            if not f.endswith(".kt"):
                continue
            path = os.path.join(dirpath, f)
            rel = os.path.relpath(path, ROOT).replace(os.sep, "/")
            if main_flow and not any(rel.startswith(d) for d in MAIN_FLOW_DIRS):
                continue
            src = open(path, encoding="utf-8").read()
            for m in UI_CALL.finditer(src):
                keys.setdefault(unescape(m.group(1)), rel)
    return keys


def collect_translations():
    tr = {}
    for f in sorted(os.listdir(I18N)):
        if not f.endswith(".kt"):
            continue
        src = open(os.path.join(I18N, f), encoding="utf-8").read()
        fm = FILE_LANG.search(f)
        current = fm.group(1).lower() if fm else None
        pos = 0
        events = [(m.start(), "lang", m.group(1)) for m in LANG_BLOCK.finditer(src)]
        events += [(m.start(), "pair", (m.group(1), m.group(2))) for m in PAIR.finditer(src)]
        events.sort()
        lang = current
        for _, kind, val in events:
            if kind == "lang":
                lang = val
            elif lang:
                tr.setdefault(lang, {}).setdefault(unescape(val[0]), val[1])
    return tr


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--languages", default="es,pt,hi")
    ap.add_argument("--main-flow", action="store_true", help="only keys used by onboarding/home/convert/alerts/paywall/settings")
    ap.add_argument("--fail-on-missing", action="store_true")
    ap.add_argument("--show", type=int, default=25)
    args = ap.parse_args()
    keys = collect_keys(args.main_flow)
    tr = collect_translations()
    total_missing = 0
    print(f"{len(keys)} ui() keys{' (main flow)' if args.main_flow else ''}")
    for lang in args.languages.split(","):
        have = tr.get(lang, {})
        missing = sorted((k, f) for k, f in keys.items() if k not in have)
        total_missing += len(missing)
        print(f"\n[{lang}] {len(have)} translations, {len(missing)} missing")
        for k, f in missing[: args.show]:
            print(f"  - {k!r}  ({f})")
        if len(missing) > args.show:
            print(f"  … {len(missing) - args.show} more (use --show N)")
    sys.exit(1 if args.fail_on_missing and total_missing else 0)


if __name__ == "__main__":
    main()
