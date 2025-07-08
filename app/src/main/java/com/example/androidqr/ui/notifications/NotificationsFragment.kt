package com.example.androidqr.ui.notifications

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidqr.R
import com.example.androidqr.databinding.FragmentNotificationsBinding
import com.example.androidqr.network.AccesoHistoryRequest
import com.example.androidqr.network.AccesoResponse
import com.example.androidqr.network.RetrofitClient
import com.example.androidqr.network.ApiError // Importar ApiError para el manejo de errores
import kotlinx.coroutines.launch
import java.util.Date // Asegúrate de importar Date

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var accessAdapter: AccessAdapter
    private lateinit var emptyHistoryMessage: TextView

    private val apiService by lazy {
        RetrofitClient.instance
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        emptyHistoryMessage = binding.emptyHistoryMessage // Inicializar el TextView del mensaje vacío
        setupRecyclerView()
        fetchAccessHistory() // Cargar el historial de accesos

        return root
    }

    private fun setupRecyclerView() {
        accessAdapter = AccessAdapter(emptyList())
        binding.accessHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = accessAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAccessHistory() // Recargar el historial cada vez que el fragmento se vuelve visible
    }

    private fun fetchAccessHistory() {
        binding.accessHistoryRecyclerView.visibility = View.GONE
        emptyHistoryMessage.visibility = View.GONE // Ocultar mensaje de vacío por defecto

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sharedPref = requireContext().getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE)
                val jwtToken = sharedPref.getString("jwt_token", null)
                val idResidenteString = sharedPref.getString("idResidente", null)
                val residenteId = idResidenteString?.toIntOrNull()

                // --- INICIO DE LOGS DE DEPURACIÓN ---
                Log.d("NotificationsFragment", "JWT Token recuperado: ${jwtToken?.take(10)}...") // Log solo los primeros 10 caracteres
                Log.d("NotificationsFragment", "ID Residente recuperado: $residenteId")
                // --- FIN DE LOGS DE DEPURACIÓN ---

                if (jwtToken == null || residenteId == null) {
                    Toast.makeText(requireContext(), "No hay sesión iniciada o ID de residente. Por favor, inicie sesión.", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_navigation_notifications_to_loginFragment)
                    return@launch
                }

                val authHeader = "Bearer $jwtToken"
                // Crear la solicitud con solo el residenteId
                val requestBody = AccesoHistoryRequest(residenteId = residenteId)

                val response = apiService.getAccessHistory(authHeader, requestBody)

                if (response.isSuccessful) {
                    val historyResponse = response.body()
                    if (historyResponse != null && historyResponse.accesos.isNotEmpty()) {
                        val accessItems = mapAccesoResponseToAccessItem(historyResponse.accesos)
                        accessAdapter.updateData(accessItems)
                        binding.accessHistoryRecyclerView.visibility = View.VISIBLE
                    } else {
                        Log.i("NotificationsFragment", "API devolvió una lista vacía de accesos o cuerpo nulo.")
                        accessAdapter.updateData(emptyList())
                        emptyHistoryMessage.visibility = View.VISIBLE // Mostrar mensaje de vacío
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    // MODIFICACIÓN CLAVE AQUÍ: Manejo específico para 404 con texto plano
                    if (response.code() == 404 && errorBody == "No se encontraron registros de acceso con los filtros especificados.") {
                        Log.i("NotificationsFragment", "404 intencional: No se encontraron datos de historial (respuesta de texto plano).")
                        accessAdapter.updateData(emptyList())
                        emptyHistoryMessage.visibility = View.VISIBLE
                        return@launch // Salir sin mostrar Toast de error
                    }

                    // Si no es el 404 intencional, intenta parsear como ApiError o muestra el cuerpo crudo
                    val errorMessage = if (!errorBody.isNullOrEmpty()) {
                        try {
                            val apiError = RetrofitClient.json.decodeFromString(ApiError.serializer(), errorBody)
                            apiError.message ?: "Error desconocido: ${response.code()}"
                        } catch (e: Exception) {
                            "Error al parsear respuesta de error: $errorBody (Código: ${response.code()})"
                        }
                    } else {
                        "Error desconocido de API. Código: ${response.code()}"
                    }
                    Log.e("NotificationsFragment", "API Error (${response.code()}): $errorMessage. Full error body: $errorBody")
                    Toast.makeText(requireContext(), "Error ${response.code()}: No se pudo cargar el historial de accesos. $errorMessage", Toast.LENGTH_LONG).show()
                    emptyHistoryMessage.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("NotificationsFragment", "Excepción de red/solicitud: ${e.message}", e)
                Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                emptyHistoryMessage.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Mapea una lista de AccesoResponse (desde la API) a una lista de objetos AccessItem (para la UI).
     * @param accesos La lista de AccesoResponse recibida de la API.
     * @return Una lista de objetos AccessItem.
     */
    private fun mapAccesoResponseToAccessItem(accesos: List<AccesoResponse>): List<AccessItem> {
        return accesos.map { accesoApi ->
            AccessItem(
                id = accesoApi.id,
                fechaAcceso = accesoApi.fechaAcceso,
                tipoAcceso = accesoApi.tipoAcceso,
                nombreInvitado = accesoApi.nombreInvitado,
                nombreGuardia = accesoApi.nombreGuardia,
                placasVehiculo = accesoApi.placasVehiculo
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
