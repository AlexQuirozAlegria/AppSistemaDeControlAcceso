package com.example.androidqr.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // Keep this for coroutines
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidqr.R
import com.example.androidqr.databinding.FragmentDashboardBinding
import com.example.androidqr.network.InvitadoResponse
import com.example.androidqr.network.RetrofitClient
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch // For coroutines

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var guestAdapter: GuestAdapter
    // private var allSampleGuests: List<Guest> = emptyList() // Remove: We'll fetch from API
    private var allFetchedGuests: List<Guest> = emptyList() // To store guests fetched from API

    // Get the API service instance
    private val apiService by lazy {
        RetrofitClient.instance
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupTabLayout() // Setup tabs

        fetchGuestsFromApi() // New function to load data from API

        return root
    }

    private fun setupRecyclerView() {
        guestAdapter = GuestAdapter(emptyList()) // Start with an empty list
        binding.guestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = guestAdapter
        }
    }

    private fun fetchGuestsFromApi() {
        // Show loading indicator (optional, but good UX)
        binding.guestsRecyclerView.visibility = View.GONE
        //binding.emptyViewDashboard?.visibility = View.GONE // Assuming you add an empty state view

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
                        // Initially display guests for the first tab after data is loaded
                        binding.tabLayout.getTabAt(0)?.let {
                            filterGuestsByStatus(it.text.toString())
                        }
                        binding.guestsRecyclerView.visibility = View.VISIBLE
                    } else if (invitadosFromApi != null && invitadosFromApi.isEmpty()) {
                        Log.i("DashboardFragment", "API returned an empty list of guests.")
                        allFetchedGuests = emptyList()
                        guestAdapter.updateData(emptyList()) // Clear adapter
                        binding.guestsRecyclerView.visibility = View.GONE
                        //binding.emptyViewDashboard?.visibility = View.VISIBLE // Show empty state
                        //binding.emptyViewDashboard?.text = "No hay invitados para mostrar."
                    } else {
                        Log.e("DashboardFragment", "API response body is null or malformed.")
                        showErrorState("Error: Respuesta de API inválida.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido de API."
                    Log.e("DashboardFragment", "API Error: ${response.code()} - $errorBody")
                    showErrorState("Error ${response.code()}: No se pudieron cargar los invitados.")
                }
            } catch (e: Exception) {

                Log.e("DashboardFragment", "Network/Request Exception: ${e.message}", e)
                showErrorState("Error de red: ${e.message}")
            }
        }
    }

    // Helper function to map API response to UI model
    private fun mapInvitadoResponseToGuest(invitados: List<InvitadoResponse>): List<Guest> {
        return invitados.map { invitadoApi ->
            Guest(
                id = invitadoApi.id,
                name = "${invitadoApi.nombre} ${invitadoApi.apellidos}".trim(), // Combine name and apellidos
                status = invitadoApi.estadoQr, // Use estadoQr for status
                fechaVencimiento= invitadoApi.fechaValidez // Use fechaValidez for expiration date
            )
        }
    }

    private fun showErrorState(message: String) {
        allFetchedGuests = emptyList()
        guestAdapter.updateData(emptyList())
        binding.guestsRecyclerView.visibility = View.GONE
        //binding.emptyViewDashboard?.visibility = View.VISIBLE
        //binding.emptyViewDashboard?.text = message
        // Optionally, you could also disable the TabLayout or show an error message there
    }


    private fun setupTabLayout() {
        // Ensure tabs are added to tabLayout in your fragment_dashboard.xml
        // or programmatically before this point if not already done.
        // If tabs are not yet present, add them:
        if (binding.tabLayout.tabCount == 0) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Activos"))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Pendientes"))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Vencidos"))
            // binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Todos")) // If you want an "All" tab
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    filterGuestsByStatus(it.text.toString())
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) { /* Do nothing */ }
            override fun onTabReselected(tab: TabLayout.Tab?) { /* Do nothing */ }
        })
    }

    private fun filterGuestsByStatus(statusFilter: String) {
        // Use allFetchedGuests which contains data from the API
        val filteredList = when (statusFilter.lowercase()) {
            "activos" -> allFetchedGuests.filter { it.status.equals("Activo", ignoreCase = true) }
            "pendientes" -> allFetchedGuests.filter { it.status.equals("Pendiente", ignoreCase = true) } // Assuming "Pendiente" is a possible estadoQr
            "vencidos" -> allFetchedGuests.filter { it.status.equals("Vencido", ignoreCase = true) }   // Assuming "Vencido" is a possible estadoQr
            // "todos" -> allFetchedGuests // If you add an "All" tab
            else -> {
                Log.w("DashboardFragment", "Unhandled tab status: $statusFilter, showing empty list or all if 'allFetchedGuests' is the default.")
                // Decide what to show for an unhandled tab, maybe the first category or all.
                // For now, let's default to all if no specific match, or emptyList if that's safer.
                if (allFetchedGuests.isNotEmpty()) allFetchedGuests else emptyList()
            }
        }
        guestAdapter.updateData(filteredList)

        // Show empty view if the filtered list is empty for the current tab
        if (filteredList.isEmpty() && allFetchedGuests.isNotEmpty()) { // only show if allFetchedGuests is not empty (i.e. API call was successful)
            //binding.emptyViewDashboard?.visibility = View.VISIBLE
            //binding.emptyViewDashboard?.text = "No hay invitados con estado '$statusFilter'."
            binding.guestsRecyclerView.visibility = View.GONE
        } else if (filteredList.isNotEmpty()) {
            //binding.emptyViewDashboard?.visibility = View.GONE
            binding.guestsRecyclerView.visibility = View.VISIBLE
        }
        // If allFetchedGuests is empty, the general empty/error state from fetchGuestsFromApi will handle it.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
