package com.example.androidqr.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidqr.R
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para el RecyclerView que muestra el historial de accesos.
 */
class AccessAdapter(private var accessList: List<AccessItem>) :
    RecyclerView.Adapter<AccessAdapter.AccessViewHolder>() {

    inner class AccessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accessDateTextView: TextView = itemView.findViewById(R.id.accessDateTextView)
        val accessTypeTextView: TextView = itemView.findViewById(R.id.accessTypeTextView)
        val accessGuestNameTextView: TextView = itemView.findViewById(R.id.accessGuestNameTextView)
        val accessGuardNameTextView: TextView = itemView.findViewById(R.id.accessGuardNameTextView)
        val accessVehiclePlateTextView: TextView = itemView.findViewById(R.id.accessVehiclePlateTextView)

        fun bind(accessItem: AccessItem) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            accessDateTextView.text = "Fecha: ${dateFormat.format(accessItem.fechaAcceso)}"
            accessTypeTextView.text = "Tipo de Acceso: ${accessItem.tipoAcceso}"

            accessGuestNameTextView.visibility = if (accessItem.nombreInvitado != null) View.VISIBLE else View.GONE
            accessGuestNameTextView.text = "Invitado: ${accessItem.nombreInvitado ?: "N/A"}"

            accessGuardNameTextView.visibility = if (accessItem.nombreGuardia != null) View.VISIBLE else View.GONE
            accessGuardNameTextView.text = "Guardia: ${accessItem.nombreGuardia ?: "N/A"}"

            accessVehiclePlateTextView.visibility = if (accessItem.placasVehiculo != null) View.VISIBLE else View.GONE
            accessVehiclePlateTextView.text = "Placas: ${accessItem.placasVehiculo ?: "N/A"}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccessViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_access_history, parent, false)
        return AccessViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccessViewHolder, position: Int) {
        holder.bind(accessList[position])
    }

    override fun getItemCount(): Int = accessList.size

    /**
     * Actualiza los datos del adaptador y notifica los cambios al RecyclerView.
     * @param newAccessList La nueva lista de objetos AccessItem.
     */
    fun updateData(newAccessList: List<AccessItem>) {
        this.accessList = newAccessList
        notifyDataSetChanged()
    }
}
