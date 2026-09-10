#!/bin/bash
# 递归恢复 porting-late 下指定路径的所有 .java 回 src/main/java
# 用法: bash restore.sh <相对路径，如 molang 或 client/animation> [更多的相对路径...]
cd "$(dirname "$0")"
for target in "$@"; do
  src="porting-late/$target"
  [ -d "$src" ] || { echo "skip (not found): $src"; continue; }
  find "$src" -name "*.java" | while IFS= read -r f; do
    rel=${f#porting-late/}
    dst="src/main/java/com/elfmcys/ysm/$rel"
    mkdir -p "$(dirname "$dst")"
    if git mv -f "$f" "$dst" 2>/dev/null; then
      :
    else
      cp "$f" "$dst" && git add "$dst" && git rm -qf "$f" 2>/dev/null
    fi
  done
  echo "restored: $target"
done
find porting-late -type d -empty -delete 2>/dev/null
exit 0
