package com.example.pinar.ui.screens.pindetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.data.AuthState
import com.example.pinar.data.CloudAnchorRepository
import com.example.pinar.data.Comentario
import com.example.pinar.data.UserData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PinDetailViewModel : ViewModel() {
    private val repo = CloudAnchorRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(PinDetailState())
    val state: StateFlow<PinDetailState> = _state.asStateFlow()

    private var currentUserData: UserData? = null

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("usuarios").document(uid).get().await()
                currentUserData = doc.toObject(UserData::class.java)
            } catch (_: Exception) {
            }
        }
    }

    fun cargarDetalles(pinId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repo.actualizarVisita(pinId)
                
                val pin = repo.getPin(pinId)
                if (pin != null) {
                    val uid = auth.currentUser?.uid
                    val liked = if (uid != null) {
                        repo.verificarLikeUser(pinId, uid)
                    } else {
                        false
                    }

                    db.collection("comentarios").whereEqualTo("pinId", pinId)
                        .addSnapshotListener { snapshots, e ->
                            if (e != null) {
                                return@addSnapshotListener
                            }
                            val lista = snapshots?.documents?.mapNotNull {
                                it.toObject(Comentario::class.java)
                            } ?: emptyList()

                            _state.update { it.copy(comentarios = lista) }
                        }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            pin = pin.copy(visitas = pin.visitas),
                            userLiked = liked
                        )
                    }
                    db.collection("usuarios").document(pin.createdBy).get()
                        .addOnSuccessListener { document ->
                            val nombreCreador = document.getString("nombre")
                            _state.update {
                                it.copy(
                                    nombreCreador = nombreCreador
                                )
                            }
                        }
                    getFoto(pin.createdBy)

                } else {
                    _state.update {
                        it.copy(isLoading = false, error = "El pin no existe o fue eliminado.")
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = "Error al cargar los detalles: ${e.localizedMessage}")
                }
            }
        }
    }

    fun agregarComentario(texto: String) {
        val pinId = _state.value.pin?.id ?: return
        if (texto.isBlank()) return

        val user = auth.currentUser
        val autorId = user?.uid ?: "anonimo"
        val autorNombre = currentUserData?.nombre 
            ?: user?.displayName 
            ?: "Usuario Anónimo"

        val nuevoComentario = Comentario(
            pinId = pinId,
            autorId = autorId,
            autorNombre = autorNombre,
            texto = texto,
            fecha = Timestamp.now()
        )

        viewModelScope.launch {
            _state.update { it.copy(isSendingComment = true) }
            try {
                val commentId = repo.addComment(nuevoComentario)
                _state.update {
                    it.copy(isSendingComment = false)
                }
            } catch (_: Exception) {
                _state.update { it.copy(isSendingComment = false) }
            }
        }
    }

    fun toggleLike() {
        val pin = _state.value.pin ?: return
        val uid = auth.currentUser?.uid ?: "anonimo"
        val currentLiked = _state.value.userLiked
        val newLiked = !currentLiked
        val likeDiff = if (newLiked) 1 else -1

        _state.update {
            it.copy(
                userLiked = newLiked,
                pin = pin.copy(likes = (pin.likes + likeDiff).coerceAtLeast(0))
            )
        }
        viewModelScope.launch {
            try {
                repo.toggleLike(pin.id, uid, newLiked)
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        userLiked = currentLiked,
                        pin = pin
                    )
                }
            }
        }
    }

    fun getFoto(uid: String) {
        db.collection("usuarios").document(uid).get().addOnSuccessListener {
            val foto = it.getString("fotoUrl")
            _state.update {
                it.copy(
                    fotoUrlCreador = foto
                )
            }
        }
    }
}
