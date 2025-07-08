package com.example.androidqr.ui.editguest

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidqr.R
import com.example.androidqr.network.QrDataRequest
import com.example.androidqr.network.RetrofitClient
import com.example.androidqr.ui.dashboard.Guest // Importa la clase Guest
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.widget.AdapterView // Importar AdapterView

class EditGuestFragment : Fragment() {

    private var guestToEdit: Guest? = null // Objeto Guest que se va a editar

    private lateinit var editGuestName: EditText
    private lateinit var editGuestApellidos: EditText
    private lateinit var editGuestInvitationTypeSpinner: Spinner
    private lateinit var editGuestFechaValidez: EditText
    private lateinit var editGuestFechaValidezLayout: TextInputLayout
    private lateinit var saveGuestButton: Button

    private val apiService by lazy {
        RetrofitClient.instance
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recuperar el objeto Guest de los argumentos
        arguments?.let {
            // ¡Cambiado a getParcelable!
            guestToEdit = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable("guest", Guest::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable("guest") as? Guest
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_edit_guest, container, false)

        // Inicializar vistas
        editGuestName = root.findViewById(R.id.editGuestName)
        editGuestApellidos = root.findViewById(R.id.editGuestApellidos)
        editGuestInvitationTypeSpinner = root.findViewById(R.id.editGuestInvitationTypeSpinner)
        editGuestFechaValidez = root.findViewById(R.id.editGuestFechaValidez)
        editGuestFechaValidezLayout = root.findViewById(R.id.editGuestFechaValidezLayout)
        saveGuestButton = root.findViewById(R.id.saveGuestButton)

        setupInvitationTypeSpinner()
        setupDatePicker()

        // Precargar datos si hay un invitado para editar
        guestToEdit?.let { guest ->
            // Asume que el nombre es la primera parte y el apellido el resto
            val nameParts = guest.name.split(" ", limit = 2)
            editGuestName.setText(nameParts.getOrNull(0) ?: "")
            editGuestApellidos.setText(nameParts.getOrNull(1) ?: "")

            // Seleccionar el tipo de invitación en el spinner
            val typeAdapter = editGuestInvitationTypeSpinner.adapter as ArrayAdapter<String>
            val spinnerPosition = typeAdapter.getPosition(guest.invitationType)
            if (spinnerPosition >= 0) {
                editGuestInvitationTypeSpinner.setSelection(spinnerPosition)
            }

            // Precargar fecha de validez si existe
            if (guest.fechaVencimiento != null) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                editGuestFechaValidez.setText(dateFormat.format(guest.fechaVencimiento))
                editGuestFechaValidezLayout.visibility = View.VISIBLE
            } else {
                editGuestFechaValidezLayout.visibility = View.GONE
            }
        }

        saveGuestButton.setOnClickListener {
            saveChanges()
        }

        return root
    }

    /**
     * Configura el Spinner para el tipo de invitación.
     */
    private fun setupInvitationTypeSpinner() {
        val invitationTypes = arrayOf("Unica", "Recurrente", "PorFecha")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, invitationTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        editGuestInvitationTypeSpinner.adapter = adapter

        editGuestInvitationTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // CORRECCIÓN AQUÍ: Cambiado 'position: Long' a 'position: Int'
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = invitationTypes[position] // Ya no necesita .toInt()
                // Mostrar u ocultar el campo de fecha según el tipo de invitación
                if (selectedType == "PorFecha") {
                    editGuestFechaValidezLayout.visibility = View.VISIBLE
                } else {
                    editGuestFechaValidezLayout.visibility = View.GONE
                    editGuestFechaValidez.setText("") // Limpiar la fecha si no es "PorFecha"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    /**
     * Configura el DatePicker para el campo de fecha de validez.
     */
    private fun setupDatePicker() {
        editGuestFechaValidez.setOnClickListener {
            val calendar = Calendar.getInstance()
            // Si ya hay una fecha, usarla para inicializar el DatePicker
            if (editGuestFechaValidez.text.isNotEmpty()) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = dateFormat.parse(editGuestFechaValidez.text.toString())
                    date?.let { calendar.time = it }
                } catch (e: Exception) {
                    Log.e("EditGuestFragment", "Error al parsear fecha para DatePicker: ${e.message}")
                }
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    editGuestFechaValidez.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    /**
     * Guarda los cambios del invitado llamando a la API.
     */
    private fun saveChanges() {
        val name = editGuestName.text.toString().trim()
        val apellidos = editGuestApellidos.text.toString().trim()
        val tipoInvitacion = editGuestInvitationTypeSpinner.selectedItem.toString()
        val fechaValidezString = editGuestFechaValidez.text.toString().trim()

        if (name.isEmpty() || apellidos.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre y Apellidos no pueden estar vacíos.", Toast.LENGTH_SHORT).show()
            return
        }

        var fechaValidez: Date? = null
        if (tipoInvitacion == "PorFecha" && fechaValidezString.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                fechaValidez = dateFormat.parse(fechaValidezString)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Formato de fecha inválido.", Toast.LENGTH_SHORT).show()
                return
            }
        } else if (tipoInvitacion == "PorFecha" && fechaValidezString.isEmpty()) {
            Toast.makeText(requireContext(), "La fecha de validez es obligatoria para invitaciones 'PorFecha'.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedGuestData = QrDataRequest(
            nombre = name,
            apellidos = apellidos,
            tipoInvitacion = tipoInvitacion,
            fechaValidez = fechaValidez
        )

        lifecycleScope.launch {
            val sharedPref = requireContext().getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE)
            val jwtToken = sharedPref.getString("jwt_token", null)

            if (jwtToken == null) {
                Toast.makeText(requireContext(), "No hay sesión iniciada para actualizar.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editGuestFragment_to_loginFragment)
                return@launch
            }

            try {
                val authHeader = "Bearer $jwtToken"
                guestToEdit?.id?.let { guestId ->
                    val response = apiService.updateInvitation(authHeader, guestId, updatedGuestData)

                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Invitación actualizada con éxito.", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack() // Regresar al DashboardFragment
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (!errorBody.isNullOrEmpty()) {
                            try {
                                val apiError = RetrofitClient.json.decodeFromString(com.example.androidqr.network.ApiError.serializer(), errorBody)
                                apiError.message ?: "Error desconocido al actualizar: ${response.code()}"
                            } catch (e: Exception) {
                                "Error al actualizar invitación: $errorBody (Código: ${response.code()})"
                            }
                        } else {
                            "Error al actualizar invitación. Código: ${response.code()}"
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        Log.e("EditGuestFragment", "Error al actualizar invitación: ${response.code()} - $errorBody")
                    }
                } ?: run {
                    Toast.makeText(requireContext(), "No se pudo obtener el ID del invitado para actualizar.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de conexión al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("EditGuestFragment", "Excepción al actualizar invitación: ${e.message}", e)
            }
        }
    }
}
