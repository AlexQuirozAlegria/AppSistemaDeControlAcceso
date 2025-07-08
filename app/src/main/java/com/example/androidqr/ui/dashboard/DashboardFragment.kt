package com.example.androidqr.ui.dashboard

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/*
 * Fragmento para el Dashboard que muestra la lista de invitados del residente.
 * Permite filtrar invitados por estado (Activos, Usados, Vencidos, Cancelados).
 * También maneja la interacción con los elementos de la lista para ver detalles y realizar acciones.
 */
class DashboardFragment : Fragment(), OnGuestClickListener { // Implementa la interfaz OnGuestClickListener

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
        // Asegúrate de que reloadButton exista en fragment_dashboard.xml si lo usas.
        binding.reloadButton.setOnClickListener {
            fetchGuestsFromApi() // Llama a la función para recargar los invitados
            Toast.makeText(requireContext(), "Recargando invitados...", Toast.LENGTH_SHORT).show()
        }

        fetchGuestsFromApi() // Cargar los invitados inicialmente

        return root
    }

    /**
     * Configura el RecyclerView con un LinearLayoutManager y el GuestAdapter.
     */
    private fun setupRecyclerView() {
        // Inicializar el adaptador pasando 'this' (el fragmento) como el OnGuestClickListener
        guestAdapter = GuestAdapter(emptyList(), this)
        binding.guestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = guestAdapter
        }
    }

    /**
     * Realiza una llamada a la API para obtener la lista de invitados del residente autenticado.
     * Maneja la autenticación, la respuesta de la API y los errores.
     */
    override fun onResume() {
        super.onResume()
        // Recargar la lista cada vez que el fragmento se vuelve visible (ej. al regresar de la edición)
        fetchGuestsFromApi()
    }

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
                        guestAdapter.updateData(emptyList()) // Ya no necesita currentSelectedTab
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
        guestAdapter.updateData(emptyList()) // Ya no necesita currentSelectedTab
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
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Usados")) // Añadido de nuevo para consistencia
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Vencidos")) // Añadido de nuevo
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Cancelados")) // Añadido de nuevo

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
        val filteredList = when (statusFilter.lowercase(Locale.getDefault())) {
            "activos" -> allFetchedGuests.filter { it.status.equals("Activo", ignoreCase = true) }
            "usados" -> allFetchedGuests.filter { it.status.equals("Usado", ignoreCase = true) }
            "vencidos" -> allFetchedGuests.filter { it.status.equals("Vencido", ignoreCase = true) }
            "cancelados" -> allFetchedGuests.filter { it.status.equals("Cancelado", ignoreCase = true) }
            else -> {
                Log.w("DashboardFragment", "Estado de pestaña no manejado: $statusFilter. Mostrando lista completa.")
                allFetchedGuests
            }
        }
        guestAdapter.updateData(filteredList) // Ya no necesita currentSelectedTab

        if (filteredList.isEmpty()) {
            binding.guestsRecyclerView.visibility = View.GONE
        } else {
            binding.guestsRecyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * Implementación del listener de clic del adaptador.
     * Se llama cuando se hace clic en un elemento de la lista.
     * @param guest El objeto Guest en el que se hizo clic.
     */
    override fun onGuestClick(guest: Guest) {
        // Solo abrir el diálogo si la invitación está "Activa"
        if (guest.status.equals("Activo", ignoreCase = true)) {
            showGuestDetailsDialog(guest)
        } else {
            Toast.makeText(requireContext(), "Solo se pueden gestionar invitaciones activas.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Muestra un diálogo con los detalles del invitado y opciones de acción.
     * @param guest El objeto Guest cuyos detalles se mostrarán.
     */
    private fun showGuestDetailsDialog(guest: Guest) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_guest_details, null)

        val dialogTitle: TextView = dialogView.findViewById(R.id.dialogTitle)
        val dialogGuestName: TextView = dialogView.findViewById(R.id.dialogGuestName)
        val dialogInvitationType: TextView = dialogView.findViewById(R.id.dialogInvitationType)
        val dialogFechaValidez: TextView = dialogView.findViewById(R.id.dialogFechaValidez)
        val dialogQrCode: TextView = dialogView.findViewById(R.id.dialogQrCode)

        val cancelButton: ImageButton = dialogView.findViewById(R.id.cancelQrButton)
        val deleteButton: ImageButton = dialogView.findViewById(R.id.deleteButton)
        val editButton: ImageButton = dialogView.findViewById(R.id.editButton)

        dialogGuestName.text = "Nombre: ${guest.name}"
        dialogInvitationType.text = "Tipo de Invitación: ${guest.invitationType}"
        dialogQrCode.text = "Código QR: ${guest.qrCode}"

        // Formatear y mostrar la fecha de validez en el diálogo
        if (guest.fechaVencimiento != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dialogFechaValidez.text = "Fecha de Validez: ${dateFormat.format(guest.fechaVencimiento)}"
            dialogFechaValidez.visibility = View.VISIBLE
        } else {
            dialogFechaValidez.visibility = View.GONE
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Listeners para los botones del diálogo
        cancelButton.setOnClickListener {
            cancelInvitation(guest.id)
            alertDialog.dismiss() // Cerrar el diálogo después de la acción
        }

        deleteButton.setOnClickListener {
            deleteInvitation(guest.id)
            alertDialog.dismiss() // Cerrar el diálogo después de la acción
        }

        editButton.setOnClickListener {
            // MODIFICACIÓN: Navegar a EditGuestFragment y pasar el objeto Guest como Parcelable
            val bundle = Bundle().apply {
                putParcelable("guest", guest) // ¡Cambiado a putParcelable!
            }
            findNavController().navigate(R.id.action_navigation_dashboard_to_editGuestFragment, bundle)
            alertDialog.dismiss() // Cerrar el diálogo
        }

        alertDialog.show()
    }

    /**
     * Llama a la API para cancelar una invitación específica.
     * @param invitationId El ID de la invitación a cancelar.
     */
    private fun cancelInvitation(invitationId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val sharedPref = requireContext().getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE)
            val jwtToken = sharedPref.getString("jwt_token", null)

            if (jwtToken == null) {
                Toast.makeText(requireContext(), "No hay sesión iniciada para cancelar.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_navigation_dashboard_to_loginFragment)
                return@launch
            }

            try {
                val authHeader = "Bearer $jwtToken"
                val response = apiService.cancelInvitation(authHeader, invitationId)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Invitación cancelada.", Toast.LENGTH_SHORT).show()
                    fetchGuestsFromApi() // Vuelve a cargar la lista para reflejar el cambio
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrEmpty()) {
                        try {
                            val apiError = RetrofitClient.json.decodeFromString(com.example.androidqr.network.ApiError.serializer(), errorBody)
                            apiError.message ?: "Error desconocido al cancelar: ${response.code()}"
                        } catch (e: Exception) {
                            "Error al cancelar invitación: $errorBody (Código: ${response.code()})"
                        }
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
     * Realiza la llamada a la API para eliminar una invitación.
     * @param invitationId El ID de la invitación a eliminar.
     */
    private fun deleteInvitation(invitationId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sharedPref = requireContext().getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE)
                val jwtToken = sharedPref.getString("jwt_token", null)
                if (jwtToken == null) {
                    Toast.makeText(requireContext(), "No hay sesión iniciada.", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_navigation_dashboard_to_loginFragment)
                    return@launch
                }
                val authHeader = "Bearer $jwtToken"

                val response = apiService.deleteInvitation(authHeader, invitationId)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Invitación eliminada con éxito.", Toast.LENGTH_SHORT).show()
                    fetchGuestsFromApi() // Refrescar la lista después de la eliminación
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrEmpty()) {
                        try {
                            val apiError = RetrofitClient.json.decodeFromString(com.example.androidqr.network.ApiError.serializer(), errorBody)
                            apiError.message ?: "Error desconocido al eliminar: ${response.code()}"
                        } catch (e: Exception) {
                            "Error al eliminar invitación: $errorBody (Código: ${response.code()})"
                        }
                    } else {
                        "Error al eliminar invitación. Código: ${response.code()}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("DashboardFragment", "Error al eliminar invitación: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e("DashboardFragment", "Excepción de red/solicitud al eliminar: ${e.message}", e)
                Toast.makeText(requireContext(), "Error de red al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
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
