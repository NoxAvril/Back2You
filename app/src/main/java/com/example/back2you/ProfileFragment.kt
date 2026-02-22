package com.example.back2you

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val ivProfile = view.findViewById<ShapeableImageView>(R.id.ivProfilePicture)
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvContacts = view.findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        val currentUser = auth.currentUser

        if (currentUser == null) {
            tvName.text = "Not Logged In"
            return
        }

        val uid = currentUser.uid

        // 🔥 Load User Profile Data
        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val username = snapshot.child("username").getValue(String::class.java)
                    val profileImage = snapshot.child("profileImageUrl").getValue(String::class.java)

                    tvName.text = username ?: "Unknown User"

                    if (!profileImage.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(profileImage)
                            .into(ivProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // 🔥 Load Contacts Count
        database.child("contacts").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount
                    tvContacts.text = "Contacts: $count"
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // 🔥 Logout
        btnLogout.setOnClickListener {
            auth.signOut()
            requireActivity().recreate()
        }
    }
}