package com.example.back2you

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private lateinit var layoutContactList: LinearLayout
    private lateinit var etEditName: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val currentUser = auth.currentUser ?: return

        etEditName = view.findViewById(R.id.etEditName)
        val etNewContact = view.findViewById<EditText>(R.id.etNewContact)
        val btnAddContact = view.findViewById<Button>(R.id.btnAddContact)
        val btnSaveProfile = view.findViewById<Button>(R.id.btnSaveProfile)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelEdit)

        layoutContactList = view.findViewById(R.id.layoutContactList)

        //  LOAD USER DATA
        database.child("users").child(currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->

                etEditName.setText(snapshot.child("name").value?.toString())

                layoutContactList.removeAllViews()

                snapshot.child("contacts").children.forEach { child ->
                    val contact = child.getValue(ContactItem::class.java)
                    contact?.let { addContactView(it) }
                }
            }

        //  ADD CONTACT
        btnAddContact.setOnClickListener {

            val text = etNewContact.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val contactId = database.child("users")
                .child(currentUser.uid)
                .child("contacts")
                .push().key ?: return@setOnClickListener

            val newContact = ContactItem(
                id = contactId,
                type = "Other",
                value = text,
                isPublic = true
            )

            database.child("users")
                .child(currentUser.uid)
                .child("contacts")
                .child(contactId)
                .setValue(newContact)

            addContactView(newContact)
            etNewContact.text.clear()
        }

        //  SAVE PROFILE NAME
        btnSaveProfile.setOnClickListener {

            val newName = etEditName.text.toString().trim()

            database.child("users")
                .child(currentUser.uid)
                .child("name")
                .setValue(newName)

            Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    //  CONTACT ROW VIEW
    private fun addContactView(contact: ContactItem) {

        val currentUser = auth.currentUser ?: return

        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
        }

        val textView = TextView(requireContext()).apply {
            text = contact.value
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val switch = Switch(requireContext()).apply {
            isChecked = contact.isPublic
        }

        val deleteBtn = ImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
        }

        //  Toggle Visibility
        switch.setOnCheckedChangeListener { _, isChecked ->
            database.child("users")
                .child(currentUser.uid)
                .child("contacts")
                .child(contact.id)
                .child("isPublic")
                .setValue(isChecked)
        }

        //  Delete Contact
        deleteBtn.setOnClickListener {
            database.child("users")
                .child(currentUser.uid)
                .child("contacts")
                .child(contact.id)
                .removeValue()

            layoutContactList.removeView(row)
        }

        row.addView(textView)
        row.addView(switch)
        row.addView(deleteBtn)

        layoutContactList.addView(row)
    }
}