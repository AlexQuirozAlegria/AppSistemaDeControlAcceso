package com.example.androidqr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidqr.network.LoginRequest
import com.example.androidqr.network.RetrofitClient
import kotlinx.coroutines.launch
import android.content.Context
import com.example.androidqr.network.LoginResponse

/**
 * Fragmento para la interfaz de inicio de sesión de la aplicación.
 * Permite a los usuarios ingresar sus credenciales para autenticarse.
 */
class LoginFragment : Fragment(R.layout.fragment_login) {

    /**
     * Referencia al campo de texto del nombre de usuario.
     */
    private val usernameEditText: EditText?
        get() = view?.findViewById(R.id.username)

    /**
     * Referencia al campo de texto de la contraseña.
     */
    private val passwordEditText: EditText?
        get() = view?.findViewById(R.id.password)

    /**
     * Valida los campos de nombre de usuario y contraseña.
     * Muestra un mensaje Toast si algún campo está vacío.
     * @return `true` si los campos son válidos, `false` en caso contrario.
     */
    private fun validFields(): Boolean {
        val username = usernameEditText?.text?.toString()?.trim() ?: ""
        val password = passwordEditText?.text?.toString()?.trim() ?: ""

        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "El nombre de usuario no puede estar vacío.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(requireContext(), "La contraseña no puede estar vacía.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * Se llama inmediatamente después de que onCreateView() ha devuelto su vista,
     * pero antes de que se haya restaurado cualquier estado guardado en la vista.
     * @param view La vista devuelta por onCreateView().
     * @param savedInstanceState Si no es nulo, este fragmento se está reconstruyendo
     * a partir de un estado guardado previamente.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = view.findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            if (!validFields()) {
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val username = view.findViewById<EditText>(R.id.username).text.toString().trim()
                val password = view.findViewById<EditText>(R.id.password).text.toString().trim()
                val loginRequest = LoginRequest(username, password)
                val apiServiceInstance = RetrofitClient.instance

                try {
                    val response = apiServiceInstance.login(loginRequest)
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val token = loginResponse?.token
                        val rol = loginResponse?.rol
                        val idResidente = loginResponse?.idResidente

                        if (rol == "Guardia") {
                            Toast.makeText(requireContext(), "La app es para solo usuarios residentes. Acceso denegado para Guardias.", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        if (token != null) {
                            val sharedPref = requireActivity().getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE)
                            val editor = sharedPref.edit()

                            editor.putString("jwt_token", token)
                            editor.putString("idResidente", idResidente?.toString() ?: "")
                            editor.apply()

                            Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
                        } else {
                            Toast.makeText(requireContext(), "Token de autenticación faltante en la respuesta.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (!errorBody.isNullOrEmpty()) {
                            "Usuario o contraseña incorrectos: $errorBody"
                        } else {
                            "Usuario o contraseña incorrectos. Código de error: ${response.code()}"
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }
}
