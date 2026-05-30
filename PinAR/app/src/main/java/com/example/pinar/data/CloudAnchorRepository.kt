package com.example.pinar.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CloudAnchorRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("cloud_anchor_pins")

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

    fun actualizarVisita(pinId: String) {
        collection.document(pinId).update("visitas", FieldValue.increment(1))
    }

    fun actualizarLikes(pin: CloudAnchorPin) {
        collection.document(pin.id).update("likes", FieldValue.increment(1))
    }

    suspend fun getPin(pinId: String): CloudAnchorPin? {
        return try {
            collection.document(pinId).get().await().toObject(CloudAnchorPin::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addComment(comentario: Comentario): String {
        val docRef = db.collection("comentarios").document()
        val commentWithId = comentario.copy(id = docRef.id)
        docRef.set(commentWithId).await()
        return docRef.id
    }

    suspend fun verificarLikeUser(pinId: String, userId: String): Boolean {
        return try {
            collection.document(pinId).collection("likes").document(userId).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleLike(pinId: String, userId: String, increment: Boolean) {
        val value = if (increment) 1L else -1L
        val pinRef = collection.document(pinId)
        val likeRef = pinRef.collection("likes").document(userId)

        db.runTransaction { transaction ->
            if (increment) {
                transaction.set(likeRef, mapOf("fecha" to FieldValue.serverTimestamp()))
            } else {
                transaction.delete(likeRef)
            }
            transaction.update(pinRef, "likes", FieldValue.increment(value))
        }.await()
    }

    //Usar para futuro. Pines en mismo edificio en un solo pin en mapa
    suspend fun getPinsForBuilding(buildingId: String): List<CloudAnchorPin> {
        return collection
            .whereEqualTo("buildingId", buildingId)
            .get()
            .await()
            .toObjects(CloudAnchorPin::class.java)
    }

    //a futuro no usar
    suspend fun getAllPins(): List<CloudAnchorPin> {
        return collection
            .get()
            .await()
            .toObjects(CloudAnchorPin::class.java)
    }

    suspend fun deletePin(pinId: String) {
        collection.document(pinId).delete().await()
    }
}
