# ⚙️ Deep Dive: Motor AR

## ARSessionHandler

### ON_RESUME

- Inicializa ARCore
- Activa cámara

### ON_PAUSE

- Detiene rastreo
- Libera recursos

### onDispose

- Cierra sesión segura

## Renderizado

Uso de:

io.github.sceneview:arsceneview

Permite manejar modelos 3D sin OpenGL directo.
