#!/bin/sh
# Regenerates the app icons in public/icons/ from public/favicon.svg (requires ImageMagick).
# The maskable and Apple icons use a full-bleed background so the glyph stays inside the safe zone.
set -e
cd "$(dirname "$0")/../public"
mkdir -p icons
full=$(mktemp --suffix=.svg)
sed 's/rx="4"/rx="0"/; s/x="2" y="2" width="20" height="20"/x="-4" y="-4" width="32" height="32"/' favicon.svg > "$full"
png() { convert -background "$1" -density 1200 "$2" -resize "$3x$3" -flatten -depth 8 "icons/$4"; }
png none favicon.svg 192 icon-192.png
png none favicon.svg 512 icon-512.png
png none "$full" 512 maskable-512.png
png '#1f6feb' "$full" 180 apple-touch-icon.png
rm "$full"
