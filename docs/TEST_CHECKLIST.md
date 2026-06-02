# PinAR social features — integration test checklist

## Mapa

- [ ] Usuario sin `memberOf` ve solo sus pines
- [ ] Usuario con comunidades ve pines propios + compartidos
- [ ] Pin en dos comunidades aparece una vez en filtro "Todos"
- [ ] Filtro por comunidad muestra solo pines de esa comunidad
- [ ] Filtro "Mis pines" muestra solo `createdBy == uid`
- [ ] No aparecen marcadores globales de `usuarios.compartirUbicacion`

## Comunidades y feed

- [ ] `POST /comunidades/{id}/pines/{pinId}` agrega comunidad al pin
- [ ] Feed Redis se actualiza al compartir
- [ ] Detalle comunidad lista pines y eventos activos
- [ ] Crear evento desde detalle (miembro)

## Eventos y ubicación en vivo

- [ ] `live_locations/{uid}` se crea al activar ubicación en evento
- [ ] Se borra al detener ubicación
- [ ] Evento expirado no permite compartir ubicación
- [ ] Ubicaciones con `expiresAt` pasado no se muestran

## FCM

- [ ] Pin compartido: `type=community_pin_shared` abre detalle de pin
- [ ] Evento creado: `type=community_event_created` abre pantalla de evento
- [ ] Token faltante no rompe Cloud Function

## Cloud Functions

- [ ] `notifyCommunityMembersOnEventCreated` desplegada
- [ ] `cleanupExpiredEventsAndLocations` programada (03:00 UTC)

## Firestore

- [ ] Desplegar `firestore.rules` y `firestore.indexes.json`
- [ ] Índices en estado "Enabled" antes de probar queries compuestas
