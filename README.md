# 📌 PinAR  
**PinAR - Equipo 6**

## Entrega 0 – Presentación propuesta  

**Enlace Canva:**  
https://www.canva.com/design/DAHA1rvB9bw/tZ8cAQjkkz2FbHNFJp5Y1g/edit?utm_content=DAHA1rvB9bw&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton

---

## 👥 Nombre del equipo  

**PinAR Team**

---

## 🚀 Nombre del Proyecto  

# PinAR  

Este proyecto es una plataforma móvil de vanguardia que fusiona el mundo físico con el digital mediante Realidad Aumentada (AR), permitiendo a los usuarios "pinear" lugares y experiencias en coordenadas exactas del espacio 3D en espacios cerrados.

---

## 📖 Tabla de Contenidos
1. [Características Principales](#características-principales)
2. [Arquitectura Técnica](#arquitectura-técnica)
3. [Visión General](#visión-general)
4. [Arquitectura del Sistema](#arquitectura-del-sistema)
5. [Módulos Principales (Screens)](#módulos-principales-screens)
6. [Deep Dive: El Motor AR](#deep-dive-el-motor-ar)
7. [Modelo de Datos y Mocks](#modelo-de-datos-y-mocks)
8. [Guía de Desarrollo y Troubleshooting](#guía-de-desarrollo-y-troubleshooting)
9. [Integrantes](#integrantes)

---

## Características Principales

### 🎯 Navegación en Realidad Aumentada
- Vista AR para mostrar pines virtuales en espacios cerrados 

### 📍 Sistema de Pines
- Creación de pines de ubicación personalizados
- Visualización de pines cercanos y populares
- Información detallada de cada ubicación (nombre, descripción, visitas)

### 🗺️ Mapas Interactivos
- Vista de mapas de edificios y pisos
- Integración con el sistema de pines

### 👤 Gestión de Usuarios
- Sistema de autenticación (inicio de sesión y registro)
- Perfil de usuario personalizado
- Historial de pines visitados

## Arquitectura Técnica

### Requisitos Mínimos
- **Android API**: 24 (Android 7.0) o superior
- **RAM**: 4GB recomendados
- **Procesador**: Soporte para ARCore
- **Cámara**: Cámara trasera

### Tecnologías Utilizadas
- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose
- **AR Framework**: Google ARCore
- **Navegación**: Navigation Compose
- **Arquitectura**: MVVM (Model-View-ViewModel)

### Compatibilidad ARCore
- El dispositivo debe ser compatible con Google ARCore
- Lista de dispositivos compatibles: [ARCore Supported Devices](https://developers.google.com/ar/devices)

---

## 🌐 Visión General
**PinAR** no es solo un mapa; es una interfaz de interacción espacial. Los usuarios pueden:

- **Detectar superficies** en tiempo real mediante la cámara del dispositivo.
- **Anclar modelos 3D (Pines)** que permanecen fijos en el espacio físico.
- **Interactuar con la comunidad** mediante notificaciones de interacciones (Likes, Comentarios, Seguidores).
- **Navegar fluídamente** entre la visualización 2D (Mapas) y la experiencia inmersiva 3D (AR).

---

## 🏗 Arquitectura del Sistema
El proyecto sigue un patrón de diseño **State-driven UI** (Interfaz dirigida por estados) utilizando **Jetpack Compose**.

- **UI Layer:** Composables puros divididos en pantallas (`screens`) y componentes reutilizables (`utils`).
- **Data Layer:** Modelos de datos claros (`models`) y objetos de estado específicos para AR.
- **Mocks:** Sistema de datos de prueba (`mock`) para agilizar el desarrollo de la interfaz sin depender de un backend activo.
- **Navigation:** Uso de `NavigationStack` con `rememberNavController` para la gestión de rutas.

---

## 📱 Módulos Principales (Screens)

### 📂 Sign Screen
Gestiona la identidad del usuario y el acceso inicial al ecosistema PinAR.

### 📂 Home Screen
El "Feed" principal. Aquí se muestran las actividades recientes y las notificaciones importantes de la red.

### 📂 Map Screen
Visualización geográfica. Permite ver dónde están los pines en un mapa convencional antes de pasar a la búsqueda visual.

### 📂 AR Screen (Core Técnico)
Es la integración más compleja del proyecto. Combina la cámara del dispositivo con el motor de renderizado 3D.
- **Componentes:** Incluye el `ARSessionHandler` y el `ARCameraView`.
- **Interacción:** Soporta el gesto de "Tap" para colocar objetos en planos detectados.

---

## ⚙️ Deep Dive: El Motor AR

### El `ARSessionHandler`
Componente crítico que sincroniza el ciclo de vida de la aplicación con el hardware de la cámara:
- **`ON_RESUME`**: Inicializa ARCore, verifica instalaciones pendientes de Google Play Services y enciende la cámara.
- **`ON_PAUSE`**: Detiene el rastreo y libera la cámara para ahorrar batería y recursos.
- **`onDispose`**: Cierra la sesión de forma segura al navegar fuera de la pantalla.

### Renderizado con SceneView
Utilizamos `io.github.sceneview:arsceneview` para simplificar la carga de modelos 3D y la gestión de nodos (`ArModelNode`) sin necesidad de manejar OpenGL directamente.

---

## 📊 Modelo de Datos y Mocks

El sistema de notificaciones está diseñado para escalar, con tipos de datos predefinidos:

- **`COMMENTS`**: Menciones y respuestas en hilos de pines.
- **`LIKES`**: Reacciones de la comunidad a tus publicaciones espaciales.
- **`FOLLOWERS`**: Nuevas conexiones sociales.
- **`SYSTEM`**: Alertas de moderación o estado del sistema.

*Referencia de archivo: `com.example.pinar.data.mock.MockNotifications.kt`*

---

## 🛠 Guía de Desarrollo y Troubleshooting

### Configuración Necesaria
1. El proyecto requiere un dispositivo físico compatible con **ARCore**. No se recomienda el uso de emuladores para pruebas de tracking.
2. Nivel de SDK mínimo: **24 (Android 7.0)**.
3. Nivel de SDK objetivo: **34/35**.

### Solución a Errores Comunes
- **Conflicto de Manifiesto:** Si el build falla por "Manifest Merger", es debido a que SceneView y ARCore definen el mismo meta-dato.
  - *Solución:* Usar `tools:replace="android:value"` en la etiqueta `<meta-data>` de `AndroidManifest.xml`.
- **Falta de Permisos:** Si la cámara se queda en negro, revisa que el permiso de cámara haya sido otorgado y que el hardware no esté siendo usado por otra app.
- **Repositorios Gradle:** Se utiliza el nuevo sistema de `settings.gradle.kts`. Evita añadir repositorios directamente en el `build.gradle` del módulo.

---

## 👤 Integrantes  

### Carlos Daniel Guiza  

<img src="img/daniel.jpg" width="100" height="100"/>

Tengo 21 años y actualmente estoy en sexto semestre de Ingeniería de Sistemas.  
Me gusta el desarrollo web, por lo que también me interesa aprender y profundizar en el área de computación móvil.

Fuera de la carrera, disfruto los videojuegos, el fútbol, ver películas y series, y compartir tiempo con mi perro. Me motiva aprender constantemente y enfrentar nuevos retos tecnológicos.

### Juan Felipe Rubiano

<img src="img/j.png" width="100" height="100"/>

Tengo 20 años y curso el sexto semestre de Ingeniería de sistemas en la Javeriana.
Me intereso principalmente en temas de ciberseguridad y desarrollo web. Actualmente, trabajo en varios proyectos del último. 

Fuera de la carrera, soy apasionado por el cine, y he trabajado en varios proyectos de cortometrajes de cine análogo. 

### Andres Felipe Beltran

<img src="img/andres.jpeg" width="100" height="100"/>

Tengo 20 años y estoy en sexto semestre de Ingeniería de sistemas en la Javeriana.
Estoy interesado en desarrollo web, pero no estoy cerrado a aprender otras areas relacionadas. 

Fuera de la carrera, me gusta la producción musical, el beatmaking, los videojuegos y aprender cosas nuevas. 

### Alejandro Parrado Di Doménico

<img src="img/alejandro.jpg" width="100" height="100"/>

Tengo 20 años y estoy en sexto semestre de Ingeniería de Sistemas en la Javeriana.
Trabajo en desarrollo Full-stack de producto y sistemas agénticos de AI.

Fuera de la carrera, me gusta leer libros de comportamiento humano, biografías y emprendimiento; también entrenar en el gym.
