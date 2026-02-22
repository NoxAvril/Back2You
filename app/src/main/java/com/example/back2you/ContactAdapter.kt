package com.example.back2you

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.toColorInt

class ContactAdapter(
    private val contacts: MutableList<ContactItem>,
    private var isEditMode: Boolean,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View, onDelete: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        val tvValue: TextView = view.findViewById(R.id.tvContactValue)
        val tvLabel: TextView = view.findViewById(R.id.tvContactLabel)
        val ivIcon: ImageView = view.findViewById(R.id.ivContactIcon)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteContact)

        init {
            btnDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDelete(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view, onDelete) // Pass the callback to the VH
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val item = contacts[position]
        holder.tvValue.text = item.value
        holder.tvLabel.text = item.type

        // Icon Logic
        val iconRes = when (item.type) {
            "Phone" -> android.R.drawable.ic_menu_call
            "Email" -> android.R.drawable.ic_dialog_email
            "Discord", "Messenger", "WhatsApp", "Line", "Telegram" -> android.R.drawable.stat_notify_chat
            else -> android.R.drawable.ic_menu_share
        }
        holder.ivIcon.setImageResource(iconRes)

        // Brand Color Logic
        val colorHex = when (item.type) {
            "WhatsApp", "Line" -> "#4CAF50"
            "Messenger", "LinkedIn" -> "#0078FF"
            "Twitter" -> "#1DA1F2"
            "Discord" -> "#5865F2"
            "Instagram" -> "#E1306C"
            "Telegram" -> "#0088cc"
            "TikTok" -> "#EE1D52"
            else -> "#757575"
        }

        try {
            holder.ivIcon.setColorFilter(colorHex.toColorInt())
        } catch (_: Exception) {
            holder.ivIcon.setColorFilter(Color.GRAY)
        }

        // Only toggle visibility here, no listener creation
        holder.btnDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = contacts.size

    fun setEditMode(enabled: Boolean) {
        if (this.isEditMode != enabled) {
            this.isEditMode = enabled
            notifyItemRangeChanged(0, contacts.size)
        }
    }
}