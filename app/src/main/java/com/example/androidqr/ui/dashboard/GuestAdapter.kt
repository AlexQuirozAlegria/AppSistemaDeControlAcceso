package com.example.androidqr.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidqr.R
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Interfaz para manejar los clics en los elementos de la lista de invitados.
 */
interface OnGuestClickListener {
    /**
     * Se llama cuando se hace clic en un elemento de la lista de invitados.
     * @param guest El objeto Guest en el que se hizo clic.
     */
    fun onGuestClick(guest: Guest)
}

/**
 * Adaptador para el RecyclerView que muestra la lista de invitados.
 * @param guests La lista de objetos Guest a mostrar.
 * @param listener El listener para manejar los clics en los elementos de la lista.
 */
class GuestAdapter(
    private var guests: List<Guest>,
    private val listener: OnGuestClickListener // Ahora recibe el listener para clics en el ítem
) : RecyclerView.Adapter<GuestAdapter.GuestViewHolder>() {

    /**
     * ViewHolder para cada elemento de la lista de invitados.
     * Contiene las referencias a las vistas del layout item_guest.xml.
     */
    inner class GuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val guestNameTextView: TextView = itemView.findViewById(R.id.guestNameTextView)
        val statusChip: Chip = itemView.findViewById(R.id.statusChip) // Asumiendo que es un Chip de Material Design
        val invitationDetailsTextView: TextView = itemView.findViewById(R.id.invitationDetailsTextView) // TextView combinado para tipo y fecha
        // Las referencias a cancelButton, textDateExpiration, guestExpirationDateValue, dateExpirationGroup han sido eliminadas
        // ya que el botón de cancelar y la lógica de fecha se manejan en el diálogo y en el texto combinado.

        /**
         * Vincula los datos de un objeto Guest a las vistas del ViewHolder.
         * @param guest El objeto Guest cuyos datos se van a mostrar.
         */
        fun bind(guest: Guest) {
            guestNameTextView.text = guest.name
            statusChip.text = guest.status

            // Construir el texto combinado para tipo de invitación y fecha de vencimiento
            val detailsText = StringBuilder("Tipo: ${guest.invitationType}")
            if (guest.fechaVencimiento != null) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                detailsText.append(" Vence: ${dateFormat.format(guest.fechaVencimiento)}")
            }
            invitationDetailsTextView.text = detailsText.toString()
        }
    }

    /**
     * Crea y devuelve un ViewHolder para cada elemento de la lista.
     * @param parent El ViewGroup padre al que se adjuntará la vista.
     * @param viewType El tipo de vista (si tuvieras múltiples layouts de elementos).
     * @return Un nuevo GuestViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guest, parent, false)
        return GuestViewHolder(view)
    }

    /**
     * Vincula los datos de un objeto Guest a las vistas del ViewHolder.
     * @param holder El ViewHolder al que se vincularán los datos.
     * @param position La posición del elemento en la lista.
     */
    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        val guest = guests[position]
        holder.bind(guest) // Vincula los datos del invitado a las vistas

        // Configurar el listener de clic para todo el elemento de la lista
        holder.itemView.setOnClickListener {
            listener.onGuestClick(guest) // Invoca el callback del listener cuando se hace clic en el ítem
        }
    }

    /**
     * Devuelve el número total de elementos en la lista de invitados.
     * @return El número de invitados.
     */
    override fun getItemCount(): Int = guests.size

    /**
     * Actualiza los datos del adaptador y notifica los cambios al RecyclerView.
     * @param newGuests La nueva lista de objetos Guest.
     */
    fun updateData(newGuests: List<Guest>) {
        this.guests = newGuests
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }
}
