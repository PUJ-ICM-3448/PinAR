package com.example.pinar.ar

import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Pose
import com.google.ar.core.Session
import java.util.function.BiConsumer

class CloudAnchorManager {

    companion object {
        private const val TAG = "CloudAnchorManager"
        const val DEFAULT_TTL_DAYS = 1 //a futuro cambiar a 365 pero toca usar JWT o parecido
    }

    fun enableCloudAnchors(session: Session) {
        val config = session.config
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        session.configure(config)
    }

     // INSUFFICIENT (con este no mandamos), SUFFICIENT, GOOD
    fun estimateHostingQuality(session: Session, pose: Pose): Session.FeatureMapQuality {
        return session.estimateFeatureMapQualityForHosting(pose)
    }

    fun hostCloudAnchor(
        session: Session,
        anchor: Anchor,
        ttlDays: Int = DEFAULT_TTL_DAYS,
        onComplete: (cloudAnchorId: String?, state: Anchor.CloudAnchorState) -> Unit
    ) {
        session.hostCloudAnchorAsync(
            anchor,
            ttlDays,
            BiConsumer { cloudAnchorId, state ->
                Log.d(TAG, "Hosting hecho: id=$cloudAnchorId, state=$state")
                onComplete(cloudAnchorId, state)
            }
        )
    }

    fun resolveCloudAnchor(
        session: Session,
        cloudAnchorId: String,
        onComplete: (anchor: Anchor?, state: Anchor.CloudAnchorState) -> Unit
    ) {
        session.resolveCloudAnchorAsync(
            cloudAnchorId,
            BiConsumer { anchor, state ->
                onComplete(anchor, state)
            }
        )
    }
}
