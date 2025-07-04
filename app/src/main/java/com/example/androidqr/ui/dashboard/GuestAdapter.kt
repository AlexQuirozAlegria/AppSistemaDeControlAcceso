package com.example.androidqr.ui.dashboard
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidqr.R // **IMPORTANT: Make sure this R import is correct for your project**

class GuestAdapter(private var guestList: List<Guest>) : // Changed 'guests' to 'guestList' for clarity
    RecyclerView.Adapter<GuestAdapter.GuestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guest, parent, false) // Ensure item_guest.xml is in res/layout
        return GuestViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        val currentGuest = guestList[position]
        holder.bind(currentGuest)
    }

    override fun getItemCount(): Int {
        return guestList.size
    }

    /**
     * Updates the list of guests in the adapter and refreshes the RecyclerView.
     */
    fun updateData(newGuestList: List<Guest>) {
        guestList = newGuestList
        notifyDataSetChanged() // For simplicity. For better performance with large lists, consider DiffUtil.
    }

    inner class GuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Get references to the views in your item_guest.xml layout
        // The IDs used here (guestNameTextView, guestStatusTextView) must match those in item_guest.xml
        private val nameTextView: TextView = itemView.findViewById(R.id.guestNameTextView)
        private val statusChipView: TextView = itemView.findViewById(R.id.statusChip)
        // Add other TextViews or ImageViews from your item_guest.xml as needed

        fun bind(guest: Guest) {
            nameTextView.text = guest.name
            statusChipView.text = guest.status
            // Set other data to your views here
            // For example, if you had an ID TextView:
            // val idTextView: TextView = itemView.findViewById(R.id.guestIdTextView)
            // idTextView.text = guest.id
        }
    }
}