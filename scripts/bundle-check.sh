#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

BASELINE="bundle-baseline.txt"
METRICS="bundle-metrics.txt"

threshold="${BUNDLE_SIZE_THRESHOLD_PCT:-5}"

emit() {
  echo "regressed=$1" >> "${GITHUB_OUTPUT:-/dev/null}"
}

if [ ! -f "$BASELINE" ] || [ ! -f "$METRICS" ]; then
  echo "No baseline available; skipping regression check"
  emit false
  exit 0
fi

current=$(awk -F '\t' '$1 == "worker.total.gzip" { print $2 }' "$METRICS")
base=$(awk -F '\t' '$1 == "worker.total.gzip" { print $2 }' "$BASELINE")

if [ -z "$current" ] || [ -z "$base" ] || [ "$base" -eq 0 ]; then
  echo "Baseline is incomplete; skipping regression check"
  emit false
  exit 0
fi

if awk -v c="$current" -v b="$base" -v t="$threshold" 'BEGIN {
  pct = (c - b) * 100 / b
  printf "worker.total.gzip: %d -> %d bytes (%+.1f%%, threshold: +%s%%)\n", b, c, pct, t
  exit pct > t ? 1 : 0
}'; then
  emit false
else
  echo "Worker bundle gzip size regression exceeds threshold."
  emit true
fi
