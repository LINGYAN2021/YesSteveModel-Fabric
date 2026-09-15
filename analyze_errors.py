#!/usr/bin/env python3
"""Group javac errors by file from a Gradle compile log."""
import re
import sys
import collections

c = collections.Counter()
sym = collections.Counter()
path = sys.argv[1]
with open(path, encoding="utf-8", errors="replace") as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    m = re.match(r"^C:.*?src\\main\\java\\com\\elfmcys\\ysm\\(.*?\.java):(\d+): ", line)
    if m and ("错误" in line or "error" in line):
        c[m.group(1)] += 1
        if "找不到符号" in line and i + 2 < len(lines):
            sm = re.search(r"符号:\s+(\S+)\s+(\S+)", lines[i + 1] + lines[i + 2])
            if sm:
                sym[sm.group(1) + " " + sm.group(2)] += 1
for k, v in c.most_common(40):
    print(v, k)
print("files:", len(c), "total:", sum(c.values()))
print("--- symbols ---")
for k, v in sym.most_common(40):
    print(v, k)
