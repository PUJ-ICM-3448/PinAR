package com.example.pinar.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pinar.data.AuthState
import com.example.pinar.data.UserData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class MainViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val _authState = mutableStateOf<AuthState>(AuthState.cargando)
    val authState: State<AuthState> = _authState

    private val _userData = mutableStateOf<UserData?>(null)
    val userData: State<UserData?> = _userData

    private val db = FirebaseFirestore.getInstance()

    private val storage = FirebaseStorage.getInstance()

    init {
        verificarAuth()
    }

    fun verificarAuth() {
        if (auth.currentUser != null) {
            val user = auth.currentUser
            user?.uid?.let { uid ->
                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { document ->
                        val data = document.toObject(UserData::class.java)
                        _userData.value = data
                        _authState.value = AuthState.autenticado
                    }
            }
        } else {
            _authState.value = AuthState.noAutenticado
        }
    }

    fun login(mail: String, contrasena: String) {
        if (mail.isEmpty() || contrasena.isEmpty()) {
            _authState.value = AuthState.Error("Por favor, completa todos los campos")
            return
        }
        _authState.value = AuthState.cargando
        auth.signInWithEmailAndPassword(mail, contrasena)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    val user = auth.currentUser
                    user?.uid?.let { uid ->
                        db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener { document ->
                                val data = document.toObject(UserData::class.java)
                                _userData.value = data
                                _authState.value = AuthState.autenticado
                            }
                    }
                } else {
                    _authState.value =
                        AuthState.Error(tarea.exception?.message ?: "Algo salio mal😧")
                }
            }
    }

    fun registrar(nombre: String, mail: String, contrasena: String, biografia: String, fotoUri: Uri?, context: Context) {
        if (mail.isEmpty() || contrasena.isEmpty() || nombre.isEmpty()) {
            _authState.value = AuthState.Error("Por favor, completa todos los campos")
            return
        }
        _authState.value = AuthState.cargando
        auth.createUserWithEmailAndPassword(mail, contrasena)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val nuevoUsuario = UserData(
                        uid = uid,
                        correo = mail,
                        nombre = nombre,
                        biografia = biografia,
                        creacion = Timestamp.now()
                    )
                    
                    db.collection("usuarios").document(uid).set(nuevoUsuario)
                        .addOnSuccessListener {
                            _userData.value = nuevoUsuario
                            _authState.value = AuthState.autenticado
                            fotoUri?.let { uri -> subirFotoPerfil(uri, context) }
                        }
                        .addOnFailureListener {
                            _authState.value = AuthState.Error("Error al guardar en base de datos")
                        }
                } else {
                    _authState.value = AuthState.Error(tarea.exception?.message ?: "Algo salio mal😧")
                }
            }
    }

    fun subirFotoPerfil(fotoUri: Uri, context: Context) {
        val ref = storage.reference
        val nomImagen = "foto_${_userData.value?.uid}"
        val espacioRef = ref.child("imagenes/perfil/${nomImagen}.jpg")

        val byteArray = context.contentResolver.openInputStream(fotoUri)?.use {it.readBytes()}

        byteArray?.let { bytes ->
            espacioRef.putBytes(bytes).addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { uri ->
                    guardarFoto(uri.toString())
                    Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(context, "Falló la subida", Toast.LENGTH_SHORT).show()
                    _userData.value = _userData.value?.copy(fotoUrl = "")
                }
            }
        }
    }

    fun guardarFoto(fotoUrl: String) {
        _userData.value = _userData.value?.copy(fotoUrl = fotoUrl)
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid).update("fotoUrl", fotoUrl)
        }
    }

    fun cerrar() {
        auth.signOut()
        _userData.value = null
        _authState.value = AuthState.noAutenticado
    }
    fun modificarNombre(nombre: String, uid: String) {
        if (nombre == _userData.value?.nombre) {
            return
        }
        db.collection("usuarios").document(uid).update("nombre", nombre)
            .addOnSuccessListener {
                _userData.value = _userData.value?.copy(nombre = nombre)
            }
    }

    fun modificarBiografia(biografia: String, uid: String) {
        if (biografia == _userData.value?.biografia) {
            return
        }
        db.collection("usuarios").document(uid).update("biografia", biografia)
            .addOnSuccessListener {
                _userData.value = _userData.value?.copy(biografia = biografia)
            }
    }

    fun modificarCompartirUbicacion(compartir: Boolean, uid: String) {
        if (compartir == _userData.value?.compartirUbicacion) {
            return
        }
        db.collection("usuarios").document(uid).update("compartirUbicacion", compartir)
            .addOnSuccessListener {
                _userData.value = _userData.value?.copy(compartirUbicacion = compartir)
                if (!compartir) {
                    // Si apaga compartir, borramos su ubicacion para no dejar rastros
                    db.collection("usuarios").document(uid).update(
                        mapOf(
                            "latitud" to null,
                            "longitud" to null
                        )
                    )
                }
            }
    }

    fun actualizarUbicacionActual(latitud: Double, longitud: Double) {
        val uid = _userData.value?.uid ?: return
        if (_userData.value?.compartirUbicacion == true) {
            db.collection("usuarios").document(uid).update(
                mapOf(
                    "latitud" to latitud,
                    "longitud" to longitud
                )
            ).addOnSuccessListener {
                _userData.value = _userData.value?.copy(latitud = latitud, longitud = longitud)
            }
        }
    }

    fun modificarImagen(uri: Uri, uid: String, context: Context) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fotoRef = storageRef.child("imagenes/perfil/foto_${uid}.jpg")

        fotoRef.delete()
            .addOnSuccessListener {
                subirFotoPerfil(uri, context)
            }
            .addOnFailureListener {
                subirFotoPerfil(uri, context)
            }
    }

    fun modificarDatos(nombre: String, biografia: String, compartirUbicacion: Boolean, uid: String, uri: Uri?, context: Context) {
        if (nombre.isNotEmpty()) {
            modificarNombre(nombre, uid)
            modificarBiografia(biografia, uid)
            modificarCompartirUbicacion(compartirUbicacion, uid)
            uri?.let {
                modificarImagen(it, uid, context)
            }
            Toast.makeText(context, "Guardando datos...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun numComentarios(uid: String): Int {
        return try {
            val querySnapshot = db.collection("comentarios")
                .whereEqualTo("autorId", uid).get().await()
            querySnapshot.size()
        }catch (e: Exception) {
            0
        }
    }
}
