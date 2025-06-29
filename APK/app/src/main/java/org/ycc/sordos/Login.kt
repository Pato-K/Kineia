package org.ycc.sordos

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.sharp.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "PantallaLogin"

@Composable
fun PantallaLogin(
    navigateTo: (String) -> Unit
) {
    val context = LocalContext.current

    // Firebase Auth
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    // Estado para campos de texto
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    // Estado para controlar la navegación entre login y registro
    var showRegistroScreen by remember { mutableStateOf(false) }
    // Configuración de Google Sign In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // ActivityResultLauncher para Google Sign In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(auth, firestore, account.idToken!!, context) { success ->
                if (success) {
                    navigateTo(Pantallas.Menu.name)
                }
                isLoading = false
            }
        } catch (e: ApiException) {
            Log.w(TAG, "Error al iniciar sesión en Google", e)
            Toast.makeText(context, "Inicio con Google fallido: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    // Verificar si ya hay usuario autenticado
    LaunchedEffect(auth) {
        if (auth.currentUser != null) {
            navigateTo(Pantallas.Menu.name)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        if (showRegistroScreen) {
            PantallaRegistro(
                onRegistroCompleto = {
                    showRegistroScreen = false
                    navigateTo(Pantallas.Menu.name)
                },
                onCancelar = { showRegistroScreen = false },
                auth = auth,
                firestore = firestore
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Tarjeta de login
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Iniciar sesión",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001E36)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de correo electrónico
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo electrónico") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F7F9),
                                unfocusedContainerColor = Color(0xFFF5F7F9),
                                disabledContainerColor = Color(0xFFF5F7F9),
                                focusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo de contraseña
                        var password by remember { mutableStateOf("") }
                        var passwordVisible by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                val image = if (passwordVisible)
                                    Icons.Filled.Lock
                                else Icons.Sharp.Lock

                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F7F9),
                                unfocusedContainerColor = Color(0xFFF5F7F9),
                                disabledContainerColor = Color(0xFFF5F7F9),
                                focusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )


                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón de inicio de sesión
                        Button(
                            onClick = {
                                if (email.isNotEmpty() && password.isNotEmpty()) {
                                    isLoading = true
                                    signInWithEmail(auth, firestore, email, password, context) { success ->
                                        if (success) {
                                            navigateTo(Pantallas.Menu.name)
                                        }
                                        isLoading = false
                                    }
                                } else {
                                    Toast.makeText(context, "Por favor ingresa email y contraseña", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0052CC)
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text("Iniciar sesión")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón de registro
                        OutlinedButton(
                            onClick = { showRegistroScreen = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF0052CC)
                            ),
                            enabled = !isLoading
                        ) {
                            Text("Crear cuenta")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "O continúa con Google",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de redes sociales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Botón de Google
                    IconButton(
                        onClick = {
                            isLoading = true
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(12.dp),
                        enabled = !isLoading
                    ) {
                        Image(
                            painter = painterResource(R.drawable.google_brands),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaRegistro(
    onRegistroCompleto: () -> Unit,
    onCancelar: () -> Unit,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore
) {
    val context = LocalContext.current

    // Estados para los campos del formulario
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de registro
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Crear nueva cuenta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001E36)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F7F9),
                            unfocusedContainerColor = Color(0xFFF5F7F9),
                            disabledContainerColor = Color(0xFFF5F7F9),
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo de correo electrónico
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F7F9),
                            unfocusedContainerColor = Color(0xFFF5F7F9),
                            disabledContainerColor = Color(0xFFF5F7F9),
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo de contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F7F9),
                            unfocusedContainerColor = Color(0xFFF5F7F9),
                            disabledContainerColor = Color(0xFFF5F7F9),
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Botón cancelar
                        OutlinedButton(
                            onClick = onCancelar,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF666666)
                            ),
                            enabled = !isLoading
                        ) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        // Botón registrar
                        Button(
                            onClick = {
                                if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                                    isLoading = true
                                    createNewAccount(auth, firestore, nombre, email, password, context) { success ->
                                        if (success) {
                                            onRegistroCompleto()
                                        }
                                        isLoading = false
                                    }
                                } else {
                                    Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0052CC)
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text("Registrar")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Función para crear nueva cuenta con todos los campos
private fun createNewAccount(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    nombre: String,
    email: String,
    password: String,
    context: Context,
    onComplete: (Boolean) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Crear documento de usuario en Firestore
                val user = auth.currentUser
                user?.let {
                    // Crear mapa de lecciones completadas iniciando todas en false
                    val leccionesCompletas = mapOf(
                        "L1-1" to false,
                        "L1-2" to false,
                        "L1-3" to false,
                        "L2-1" to false,
                        "L2-2" to false,
                        "L2-3" to false
                    )

                    val userMap = hashMapOf(
                        "Nombre" to nombre,
                        "email" to email,
                        "password" to password, // guardar contraseñas en texto plano
                        "LeccionesCompletas" to leccionesCompletas
                    )

                    firestore.collection("Usuarios")
                        .document(user.uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "Usuario creado exitosamente")
                            Toast.makeText(context, "¡Bienvenido $nombre!", Toast.LENGTH_SHORT).show()
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error al crear usuario", e)
                            Toast.makeText(context, "Error al crear perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                            onComplete(false)
                        }
                }
            } else {
                Log.e(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(context, "Registro fallido: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
}

// Función para iniciar sesión con email y contraseña
private fun signInWithEmail(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    email: String,
    password: String,
    context: Context,
    onComplete: (Boolean) -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithEmail:success")
                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                onComplete(true)
            } else {
                Log.e(TAG, "signInWithEmail:failure", task.exception)
                Toast.makeText(context, "Inicio de sesión fallido: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
}

// Función para autenticar con Google
private fun firebaseAuthWithGoogle(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    idToken: String,
    context: Context,
    onComplete: (Boolean) -> Unit
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithCredential:success")
                val user = auth.currentUser

                // Verificar si es un usuario nuevo
                user?.let {
                    firestore.collection("Usuarios").document(it.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            if (!document.exists()) {
                                // Obtener el nombre y email del usuario de Google
                                val nombre = user.displayName ?: "Usuario"
                                val email = user.email ?: ""

                                // Crear mapa de lecciones completadas iniciando todas en false
                                val leccionesCompletas = mapOf(
                                    "L1-1" to false,
                                    "L1-2" to false,
                                    "L1-3" to false,
                                    "L2-1" to false,
                                    "L2-2" to false,
                                    "L2-3" to false
                                )

                                // Nuevo usuario, crear documento
                                val userMap = hashMapOf(
                                    "Nombre" to nombre,
                                    "email" to email,
                                    "LeccionesCompletas" to leccionesCompletas
                                )

                                firestore.collection("Usuarios")
                                    .document(user.uid)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Documento de usuario de Google creado exitosamente")
                                        Toast.makeText(context, "¡Bienvenido $nombre!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error al crear documento de usuario de Google", e)
                                        Toast.makeText(context, "Error al crear perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // Usuario existente
                                Toast.makeText(context, "Inicio con Google exitoso", Toast.LENGTH_SHORT).show()
                            }
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error al verificar usuario en Firestore", e)
                            Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                            onComplete(false)
                        }
                } ?: onComplete(false)
            } else {
                Log.e(TAG, "signInWithCredential:failure", task.exception)
                Toast.makeText(context, "Inicio con Google fallido: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
}