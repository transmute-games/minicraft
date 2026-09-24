#!/usr/bin/env bash
# Validate Mermaid fences in docs / README / AGENTS.
# Always runs a structural syntax check. Optionally renders with mmdc when a
# Chrome/Chromium binary is available (CI on Linux).
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"

files=("$root/README.md" "$root/AGENTS.md")
if [[ -d "$root/docs" ]]; then
  while IFS= read -r -d '' f; do
    files+=("$f")
  done < <(find "$root/docs" -type f -name '*.md' -print0)
fi

python3 - "${files[@]}" <<'PY'
import re, sys
from pathlib import Path

DIAGRAM_START = re.compile(
    r"^(flowchart|graph|sequenceDiagram|classDiagram|stateDiagram(?:-v2)?|erDiagram|"
    r"gantt|pie|journey|gitGraph|mindmap|timeline|quadrantChart|C4Context|"
    r"C4Container|C4Component|C4Dynamic|C4Deployment)\b",
    re.I,
)
FENCE = re.compile(r"```mermaid\s*\n(.*?)```", re.S | re.I)

paths = [Path(p) for p in sys.argv[1:] if Path(p).is_file()]
total = 0
errors = []

for md in paths:
    text = md.read_text(encoding="utf-8")
    blocks = FENCE.findall(text)
    for i, body in enumerate(blocks):
        total += 1
        lines = [ln.strip() for ln in body.strip().splitlines() if ln.strip() and not ln.strip().startswith("%%")]
        if not lines:
            errors.append(f"{md}: block {i}: empty mermaid diagram")
            continue
        if not DIAGRAM_START.match(lines[0]):
            errors.append(f"{md}: block {i}: unknown/missing diagram type ({lines[0]!r})")
            continue
        joined = "\n".join(lines)
        if joined.count("[") != joined.count("]"):
            errors.append(f"{md}: block {i}: unbalanced []")
        if joined.count("(") != joined.count(")"):
            errors.append(f"{md}: block {i}: unbalanced ()")
        if joined.count("{") != joined.count("}"):
            errors.append(f"{md}: block {i}: unbalanced {{}}")

if total == 0:
    print("check-mermaid: no mermaid blocks found", file=sys.stderr)
    sys.exit(1)
if errors:
    print("check-mermaid: FAILED", file=sys.stderr)
    for e in errors:
        print(f"  {e}", file=sys.stderr)
    sys.exit(1)
print(f"check-mermaid: syntax OK ({total} diagram(s))")
PY

# Optional render pass (Linux CI with Chromium).
chrome=""
for candidate in chromium chromium-browser google-chrome google-chrome-stable; do
  if command -v "$candidate" >/dev/null 2>&1; then
    chrome="$(command -v "$candidate")"
    break
  fi
done

if [[ -n "$chrome" ]] && command -v mmdc >/dev/null 2>&1; then
  tmpdir="$(mktemp -d)"
  trap 'rm -rf "$tmpdir"' EXIT
  export PUPPETEER_EXECUTABLE_PATH="$chrome"
  export PUPPETEER_SKIP_DOWNLOAD=true
  count=0
  fail=0
  for md in "${files[@]}"; do
    [[ -f "$md" ]] || continue
    while IFS= read -r mmd; do
      [[ -n "$mmd" ]] || continue
      count=$((count + 1))
      svg="$tmpdir/$(basename "$mmd" .mmd).svg"
      if ! mmdc -i "$mmd" -o "$svg" -q 2>"$tmpdir/err.txt"; then
        echo "check-mermaid: render FAILED $mmd" >&2
        cat "$tmpdir/err.txt" >&2 || true
        fail=1
      fi
    done < <(
      python3 - "$md" "$tmpdir" <<'PY'
import re, sys, pathlib, hashlib
md = pathlib.Path(sys.argv[1])
out_dir = pathlib.Path(sys.argv[2])
text = md.read_text(encoding="utf-8")
blocks = re.findall(r"```mermaid\s*\n(.*?)```", text, flags=re.S | re.I)
digest = hashlib.sha1(str(md).encode()).hexdigest()[:8]
for i, body in enumerate(blocks):
    path = out_dir / f"{digest}-{i}.mmd"
    path.write_text(body.strip() + "\n", encoding="utf-8")
    print(path)
PY
    )
  done
  if [[ "$fail" -ne 0 ]]; then
    exit 1
  fi
  echo "check-mermaid: render OK ($count diagram(s) via $chrome)"
else
  echo "check-mermaid: skip render (no Chromium on PATH)"
fi
