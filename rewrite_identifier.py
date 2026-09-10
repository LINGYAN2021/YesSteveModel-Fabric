#!/usr/bin/env python3
"""把 new Identifier(...) 构造调用改写为 26.1.2 的静态工厂：
1 个参数 -> Identifier.parse(arg)
2 个参数 -> Identifier.fromNamespaceAndPath(a, b)
"""
import os
import sys

ROOTS = ["src", "porting", "porting-late"]
changed_files = 0
rewrites = 0
flagged = []

def process(text):
    global rewrites
    out = []
    i = 0
    needle = "new Identifier("
    while True:
        j = text.find(needle, i)
        if j == -1:
            out.append(text[i:])
            break
        out.append(text[i:j])
        # 从 ( 开始配平括号
        k = j + len(needle) - 1  # 指向 (
        depth = 0
        m = k
        in_str = False
        in_char = False
        esc = False
        while m < len(text):
            c = text[m]
            if esc:
                esc = False
            elif c == '\\':
                esc = True
            elif in_str:
                if c == '"':
                    in_str = False
            elif in_char:
                if c == "'":
                    in_char = False
            elif c == '"':
                in_str = True
            elif c == "'":
                in_char = True
            elif c == '(':
                depth += 1
            elif c == ')':
                depth -= 1
                if depth == 0:
                    break
            m += 1
        if depth != 0:
            out.append(text[j:])
            break
        args_text = text[k + 1:m]
        # 顶层逗号切分
        parts = []
        depth2 = 0
        in_str = in_char = esc = False
        cur = []
        for c in args_text:
            if esc:
                cur.append(c); esc = False; continue
            if c == '\\':
                cur.append(c); esc = True; continue
            if in_str:
                cur.append(c)
                if c == '"':
                    in_str = False
                continue
            if in_char:
                cur.append(c)
                if c == "'":
                    in_char = False
                continue
            if c == '"':
                in_str = True; cur.append(c); continue
            if c == "'":
                in_char = True; cur.append(c); continue
            if c in '([{':
                depth2 += 1
            elif c in ')]}':
                depth2 -= 1
            if c == ',' and depth2 == 0:
                parts.append(''.join(cur).strip())
                cur = []
            else:
                cur.append(c)
        if cur:
            parts.append(''.join(cur).strip())
        if len(parts) == 1:
            out.append(f"Identifier.parse({parts[0]})")
            rewrites += 1
        elif len(parts) == 2:
            out.append(f"Identifier.fromNamespaceAndPath({parts[0]}, {parts[1]})")
            rewrites += 1
        else:
            out.append(text[j:m + 1])
            flagged.append(args_text[:80])
        i = m + 1
    return ''.join(out)

for root in ROOTS:
    if not os.path.isdir(root):
        continue
    for dirpath, _, files in os.walk(root):
        for fn in files:
            if not fn.endswith(".java"):
                continue
            p = os.path.join(dirpath, fn)
            with open(p, encoding="utf-8") as f:
                text = f.read()
            if "new Identifier(" not in text:
                continue
            new_text = process(text)
            if new_text != text:
                with open(p, "w", encoding="utf-8", newline="") as f:
                    f.write(new_text)
                changed_files += 1

print(f"files changed: {changed_files}, call sites rewritten: {rewrites}")
if flagged:
    print("flagged (not rewritten):")
    for f_ in flagged:
        print("  ", f_)
