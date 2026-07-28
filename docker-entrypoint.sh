#!/bin/sh
set -eu

upload_dir="${UPLOAD_DIR:-/app/uploads}"
mkdir -p "$upload_dir"
chown -R app:app "$upload_dir"

exec su-exec app:app java -XX:MaxRAMPercentage=75.0 -jar /app/app.jar