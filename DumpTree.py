#!/usr/bin/env python3
import os
import sys
from datetime import datetime, timezone


def print_tree(root, file, prefix=""):
    entries = sorted(os.listdir(root))
    entries = [e for e in entries if e not in {".git", ".idea", "out", "build"}
               and not e.endswith((".class",".iml"))]

    for i, entry in enumerate(entries):
        path = os.path.join(root, entry)
        connector = "└── " if i == len(entries) - 1 else "├── "

        if os.path.isdir(path):
            file.write(prefix + connector + entry + "/" + "\n")
            extension = "    " if i == len(entries) - 1 else "│   "
            print_tree(path, file, prefix + extension)
        else:
            try:
                size = os.path.getsize(path)
                status = "[EMPTY]" if size == 0 else "[OK]"
            except OSError:
                status = "[?]"
            file.write(prefix + connector + entry + " " + status + "\n")

def main():
    root = sys.argv[1] if len(sys.argv) > 1 else "."
    outdir = os.path.join(root, "docs/")
    os.makedirs(outdir, exist_ok=True)
    outfile = os.path.join(outdir, "project_tree.txt")

    with open(outfile, "w", encoding="utf-8") as f:
        f.write("Generated: " + datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (local)\n\n")

        root_name = os.path.basename(os.path.abspath(root))
        f.write(root_name + "/" + "\n")

        print_tree(root, f)

    print(f"Saved tree to {outfile}")

if __name__ == "__main__":
    main()
