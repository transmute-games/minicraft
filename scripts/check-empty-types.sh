#!/usr/bin/env bash
# Fails if Transmute-authored sources declare an empty top-level type body.
# Catches stubs like `public class Server {}` that survive ports.
# Upstream Minicraft+ domain packages are excluded (they use empty marker types).
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
found=0

paths=(
  "$root/client/src/main/java/games"
  "$root/client/src/test/java/games"
  "$root/common/src/main/java/minicraft/network"
  "$root/common/src/test/java/minicraft/network"
  "$root/server/src/main/java/games"
)

check_file() {
  local file="$1"
  if ! python3 - "$file" <<'PY'
import re, sys
path = sys.argv[1]
name = path.rsplit("/", 1)[-1]
if name in ("package-info.java", "module-info.java"):
    sys.exit(0)
text = open(path, encoding="utf-8", errors="ignore").read()
text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
text = re.sub(r"//.*?$", "", text, flags=re.M)
# Only flag top-level types (declaration at column 0 after optional modifiers).
for m in re.finditer(
    r"(?m)^(?:public\s+|protected\s+|private\s+|abstract\s+|final\s+|sealed\s+|static\s+)*"
    r"(class|interface|enum|record)\s+(\w+)\b[^{;]*\{([^{}]*)\}",
    text,
):
    if m.group(3).strip() == "":
        print(f"{path}: empty {m.group(1)} {m.group(2)}")
        sys.exit(2)
sys.exit(0)
PY
  then
    local status=$?
    if [[ "$status" -eq 2 ]]; then
      found=1
    else
      echo "check-empty-types: failed parsing $file" >&2
      exit 1
    fi
  fi
}

for base in "${paths[@]}"; do
  [[ -d "$base" ]] || continue
  while IFS= read -r -d '' file; do
    check_file "$file"
  done < <(find "$base" -name '*.java' -print0)
done

if [[ "$found" -ne 0 ]]; then
  echo "check-empty-types: empty type stubs detected" >&2
  exit 1
fi

echo "check-empty-types: OK"
