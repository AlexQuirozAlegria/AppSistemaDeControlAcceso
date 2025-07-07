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

class LoginFragment : Fragment(R.layout.fragment_login) {

    private fun validFields(): Boolean {
        val usernameInput = view?.findViewById<EditText>(R.id.username)
        val passwordInput = view?.findViewById<EditText>(R.id.password)
        
        return true
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = view.findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            if (!validFields()) {
                Toast.makeText(requireContext(), "Por favor llene todos los campos, sin espacios al inicio ni al final", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // This part is fine as view is non-null here
                val username = view.findViewById<EditText>(R.id.username).text.toString().trim()
                val password = view.findViewById<EditText>(R.id.password).text.toString().trim()
                val loginRequest = LoginRequest(username, password)
                val apiServiceInstance = RetrofitClient.instance

                try {
                    val response = apiServiceInstance.login(loginRequest)
                    if (response.isSuccessful) {
                        val token = response.body()?.token
                        if (token != null) {
                            Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_LONG).show()
                            // TODO: Save token if needed
                            findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
                        } else {
                            Toast.makeText(requireContext(), "Token faltante", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }
}
