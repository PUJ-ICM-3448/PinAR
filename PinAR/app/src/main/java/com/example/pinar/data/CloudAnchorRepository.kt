package com.example.pinar.data

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

    //a implementar
    suspend fun deletePin(pinId: String) {
        collection.document(pinId).delete().await()
    }
}
