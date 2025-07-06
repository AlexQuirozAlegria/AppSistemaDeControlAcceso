package com.example.androidqr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.androidqr.network.ApiServiceBD
import com.example.androidqr.network.LoginRequest
import com.example.androidqr.network.LoginResponse
import com.example.androidqr.network.RetrofitClient

class LoginFragment : Fragment(R.layout.fragment_login) {
    fun validFields(): Boolean {
        val username = view?.findViewById<EditText>(R.id.username)?.text.toString()
        val password = view?.findViewById<EditText>(R.id.password)?.text.toString()

        if(username.isEmpty() || password.isEmpty() ||
            username.get(0) == ' ' || password.get(0) == ' ' ||
            username.get(username.length - 1) == ' ' || password.get(password.length - 1) == ' ') {
            return false
        }
        return true;
    }

    fun validCredentials(): Boolean {
        val username = view?.findViewById<EditText>(R.id.username)?.text.toString()
        val password = view?.findViewById<EditText>(R.id.password)?.text.toString()

        val loginRequest = LoginRequest(username, password)
        val apiServiceInstance: ApiServiceBD = RetrofitClient.instance

        var isValid = false

        try {
            // 3. Make the suspend network call
            val response: retrofit2.Response<LoginResponse> = apiServiceInstance.login(loginRequest)

            // 4. Handle the response
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null && loginResponse.token != null /* && tokenDTO.success == true */) {
                    // Login successful and token received
                    val token = loginResponse.token
                    Toast.makeText(requireContext(), "Login Successful. Token: $token", Toast.LENGTH_LONG).show()

                    // TODO: Store the token securely (e.g., SharedPreferences, DataStore)
                    // SecureStorageManager.saveToken(token)

                    // Navigate to the next screen
                    // findNavController().navigate(R.id.action_loginFragment_to_homeFragment) // Replace with your action
                    isValid = true
                } else {
                    // HTTP success, but API might indicate logical failure (e.g., wrong credentials)
                    Toast.makeText(requireContext(), "Login failed: ${response.code()} ${response.message()}", Toast.LENGTH_LONG).show()
                }
            } else {
                // HTTP error (e.g., 401 Unauthorized, 404 Not Found, 500 Server Error)
                val errorBody = response.errorBody()?.string() // Read error body once
                val errorMessage = "Login failed: ${response.code()} ${response.message()}" +
                        if (!errorBody.isNullOrEmpty()) " - $errorBody" else ""
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            // Network error (e.g., no internet) or other exceptions during the call
            Toast.makeText(requireContext(), "Login error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace() // Log the stack trace for debugging
        }
        return isValid
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = view.findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            if(!validFields()) {
                Toast.makeText(context, "Por favor llene todos los campos, sin espacios al inicio ni al final", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(validCredentials()) {
                findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
            } else {
                Toast.makeText(context, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }
}