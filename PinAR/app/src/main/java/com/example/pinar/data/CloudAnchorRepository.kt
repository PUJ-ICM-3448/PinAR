package com.example.pinar.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para CRUD de pines con Cloud Anchors en Firestore.
 *
 * Colección: "cloud_anchor_pins"
 * Cada documento contiene un CloudAnchorPin con el cloudAnchorId
 * que ARCore necesita para resolver el anchor.
 */
class CloudAnchorRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("cloud_anchor_pins")

    /**
     * Guarda un pin en Firestore. Retorna el ID del documento.
     */
    suspend fun savePin(pin: CloudAnchorPin): String {
        val docRef = if (pin.id.isNotEmpty()) {
            collection.document(pin.id)
        } else {
            collection.document()
        }
        val pinWithId = pin.copy(id = docRef.id)
        docRef.set(pinWithId).await()
        return docRef.id
    }

    /**
     * Obtiene todos los pines de un edificio/espacio específico.
     */
    suspend fun getPinsForBuilding(buildingId: String): List<CloudAnchorPin> {
        return collection
            .whereEqualTo("buildingId", buildingId)
            .get()
            .await()
            .toObjects(CloudAnchorPin::class.java)
    }

    /**
     * Obtiene todos los pines guardados.
     */
    suspend fun getAllPins(): List<CloudAnchorPin> {
        return collection
            .get()
            .await()
            .toObjects(CloudAnchorPin::class.java)
    }

    /**
     * Busca un pin por su Cloud Anchor ID.
     */
    suspend fun getPinByCloudAnchorId(cloudAnchorId: String): CloudAnchorPin? {
        return collection
            .whereEqualTo("cloudAnchorId", cloudAnchorId)
            .get()
            .await()
            .toObjects(CloudAnchorPin::class.java)
            .firstOrNull()
    }

    /**
     * Elimina un pin de Firestore.
     */
    suspend fun deletePin(pinId: String) {
        collection.document(pinId).delete().await()
    }
}
