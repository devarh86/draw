import os
import re
import sys
from collections import defaultdict


TEXT_EXTENSIONS = {
    ".kt",
    ".java",
    ".xml",
    ".gradle",
    ".txt",
    ".pro",
    ".properties",
    ".json",
    ".md",
    ".yaml",
    ".yml",
}

IGNORE_DIRS = {
    ".git",
    ".idea",
    ".gradle",
    "build",
    "out",
}

IMAGE_EXTENSIONS = {".png", ".webp", ".jpg", ".jpeg", ".svg"}


def is_text_file(path: str) -> bool:
    _, ext = os.path.splitext(path)
    return ext.lower() in TEXT_EXTENSIONS


def should_skip_dir(dirname: str) -> bool:
    return dirname in IGNORE_DIRS


def collect_resources(root: str):
    drawables = []
    layouts = []

    for dirpath, dirnames, filenames in os.walk(root):
        # Skip heavy/irrelevant directories early
        dirnames[:] = [d for d in dirnames if not should_skip_dir(d)]

        if os.path.sep + "res" + os.path.sep not in dirpath:
            continue

        base = os.path.basename(dirpath)
        is_drawable_dir = base.startswith("drawable")
        is_layout_dir = base == "layout"

        if not (is_drawable_dir or is_layout_dir):
            continue

        for fname in filenames:
            full = os.path.join(dirpath, fname)
            name_no_ext, ext = os.path.splitext(fname)
            ext = ext.lower()

            if is_layout_dir and ext == ".xml":
                layouts.append(
                    {
                        "name": name_no_ext,
                        "ext": ext,
                        "path": os.path.relpath(full, root),
                        "type": "layout",
                    }
                )
            elif is_drawable_dir and (ext in IMAGE_EXTENSIONS or ext == ".xml"):
                drawables.append(
                    {
                        "name": name_no_ext,
                        "ext": ext,
                        "path": os.path.relpath(full, root),
                        "type": "drawable",
                    }
                )

    return drawables, layouts


def scan_usages(root: str):
    used_drawables = set()
    used_layouts = set()

    # Regex patterns to extract resource names from usages
    drawable_patterns = [
        re.compile(r"@drawable/([a-zA-Z0-9_]+)"),
        re.compile(r"@mipmap/([a-zA-Z0-9_]+)"),
        re.compile(r"R\.drawable\.([a-zA-Z0-9_]+)"),
        re.compile(r"R\.mipmap\.([a-zA-Z0-9_]+)"),
    ]

    layout_patterns = [
        re.compile(r"@layout/([a-zA-Z0-9_]+)"),
        re.compile(r"R\.layout\.([a-zA-Z0-9_]+)"),
    ]

    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if not should_skip_dir(d)]

        for fname in filenames:
            full = os.path.join(dirpath, fname)
            if not is_text_file(full):
                continue

            try:
                with open(full, "r", encoding="utf-8", errors="ignore") as f:
                    content = f.read()
            except Exception:
                continue

            for pat in drawable_patterns:
                for m in pat.findall(content):
                    used_drawables.add(m)

            for pat in layout_patterns:
                for m in pat.findall(content):
                    used_layouts.add(m)

    return used_drawables, used_layouts


def main():
    root = sys.argv[1] if len(sys.argv) > 1 else os.getcwd()
    root = os.path.abspath(root)

    print(f"Scanning project root: {root}")

    drawables, layouts = collect_resources(root)
    print(f"Found {len(drawables)} drawable resources and {len(layouts)} layout resources.")

    used_drawables, used_layouts = scan_usages(root)
    print(f"Detected {len(used_drawables)} used drawable/mipmap names and {len(used_layouts)} used layout names.\n")

    unused_drawables = [d for d in drawables if d["name"] not in used_drawables]
    unused_layouts = [l for l in layouts if l["name"] not in used_layouts]

    print("==== UNUSED DRAWABLE RESOURCES (including png/webp/svg/xml) ====")
    if not unused_drawables:
        print("None")
    else:
        for d in sorted(unused_drawables, key=lambda x: x["path"]):
            print(f"{d['path']}  (name='{d['name']}', ext='{d['ext']}')")

    print("\n==== UNUSED LAYOUT RESOURCES ====")
    if not unused_layouts:
        print("None")
    else:
        for l in sorted(unused_layouts, key=lambda x: x["path"]):
            print(f"{l['path']}  (name='{l['name']}', ext='{l['ext']}')")


if __name__ == \"__main__\":
    main()

