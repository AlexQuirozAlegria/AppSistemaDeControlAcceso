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
 * Ahora muestra dos pestañas: "Activos" e "Invalidos".
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var guestAdapter: GuestAdapter
    private var allFetchedGuests: List<Guest> = emptyList()
    // Inicializar con "Activos" ya que será la primera pestaña por defecto
    private var currentSelectedTab: String = "Activos"

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

        // El botón de recargar no está en el layout, por lo tanto, no hay lógica aquí.
        // Si se desea añadir de nuevo, se debe incluir en el XML y su lógica aquí.

        fetchGuestsFromApi() // Cargar los invitados inicialmente

        return root
    }

    /**
     * Configura el RecyclerView con un LinearLayoutManager y el GuestAdapter.
     */
    private fun setupRecyclerView() {
        // Inicializar el adaptador con el callback para el botón Cancelar
        guestAdapter = GuestAdapter(emptyList(), currentSelectedTab) { guestToCancel ->
            // Lógica para manejar el clic en el botón Cancelar
            cancelInvitation(guestToCancel)
        }
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
                        // Si no hay ninguna pestaña seleccionada (ej. primera carga), usar "Activos"
                        val selectedTab = binding.tabLayout.getTabAt(binding.tabLayout.selectedTabPosition)
                        selectedTab?.let {
                            filterGuestsByStatus(it.text.toString())
                        } ?: run {
                            filterGuestsByStatus("Activos") // Por defecto "Activos"
                        }
                        binding.guestsRecyclerView.visibility = View.VISIBLE
                    } else {
                        Log.i("DashboardFragment", "API devolvió una lista vacía de invitados o cuerpo nulo.")
                        allFetchedGuests = emptyList()
                        guestAdapter.updateData(emptyList(), currentSelectedTab) // Pasa el filtro actual
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
                invitationType = invitadoApi.tipoInvitacion,
                status = invitadoApi.estadoQr,
                qrCode = invitadoApi.qrCode,
                fechaVencimiento = invitadoApi.fechaValidez,
                residenteId = invitadoApi.residenteId
            )
        }
    }

    /**
     * Muestra un estado de error en la UI y limpia la lista de invitados.
     * @param message El mensaje de error a mostrar.
     */
    private fun showErrorState(message: String) {
        allFetchedGuests = emptyList()
        guestAdapter.updateData(emptyList(), currentSelectedTab) // Pasa el filtro actual
        binding.guestsRecyclerView.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    /**
     * Configura las pestañas del TabLayout y sus listeners para filtrar la lista de invitados.
     * Ahora añade solo las pestañas "Activos" e "Invalidos".
     */
    private fun setupTabLayout() {
        binding.tabLayout.removeAllTabs() // Asegurarse de que no haya pestañas duplicadas
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Activos"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Invalidos"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    currentSelectedTab = it.text.toString() // Actualiza la pestaña seleccionada
                    filterGuestsByStatus(currentSelectedTab)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) { /* No hacer nada */ }
            override fun onTabReselected(tab: TabLayout.Tab?) { /* No hacer nada */ }
        })

        // Seleccionar la primera pestaña por defecto al iniciar
        binding.tabLayout.getTabAt(0)?.select()
    }

    /**
     * Filtra la lista de invitados mostrada en el RecyclerView según el estado seleccionado en la pestaña.
     * @param statusFilter El estado por el cual filtrar (ej. "Activos", "Invalidos").
     */
    private fun filterGuestsByStatus(statusFilter: String) {
        val filteredList = when (statusFilter.lowercase()) {
            "activos" -> allFetchedGuests.filter { it.status.equals("Activo", ignoreCase = true) }
            "invalidos" -> allFetchedGuests.filter {
                it.status.equals("Vencido", ignoreCase = true) ||
                        it.status.equals("Usado", ignoreCase = true) ||
                        it.status.equals("Cancelado", ignoreCase = true)
            }
            else -> {
                Log.w("DashboardFragment", "Estado de pestaña no manejado: $statusFilter. Mostrando lista completa.")
                allFetchedGuests
            }
        }
        guestAdapter.updateData(filteredList, currentSelectedTab) // Pasa el filtro actual al adaptador

        if (filteredList.isEmpty()) {
            binding.guestsRecyclerView.visibility = View.GONE
        } else {
            binding.guestsRecyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * Llama a la API para cancelar una invitación específica.
     * @param guest El objeto Guest a cancelar.
     */
    private fun cancelInvitation(guest: Guest) {
        viewLifecycleOwner.lifecycleScope.launch {
            val sharedPref = requireContext().getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE)
            val jwtToken = sharedPref.getString("jwt_token", null)

            if (jwtToken == null) {
                Toast.makeText(requireContext(), "No hay sesión iniciada para cancelar.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val authHeader = "Bearer $jwtToken"
                val response = apiService.cancelInvitation(authHeader, guest.id)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Invitación de ${guest.name} cancelada.", Toast.LENGTH_SHORT).show()
                    fetchGuestsFromApi() // Vuelve a cargar la lista para reflejar el cambio
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrEmpty()) {
                        "Error al cancelar invitación: $errorBody"
                    } else {
                        "Error al cancelar invitación. Código: ${response.code()}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("DashboardFragment", "Error al cancelar invitación: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de conexión al cancelar: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("DashboardFragment", "Excepción al cancelar invitación: ${e.message}", e)
            }
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
