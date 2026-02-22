package com.example.back2you

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    companion object {
        private const val ARG_USER_ID = "userId"
        private const val ARG_USER_NAME = "userName"

        fun newInstance(userId: String, userName: String): UserProfileFragment {
            val fragment = UserProfileFragment()
            val bundle = Bundle()
            bundle.putString(ARG_USER_ID, userId)
            bundle.putString(ARG_USER_NAME, userName)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var contactsRef: DatabaseReference
    private lateinit var listener: ValueEventListener
    private lateinit var adapter: ContactAdapter
    private val contactList = mutableListOf<ContactItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName = view.findViewById<TextView>(R.id.tvUserDetailName)
        val rvContacts = view.findViewById<RecyclerView>(R.id.rvUserPublicContacts)
        val btnBackArrow = view.findViewById<ImageButton>(R.id.btnBackFromProfile)
        val btnClose = view.findViewById<Button>(R.id.btnCloseProfile)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyContacts)

        val userId = arguments?.getString(ARG_USER_ID)
        val userName = arguments?.getString(ARG_USER_NAME) ?: "Unknown User"

        tvName.text = userName

        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        adapter = ContactAdapter(contactList, false) {}
        rvContacts.adapter = adapter

        contactsRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("contacts")

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contactList.clear()

                if (!snapshot.exists()) {
                    tvEmpty.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                    return
                }

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
                    "Failed to load contacts: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        contactsRef.addValueEventListener(listener)

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