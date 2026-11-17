#!/bin/bash
# Script to generate all Android icon sizes from SVG
# Requires: ImageMagick or Inkscape

SVG_FILE="app/src/main/assets/icon.svg"
RES_DIR="app/src/main/res"

# Check if ImageMagick is available
if command -v convert &>/dev/null; then
  CONVERTER="imagemagick"
  echo "Using ImageMagick..."
elif command -v inkscape &>/dev/null; then
  CONVERTER="inkscape"
  echo "Using Inkscape..."
else
  echo "ERROR: Neither ImageMagick nor Inkscape found!"
  echo "Install one of them:"
  echo "  - ImageMagick: sudo apt install imagemagick  (or brew install imagemagick)"
  echo "  - Inkscape: sudo apt install inkscape  (or brew install inkscape)"
  exit 1
fi

# Create directories
mkdir -p "$RES_DIR"/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}

# Function to convert using ImageMagick
convert_imagemagick() {
  local size=$1
  local output=$2
  convert "$SVG_FILE" -resize ${size}x${size} -background white -alpha remove "$output"
}

# Function to convert using Inkscape
convert_inkscape() {
  local size=$1
  local output=$2
  inkscape "$SVG_FILE" --export-filename="$output" -w $size -h $size
}

# Choose converter function
if [ "$CONVERTER" = "imagemagick" ]; then
  convert_func=convert_imagemagick
else
  convert_func=convert_inkscape
fi

echo "Generating launcher icons..."

# Legacy launcher icons
$convert_func 48 "$RES_DIR/mipmap-mdpi/ic_launcher.png"
$convert_func 72 "$RES_DIR/mipmap-hdpi/ic_launcher.png"
$convert_func 96 "$RES_DIR/mipmap-xhdpi/ic_launcher.png"
$convert_func 144 "$RES_DIR/mipmap-xxhdpi/ic_launcher.png"
$convert_func 192 "$RES_DIR/mipmap-xxxhdpi/ic_launcher.png"

echo "Generating adaptive icon foregrounds..."

# Adaptive icon foregrounds (larger for safe zone)
$convert_func 108 "$RES_DIR/mipmap-mdpi/ic_launcher_foreground.png"
$convert_func 162 "$RES_DIR/mipmap-hdpi/ic_launcher_foreground.png"
$convert_func 216 "$RES_DIR/mipmap-xhdpi/ic_launcher_foreground.png"
$convert_func 324 "$RES_DIR/mipmap-xxhdpi/ic_launcher_foreground.png"
$convert_func 432 "$RES_DIR/mipmap-xxxhdpi/ic_launcher_foreground.png"

echo "✅ All icons generated successfully!"
echo ""
echo "Generated files:"
ls -lh "$RES_DIR"/mipmap-*/ic_launcher*.png
