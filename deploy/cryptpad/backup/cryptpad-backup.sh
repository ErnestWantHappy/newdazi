#!/usr/bin/env bash
set -euo pipefail

ROOT=/srv/cryptpad
BACKUP_DIR="$ROOT/backups"
STAMP=$(date -u +%Y%m%d_%H%M%S)
mkdir -p "$BACKUP_DIR"
tar --xattrs --acls --numeric-owner -C "$ROOT" -I 'zstd -T0 -3' \
  -cf "$BACKUP_DIR/cryptpad_${STAMP}.tar.zst" data config customize docker-compose.yml
sha256sum "$BACKUP_DIR/cryptpad_${STAMP}.tar.zst" > "$BACKUP_DIR/cryptpad_${STAMP}.sha256"
find "$BACKUP_DIR" -type f -mtime +30 -delete
