package com.example.androidqr.ui.dashboard

// If you are using lifecycleScope for other reasons, keep it, otherwise it can be removed
// import androidx.lifecycle.lifecycleScope
// Replace 'com.yourcompany.androidqr.databinding.FragmentDashboardBinding' with your actual binding class
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidqr.databinding.FragmentDashboardBinding
import com.google.android.material.tabs.TabLayout

// Import your Guest data class if it's in a different package
// import com.example.androidqr.ui.dashboard.Guest // Assuming Guest.kt is in this package

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var guestAdapter: GuestAdapter
    private var allSampleGuests: List<Guest> = emptyList() // To store all sample guests

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        loadSampleGuests() // Load sample data
        setupTabLayout() // Setup tabs after data is loaded

        // Initially display guests for the first tab (e.g., "Activos")
        // Ensure tabs are added to tabLayout in your fragment_dashboard.xml
        // or programmatically before this point if not already done.
        binding.tabLayout.getTabAt(0)?.let {
            filterGuestsByStatus(it.text.toString())
        }


        return root
    }

    private fun setupRecyclerView() {
        // Assuming GuestAdapter is in the same package or imported correctly
        guestAdapter = GuestAdapter(emptyList())
        binding.guestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = guestAdapter
        }
    }

    private fun loadSampleGuests() {
        // Create your sample guest list here
        // quitar cuando se añada la BD
        allSampleGuests = listOf(
            Guest(id = 1, name = "Ana Torres", status = "Activo"),
            Guest(id = 2, name = "Luis Jimenez", status = "Pendiente"),
            Guest(id = 3, name = "Maria Gonzalez", status = "Vencido"),
            Guest(id = 5, name = "Sofia Rodriguez", status = "Activo"),
            Guest(id = 6, name = "David Fernández", status = "Pendiente"),
            Guest(id = 7, name = "Laura Sánchez", status = "Vencido"),
            Guest(id = 8, name = "Pedro Gómez", status = "Activo"),
            Guest(id = 9, name = "Elena Pérez", status = "Pendiente"),
            Guest(id = 10, name = "Miguel Díaz", status = "Activo"),
            Guest(id = 11, name = "Carmen Ruiz", status = "Vencido")
            // Add more sample guests as needed
        )
    }

    private fun setupTabLayout() {
        // Make sure your TabItems are already defined in your fragment_dashboard.xml
        // e.g., <com.google.android.material.tabs.TabItem android:text="Activos" />

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

    private fun filterGuestsByStatus(status: String) {
        val filteredList = when (status.lowercase()) { // Use lowercase for robust comparison
            "activos" -> allSampleGuests.filter { it.status.equals("Activo", ignoreCase = true) }
            "pendientes" -> allSampleGuests.filter { it.status.equals("Pendiente", ignoreCase = true) }
            "vencidos" -> allSampleGuests.filter { it.status.equals("Vencido", ignoreCase = true) }
            // Add a case for an "All" tab if you have one, or a default
            // "todos" -> allSampleGuests // Example if you add an "All" tab
            else -> {
                Log.w("com.example.androidqr.ui.dashboard.DashboardFragment", "Unhandled tab status: $status, showing empty list.")
                emptyList()
            }
        }
        guestAdapter.updateData(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
