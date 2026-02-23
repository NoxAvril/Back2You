package com.example.back2you

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    companion object {
        private const val ARG_USER_ID = "userId"

        fun newInstance(userId: String): UserProfileFragment {
            val fragment = UserProfileFragment()
            val bundle = Bundle()
            bundle.putString(ARG_USER_ID, userId)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var userRef: DatabaseReference
    private lateinit var contactsRef: DatabaseReference
    private lateinit var listener: ValueEventListener

    private lateinit var adapter: ContactAdapter
    private val contactList = mutableListOf<ContactItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName = view.findViewById<TextView>(R.id.tvUserDetailName)
        val ivProfile = view.findViewById<ImageView>(R.id.ivUserDetailAvatar)
        val rvContacts = view.findViewById<RecyclerView>(R.id.rvUserPublicContacts)
        val btnBackArrow = view.findViewById<ImageButton>(R.id.btnBackFromProfile)
        val btnClose = view.findViewById<Button>(R.id.btnCloseProfile)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyContacts)

        val userId = arguments?.getString(ARG_USER_ID)

        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        // ----------------------------
        // Recycler Setup
        // ----------------------------
        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        adapter = ContactAdapter(contactList, false) {}
        rvContacts.adapter = adapter

        // ----------------------------
        // Firebase References
        // ----------------------------
        userRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)

        contactsRef = userRef
            .child("contacts")
            .orderByChild("isPublic")
            .equalTo(true)
            .ref

        // ----------------------------
        // Load User Info (Name + Image)
        // ----------------------------
        userRef.get().addOnSuccessListener { snapshot ->

            val username = snapshot.child("username")
                .getValue(String::class.java)

            val profileImage = snapshot.child("profileImage")
                .getValue(String::class.java)

            tvName.text = username ?: "Unknown User"

            if (!profileImage.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(profileImage) // Cloudinary URL
                    .into(ivProfile)
            }
        }

        // ----------------------------
        // Load ONLY Public Contacts
        // ----------------------------
        listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                contactList.clear()

                for (child in snapshot.children) {
                    val contact = child.getValue(ContactItem::class.java)
                    contact?.let { contactList.add(it) }
                }

                adapter.notifyDataSetChanged()

                tvEmpty.visibility =
                    if (contactList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Failed to load contacts",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        contactsRef.addValueEventListener(listener)

        // ----------------------------
        // Back Navigation
        // ----------------------------
        val navigateBack = View.OnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnBackArrow.setOnClickListener(navigateBack)
        btnClose.setOnClickListener(navigateBack)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contactsRef.removeEventListener(listener)
    }
}