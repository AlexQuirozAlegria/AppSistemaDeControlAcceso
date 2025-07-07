package com.example.androidqr.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout // Importar LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidqr.R
import java.text.SimpleDateFormat
import java.util.Locale

// Asegúrate de importar la clase Guest si está en un paquete diferente
// import com.example.androidqr.data.model.Guest // Si Guest está en otro paquete

/**
 * Adaptador para el RecyclerView que muestra la lista de invitados.
 * @param guests La lista de objetos Guest a mostrar.
 * @param currentTabFilter El filtro de la pestaña actual para determinar la visibilidad del botón Cancelar.
 * @param onCancelClick Callback que se invoca cuando se hace clic en el botón Cancelar de un invitado.
 */
class GuestAdapter(
    private var guests: List<Guest>,
    private var currentTabFilter: String,
    private val onCancelClick: (Guest) -> Unit
) : RecyclerView.Adapter<GuestAdapter.GuestViewHolder>() {

    /**
     * ViewHolder para cada elemento de la lista de invitados.
     * Contiene las referencias a las vistas del layout item_guest.xml.
     */
    class GuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val guestNameTextView: TextView = itemView.findViewById(R.id.guestNameTextView)
        val statusChip: TextView = itemView.findViewById(R.id.statusChip)
        val invitationTypeTextView: TextView = itemView.findViewById(R.id.invitationTypeTextView)
        val textDateExpiration: TextView = itemView.findViewById(R.id.textDateExpiration)
        val guestExpirationDateValue: TextView = itemView.findViewById(R.id.guestExpirationDateValue)
        val cancelButton: Button = itemView.findViewById(R.id.cancelButton) // Referencia al botón Cancelar
        val dateExpirationGroup: LinearLayout = itemView.findViewById(R.id.dateExpirationGroup) // NUEVO: Referencia al LinearLayout
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

        holder.guestNameTextView.text = guest.name
        holder.statusChip.text = guest.status
        holder.invitationTypeTextView.text = "Tipo: ${guest.invitationType}"

        // Formatear y mostrar la fecha de vencimiento si existe y el tipo es "PorFecha"
        if (guest.fechaVencimiento != null && guest.invitationType == "PorFecha") {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            holder.guestExpirationDateValue.text = dateFormat.format(guest.fechaVencimiento)
            holder.dateExpirationGroup.visibility = View.VISIBLE // Hacer visible el grupo completo
        } else {
            holder.dateExpirationGroup.visibility = View.GONE // Ocultar el grupo completo
        }

        // Lógica para mostrar/ocultar el botón Cancelar
        // El botón Cancelar solo debe ser visible si el invitado está "Activo"
        // y la pestaña actual es "Activos"
        if (guest.status.equals("Activo", ignoreCase = true) && currentTabFilter == "Activos") {
            holder.cancelButton.visibility = View.VISIBLE
            holder.cancelButton.setOnClickListener {
                onCancelClick(guest) // Invoca el callback al hacer clic
            }
        } else {
            holder.cancelButton.visibility = View.GONE
            holder.cancelButton.setOnClickListener(null) // Elimina el listener para evitar fugas
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
     * @param newTabFilter El nuevo filtro de pestaña seleccionado.
     */
    fun updateData(newGuests: List<Guest>, newTabFilter: String) {
        this.guests = newGuests
        this.currentTabFilter = newTabFilter
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }
}
