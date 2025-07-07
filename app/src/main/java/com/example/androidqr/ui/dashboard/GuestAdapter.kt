package com.example.androidqr.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidqr.R // Importación para acceder a los IDs de recursos
import com.google.android.material.chip.Chip // Importar Chip
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

/**
 * Adaptador para mostrar una lista de objetos Guest en un RecyclerView.
 * Se encarga de inflar el layout de cada elemento y vincular los datos.
 */
class GuestAdapter(private var guestList: List<Guest>) :
    RecyclerView.Adapter<GuestAdapter.GuestViewHolder>() {

    /**
     * Crea y devuelve un ViewHolder para la vista de un elemento de la lista.
     * @param parent El ViewGroup al que se adjuntará la nueva vista.
     * @param viewType El tipo de vista del nuevo ViewHolder.
     * @return Un nuevo GuestViewHolder que contiene la vista de un elemento de la lista.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guest, parent, false) // Asegúrate de que item_guest.xml está en res/layout
        return GuestViewHolder(itemView)
    }

    /**
     * Actualiza el contenido de un ViewHolder existente con los datos de un elemento en una posición dada.
     * @param holder El ViewHolder que debe actualizarse.
     * @param position La posición del elemento dentro del conjunto de datos del adaptador.
     */
    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        val currentGuest = guestList[position]
        holder.bind(currentGuest)
    }

    /**
     * Devuelve el número total de elementos en el conjunto de datos.
     * @return El número total de elementos.
     */
    override fun getItemCount(): Int {
        return guestList.size
    }

    /**
     * Actualiza la lista de invitados en el adaptador y notifica al RecyclerView para que se refresque.
     * @param newGuestList La nueva lista de invitados a mostrar.
     */
    fun updateData(newGuestList: List<Guest>) {
        guestList = newGuestList
        notifyDataSetChanged() // Para simplicidad. Para mejor rendimiento, considera DiffUtil.
    }

    /**
     * ViewHolder para los elementos individuales de la lista de invitados.
     * Contiene las referencias a las vistas dentro de item_guest.xml y un método para vincular datos.
     */
    inner class GuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referencias a las vistas en tu layout item_guest.xml
        // ¡Estos IDs deben coincidir EXACTAMENTE con los IDs en item_guest.xml que proporcionaste!
        private val nameTextView: TextView = itemView.findViewById(R.id.guestNameTextView)
        private val invitationTypeTextView: TextView = itemView.findViewById(R.id.invitationTypeTextView)
        private val statusChip: Chip = itemView.findViewById(R.id.statusChip)
        private val expirationDateLabel: TextView = itemView.findViewById(R.id.textDateExpiration) // ID de la etiqueta "Fecha de expiracion:"
        private val expirationDateValue: TextView = itemView.findViewById(R.id.guestExpirationDateValue) // ID para el VALOR de la fecha


        /**
         * Vincula los datos de un objeto Guest a las vistas del ViewHolder.
         * @param guest El objeto Guest cuyos datos se van a mostrar.
         */
        fun bind(guest: Guest) {
            nameTextView.text = guest.name
            invitationTypeTextView.text = "Tipo: ${guest.invitationType}"
            statusChip.text = guest.status

            // Formatear y mostrar la fecha de vencimiento si existe
            if (guest.fechaVencimiento != null) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                expirationDateValue.text = dateFormat.format(guest.fechaVencimiento)
                expirationDateLabel.visibility = View.VISIBLE
                expirationDateValue.visibility = View.VISIBLE
            } else {
                expirationDateLabel.visibility = View.GONE
                expirationDateValue.visibility = View.GONE
            }
        }
    }
}
