package com.example.back2you

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ItemDetailFragment : Fragment(R.layout.fragment_item_detail) {

    companion object {
        private const val ARG_ITEM = "selected_item"

        fun newInstance(item: PostItem): ItemDetailFragment {
            return ItemDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ITEM, item)
                }
            }
        }
    }

    private var selectedItem: PostItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_ITEM, PostItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_ITEM)
        }

        val ivItemImage = view.findViewById<ImageView>(R.id.ivItemImage)
        val ivUserImage = view.findViewById<ImageView>(R.id.ivUserImage)
        val tvTitle = view.findViewById<TextView>(R.id.tvItemTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvItemDescription)
        val tvFinderName = view.findViewById<TextView>(R.id.tvFinderName)
        val tvPublicContacts = view.findViewById<TextView>(R.id.tvPublicContacts)
        val tvReturnedStatus = view.findViewById<TextView>(R.id.tvReturnedStatus)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnViewProfile = view.findViewById<Button>(R.id.btnViewProfile)
        val btnMarkReturned = view.findViewById<Button>(R.id.btnMarkReturned)

        val currentUser = FirebaseAuth.getInstance().currentUser

        selectedItem?.let { item ->

            // -----------------------------
            // BASIC INFO
            // -----------------------------
            tvTitle.text = item.title ?: "No Title"
            tvDescription.text = item.description ?: "No Description"

            if (!item.imageUrl.isNullOrEmpty()) {
                ivItemImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(item.imageUrl)
                    .centerCrop()
                    .into(ivItemImage)
            } else {
                ivItemImage.visibility = View.GONE
            }

            val role = if (item.type == "Found") "Finder" else "Owner"
            btnViewProfile.text = "View $role Profile"

            // -----------------------------
            // LOAD USER INFO
            // -----------------------------
            val uid = item.finderUid

            if (!uid.isNullOrEmpty()) {
                FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val username =
                            snapshot.child("username").getValue(String::class.java)
                        val profileImage =
                            snapshot.child("profileImage").getValue(String::class.java)

                        tvFinderName.text = username ?: "User"

                        if (!profileImage.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profileImage)
                                .circleCrop()
                                .into(ivUserImage)
                        }

                        val contactsNode = snapshot.child("contacts")
                        val contactsList = mutableListOf<String>()

                        for (child in contactsNode.children) {
                            val contact = child.getValue(ContactItem::class.java)
                            if (contact != null && contact.isPublic) {
                                contactsList.add("${contact.type}: ${contact.value}")
                            }
                        }

                        if (contactsList.isNotEmpty()) {
                            tvPublicContacts.visibility = View.VISIBLE
                            tvPublicContacts.text = contactsList.joinToString("\n")
                        } else {
                            tvPublicContacts.visibility = View.GONE
                        }
                    }
            }

            // -----------------------------
            // LOAD RETURNED STATE FROM FIREBASE
            // -----------------------------
            item.id?.let { itemId ->
                FirebaseDatabase.getInstance()
                    .getReference("items")
                    .child(itemId)
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val isReturned =
                            snapshot.child("returned").getValue(Boolean::class.java) ?: false

                        item.returned = isReturned

                        if (isReturned) {
                            tvReturnedStatus.visibility = View.VISIBLE
                        } else {
                            tvReturnedStatus.visibility = View.GONE
                        }

                        updateReturnedButtonUI(btnMarkReturned, isReturned)
                    }
            }

            // -----------------------------
            // MARK AS RETURNED (OWNER ONLY)
            // -----------------------------
            if (currentUser?.uid == item.finderUid) {

                btnMarkReturned.visibility = View.VISIBLE

                btnMarkReturned.setOnClickListener {

                    val newState = !item.returned

                    val updates = mapOf<String, Any?>(
                        "returned" to newState,
                        "returnTimestamp" to if (newState)
                            System.currentTimeMillis()
                        else null
                    )

                    item.id?.let { itemId ->
                        FirebaseDatabase.getInstance()
                            .getReference("items")
                            .child(itemId)
                            .updateChildren(updates)
                            .addOnSuccessListener {

                                item.returned = newState

                                if (newState) {
                                    tvReturnedStatus.visibility = View.VISIBLE
                                } else {
                                    tvReturnedStatus.visibility = View.GONE
                                }

                                updateReturnedButtonUI(btnMarkReturned, newState)
                            }
                    }
                }

            } else {
                btnMarkReturned.visibility = View.GONE
            }

            // -----------------------------
            // PROFILE CLICK
            // -----------------------------
            btnViewProfile.setOnClickListener {
                val profileFragment =
                    UserProfileFragment.newInstance(uid ?: "")

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateReturnedButtonUI(button: Button, isReturned: Boolean) {

        if (isReturned) {
            button.text = "Marked as Returned ✓"
            button.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
            )
        } else {
            button.text = "Mark as Returned"
            button.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary)
            )
        }
    }
}