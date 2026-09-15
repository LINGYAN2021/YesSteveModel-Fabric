import os, re

ROOT = 'src/main/java'
IMPORT = 'import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;'

def find_receiver(s, dot_pos):
    """Scan backwards from the '.' before getCapability to find receiver expression start."""
    i = dot_pos - 1
    depth = 0
    while i >= 0:
        c = s[i]
        if c in ')]':
            depth += 1
        elif c in '([':
            depth -= 1
            if depth < 0:
                return i + 1
        elif depth == 0:
            if c.isalnum() or c in '_.$':
                pass
            else:
                return i + 1
        i -= 1
    return 0

def find_arg_end(s, open_pos):
    depth = 1
    i = open_pos + 1
    while i < len(s):
        c = s[i]
        if c == '(':
            depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1

def transform(s):
    out = s
    while True:
        idx = out.find('.getCapability(')
        if idx < 0:
            return out
        arg_open = idx + len('.getCapability')
        arg_end = find_arg_end(out, arg_open)
        if arg_end < 0:
            return out
        recv_start = find_receiver(out, idx)
        receiver = out[recv_start:idx].strip()
        arg = out[arg_open + 1:arg_end].strip()
        if not receiver or not arg or 'EntityCapabilityHolder' in receiver:
            return out
        out = out[:recv_start] + f'EntityCapabilityHolder.get({receiver}, {arg})' + out[arg_end + 1:]

count = 0
for dirpath, _, files in os.walk(ROOT):
    for name in files:
        if not name.endswith('.java'):
            continue
        p = os.path.join(dirpath, name)
        s = open(p, encoding='utf-8').read()
        if '.getCapability(' not in s:
            continue
        new = transform(s)
        if new == s:
            continue
        if IMPORT not in new and 'package com.elfmcys.ysm.capability.fabric;' not in new:
            # insert import after last existing import
            lines = new.split('\n')
            last_import = max(i for i, l in enumerate(lines) if l.startswith('import '))
            lines.insert(last_import + 1, IMPORT)
            new = '\n'.join(lines)
        open(p, 'w', encoding='utf-8').write(new)
        count += 1
print('files swept:', count)
