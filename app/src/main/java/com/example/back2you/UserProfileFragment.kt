package com.example.back2you

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Inflation happens here in the constructor
class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize Views with correct IDs from XML
        val tvName = view.findViewById<TextView>(R.id.tvUserDetailName)
        val rvContacts = view.findViewById<RecyclerView>(R.id.rvUserPublicContacts)
        val btnBackArrow = view.findViewById<ImageButton>(R.id.btnBackFromProfile)
        val btnClose = view.findViewById<Button>(R.id.btnCloseProfile)

        // 2. Retrieve Arguments passed from ItemDetailFragment
        val userName = arguments?.getString("userName") ?: "Unknown User"
        tvName?.text = userName

        // 3. Setup RecyclerView safely
        rvContacts?.let {
            it.layoutManager = LinearLayoutManager(requireContext())

            // Mock data for public view
            val publicContacts = mutableListOf(
                ContactItem("WhatsApp", "0812-3456-7890"),
                ContactItem("Email", "finder@example.com")
            )

            // isEditMode = false means the delete button is hidden
            it.adapter = ContactAdapter(publicContacts, false) {
                // No action needed for contact deletion in public view
            }
        }

        // 4. Set Navigation Listeners
        val navigateBack = View.OnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnBackArrow?.setOnClickListener(navigateBack)
        btnClose?.setOnClickListener(navigateBack)
    }
}