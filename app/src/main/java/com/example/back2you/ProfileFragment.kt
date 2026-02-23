package com.example.back2you

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var userListener: ValueEventListener? = null

    private lateinit var ivProfileImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvContacts: TextView
    private lateinit var layoutContactList: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        ivProfileImage = view.findViewById(R.id.ivProfilePic)
        val btnDayNight = view.findViewById<ImageButton>(R.id.btnDayNight)
        tvName = view.findViewById(R.id.tvName)
        tvContacts = view.findViewById(R.id.tvContacts)
        layoutContactList = view.findViewById(R.id.layoutContactList)
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        val user = auth.currentUser
        if (user == null) {
            (activity as? MainActivity)?.logout()
            return
        }

        attachUserListener(user.uid)

        setupThemeToggle(btnDayNight)
        setupButtons(btnEdit, btnLogout)
    }

    // ---------------------------------
    // REALTIME USER LISTENER
    // ---------------------------------
    private fun attachUserListener(uid: String) {

        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (!isAdded) return

                val username =
                    snapshot.child("username")
                        .getValue(String::class.java) ?: "User"

                tvName.text = username

                // 🔥 LOAD PROFILE IMAGE
                val profileImageUrl =
                    snapshot.child("profileImage")
                        .getValue(String::class.java)

                if (!profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this@ProfileFragment)
                        .load(profileImageUrl)
                        .circleCrop()
                        .into(ivProfileImage)
                } else {
                    ivProfileImage.setImageResource(
                        R.drawable.ic_profile_placeholder
                    )
                }

                loadContacts(snapshot.child("contacts"))
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        database.child("users")
            .child(uid)
            .addValueEventListener(userListener!!)
    }

    // ---------------------------------
    // CONTACTS
    // ---------------------------------
    private fun loadContacts(contactsSnapshot: DataSnapshot) {

        val totalContacts = contactsSnapshot.childrenCount
        tvContacts.text = "$totalContacts contacts"

        layoutContactList.removeAllViews()

        contactsSnapshot.children.forEach { child ->

            val isPublic =
                child.child("isPublic")
                    .getValue(Boolean::class.java) ?: false

            if (isPublic) {

                val type =
                    child.child("type").value?.toString() ?: ""

                val value =
                    child.child("value").value?.toString() ?: ""

                val contactView = layoutInflater.inflate(
                    R.layout.item_contact,
                    layoutContactList,
                    false
                )

                contactView.findViewById<TextView>(R.id.tvContactValue).text = value
                contactView.findViewById<TextView>(R.id.tvContactLabel).text = type

                val icon =
                    contactView.findViewById<ImageView>(R.id.ivContactIcon)

                when (type.lowercase()) {
                    "phone" ->
                        icon.setImageResource(android.R.drawable.ic_menu_call)
                    "email" ->
                        icon.setImageResource(android.R.drawable.ic_dialog_email)
                    "instagram" ->
                        icon.setImageResource(android.R.drawable.ic_menu_share)
                    else ->
                        icon.setImageResource(android.R.drawable.ic_menu_info_details)
                }

                layoutContactList.addView(contactView)
            }
        }
    }

    // ---------------------------------
    // THEME TOGGLE
    // ---------------------------------
    private fun setupThemeToggle(btnDayNight: ImageButton) {

        val sharedPref =
            requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)

        val isDarkMode = sharedPref.getBoolean("DarkMode", false)
        updateThemeIcon(btnDayNight, isDarkMode)

        btnDayNight.setOnClickListener {

            val newMode = !isDarkMode
            sharedPref.edit().putBoolean("DarkMode", newMode).apply()

            AppCompatDelegate.setDefaultNightMode(
                if (newMode)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )

            updateThemeIcon(btnDayNight, newMode)
        }
    }

    // ---------------------------------
    // BUTTONS
    // ---------------------------------
    private fun setupButtons(btnEdit: Button, btnLogout: Button) {

        btnEdit.setOnClickListener {
            (activity as? MainActivity)
                ?.replaceFragment(EditProfileFragment())
        }

        btnLogout.setOnClickListener {
            (activity as? MainActivity)?.logout()
            Toast.makeText(
                requireContext(),
                "Logged out",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val user = auth.currentUser ?: return
        userListener?.let {
            database.child("users")
                .child(user.uid)
                .removeEventListener(it)
        }
    }

    private fun updateThemeIcon(button: ImageButton, isDark: Boolean) {
        if (isDark) {
            button.setImageResource(R.drawable.ic_night)
        } else {
            button.setImageResource(R.drawable.ic_day)
        }
    }
}