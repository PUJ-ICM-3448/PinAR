package com.example.pinar.ar

import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Pose
import com.google.ar.core.Session
import java.util.function.BiConsumer

/**
 * Encapsula toda la interacción con la API de Cloud Anchors de ARCore.
 *
 * Ref: https://developers.google.com/ar/develop/java/cloud-anchors/developer-guide
 *
 * Usa hostCloudAnchorAsync / resolveCloudAnchorAsync (ARCore 1.33+).
 * Las operaciones son asíncronas: se inician y el resultado llega por callback.
 */
class CloudAnchorManager {

    companion object {
        private const val TAG = "CloudAnchorManager"
        const val DEFAULT_TTL_DAYS = 1
    }

    /**
     * Habilita Cloud Anchors en la configuración de sesión.
     *
     * Ref: Developer Guide — "Enable Cloud Anchor capabilities in the session configuration"
     */
    fun enableCloudAnchors(session: Session) {
        val config = session.config
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        session.configure(config)
        Log.d(TAG, "Cloud Anchors habilitados en la sesión")
    }

    /**
     * Evalúa la calidad del mapa de features en la posición actual de la cámara.
     *
     * Ref: https://developers.google.com/ar/reference/java/com/google/ar/core/Session.FeatureMapQuality
     *
     * Retorna: INSUFFICIENT, SUFFICIENT, o GOOD
     */
    fun estimateHostingQuality(session: Session, pose: Pose): Session.FeatureMapQuality {
        return session.estimateFeatureMapQualityForHosting(pose)
    }

    /**
     * Inicia el hosting asíncrono de un Cloud Anchor.
     *
     * Ref: https://developers.google.com/ar/reference/java/com/google/ar/core/Session#hostCloudAnchorAsync
     *
     * Firma del método ARCore 1.33+:
     *   hostCloudAnchorAsync(anchor, ttlDays, BiConsumer<String, CloudAnchorState>)
     *
     * @param session    La sesión ARCore activa
     * @param anchor     El anchor local creado desde un hit-test
     * @param ttlDays    Tiempo de vida del Cloud Anchor (1–365 días)
     * @param onComplete Callback con (cloudAnchorId, state)
     */
    fun hostCloudAnchor(
        session: Session,
        anchor: Anchor,
        ttlDays: Int = DEFAULT_TTL_DAYS,
        onComplete: (cloudAnchorId: String?, state: Anchor.CloudAnchorState) -> Unit
    ) {
        Log.d(TAG, "Iniciando hosting de Cloud Anchor (TTL: $ttlDays días)")
        session.hostCloudAnchorAsync(
            anchor,
            ttlDays,
            BiConsumer { cloudAnchorId, state ->
                Log.d(TAG, "Hosting resultado: id=$cloudAnchorId, state=$state")
                onComplete(cloudAnchorId, state)
            }
        )
    }

    /**
     * Resuelve un Cloud Anchor previamente hosteado (asíncrono).
     * ARCore compara las features visuales actuales con el mapa 3D almacenado.
     *
     * Ref: https://developers.google.com/ar/reference/java/com/google/ar/core/Session#resolveCloudAnchor
     *
     * @param session        La sesión ARCore activa
     * @param cloudAnchorId  El ID del Cloud Anchor obtenido en el hosting
     * @param onComplete     Callback con (anchor, state)
     */
    fun resolveCloudAnchor(
        session: Session,
        cloudAnchorId: String,
        onComplete: (anchor: Anchor?, state: Anchor.CloudAnchorState) -> Unit
    ) {
        Log.d(TAG, "Resolviendo Cloud Anchor: $cloudAnchorId")
        session.resolveCloudAnchorAsync(
            cloudAnchorId,
            BiConsumer { anchor, state ->
                Log.d(TAG, "Resolving resultado: state=$state")
                onComplete(anchor, state)
            }
        )
    }
}
