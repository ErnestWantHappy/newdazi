#!/usr/bin/env bash
set -euo pipefail
umask 077

ROOT=/srv/judge0-python
BACKUP_DIR="$ROOT/backups"
STAMP=$(date -u +%Y%m%d_%H%M%S)
mkdir -p "$BACKUP_DIR"

"/usr/bin/docker" compose -f "$ROOT/docker-compose.yml" exec -T db \
  pg_dump -U judge0 -d judge0 --format=custom > "$BACKUP_DIR/judge0_${STAMP}.dump"
tar --xattrs --acls --numeric-owner -C "$ROOT" -I 'zstd -T0 -3' \
  -cf "$BACKUP_DIR/judge0_${STAMP}_config.tar.zst" config docker-compose.yml
sha256sum "$BACKUP_DIR/judge0_${STAMP}.dump" "$BACKUP_DIR/judge0_${STAMP}_config.tar.zst" \
  > "$BACKUP_DIR/judge0_${STAMP}.sha256"
find "$BACKUP_DIR" -type f -mtime +30 -delete
