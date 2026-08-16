#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

BUNDLE_DIR="build/bundle-report/worker"
ASSETS_DIR="output"
BASELINE="bundle-baseline.txt"
METRICS="bundle-metrics.txt"
REPORT="bundle-report.md"

rm -rf "$BUNDLE_DIR"
npx wrangler deploy --dry-run --outdir="$BUNDLE_DIR" > /dev/null

baseline_get() {
  if [ -f "$BASELINE" ]; then
    awk -F '\t' -v k="$1" '$1 == k { print $2 }' "$BASELINE"
  fi
}

format_size() {
  awk -v b="$1" 'BEGIN {
    if (b >= 1048576) printf "%.2f MiB", b / 1048576
    else if (b >= 1024) printf "%.1f KiB", b / 1024
    else printf "%d B", b
  }'
}

format_diff() {
  local current=$1 base=${2:-} diff sign abs
  if [ -z "$base" ]; then
    printf 'N/A'
    return
  fi
  diff=$((current - base))
  if [ "$diff" -eq 0 ]; then
    printf '±0'
    return
  fi
  if [ "$diff" -gt 0 ]; then sign='+'; else sign='-'; fi
  abs=${diff#-}
  printf '%s%s' "$sign" "$(format_size "$abs")"
  if [ "$base" -ne 0 ]; then
    awk -v d="$abs" -v b="$base" -v s="$sign" 'BEGIN { printf " (%s%.1f%%)", s, d * 100 / b }'
  fi
}

format_count_diff() {
  local current=$1 base=${2:-} diff
  if [ -z "$base" ]; then
    printf 'N/A'
    return
  fi
  diff=$((current - base))
  if [ "$diff" -eq 0 ]; then
    printf '±0'
  elif [ "$diff" -gt 0 ]; then
    printf '+%d' "$diff"
  else
    printf '%d' "$diff"
  fi
}

worker_total_raw=0
worker_total_gzip=0
file_rows=""

for f in "$BUNDLE_DIR"/*; do
  name=$(basename "$f")
  case "$name" in
    *.map | README.md) continue ;;
  esac
  name=$(printf '%s' "$name" | sed -E 's/^[0-9a-f]{8,}-//')
  raw=$(wc -c < "$f" | tr -d '[:space:]')
  gz=$(gzip -9 -c "$f" | wc -c | tr -d '[:space:]')
  worker_total_raw=$((worker_total_raw + raw))
  worker_total_gzip=$((worker_total_gzip + gz))
  file_rows+="${raw}"$'\t'"${name}"$'\t'"${gz}"$'\n'
done

sorted=$(printf '%s' "$file_rows" | sort -rn)

assets_count=$(find "$ASSETS_DIR" -type f | wc -l | tr -d '[:space:]')
assets_raw=$(find "$ASSETS_DIR" -type f -exec wc -c {} + | awk '$2 != "total" { s += $1 } END { print s + 0 }')

{
  while IFS=$'\t' read -r raw name gz; do
    [ -n "$name" ] || continue
    printf 'file.%s.raw\t%s\n' "$name" "$raw"
    printf 'file.%s.gzip\t%s\n' "$name" "$gz"
  done <<< "$sorted"
  printf 'worker.total.raw\t%s\n' "$worker_total_raw"
  printf 'worker.total.gzip\t%s\n' "$worker_total_gzip"
  printf 'assets.count\t%s\n' "$assets_count"
  printf 'assets.raw\t%s\n' "$assets_raw"
} > "$METRICS"

{
  printf '### 📦 Bundle Report\n\n'
  printf '**Worker bundle**\n\n'
  printf '| File | Raw | Gzip | Δ Gzip |\n'
  printf '|:--|--:|--:|--:|\n'
  while IFS=$'\t' read -r raw name gz; do
    [ -n "$name" ] || continue
    printf '| %s | %s | %s | %s |\n' \
      "$name" \
      "$(format_size "$raw")" \
      "$(format_size "$gz")" \
      "$(format_diff "$gz" "$(baseline_get "file.${name}.gzip")")"
  done <<< "$sorted"
  printf '| **Total** | **%s** | **%s** | **%s** |\n' \
    "$(format_size "$worker_total_raw")" \
    "$(format_size "$worker_total_gzip")" \
    "$(format_diff "$worker_total_gzip" "$(baseline_get worker.total.gzip)")"
  printf '\n**Static assets:** %s files (Δ %s), %s (Δ %s)\n' \
    "$assets_count" \
    "$(format_count_diff "$assets_count" "$(baseline_get assets.count)")" \
    "$(format_size "$assets_raw")" \
    "$(format_diff "$assets_raw" "$(baseline_get assets.raw)")"
  printf '\n<sub>Gzip sizes are gzip -9 estimates. Workers script limit: 3 MiB gzip (Free) / 10 MiB (Paid). Baseline: latest develop deploy.</sub>\n'
} > "$REPORT"
