package com.example.back2you

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val btnDayNight = view.findViewById<ImageButton>(R.id.btnDayNight)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvContacts = view.findViewById<TextView>(R.id.tvContacts)
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        val user = auth.currentUser

        if (user == null) {
            (activity as? MainActivity)?.logout()
            return
        }

        user.reload().addOnCompleteListener { task ->

            if (!isAdded) return@addOnCompleteListener

            if (task.isSuccessful && auth.currentUser != null) {

                val updatedUser = auth.currentUser!!

                tvName.text = updatedUser.displayName ?: "User"
                tvContacts.text = "0 contacts"

            } else {
                (activity as? MainActivity)?.logout()
            }
        }

        val sharedPref = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)

        updateThemeIcon(btnDayNight, isDarkMode)

        btnDayNight.setOnClickListener {

            val newMode = !isDarkMode

            sharedPref.edit().putBoolean("DarkMode", newMode).apply()

            if (newMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        btnEdit.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(EditProfileFragment())
        }

        btnLogout.setOnClickListener {
            (activity as? MainActivity)?.logout()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
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