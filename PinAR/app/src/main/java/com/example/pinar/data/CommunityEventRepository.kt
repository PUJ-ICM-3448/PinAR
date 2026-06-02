package com.example.pinar.data

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit

class CommunityEventRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private companion object {
        const val TAG = "CommunityEventRepository"
    }
    private fun eventsCollection(communityId: String) =
        db.collection("comunidades").document(communityId).collection("eventos")

    private fun liveLocationsCollection(communityId: String, eventId: String) =
        eventsCollection(communityId).document(eventId).collection("live_locations")

    suspend fun createEvent(communityId: String, event: CommunityEvent): String {
        val docRef = eventsCollection(communityId).document()
        val now = Timestamp.now()
        val endsAt = event.endsAt ?: Timestamp(
            Date(now.toDate().time + TimeUnit.DAYS.toMillis(7))
        )
        val expiresAt = event.expiresAt ?: endsAt
        val toSave = event.copy(
            id = docRef.id,
            communityId = communityId,
            createdAt = now,
            startsAt = event.startsAt ?: now,
            endsAt = endsAt,
            expiresAt = expiresAt,
            isActive = true,
            participants = event.participants.ifEmpty {
                listOfNotNull(event.createdBy.takeIf { it.isNotBlank() })
            }
        )
        docRef.set(toSave).await()
        return docRef.id
    }

    fun observeActiveEvents(communityId: String): Flow<List<CommunityEvent>> = callbackFlow {
        val listener: ListenerRegistration = eventsCollection(communityId)
            .orderBy("startsAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing events for community $communityId", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val now = System.currentTimeMillis()
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toCommunityEvent(communityId)
                }.orEmpty()
                    .filter { event ->
                        event.isActive &&
                            (event.expiresAt?.toDate()?.time ?: Long.MAX_VALUE) > now
                    }
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    private fun DocumentSnapshot.toCommunityEvent(communityId: String): CommunityEvent? {
        val event = toObject(CommunityEvent::class.java) ?: return null
        return event.copy(
            id = id,
            communityId = communityId,
            isActive = readIsActive(event.isActive)
        )
    }

    /** Kotlin Firestore used to persist [CommunityEvent.isActive] as "active". */
    private fun DocumentSnapshot.readIsActive(fallback: Boolean): Boolean =
        when {
            getBoolean("isActive") != null -> getBoolean("isActive") == true
            getBoolean("active") != null -> getBoolean("active") == true
            else -> fallback
        }

    suspend fun joinEvent(communityId: String, eventId: String, uid: String) {
        eventsCollection(communityId).document(eventId)
            .update("participants", FieldValue.arrayUnion(uid))
            .await()
    }

    suspend fun leaveEvent(communityId: String, eventId: String, uid: String) {
        eventsCollection(communityId).document(eventId)
            .update("participants", FieldValue.arrayRemove(uid))
            .await()
        stopLiveLocation(communityId, eventId, uid)
    }

    suspend fun getEvent(communityId: String, eventId: String): CommunityEvent? {
        val snap = eventsCollection(communityId).document(eventId).get().await()
        return snap.toCommunityEvent(communityId)
    }

    suspend fun startLiveLocation(
        communityId: String,
        eventId: String,
        user: UserData
    ) {
        val uid = user.uid
        val expiresAt = Timestamp(Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)))
        val location = LiveLocation(
            uid = uid,
            name = user.nombre,
            photoUrl = user.fotoUrl,
            latitude = user.latitud ?: 0.0,
            longitude = user.longitud ?: 0.0,
            updatedAt = Timestamp.now(),
            expiresAt = expiresAt
        )
        liveLocationsCollection(communityId, eventId).document(uid).set(location).await()
    }

    suspend fun stopLiveLocation(communityId: String, eventId: String, uid: String) {
        liveLocationsCollection(communityId, eventId).document(uid).delete().await()
    }

    suspend fun updateLiveLocation(
        communityId: String,
        eventId: String,
        location: LatLng,
        user: UserData
    ) {
        val expiresAt = Timestamp(Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)))
        liveLocationsCollection(communityId, eventId).document(user.uid).set(
            mapOf(
                "uid" to user.uid,
                "name" to user.nombre,
                "photoUrl" to user.fotoUrl,
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "updatedAt" to FieldValue.serverTimestamp(),
                "expiresAt" to expiresAt
            )
        ).await()
    }

    fun observeLiveLocations(communityId: String, eventId: String): Flow<List<LiveLocation>> =
        callbackFlow {
            val listener = liveLocationsCollection(communityId, eventId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val now = System.currentTimeMillis()
                    val locations = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(LiveLocation::class.java)
                    }.orEmpty()
                        .filter { loc ->
                            val expires = loc.expiresAt?.toDate()?.time ?: 0L
                            expires > now && (loc.latitude != 0.0 || loc.longitude != 0.0)
                        }
                    trySend(locations)
                }
            awaitClose { listener.remove() }
        }
}
