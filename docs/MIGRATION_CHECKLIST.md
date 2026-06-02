# Firestore migration checklist (PinAR communities)

Run manually in Firebase Console or via Admin SDK script. Do **not** delete legacy fields yet.

## comunidades

- [ ] Ensure `members` is a non-null array (copy from `memberIds` if present)
- [ ] Ensure `memberCount` matches `members.length` (copy from `numMembers` if present)
- [ ] Ensure `isPublic`, `createdBy`, `createdAt`, `imageUrl` exist

## usuarios

- [ ] Ensure `memberOf` is an array (default `[]`)
- [ ] Ensure `FCMToken` field name is consistent (`FCMToken`)
- [ ] Leave `compartirUbicacion`, `latitud`, `longitud` untouched (deprecated in app)

## cloud_anchor_pins

- [ ] Ensure `comunidades` array exists (default `[]`)
- [ ] Ensure `createdAt` is numeric (Long ms); backfill from `fecha` if needed
- [ ] Ensure `createdBy`, `latitude`, `longitude` are set for map pins

## Indexes

Deploy `firestore.indexes.json` and wait for index build completion before testing map/community pin queries.

## Rules

Deploy `firestore.rules` after validating test users can read/write expected paths.
