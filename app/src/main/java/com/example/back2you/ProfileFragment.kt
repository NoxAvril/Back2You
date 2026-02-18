package com.example.back2you

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProfileFragment : Fragment() {

    private var isEditMode = false
    private lateinit var contactAdapter: ContactAdapter
    private val contactList = mutableListOf<ContactItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val rvContacts = view.findViewById<RecyclerView>(R.id.rvContacts)
        val tvEditToggle = view.findViewById<TextView>(R.id.tvEditToggle)
        val btnAddContact = view.findViewById<Button>(R.id.btnAddContact)
        val etName = view.findViewById<EditText>(R.id.etProfileName)
        val btnThemeToggle = view.findViewById<ImageButton>(R.id.btnThemeToggle)

        // 1. Efficient Theme Toggle
        btnThemeToggle.setOnClickListener {
            (requireActivity() as? MainActivity)?.toggleTheme()
        }

        // 2. Efficient List Logic
        contactAdapter = ContactAdapter(contactList, isEditMode) { position ->
            if (position != RecyclerView.NO_POSITION) {
                contactList.removeAt(position)
                contactAdapter.notifyItemRemoved(position)
                contactAdapter.notifyItemRangeChanged(position, contactList.size)
            }
        }

        rvContacts.layoutManager = LinearLayoutManager(context)
        rvContacts.adapter = contactAdapter

        tvEditToggle.setOnClickListener {
            isEditMode = !isEditMode
            tvEditToggle.text = if (isEditMode) "Save" else "Edit"
            etName.isEnabled = isEditMode
            btnAddContact.visibility = if (isEditMode) View.VISIBLE else View.GONE
            contactAdapter.setEditMode(isEditMode)

            if (!isEditMode) Toast.makeText(context, "Profile Saved", Toast.LENGTH_SHORT).show()
        }

        btnAddContact.setOnClickListener { showAddDialog() }

        return view
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_contact, null)
        val etValue = dialogView.findViewById<EditText>(R.id.etNewValue)
        val spType = dialogView.findViewById<Spinner>(R.id.spContactType)

        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.contact_types,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spType.adapter = spinnerAdapter

        AlertDialog.Builder(requireContext())
            .setTitle("Add Contact")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val type = spType.selectedItem.toString()
                val value = etValue.text.toString()
                if (value.isNotEmpty()) {
                    contactList.add(ContactItem(type, value))
                    contactAdapter.notifyItemInserted(contactList.size - 1)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}