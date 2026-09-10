#!/bin/bash
# 递归恢复 porting-late 下指定路径的所有 .java 回 src/main/java
cd "$(dirname "$0")"
FAILED=0
for target in "$@"; do
  src="porting-late/$target"
  [ -d "$src" ] || { echo "skip (not found): $src"; continue; }
  while IFS= read -r f; do
    rel=${f#porting-late/}
    dst="src/main/java/com/elfmcys/ysm/$rel"
    mkdir -p "$(dirname "$dst")"
    if git mv -f "$f" "$dst" 2>/tmp/restore_err.txt; then
      :
    elif cp "$f" "$dst" && git add "$dst" 2>/dev/null && git rm -qf "$f" 2>/dev/null; then
      :
    else
      echo "FAILED: $f ($(head -1 /tmp/restore_err.txt 2>/dev/null))"
      FAILED=1
    fi
  done < <(find "$src" -name "*.java")
  echo "restored: $target"
done
find porting-late -type d -empty -delete 2>/dev/null
exit $FAILED
