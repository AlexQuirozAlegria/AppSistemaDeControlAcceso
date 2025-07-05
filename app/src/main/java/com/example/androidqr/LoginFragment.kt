package com.example.androidqr

import android.os.Bundle
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController

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
        // #TODO: anadir conexion con la api para la validacion
        return true;
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