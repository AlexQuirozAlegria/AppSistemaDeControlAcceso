package com.example.androidqr.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidqr.R
import com.example.androidqr.databinding.FragmentDashboardBinding
import com.example.androidqr.network.InvitadoResponse
import com.example.androidqr.network.RetrofitClient
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Fragmento para el Dashboard que muestra la lista de invitados del residente.
 * Permite filtrar invitados por estado (Activos, Usados, Vencidos, Cancelados).
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var guestAdapter: GuestAdapter
    private var allFetchedGuests: List<Guest> = emptyList()

    private val apiService by lazy {
        RetrofitClient.instance
    }

    /**
     * Se llama para crear y devolver la jerarquía de vistas asociada con el fragmento.
     * @param inflater El objeto LayoutInflater que puede inflar cualquier vista en el contexto actual.
     * @param container Si no es nulo, este es el padre al que se adjuntará la jerarquía de vistas.
     * @param savedInstanceState Si no es nulo, este fragmento se está reconstruyendo a partir de un estado guardado previamente.
     * @return La Vista para la UI del fragmento, o null.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupTabLayout()

        fetchGuestsFromApi()

        return root
    }

    /**
     * Configura el RecyclerView con un LinearLayoutManager y el GuestAdapter.
     */
    private fun setupRecyclerView() {
        guestAdapter = GuestAdapter(emptyList())
        binding.guestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = guestAdapter
        }
    }

    /**
     * Realiza una llamada a la API para obtener la lista de invitados del residente autenticado.
     * Maneja la autenticación, la respuesta de la API y los errores.
     */
    private fun fetchGuestsFromApi() {
        binding.guestsRecyclerView.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sharedPref = requireContext().getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE)
                val jwtToken = sharedPref.getString("jwt_token", null)
                if (jwtToken == null) {
                    Toast.makeText(requireContext(), "No hay sesión iniciada. Por favor, inicie sesión.", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_nh_to_login)
                    return@launch
                }
                val authHeader = "Bearer $jwtToken"
                val response = apiService.getMyInvitations(authHeader)

                if (response.isSuccessful) {
                    val invitadosFromApi = response.body()
                    if (invitadosFromApi != null && invitadosFromApi.isNotEmpty()) {
                        allFetchedGuests = mapInvitadoResponseToGuest(invitadosFromApi)
                        // Asegurarse de que la pestaña seleccionada se filtre correctamente al cargar
                        val selectedTab = binding.tabLayout.getTabAt(binding.tabLayout.selectedTabPosition)
                        selectedTab?.let {
                            filterGuestsByStatus(it.text.toString())
                        } ?: run {
                            // Si no hay ninguna pestaña seleccionada (ej. primera carga), usar "Activos"
                            filterGuestsByStatus("Activos")
                        }
                        binding.guestsRecyclerView.visibility = View.VISIBLE
                    } else {
                        Log.i("DashboardFragment", "API devolvió una lista vacía de invitados o cuerpo nulo.")
                        allFetchedGuests = emptyList()
                        guestAdapter.updateData(emptyList())
                        binding.guestsRecyclerView.visibility = View.GONE
                        Toast.makeText(requireContext(), "No hay invitados registrados.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido de API."
                    Log.e("DashboardFragment", "API Error: ${response.code()} - $errorBody")
                    showErrorState("Error ${response.code()}: No se pudieron cargar los invitados.")
                }
            } catch (e: Exception) {
                Log.e("DashboardFragment", "Excepción de red/solicitud: ${e.message}", e)
                showErrorState("Error de red: ${e.message}")
            }
        }
    }

    /**
     * Mapea una lista de InvitadoResponse (desde la API) a una lista de objetos Guest (para la UI).
     * @param invitados La lista de InvitadoResponse recibida de la API.
     * @return Una lista de objetos Guest.
     */
    private fun mapInvitadoResponseToGuest(invitados: List<InvitadoResponse>): List<Guest> {
        return invitados.map { invitadoApi ->
            Guest(
                id = invitadoApi.id,
                name = "${invitadoApi.nombre} ${invitadoApi.apellidos}".trim(),
                invitationType = invitadoApi.tipoInvitacion, // ¡CORRECCIÓN: Se pasa el tipo de invitación!
                status = invitadoApi.estadoQr,
                qrCode = invitadoApi.qrCode,
                fechaVencimiento = invitadoApi.fechaValidez,
                residenteId = invitadoApi.residenteId // Asegúrate de que este campo también se mapee
            )
        }
    }

    /**
     * Muestra un estado de error en la UI y limpia la lista de invitados.
     * @param message El mensaje de error a mostrar.
     */
    private fun showErrorState(message: String) {
        allFetchedGuests = emptyList()
        guestAdapter.updateData(emptyList())
        binding.guestsRecyclerView.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    /**
     * Configura las pestañas del TabLayout y sus listeners para filtrar la lista de invitados.
     */
    private fun setupTabLayout() {
        if (binding.tabLayout.tabCount == 0) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Activos"))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Usados"))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Vencidos"))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Cancelados"))
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    filterGuestsByStatus(it.text.toString())
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /**
     * Filtra la lista de invitados mostrada en el RecyclerView según el estado seleccionado en la pestaña.
     * @param statusFilter El estado por el cual filtrar (ej. "Activos", "Usados").
     */
    private fun filterGuestsByStatus(statusFilter: String) {
        val filteredList = when (statusFilter.lowercase()) {
            "activos" -> allFetchedGuests.filter { it.status.equals("Activo", ignoreCase = true) }
            "usados" -> allFetchedGuests.filter { it.status.equals("Usado", ignoreCase = true) }
            "vencidos" -> allFetchedGuests.filter { it.status.equals("Vencido", ignoreCase = true) }
            "cancelados" -> allFetchedGuests.filter { it.status.equals("Cancelado", ignoreCase = true) }
            else -> {
                Log.w("DashboardFragment", "Estado de pestaña no manejado: $statusFilter. Mostrando lista completa.")
                allFetchedGuests
            }
        }
        guestAdapter.updateData(filteredList)

        if (filteredList.isEmpty()) {
            binding.guestsRecyclerView.visibility = View.GONE
        } else {
            binding.guestsRecyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * Se llama cuando la vista del fragmento está a punto de ser destruida.
     * Limpia la referencia al binding para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
