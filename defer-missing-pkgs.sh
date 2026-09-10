#!/bin/bash
# 批量暂存 v3：缺包 或 引用了已暂存类/包 的文件移入 porting-late，直到收敛
cd "$(dirname "$0")"
export JAVA_HOME="C:\Java\jdk-25"
ROUND=0
while true; do
  ROUND=$((ROUND+1))
  find porting-late -name "*.java" 2>/dev/null | sed 's|porting-late/||; s|/|.|g; s|\.java$||' | sort -u > /tmp/ysm_deferred_fqcn.txt
  sed 's/.*\.//' /tmp/ysm_deferred_fqcn.txt | sort -u > /tmp/ysm_deferred_names.txt
  sed -E 's/\.[A-Za-z_][A-Za-z0-9_]*$//' /tmp/ysm_deferred_fqcn.txt | sort -u > /tmp/ysm_deferred_pkgs.txt

  ./gradlew.bat compileJava > /tmp/ysm_build.log 2>&1

  # 用 awk 按文件聚合“找不到符号”错误的 符号/位置 细节，判断是否引用已暂存内容
  awk '
    /错误: 找不到符号/ {
      # 记录当前错误所属文件
      if (match($0, /^[A-Za-z]:[\\\/][^:]*\.java/)) {
        curfile = substr($0, RSTART, RLENGTH)
      }
      insym = 1
      next
    }
    /错误:/ { insym = 0 }
    insym && /^  (符号|位置)/ {
      # 提取 类/接口/程序包 名
      line = $0
      gsub(/\r/, "", line)
      n = split(line, parts, /[ ]+/)
      for (i = 1; i <= n; i++) {
        if (parts[i] ~ /^(类|接口|程序包|变量|方法)$/ && (i+1) <= n) {
          print curfile "\t" parts[i+1]
        }
      }
    }
  ' /tmp/ysm_build.log | sort -u > /tmp/ysm_sym_pairs.txt

  : > /tmp/ysm_defer.txt
  # 缺包文件
  grep "程序包" /tmp/ysm_build.log | grep "不存在" | grep -oE "[A-Za-z]:[\\\\/][^:]*\.java" >> /tmp/ysm_defer.txt
  # 符号引用已暂存类/包的文件
  while IFS=$'\t' read -r f sym; do
    simple=${sym##*.}
    if grep -qx "$simple" /tmp/ysm_deferred_names.txt || grep -qx "$sym" /tmp/ysm_deferred_pkgs.txt; then
      echo "$f" >> /tmp/ysm_defer.txt
    fi
  done < /tmp/ysm_sym_pairs.txt

  sort -u /tmp/ysm_defer.txt -o /tmp/ysm_defer.txt
  COUNT=$(grep -c . /tmp/ysm_defer.txt)
  echo "round $ROUND: $COUNT files to defer"
  [ "$COUNT" -eq 0 ] && { echo converged; break; }
  MOVED=0
  while IFS= read -r f; do
    [ -z "$f" ] && continue
    uf=$(echo "$f" | sed 's|\\|/|g')
    rel=${uf#*src/main/java/com/elfmcys/ysm/}
    [ "$rel" = "$uf" ] && continue
    [ ! -f "src/main/java/com/elfmcys/ysm/$rel" ] && continue
    mkdir -p "porting-late/$(dirname "$rel")"
    git mv -f "src/main/java/com/elfmcys/ysm/$rel" "porting-late/$rel" 2>/dev/null && MOVED=$((MOVED+1))
  done < /tmp/ysm_defer.txt
  echo "  moved: $MOVED"
  [ "$MOVED" -eq 0 ] && { echo "no progress"; break; }
done
